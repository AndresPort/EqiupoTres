package com.andresport.app_inventory.repository

import com.andresport.app_inventory.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val productCollection = firestore.collection("products")

    // ------------------------------------------
    // INSERTAR PRODUCTO
    // ------------------------------------------
    suspend fun insertProduct(product: Product) {
        productCollection
            .document(product.productRef)
            .set(product)
            .await()
    }

    // ------------------------------------------
    // OBTENER TODOS LOS PRODUCTOS
    // ------------------------------------------
    suspend fun getAllProducts(): List<Product> {
        return productCollection
            .get()
            .await()
            .toObjects(Product::class.java)
    }

    // ------------------------------------------
    // OBTENER PRODUCTO POR ID
    // ------------------------------------------
    suspend fun getProductById(productRef: String): Product? {
        val snapshot = productCollection
            .document(productRef)
            .get()
            .await()

        return snapshot.toObject(Product::class.java)
    }

    // ------------------------------------------
    // ACTUALIZAR PRODUCTO
    // ------------------------------------------
    suspend fun updateProduct(product: Product) {
        productCollection
            .document(product.productRef)
            .set(product)
            .await()
    }

    // ------------------------------------------
    // ELIMINAR PRODUCTO
    // ------------------------------------------
    suspend fun deleteProduct(productRef: String) {
        productCollection
            .document(productRef)
            .delete()
            .await()
    }
}
