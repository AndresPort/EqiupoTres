package com.andresport.app_inventory.repository

import com.andresport.app_inventory.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val productCollection = firestore.collection("products")

    // INSERTAR
    suspend fun insertProduct(product: Product) {
        productCollection
            .document(product.productRef)
            .set(product)
            .await()
    }

    // OBTENER TODOS
    suspend fun getAllProducts(): List<Product> {
        return productCollection
            .get()
            .await()
            .toObjects(Product::class.java)
    }

    // OBTENER UNO
    suspend fun getProductById(productRef: String): Product? {
        val snapshot = productCollection
            .document(productRef)
            .get()
            .await()

        return snapshot.toObject(Product::class.java)
    }

    // ACTUALIZAR
    suspend fun updateProduct(product: Product) {
        productCollection
            .document(product.productRef)
            .set(product)
            .await()
    }

    // ELIMINAR
    suspend fun deleteProduct(productRef: String) {
        productCollection
            .document(productRef)
            .delete()
            .await()
    }
}
