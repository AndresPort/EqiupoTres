package com.andresport.app_inventory.di

import com.andresport.app_inventory.repository.AuthenticationRepository
import com.andresport.app_inventory.repository.IAuthenticationRepository
import com.andresport.app_inventory.repository.IProductRepository
import com.andresport.app_inventory.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(productRepository: ProductRepository): IProductRepository

    @Binds
    @Singleton
    abstract fun bindAuthenticationRepository(authenticationRepository: AuthenticationRepository): IAuthenticationRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
