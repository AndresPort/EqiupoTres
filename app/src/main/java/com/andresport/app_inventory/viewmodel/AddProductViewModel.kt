package com.andresport.app_inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andresport.app_inventory.model.Product
import com.andresport.app_inventory.repository.ProductRepository
import kotlinx.coroutines.launch

class AddProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    fun insertProduct(product: Product, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.insertProduct(product)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
