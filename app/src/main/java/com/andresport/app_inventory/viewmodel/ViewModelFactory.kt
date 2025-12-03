/*
package com.andresport.app_inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andresport.app_inventory.repository.ProductRepository

class ViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }

        if (modelClass.isAssignableFrom(EditProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditProductViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

 */