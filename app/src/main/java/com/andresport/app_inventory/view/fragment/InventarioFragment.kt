package com.andresport.app_inventory.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.andresport.app_inventory.R
import com.andresport.app_inventory.databinding.FragmentInventarioBinding
import com.andresport.app_inventory.util.EventObserver
import com.andresport.app_inventory.viewmodel.AuthenticationState
import com.andresport.app_inventory.viewmodel.LoginViewModel

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!

    // Usamos activityViewModels para compartir el ViewModel con la Activity y otros fragments.
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observamos el LiveData de navegación con nuestro EventObserver.
        // Esto garantiza que la navegación se dispare solo una vez.
        loginViewModel.navigationToLoginState.observe(viewLifecycleOwner, EventObserver { state ->
            if (state is AuthenticationState.UNAUTHENTICATED) {
                // Navegamos al LoginFragment si el usuario no está autenticado.
                // Asegúrate de que el ID de la acción en tu nav_graph.xml sea correcto.
                findNavController().navigate(R.id.action_inventarioFragment_to_LoginFragment)
            }
        })

        // --- SOLUCIÓN APLICADA AQUÍ ---
        // El ícono de logout está en un menú dentro de la Toolbar, no es una vista directa.
        // Por lo tanto, configuramos un listener en la Toolbar para capturar los clics en sus ítems.
        binding.toolbarInventario.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // Comparamos el ID del ítem presionado con el ID del ítem de logout en tu archivo de menú.
                // Reemplaza 'action_logout' si tu ID es diferente en el XML del menú.
                R.id.action_logout -> {
                    // Si es el ítem correcto, llamamos a la función de logout.
                    loginViewModel.logout()
                    // Devolvemos 'true' para indicar que hemos manejado el evento.
                    true
                }
                // Para cualquier otro ítem del menú, no hacemos nada.
                else -> false
            }
        }

        loginViewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            if (state is AuthenticationState.UNAUTHENTICATED) {
                findNavController().navigate(R.id.action_inventarioFragment_to_LoginFragment)
            }
        }

        adapter = ProductAdapter { selectedProduct ->
            val bundle = Bundle().apply {
                putString("productRef", selectedProduct.productRef)
            }
            findNavController().navigate(R.id.action_inventarioFragment_to_detailProductFragment, bundle)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val fabAddProduct = view.findViewById<FloatingActionButton>(R.id.fabAddProduct)
        fabAddProduct.setOnClickListener {
            openAddProductFragment()
        }

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // Inicializar ProductRepository con Firebase
        val repository = ProductRepository()
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProductViewModel::class.java]

        progressBar.visibility = View.VISIBLE
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.setProducts(products)
            progressBar.visibility = View.GONE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadProducts()
        }

        setFragmentResultListener("editProductRequest") { _, bundle ->
            val wasUpdated = bundle.getBoolean("productUpdated", false)
            if (wasUpdated) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.loadProducts()
                }
                Toast.makeText(requireContext(), "Lista actualizada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openAddProductFragment() {
        findNavController().navigate(R.id.action_inventarioFragment_to_addProductFragment)
    }
}
