package com.andresport.app_inventory.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andresport.app_inventory.repository.ProductRepository
import com.andresport.app_inventory.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


// El ViewModel recibe el Repositorio como parámetro para poder pedirle datos.
@HiltViewModel // aqui se usa HiltViewModel para inyectar dependencias
class EditProductViewModel @Inject constructor( // se usa inject para inyectar el repositorio
    private val repository: ProductRepository) : ViewModel() {

    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product

    fun loadProduct(productRef: String) {
        // viewModelScope es la forma segura de lanzar corrutinas.
        // Se cancelan automáticamente si el ViewModel se destruye.
        viewModelScope.launch {
            val productData = repository.getProductById(productRef)
            _product.postValue(productData)
        }
    }
    fun updateProduct(productRef: String, newName: String, newPrice: Double, newStock: Long) {
        viewModelScope.launch {
            // Creamos un nuevo objeto Product con los datos actualizados
            val updatedProduct = Product(
                productRef = productRef,
                productName = newName,
                unitPrice = newPrice,
                stock = newStock
            )
            // Le pasamos el producto actualizado al repositorio para que lo guarde en la BD
            repository.updateProduct(updatedProduct)
        }
    }
}
