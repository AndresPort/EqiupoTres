package com.andresport.app_inventory.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.andresport.app_inventory.model.Product
import com.andresport.app_inventory.repository.IProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class) // <-- Se usa el runner de Mockito
class EditProductViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock // <-- Se usa la anotación de Mockito
    private lateinit var mockRepository: IProductRepository

    private lateinit var viewModel: EditProductViewModel

    @Before
    fun onBefore() {
        Dispatchers.setMain(testDispatcher)
        viewModel = EditProductViewModel(mockRepository)
    }

    @After
    fun onAfter() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando se llama loadProduct con una referencia valida, debe cargar el producto correctamente`() = runTest {
        // Given
        val productRef = "PROD001"
        val expectedProduct = Product(productRef, "Producto Test", 100.0, 50L)
        // Se usa `whenever` de mockito-kotlin
        whenever(mockRepository.getProductById(productRef)).thenReturn(expectedProduct)

        // When
        viewModel.loadProduct(productRef)
        advanceUntilIdle()

        // Then
        verify(mockRepository).getProductById(productRef) // Se usa `verify` de mockito-kotlin
        assert(viewModel.product.value == expectedProduct)
    }

    @Test
    fun `cuando se llama loadProduct con referencia inexistente, debe retornar null`() = runTest {
        // Given
        val productRef = "PROD_INEXISTENTE"
        whenever(mockRepository.getProductById(productRef)).thenReturn(null)

        // When
        viewModel.loadProduct(productRef)
        advanceUntilIdle()

        // Then
        verify(mockRepository).getProductById(productRef)
        assert(viewModel.product.value == null)
    }

    @Test
    fun `cuando se llama updateProduct, debe actualizar el producto con los nuevos datos`() = runTest {
        // Given
        val productRef = "PROD001"
        val newName = "Producto Actualizado"
        val newPrice = 150.0
        val newStock = 75L

        val expectedProduct = Product(productRef, newName, newPrice, newStock)

        // When
        viewModel.updateProduct(productRef, newName, newPrice, newStock)
        advanceUntilIdle()

        // Then
        verify(mockRepository).updateProduct(expectedProduct)
    }

    @Test
    fun `cuando se carga un producto y luego se actualiza, debe mantener la misma referencia`() = runTest {
        // Given
        val productRef = "PROD001"
        val initialProduct = Product(productRef, "Producto Inicial", 100.0, 50L)
        whenever(mockRepository.getProductById(productRef)).thenReturn(initialProduct)

        // When
        viewModel.loadProduct(productRef)
        advanceUntilIdle()
        val loadedProduct = viewModel.product.value

        viewModel.updateProduct(productRef, "Actualizado", 200.0, 100L)
        advanceUntilIdle()

        // Then
        assert(loadedProduct?.productRef == productRef)
        verify(mockRepository).updateProduct(any()) // Se usa `any()` de mockito-kotlin
    }
}
