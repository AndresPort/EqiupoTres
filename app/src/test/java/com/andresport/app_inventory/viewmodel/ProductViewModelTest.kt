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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
@RunWith(MockitoJUnitRunner::class)
class ProductViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: IProductRepository

    private lateinit var viewModel: ProductViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProductViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Pruebas para loadProducts (ya existentes) ---
    @Test
    fun `loadProducts - cuando el repositorio tiene datos, el LiveData se actualiza`() = runTest {
        val fakeProducts = listOf(
            Product("1", "P1", 10.0, 5),
            Product("2", "P2", 20.0, 2)
        )
        whenever(mockRepository.getAllProducts()).thenReturn(fakeProducts)

        viewModel.loadProducts()
        advanceUntilIdle()

        assertEquals(fakeProducts, viewModel.products.value)
        assertEquals(90.0, viewModel.totalSum.value!!, 0.0)
    }

    @Test
    fun `loadProducts - cuando el repositorio está vacío, el LiveData está vacío`() = runTest {
        whenever(mockRepository.getAllProducts()).thenReturn(emptyList())

        viewModel.loadProducts()
        advanceUntilIdle()

        assertTrue(viewModel.products.value?.isEmpty() ?: false)
        assertEquals(0.0, viewModel.totalSum.value!!, 0.0)
    }

    // --- Pruebas para loadProductByRef ---
    @Test
    fun `loadProductByRef - cuando la referencia existe - el LiveData del producto seleccionado se actualiza`() = runTest {
        val productRef = "prod123"
        val fakeProduct = Product(productRef, "Test Product", 10.0, 1)
        whenever(mockRepository.getProductById(productRef)).thenReturn(fakeProduct)

        viewModel.loadProductByRef(productRef)
        advanceUntilIdle()

        assertEquals(fakeProduct, viewModel.selectedProduct.value)
    }

    // --- Pruebas para addProduct ---
    @Test
    fun `addProduct - debe llamar a insertProduct del repositorio y recargar la lista`() = runTest {
        val newProduct = Product("newProd", "New Product", 15.0, 10)
        // Hacemos que getAllProducts devuelva una lista vacía la primera vez y la nueva lista la segunda vez
        whenever(mockRepository.getAllProducts()).thenReturn(emptyList(), listOf(newProduct))

        viewModel.addProduct(newProduct)
        advanceUntilIdle()

        // Verifica que se llamó a insertar y luego a recargar (que a su vez llama a getAllProducts)
        verify(mockRepository).insertProduct(newProduct)
        verify(mockRepository).getAllProducts()
        assertEquals(listOf(newProduct), viewModel.products.value)
    }

    // --- Pruebas para updateProduct ---
    @Test
    fun `updateProduct - debe llamar a updateProduct del repositorio y recargar la lista`() = runTest {
        val updatedProduct = Product("prod1", "Updated Product", 25.0, 20)
        whenever(mockRepository.getAllProducts()).thenReturn(listOf(updatedProduct))

        viewModel.updateProduct(updatedProduct)
        advanceUntilIdle()

        verify(mockRepository).updateProduct(updatedProduct)
        verify(mockRepository).getAllProducts()
        assertEquals(listOf(updatedProduct), viewModel.products.value)
    }

    // --- Pruebas para deleteProduct ---
    @Test
    fun `deleteProduct - debe llamar a deleteProduct del repositorio y recargar la lista`() = runTest {
        val productToDelete = Product("prod2", "ToDelete", 5.0, 5)
        // La segunda llamada a getAllProducts debe devolver una lista vacía
        whenever(mockRepository.getAllProducts()).thenReturn(emptyList())

        viewModel.deleteProduct(productToDelete)
        advanceUntilIdle()

        verify(mockRepository).deleteProduct(productToDelete)
        verify(mockRepository).getAllProducts()
        assertTrue(viewModel.products.value?.isEmpty() ?: false)
    }

    // --- Pruebas para getProductById ---
    @Test
    fun `getProductById - debe llamar al repositorio y devolver el producto`() = runTest {
        val productRef = "prod_direct"
        val expectedProduct = Product(productRef, "Direct Product", 1.0, 1)
        whenever(mockRepository.getProductById(productRef)).thenReturn(expectedProduct)

        val result = viewModel.getProductById(productRef)
        advanceUntilIdle()

        verify(mockRepository).getProductById(productRef)
        assertEquals(expectedProduct, result)
    }
}
