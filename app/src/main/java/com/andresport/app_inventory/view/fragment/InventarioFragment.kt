package com.andresport.app_inventory.view.fragment

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andresport.app_inventory.R
import com.andresport.app_inventory.repository.ProductRepository
import com.andresport.app_inventory.view.adapter.ProductAdapter
import com.andresport.app_inventory.viewmodel.AuthenticationState
import com.andresport.app_inventory.viewmodel.LoginViewModel
import com.andresport.app_inventory.viewmodel.ProductViewModel
import com.andresport.app_inventory.viewmodel.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

// Heredamos de Fragment(R.layout.fragment_inventario) para que infle el layout automáticamente.
// Esto nos permite eliminar el método onCreateView.
class InventarioFragment : Fragment(R.layout.fragment_inventario) {

    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: ProductAdapter
    private val loginViewModel: LoginViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarInventario)

        toolbar.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                // Usamos el ID de tu menú: R.id.action_logout.
                R.id.action_logout -> {
                    loginViewModel.logout()
                    true
                }
                else -> {
                    // Si es otro ítem, retornamos 'false' para que el sistema lo maneje.
                    false
                }
            }
        }
        loginViewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            if (state is AuthenticationState.UNAUTHENTICATED) {
                // Navega de vuelta al LoginFragment.
                // Asegúrate de tener una acción con este ID en tu nav_graph.xml
                findNavController().navigate(R.id.action_inventarioFragment_to_LoginFragment)
            }
        }
        // --- FIN DE LA CORRECCIÓN ---


        // --- Resto de tu código de inicialización (sin cambios) ---

        // Inicializar RecyclerView
        adapter = ProductAdapter { selectedProduct ->
            val bundle = Bundle().apply {
                putString("productRef", selectedProduct.productRef)
            }
            findNavController().navigate(R.id.action_inventarioFragment_to_detailProductFragment, bundle)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Inicializar botón flotante
        val fabAddProduct = view.findViewById<FloatingActionButton>(R.id.fabAddProduct)
        fabAddProduct.setOnClickListener {
            openAddProductFragment()
        }

        // Inicializar ProgressBar
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // Inicializar ViewModel
        val dao = AppDatabase.getInstance(requireContext()).productDao()
        val repository = ProductRepository(dao)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProductViewModel::class.java]

        // Observar y cargar productos
        progressBar.visibility = View.VISIBLE
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.setProducts(products)
            progressBar.visibility = View.GONE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadProducts()
        }

        // Listener para resultados de otros fragments (como editar producto)
        setFragmentResultListener("editProductRequest") { _, bundle ->
            val wasUpdated = bundle.getBoolean("productUpdated", false)
            if (wasUpdated) {
                // Recargar la lista de productos si hubo una actualización
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
