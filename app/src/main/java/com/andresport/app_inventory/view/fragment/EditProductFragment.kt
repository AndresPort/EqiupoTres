package com.andresport.app_inventory.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.andresport.app_inventory.R
import com.andresport.app_inventory.viewmodel.ProductViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProductFragment : Fragment(R.layout.fragment_edit_product) {

    private var productId: String? = null


    private val viewModel: ProductViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getString("productRef")

        if (productId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Error: No se pudo obtener la referencia del producto", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }

        val returnIc = view.findViewById<ImageView>(R.id.returnIc)
        val tvProductValue = view.findViewById<TextView>(R.id.tvProductIdValue)
        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etPrice)
        val etQuantity = view.findViewById<TextInputEditText>(R.id.etQuantity)
        val btnSaveChanges = view.findViewById<Button>(R.id.btnSaveChanges)

        tvProductValue.text = productId

        returnIc.setOnClickListener { findNavController().popBackStack() }

        // -------------------- Habilitar botón si todos los campos están llenos --------------------
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnSaveChanges.isEnabled =
                    etName.text?.isNotBlank() == true &&
                            etPrice.text?.isNotBlank() == true &&
                            etQuantity.text?.isNotBlank() == true
            }
        }

        etName.addTextChangedListener(watcher)
        etPrice.addTextChangedListener(watcher)
        etQuantity.addTextChangedListener(watcher)

        // -------------------- Obtener producto --------------------
        viewModel.selectedProduct.observe(viewLifecycleOwner) { product ->
            product?.let {
                etName.setText(it.productName)
                etPrice.setText(it.unitPrice.toString())
                etQuantity.setText(it.stock.toString())
            } ?: run {
                Toast.makeText(requireContext(), "Producto no encontrado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        // cargar desde DB
        viewModel.loadProductByRef(productId!!)

        // -------------------- Guardar cambios --------------------
        btnSaveChanges.setOnClickListener {
            val name = etName.text.toString().trim()
            val price = etPrice.text.toString().toDoubleOrNull()
            val quantity = etQuantity.text.toString().toLongOrNull()

            if (name.isBlank() || price == null || quantity == null || price <= 0 || quantity < 0) {
                Toast.makeText(requireContext(), "Ingrese valores válidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateProductFields(productId!!, name, price, quantity) { success, msg ->
                if (success) {
                    Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editProductFragment_to_inventarioFragment)
                } else {
                    Toast.makeText(requireContext(), msg ?: "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
