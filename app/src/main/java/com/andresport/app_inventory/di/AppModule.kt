package com.andresport.app_inventory.di

import android.content.Context
import com.andresport.app_inventory.model.InventoryRepository
import com.andresport.app_inventory.repository.ProductRepository
import com.andresport.app_inventory.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideProductRepository(): ProductRepository = ProductRepository()

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager =
        SessionManager(context)

    @Provides
    @Singleton
    fun provideInventoryRepository(@ApplicationContext context: Context): InventoryRepository =
        InventoryRepository(context)
}
