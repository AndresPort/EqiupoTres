package com.andresport.app_inventory.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andresport.app_inventory.repository.IProductRepository // <-- CAMBIO
import com.andresport.app_inventory.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProductViewModel @Inject constructor(
    private val repository: IProductRepository // <-- CAMBIO
) : ViewModel() {

    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product

    fun loadProduct(productRef: String) {
        viewModelScope.launch {
            val productData = repository.getProductById(productRef)
            _product.postValue(productData)
        }
    }

    fun updateProduct(productRef: String, newName: String, newPrice: Double, newStock: Long) {
        viewModelScope.launch {
            val updatedProduct = Product(
                productRef = productRef,
                productName = newName,
                unitPrice = newPrice,
                stock = newStock
            )
            repository.updateProduct(updatedProduct)
        }
    }
}
