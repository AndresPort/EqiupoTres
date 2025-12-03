package com.andresport.app_inventory.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andresport.app_inventory.model.Product
import com.andresport.app_inventory.repository.IProductRepository
import com.andresport.app_inventory.util.OpenForTesting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@OpenForTesting
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: IProductRepository
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _selectedProduct = MutableLiveData<Product?>()
    val selectedProduct: LiveData<Product?> get() = _selectedProduct

    private val _totalSum = MutableLiveData<Double>()
    val totalSum: LiveData<Double> get() = _totalSum


    // ============================================================
    // Load
    // ============================================================
    fun loadProducts() {
        viewModelScope.launch {
            val list = repository.getAllProducts()
            _products.value = list
            _totalSum.value = list.sumOf { it.unitPrice * it.stock }
        }
    }

    fun loadProductByRef(productRef: String) {
        viewModelScope.launch {
            val product = repository.getProductById(productRef)
            _selectedProduct.postValue(product)
        }
    }

    // ============================================================
    // Insert (antes AddProductViewModel)
    // ============================================================
    fun insertProduct(product: Product, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.insertProduct(product)

                if (success) {
                    loadProducts()
                    onResult(true, null)
                } else {
                    onResult(false, "Error: La referencia del producto ya existe.")
                }

            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // ============================================================
    // Update general + Update tipo EditProduct
    // ============================================================
    fun updateProduct(product: Product, onResult: (Boolean, String?) -> Unit = {_,_->} ) {
        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                loadProducts()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
    fun updateProductFields(
        productRef: String,
        newName: String,
        newPrice: Double,
        newStock: Long,
        onResult: (Boolean, String?) -> Unit = {_,_->}
    ) {
        val updated = Product(
            productRef = productRef,
            productName = newName,
            unitPrice = newPrice,
            stock = newStock
        )
        updateProduct(updated, onResult)
    }


    // ============================================================
    // Delete
    // ============================================================
    fun deleteProduct(product: Product, onResult: (Boolean, String?) -> Unit = {_,_->}) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                loadProducts()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }


    suspend fun getProductById(productRef: String): Product? =
        repository.getProductById(productRef)
}
