package com.andresport.app_inventory.viewmodel

import androidx.lifecycle.*
import com.andresport.app_inventory.model.Product
import com.andresport.app_inventory.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _selectedProduct = MutableLiveData<Product>()
    val selectedProduct: LiveData<Product> get() = _selectedProduct

    private val _totalSum = MutableLiveData<Double>()
    val totalSum: LiveData<Double> get() = _totalSum

    fun loadProducts() {
        viewModelScope.launch {
            try {
                val items = repository.getAllProducts()
                _products.value = items
                _totalSum.value = items.sumOf { it.unitPrice * it.stock }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadProductByRef(productRef: String) {
        viewModelScope.launch {
            try {
                val product = repository.getProductById(productRef)
                product?.let { _selectedProduct.value = it }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.insertProduct(product)
                loadProducts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                loadProducts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product.productRef)   // <-- FIX IMPORTANTE
                loadProducts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
