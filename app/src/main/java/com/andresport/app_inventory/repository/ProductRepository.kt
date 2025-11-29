package com.andresport.app_inventory.repository

import com.andresport.app_inventory.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("products")

    suspend fun exists(productRef: String): Boolean {
        return try {
            val document = collection.document(productRef).get().await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun insertProduct(product: Product): Boolean {
        return try {

            // VALIDAR DUPLICADO
            if (exists(product.productRef)) {
                return false  // No se debe insertar
            }

            // Insertar normalmente
            collection.document(product.productRef)
                .set(product)
                .await()

            true  // Insertado correctamente

        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllProducts(): List<Product> {
        return try {
            val snapshot = collection.get().await()
            snapshot.toObjects(Product::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateProduct(product: Product) {
        try {
            collection.document(product.productRef).set(product).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteProduct(product: Product) {
        try {
            collection.document(product.productRef).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getProductById(productRef: String): Product? {
        return try {
            val doc = collection.document(productRef).get().await()
            doc.toObject(Product::class.java)
        } catch (e: Exception) {
            null
        }
    }
}



