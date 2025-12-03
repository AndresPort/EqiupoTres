package com.andresport.app_inventory.widget

import com.andresport.app_inventory.model.InventoryRepository
import com.andresport.app_inventory.utils.SessionManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun inventoryRepository(): InventoryRepository
    fun sessionManager(): SessionManager
}
