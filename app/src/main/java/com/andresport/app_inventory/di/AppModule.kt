package com.andresport.app_inventory.di

import com.andresport.app_inventory.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton // Esto asegura que solo haya una instancia del repositorio en toda la app.
    fun provideProductRepository(): ProductRepository {
        return ProductRepository()
    }
}
