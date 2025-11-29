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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.setFragmentResult
import com.andresport.app_inventory.R
import com.andresport.app_inventory.repository.ProductRepository
import com.andresport.app_inventory.viewmodel.EditProductViewModel
import com.andresport.app_inventory.viewmodel.ViewModelFactory
import com.google.android.material.textfield.TextInputEditText

class EditProductFragment : Fragment(R.layout.fragment_edit_product) {
    private var productId: String? = null // variable local para guardar el ID del producto
    private val viewModel: EditProductViewModel by viewModels {
        ViewModelFactory(
            ProductRepository()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productId = arguments?.getString("productRef")

        if (productId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Error: No se pudo obtener la referencia del producto", Toast.LENGTH_LONG).show()
            findNavController().popBackStack() // Regresa a la pantalla anterior si no hay ID
            return
        }

        // CRITERIO 1: Toolbar - Botón de retroceso
        val returnIc = view.findViewById<ImageView>(R.id.returnIc)
        returnIc.setOnClickListener {
            findNavController().popBackStack()
        }

        // CRITERIO 2: ID no editable
        val tvProductValue = view.findViewById<TextView>(R.id.tvProductIdValue)
        tvProductValue.text = productId

        // CRITERIO 3: Campos
        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etPrice)
        val etQuantity = view.findViewById<TextInputEditText>(R.id.etQuantity)
        val btnSaveChanges = view.findViewById<Button>(R.id.btnSaveChanges)

        // CRITERIO 5: Habilitar botón solo si TODOS los campos tienen texto
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val nameNotEmpty = etName.text?.isNotBlank() == true
                val priceNotEmpty = etPrice.text?.isNotBlank() == true
                val quantityNotEmpty = etQuantity.text?.isNotBlank() == true

                btnSaveChanges.isEnabled = nameNotEmpty && priceNotEmpty && quantityNotEmpty
            }
        }

        etName.addTextChangedListener(textWatcher)
        etPrice.addTextChangedListener(textWatcher)
        etQuantity.addTextChangedListener(textWatcher)

        // Observar y rellenar campos desde BD
        viewModel.product.observe(viewLifecycleOwner, Observer { product ->
            product?.let {
                etName.setText(it.productName)
                etPrice.setText(it.unitPrice.toString())
                etQuantity.setText(it.stock.toString())
            } ?: run {
                Toast.makeText(requireContext(), "Producto no encontrado en la base de datos", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        })

        // CRITERIO 4: Guardar cambios (UPDATE en BD)
        btnSaveChanges.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newPriceStr = etPrice.text.toString().trim()
            val newQuantityStr = etQuantity.text.toString().trim()
            val newPrice = newPriceStr.toDoubleOrNull()
            val newQuantity = newQuantityStr.toLongOrNull()

            if (newPrice == null || newQuantity == null || newPrice <= 0 || newQuantity < 0) {
                Toast.makeText(requireContext(), "Precio y cantidad deben ser números válidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Actualizar en BD
            viewModel.updateProduct(productId!!, newName, newPrice, newQuantity)

            Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_editProductFragment_to_inventarioFragment)
        }

        // Cargar producto desde BD
        viewModel.loadProduct(productId!!)
    }
}