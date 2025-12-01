package com.andresport.app_inventory.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.andresport.app_inventory.repository.ProductRepository
import com.andresport.app_inventory.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(private val repository: ProductRepository) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _selectedProduct = MutableLiveData<Product>()
    val selectedProduct: LiveData<Product> get() = _selectedProduct

    private val _totalSum = MutableLiveData<Double>()
    val totalSum: LiveData<Double> get() = _totalSum

    fun loadProducts() {
        viewModelScope.launch {
            val entities = repository.getAllProducts()
            _products.value = entities.map { e ->
                Product(
                    productRef = e.productRef,
                    productName = e.productName,
                    unitPrice = e.unitPrice,
                    stock = e.stock
                )
            }
            // Calcular el total sumando unitPrice * stock de cada producto
            _totalSum.value = entities.sumOf { it.unitPrice * it.stock }
        }
    }

    fun loadProductByRef(productRef: String) {
        viewModelScope.launch {
            val product = repository.getProductById(productRef)
            product?.let { _selectedProduct.value = it }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            val entity = Product(
                productRef = product.productRef,
                productName = product.productName,
                stock = product.stock,
                unitPrice = product.unitPrice
            )
            repository.insertProduct(entity)
            loadProducts()
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            val entity = Product(
                productRef = product.productRef,
                productName = product.productName,
                stock = product.stock,
                unitPrice = product.unitPrice
            )
            repository.updateProduct(entity)
            loadProducts()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            val entity = Product(
                productRef = product.productRef,
                productName = product.productName,
                stock = product.stock,
                unitPrice = product.unitPrice
            )
            repository.deleteProduct(entity)
            loadProducts()
        }
    }

    suspend fun getProductById(productRef: String): Product? {
        return repository.getProductById(productRef)
    }
}
