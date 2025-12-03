package com.andresport.app_inventory.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.andresport.app_inventory.model.Product
import com.andresport.app_inventory.repository.IProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any // <-- CAMBIO: Importar desde la nueva librería
import org.mockito.kotlin.whenever // <-- CAMBIO: Usar whenever en lugar de `when`

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AddProductViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: IProductRepository

    private lateinit var viewModel: AddProductViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddProductViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insertProduct - cuando el producto es nuevo - retorna éxito`() = runTest {
        // ARRANGE
        val newProduct = Product("ref123", "New Product", 10.0, 5)
        // CAMBIO: Usar whenever y el nuevo any() para seguridad de nulos
        whenever(mockRepository.insertProduct(any())).thenReturn(true)

        // ACT
        var successResult = false
        var messageResult: String? = "initial"

        viewModel.insertProduct(newProduct) { success, message ->
            successResult = success
            messageResult = message
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // ASSERT
        assertEquals(true, successResult)
        assertNull(messageResult)
    }

    @Test
    fun `insertProduct - cuando el producto ya existe - retorna fallo`() = runTest {
        // ARRANGE
        val existingProduct = Product("ref456", "Existing Product", 20.0, 10)
        whenever(mockRepository.insertProduct(any())).thenReturn(false)

        // ACT
        var successResult = true
        var messageResult: String? = null

        viewModel.insertProduct(existingProduct) { success, message ->
            successResult = success
            messageResult = message
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // ASSERT
        assertEquals(false, successResult)
        assertEquals("Error: La referencia del producto ya existe.", messageResult)
    }

    @Test
    fun `insertProduct - cuando el repositorio lanza una excepción - retorna fallo`() = runTest {
        // ARRANGE
        val product = Product("ref789", "Error Product", 1.0, 1)
        val exceptionMessage = "Network error"
        whenever(mockRepository.insertProduct(any())).thenThrow(RuntimeException(exceptionMessage))

        // ACT
        var successResult = true
        var messageResult: String? = null

        viewModel.insertProduct(product) { success, message ->
            successResult = success
            messageResult = message
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // ASSERT
        assertEquals(false, successResult)
        assertEquals(exceptionMessage, messageResult)
    }
}
