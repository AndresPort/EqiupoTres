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

class InventarioFragment : Fragment(R.layout.fragment_inventario) {

    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: ProductAdapter
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarInventario)

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    loginViewModel.logout()
                    true
                }
                else -> false
            }
        }

        // Logout listener
        loginViewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            if (state is AuthenticationState.UNAUTHENTICATED) {
                findNavController().navigate(R.id.action_inventarioFragment_to_LoginFragment)
            }
        }

        // Adapter con callback de clic
        adapter = ProductAdapter { selectedProduct ->
            val bundle = Bundle().apply {
                putString("productRef", selectedProduct.productRef)
            }
            findNavController().navigate(
                R.id.action_inventarioFragment_to_detailProductFragment,
                bundle
            )
        }

        // RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val fabAddProduct = view.findViewById<FloatingActionButton>(R.id.fabAddProduct)
        fabAddProduct.setOnClickListener {
            findNavController().navigate(R.id.action_inventarioFragment_to_addProductFragment)
        }

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // Firestore Repository
        val repository = ProductRepository()
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProductViewModel::class.java]

        progressBar.visibility = View.VISIBLE

        // Observa la lista de productos
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.setProducts(products)
            progressBar.visibility = View.GONE
        }

        // Carga inicial
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadProducts()
        }

        // Listener que se activa al volver de editar
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
}
