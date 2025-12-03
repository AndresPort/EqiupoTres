package com.andresport.app_inventory.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.andresport.app_inventory.model.Product
import com.andresport.app_inventory.repository.ProductRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
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

@ExperimentalCoroutinesApi
class EditProductViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @RelaxedMockK
    private lateinit var mockRepository: ProductRepository

    private lateinit var viewModel: EditProductViewModel

    @Before
    fun onBefore() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = EditProductViewModel(mockRepository)
    }

    @After
    fun onAfter() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando se llama loadProduct con una referencia valida, debe cargar el producto correctamente`() = runTest(testDispatcher) {
        // Given
        val productRef = "PROD001"
        val expectedProduct = Product(
            productRef = productRef,
            productName = "Producto Test",
            unitPrice = 100.0,
            stock = 50L
        )
        coEvery { mockRepository.getProductById(productRef) } returns expectedProduct

        // When
        viewModel.loadProduct(productRef)
        advanceUntilIdle() // Espera a que terminen todas las corrutinas

        // Then
        coVerify { mockRepository.getProductById(productRef) }
        assert(viewModel.product.value == expectedProduct)
    }

    @Test
    fun `cuando se llama loadProduct con referencia inexistente, debe retornar null`() = runTest(testDispatcher) {
        // Given
        val productRef = "PROD_INEXISTENTE"
        coEvery { mockRepository.getProductById(productRef) } returns null

        // When
        viewModel.loadProduct(productRef)
        advanceUntilIdle()

        // Then
        coVerify { mockRepository.getProductById(productRef) }
        assert(viewModel.product.value == null)
    }

    @Test
    fun `cuando se llama updateProduct, debe actualizar el producto con los nuevos datos`() = runTest(testDispatcher) {
        // Given
        val productRef = "PROD001"
        val newName = "Producto Actualizado"
        val newPrice = 150.0
        val newStock = 75L

        val expectedProduct = Product(
            productRef = productRef,
            productName = newName,
            unitPrice = newPrice,
            stock = newStock
        )

        // When
        viewModel.updateProduct(productRef, newName, newPrice, newStock)
        advanceUntilIdle()

        // Then
        coVerify { mockRepository.updateProduct(expectedProduct) }
    }

    @Test
    fun `cuando se carga un producto y luego se actualiza, debe mantener la misma referencia`() = runTest(testDispatcher) {
        // Given
        val productRef = "PROD001"
        val initialProduct = Product(
            productRef = productRef,
            productName = "Producto Inicial",
            unitPrice = 100.0,
            stock = 50L
        )
        coEvery { mockRepository.getProductById(productRef) } returns initialProduct

        // When
        viewModel.loadProduct(productRef)
        advanceUntilIdle()
        val loadedProduct = viewModel.product.value

        viewModel.updateProduct(productRef, "Actualizado", 200.0, 100L)
        advanceUntilIdle()

        // Then
        assert(loadedProduct?.productRef == productRef)
        coVerify { mockRepository.updateProduct(any()) }
    }
}
