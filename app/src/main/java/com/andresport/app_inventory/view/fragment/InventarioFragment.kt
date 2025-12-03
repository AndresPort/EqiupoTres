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

@AndroidEntryPoint
class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val viewModel: ProductViewModel by viewModels()

    private lateinit var adapter: ProductAdapter

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

        // CAMBIO: Observar el LiveData envuelto en un Event
        loginViewModel.navigationToLoginState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { state ->
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
        }

        setupRecyclerView()

        binding.fabAddProduct.setOnClickListener {
            openAddProductFragment()
        }

        observeViewModel()

        setFragmentResultListeners()
    }

    private fun setupRecyclerView() {
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
    }

    private fun observeViewModel() {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.setProducts(products)
            binding.progressBar.visibility = View.GONE
        }
        viewModel.loadProducts()
    }

    private fun setFragmentResultListeners() {
        setFragmentResultListener("editProductRequest") { _, bundle ->
            val wasUpdated = bundle.getBoolean("productUpdated", false)
            if (wasUpdated) {
                viewModel.loadProducts()
                Toast.makeText(requireContext(), "Lista actualizada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openAddProductFragment() {
        findNavController().navigate(R.id.action_inventarioFragment_to_addProductFragment)
    }
}
