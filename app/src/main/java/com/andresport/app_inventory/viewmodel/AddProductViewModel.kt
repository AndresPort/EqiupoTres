package com.andresport.app_inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andresport.app_inventory.model.Product
import com.andresport.app_inventory.repository.IProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val repository: IProductRepository
) : ViewModel() {

    fun insertProduct(product: Product, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // La función insertProduct del repositorio devuelve `true` si tiene éxito,
                // y `false` si el producto ya existe.
                val success = repository.insertProduct(product)

                if (success) {
                    onResult(true, null)
                } else {
                    onResult(false, "Error: La referencia del producto ya existe.")
                }
            } catch (e: Exception) {
                // Captura cualquier otro error (ej: fallo de red con Firebase)
                onResult(false, e.message)
            }
        }
    }
}
