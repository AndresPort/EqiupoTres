package com.andresport.app_inventory.model

data class Product(
    val productRef: String = "",
    val productName: String = "",
    val unitPrice: Double = 0.0,
    val stock: Long = 0
)
