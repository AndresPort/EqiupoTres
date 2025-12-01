package com.andresport.app_inventory.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Importa tus clases de model y repository
import com.andresport.app_inventory.repository.ProductRepository
import com.andresport.app_inventory.model.Product
import kotlinx.coroutines.launch

// El ViewModel recibe el Repositorio como parámetro para poder pedirle datos.
class EditProductViewModel(private val repository: ProductRepository) : ViewModel() {

    // Este LiveData guardará el producto que carguemos. Es privado para que
    // solo el ViewModel pueda modificarlo.
    private val _product = MutableLiveData<Product?>()

    // Este es el LiveData público que la vista (el Fragment) observará.
    // Es inmutable desde fuera para proteger los datos.
    val product: LiveData<Product?> = _product

    // OJO: La función debe recibir un String, porque el ID de tu producto es un String.
    fun loadProduct(productRef: String) {
        // viewModelScope es la forma segura de lanzar corrutinas en un ViewModel.
        // Se cancelan automáticamente si el ViewModel se destruye.
        viewModelScope.launch {
            // Llama a la función del repositorio (que ahora está devolviendo datos simulados).
            val productData = repository.getProductById(productRef)
            // postValue se usa para actualizar un LiveData desde una corrutina en un hilo secundario.
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
