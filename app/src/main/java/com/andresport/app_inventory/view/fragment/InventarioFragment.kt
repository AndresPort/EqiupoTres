package com.andresport.app_inventory.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.andresport.app_inventory.R
import com.andresport.app_inventory.databinding.FragmentInventarioBinding
import com.andresport.app_inventory.view.adapter.ProductAdapter
import com.andresport.app_inventory.viewmodel.AuthenticationState
import com.andresport.app_inventory.viewmodel.LoginViewModel
import com.andresport.app_inventory.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint

// PASO 1: Anotar el Fragment para que Hilt pueda inyectar dependencias.
@AndroidEntryPoint
class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!

    // El LoginViewModel sigue obteniéndose de la Activity, esto no cambia.
    private val loginViewModel: LoginViewModel by activityViewModels()

    // --- DECLARACIÓN DE VARIABLES ---
    private lateinit var adapter: ProductAdapter

    // PASO 2: Inyectar el ProductViewModel usando Hilt.
    // Hilt se encargará de crear el ViewModel con su repositorio.
    private val viewModel: ProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarInventario.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    loginViewModel.logout()
                    true
                }
                else -> false
            }
        }

        // Observador para el estado de autenticación (sin cambios).
        loginViewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            if (state is AuthenticationState.UNAUTHENTICATED) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(findNavController().graph.startDestinationId, true)
                    .build()
                findNavController().navigate(
                    R.id.action_inventarioFragment_to_LoginFragment,
                    null,
                    navOptions
                )
            }
        }

        // Configuración del adaptador del RecyclerView (sin cambios).
        adapter = ProductAdapter { selectedProduct ->
            val bundle = Bundle().apply {
                putString("productRef", selectedProduct.productRef)
            }
            findNavController().navigate(
                R.id.action_inventarioFragment_to_detailProductFragment,
                bundle
            )
        }

        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProducts.adapter = adapter

        binding.fabAddProduct.setOnClickListener {
            openAddProductFragment()
        }

        // PASO 3: Eliminar la creación manual del ViewModel y sus dependencias.
        // val repository = ProductRepository() // <-- ELIMINADO
        // val factory = ViewModelFactory(repository) // <-- ELIMINADO
        // viewModel = ViewModelProvider(this, factory)[ProductViewModel::class.java] // <-- REEMPLAZADO por "by viewModels()"

        // Observar los datos del ViewModel inyectado.
        binding.progressBar.visibility = View.VISIBLE
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.setProducts(products)
            binding.progressBar.visibility = View.GONE
        }

        // Cargar los productos.
        // El ViewModel ya está disponible, así que podemos llamar a sus métodos directamente.
        viewModel.loadProducts()

        // Listener para cuando un producto es editado (sin cambios funcionales).
        setFragmentResultListener("editProductRequest") { _, bundle ->
            val wasUpdated = bundle.getBoolean("productUpdated", false)
            if (wasUpdated) {
                viewModel.loadProducts() // Recargamos los productos.
                Toast.makeText(requireContext(), "Lista actualizada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpia la referencia al binding para evitar memory leaks.
    }

    private fun openAddProductFragment() {
        findNavController().navigate(R.id.action_inventarioFragment_to_addProductFragment)
    }
}
