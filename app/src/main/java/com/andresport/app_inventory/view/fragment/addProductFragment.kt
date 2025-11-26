package com.andresport.app_inventory.view.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.andresport.app_inventory.R
import com.andresport.app_inventory.model.Product
import com.andresport.app_inventory.viewmodel.AddProductViewModel
import com.google.android.material.textfield.TextInputEditText

class addProductFragment : Fragment() {

    private lateinit var productRefTIET: TextInputEditText
    private lateinit var productNameTIET: TextInputEditText
    private lateinit var unitPriceTIET: TextInputEditText
    private lateinit var stockTIET: TextInputEditText
    private lateinit var saveBtn: android.widget.Button
    private lateinit var returnIc: ImageView

    private val viewModel: AddProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        returnIc = view.findViewById(R.id.returnIc)
        productRefTIET = view.findViewById(R.id.productRefTIET)
        productNameTIET = view.findViewById(R.id.productNameTIET)
        unitPriceTIET = view.findViewById(R.id.unitPriceTIET)
        stockTIET = view.findViewById(R.id.stockTIET)
        saveBtn = view.findViewById(R.id.saveBtn)

        returnIc.setOnClickListener {
            returnInventoryPage()
        }

        // TextWatcher para habilitar botón
        val textWatcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                saveBtn.isEnabled =
                    productRefTIET.text!!.isNotEmpty() &&
                            productNameTIET.text!!.isNotEmpty() &&
                            unitPriceTIET.text!!.isNotEmpty() &&
                            stockTIET.text!!.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        productRefTIET.addTextChangedListener(textWatcher)
        productNameTIET.addTextChangedListener(textWatcher)
        unitPriceTIET.addTextChangedListener(textWatcher)
        stockTIET.addTextChangedListener(textWatcher)

        saveBtn.setOnClickListener {
            saveProduct()
        }
    }

    private fun saveProduct() {
        val ref = productRefTIET.text.toString().trim()
        val name = productNameTIET.text.toString().trim()
        val price = unitPriceTIET.text.toString()
            .replace(".", "")
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0
        val stock = stockTIET.text.toString().toLongOrNull() ?: 0L

        val product = Product(
            productRef = ref,
            productName = name,
            unitPrice = price,
            stock = stock
        )

        // Guardar con Firestore
        viewModel.insertProduct(product) { success, errorMessage ->
            if (success) {
                Toast.makeText(requireContext(), "Producto guardado correctamente", Toast.LENGTH_SHORT).show()
                returnInventoryPage()
            } else {
                Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun returnInventoryPage() {
        findNavController().navigate(R.id.action_addProductFragment_to_inventarioFragment)
    }
}
