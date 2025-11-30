package com.andresport.app_inventory.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andresport.app_inventory.R
// Asegúrate de que las siguientes rutas de importación coincidan con la estructura de tu proyecto
import com.andresport.app_inventory.repository.ProductRepository
import com.andresport.app_inventory.databinding.FragmentInventarioBinding
import com.andresport.app_inventory.util.EventObserver
import com.andresport.app_inventory.view.adapter.ProductAdapter
import com.andresport.app_inventory.viewmodel.AuthenticationState
import com.andresport.app_inventory.viewmodel.LoginViewModel
import com.andresport.app_inventory.viewmodel.ProductViewModel
import com.andresport.app_inventory.viewmodel.ViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()

    // --- DECLARACIÓN DE VARIABLES ---
    private lateinit var adapter: ProductAdapter
    private lateinit var viewModel: ProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        Este fragmento de código está comentado porque genera problemas al momento de cerrar
        sesión, por haber 2 observers pendientes de lo mismo, entonces en caso de que lo necesiten
        preparense para solucionar el problema

        loginViewModel.navigationToLoginState.observe(viewLifecycleOwner, EventObserver { state ->
            if (state is AuthenticationState.UNAUTHENTICATED) {
                findNavController().navigate(R.id.action_inventarioFragment_to_LoginFragment)
            }
        })
        */

        binding.toolbarInventario.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    loginViewModel.logout()
                    true
                }
                else -> false
            }
        }

        // --- INICIO DE LA CORRECCIÓN ---
        // Observa el estado de autenticación para redirigir al Login si no está autenticado.
        loginViewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            if (state is AuthenticationState.UNAUTHENTICATED) {
                // Prepara las opciones de navegación para limpiar la pila de fragmentos.
                val navOptions = NavOptions.Builder()
                    // Elimina todos los fragmentos hasta llegar al inicio del grafo de navegación
                    // y elimina este fragmento (InventarioFragment) de la pila.
                    .setPopUpTo(findNavController().graph.startDestinationId, true)
                    .build()

                // Navega al LoginFragment, asegurándote de que no se pueda volver atrás al inventario.
                findNavController().navigate(
                    R.id.action_inventarioFragment_to_LoginFragment,
                    null, // No se pasan argumentos en el bundle
                    navOptions // Se aplican las opciones para limpiar la pila
                )
            }
        }
        // --- FIN DE LA CORRECCIÓN ---

        adapter = ProductAdapter { selectedProduct ->
            val bundle = Bundle().apply {
                putString("productRef", selectedProduct.productRef)
            }
            findNavController().navigate(
                R.id.action_inventarioFragment_to_detailProductFragment,
                bundle
            )
        }

        val recyclerView = binding.recyclerViewProducts
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val fabAddProduct = binding.fabAddProduct
        fabAddProduct.setOnClickListener {
            openAddProductFragment()
        }

        val progressBar = binding.progressBar

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpia la referencia al binding para evitar memory leaks
    }

    private fun openAddProductFragment() {
        findNavController().navigate(R.id.action_inventarioFragment_to_addProductFragment)
    }
}
