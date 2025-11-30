package com.andresport.app_inventory.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar // -> IMPORTACIÓN AÑADIDA
import android.widget.Toast // -> IMPORTACIÓN AÑADIDA
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener // -> IMPORTACIÓN AÑADIDA
import androidx.lifecycle.ViewModelProvider // -> IMPORTACIÓN AÑADIDA
import androidx.lifecycle.lifecycleScope // -> IMPORTACIÓN AÑADIDA
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager // -> IMPORTACIÓN AÑADIDA
import androidx.recyclerview.widget.RecyclerView // -> IMPORTACIÓN AÑADIDA
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
import com.google.android.material.floatingactionbutton.FloatingActionButton // -> IMPORTACIÓN AÑADIDA
import kotlinx.coroutines.launch // -> IMPORTACIÓN AÑADIDA

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()

    // --- DECLARACIÓN DE VARIABLES FALTANTES ---
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

        loginViewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            if (state is AuthenticationState.UNAUTHENTICATED) {
                findNavController().navigate(R.id.action_inventarioFragment_to_LoginFragment)
            }
        }

        adapter = ProductAdapter { selectedProduct ->
            val bundle = Bundle().apply {
                putString("productRef", selectedProduct.productRef)
            }
            findNavController().navigate(
                R.id.action_inventarioFragment_to_detailProductFragment,
                bundle
            )
        }

        // Es mejor usar 'binding' para acceder a las vistas si tienes View Binding habilitado
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
            // Asumiendo que tu adapter tiene un método setProducts o similar para actualizar la lista
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
