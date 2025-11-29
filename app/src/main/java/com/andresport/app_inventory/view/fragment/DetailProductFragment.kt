package com.andresport.app_inventory.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.andresport.app_inventory.R
import com.andresport.app_inventory.databinding.FragmentProductDetailBinding
import com.andresport.app_inventory.repository.ProductRepository
import com.andresport.app_inventory.viewmodel.ProductViewModel
import com.andresport.app_inventory.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
 
class DetailProductFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProductViewModel
    private var productRef: String? = null
    private var currentProduct: com.andresport.app_inventory.model.Product? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)

        binding.icBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Inicializar ProductRepository con Firebase (sin necesidad de DAO)
        val repository = ProductRepository()
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(repository)
        )[ProductViewModel::class.java]

        productRef = arguments?.getString("productRef")

        productRef?.let {
            viewModel.loadProductByRef(it)
        }

        viewModel.selectedProduct.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                currentProduct = product

                binding.txtProductName.text = product.productName
                binding.txtUnitPrice.text = String.format("$ %,.2f", product.unitPrice)
                binding.txtStock.text = product.stock.toString()
                // Calcular el total multiplicando precio por stock
                val total = product.unitPrice * product.stock
                binding.txtTotal.text = String.format("$ %,.2f", total)
            }
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.btnEditar.setOnClickListener {
            currentProduct?.let { product ->
                val bundle = Bundle().apply {
                    putString("productRef", product.productRef)
                }

                findNavController().navigate(R.id.action_productDetailFragment_to_editProductFragment, bundle)

            } ?: run {
                Toast.makeText(context, "No se pudo obtener la referencia del producto", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun showDeleteConfirmationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmar eliminación")
        builder.setMessage("¿Estás seguro de que deseas eliminar este producto?")
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setPositiveButton("Sí") { dialog, _ ->
            productRef?.let { ref ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val product = viewModel.getProductById(ref)
                    if (product != null) {
                        viewModel.deleteProduct(product)
                        Toast.makeText(
                            requireContext(),
                            "Producto eliminado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        findNavController().navigate(R.id.action_productDetailFragment_to_inventarioFragment)
                    }
                }
            }
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.showDialog))

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.showDialog))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
