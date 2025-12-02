package com.andresport.app_inventory.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.andresport.app_inventory.model.Product
import com.andresport.app_inventory.repository.IProductRepository // <-- CAMBIO: Importar la interfaz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ProductViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: IProductRepository // <-- CAMBIO: Mockear la interfaz

    private lateinit var viewModel: ProductViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Inyectamos el mock de la interfaz en el ViewModel
        viewModel = ProductViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProducts - cuando el repositorio tiene datos, el LiveData se actualiza`() = runTest {
        // ARRANGE
        val fakeProducts = listOf(
            Product(productRef = "1", productName = "Test Product 1", unitPrice = 10.0, stock = 5), // Total: 50.0
            Product(productRef = "2", productName = "Test Product 2", unitPrice = 20.0, stock = 2)  // Total: 40.0
        )
        `when`(mockRepository.getAllProducts()).thenReturn(fakeProducts)

        // ACT
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        // ASSERT
        assertEquals(fakeProducts, viewModel.products.value)
        assertEquals(90.0, viewModel.totalSum.value!!, 0.0)
    }

    @Test
    fun `loadProducts - cuando el repositorio está vacío, el LiveData está vacío`() = runTest {
        // ARRANGE
        val emptyList = emptyList<Product>()
        `when`(mockRepository.getAllProducts()).thenReturn(emptyList)

        // ACT
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        // ASSERT
        assertTrue(viewModel.products.value?.isEmpty() ?: false)
        assertEquals(0.0, viewModel.totalSum.value!!, 0.0)
    }
}
