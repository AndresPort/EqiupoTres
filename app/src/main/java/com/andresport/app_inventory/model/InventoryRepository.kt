package com.andresport.app_inventory.model

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio que maneja las operaciones de datos para el inventario.
 * Actúa como la capa del Modelo en la arquitectura MVC.
 * Usado específicamente para el widget de inventario.
 */
class InventoryRepository(context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val productCollection = firestore.collection("products")

    /**
     * Criterio 8: Calcula el valor total del inventario multiplicando el precio de cada producto
     * por su cantidad y sumando los totales.
     */
    suspend fun getTotalInventoryValue(): Double {
        return try {
            val allProducts = productCollection.get().await().toObjects(Product::class.java)
            var totalValue = 0.0
            for (product in allProducts) {
                val productTotal = product.unitPrice * product.stock
                totalValue += productTotal
            }
            totalValue
        } catch (e: Exception) {
            0.0
        }
    }
}
