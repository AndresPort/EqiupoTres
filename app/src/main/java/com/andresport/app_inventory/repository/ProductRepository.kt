package com.andresport.app_inventory.repository

import com.andresport.app_inventory.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// 1. SE DEFINE LA INTERFAZ
interface IProductRepository {
    suspend fun exists(productRef: String): Boolean
    suspend fun insertProduct(product: Product): Boolean
    suspend fun getAllProducts(): List<Product>
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun getProductById(productRef: String): Product?
}

// 2. LA CLASE AHORA IMPLEMENTA LA INTERFAZ
class ProductRepository @Inject constructor() : IProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("products")

    override suspend fun exists(productRef: String): Boolean {
        return try {
            val document = collection.document(productRef).get().await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun insertProduct(product: Product): Boolean {
        return try {
            if (exists(product.productRef)) {
                return false
            }
            collection.document(product.productRef).set(product).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAllProducts(): List<Product> {
        return try {
            val snapshot = collection.get().await()
            snapshot.toObjects(Product::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateProduct(product: Product) {
        try {
            collection.document(product.productRef).set(product).await()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deleteProduct(product: Product) {
        try {
            collection.document(product.productRef).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getProductById(productRef: String): Product? {
        return try {
            val doc = collection.document(productRef).get().await()
            doc.toObject(Product::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
