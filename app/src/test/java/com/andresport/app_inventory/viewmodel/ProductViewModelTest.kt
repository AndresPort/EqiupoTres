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
<<<<<<< Updated upstream
=======
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
    @Test
    fun `loadProducts - cuando el repositorio tiene datos, el LiveData se actualiza`() = runTest {
        // ARRANGE
        val fakeProducts = listOf(
            Product(productRef = "1", productName = "Test Product 1", unitPrice = 10.0, stock = 5), // Total: 50.0
            Product(productRef = "2", productName = "Test Product 2", unitPrice = 20.0, stock = 2)  // Total: 40.0
        )
        `when`(mockRepository.getAllProducts()).thenReturn(fakeProducts)
=======
    // -------------------- loadProducts --------------------
    @Test
    fun `loadProducts con datos actualiza LiveData`() = runTest {
        val fakeProducts = listOf(Product("1", "P1", 10.0, 5))

        whenever(mockRepository.getAllProducts()).thenReturn(fakeProducts)
>>>>>>> Stashed changes

        // ACT
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        // ASSERT
        assertEquals(fakeProducts, viewModel.products.value)
        assertEquals(50.0, viewModel.totalSum.value!!, 0.0)
    }

    @Test
<<<<<<< Updated upstream
    fun `loadProducts - cuando el repositorio está vacío, el LiveData está vacío`() = runTest {
        // ARRANGE
        val emptyList = emptyList<Product>()
        `when`(mockRepository.getAllProducts()).thenReturn(emptyList)
=======
    fun `loadProducts vacío produce lista vacía`() = runTest {
        whenever(mockRepository.getAllProducts()).thenReturn(emptyList())
>>>>>>> Stashed changes

        // ACT
        viewModel.loadProducts()
        testDispatcher.scheduler.advanceUntilIdle()

<<<<<<< Updated upstream
        // ASSERT
        assertTrue(viewModel.products.value?.isEmpty() ?: false)
        assertEquals(0.0, viewModel.totalSum.value!!, 0.0)
    }
=======
        assertTrue(viewModel.products.value!!.isEmpty())
        assertEquals(0.0, viewModel.totalSum.value!!, 0.0)
    }

    // -------------------- loadProductByRef --------------------
    @Test
    fun `loadProductByRef actualiza selectedProduct`() = runTest {
        val productRef = "p10"
        val fakeProduct = Product(productRef, "Test", 10.0, 1)

        whenever(mockRepository.getProductById(productRef)).thenReturn(fakeProduct)

        viewModel.loadProductByRef(productRef)
        advanceUntilIdle()

        assertEquals(fakeProduct, viewModel.selectedProduct.value)
    }

    // -------------------- INSERT (Nuevo formato con callback) --------------------
    @Test
    fun `insertProduct llama insert y recarga lista correctamente`() = runTest {
        val p = Product("N1","Nuevo",20.0,4)

        whenever(mockRepository.insertProduct(p)).thenReturn(true)
        whenever(mockRepository.getAllProducts()).thenReturn(listOf(p))

        var callbackResult: Pair<Boolean, String?>? = null

        viewModel.insertProduct(p) { success, msg ->
            callbackResult = success to msg
        }
        advanceUntilIdle()

        verify(mockRepository).insertProduct(p)
        verify(mockRepository).getAllProducts()

        assertEquals(listOf(p), viewModel.products.value)
        assertNotNull(callbackResult)
        assertTrue(callbackResult!!.first)
    }

    @Test
    fun `insertProduct falla y devuelve mensaje de error`() = runTest {
        val p = Product("N1","Nuevo",20.0,4)

        whenever(mockRepository.insertProduct(p)).thenReturn(false)

        var callbackResult: Pair<Boolean, String?>? = null

        viewModel.insertProduct(p) { success, msg ->
            callbackResult = success to msg
        }
        advanceUntilIdle()

        verify(mockRepository).insertProduct(p)

        // No debe recargar lista porque insert falló
        assertNotNull(callbackResult)
        assertFalse(callbackResult!!.first)
        assertEquals("Error: La referencia del producto ya existe.", callbackResult!!.second)
    }

    // -------------------- UPDATE --------------------
    @Test
    fun `updateProduct actualiza y recarga lista`() = runTest {
        val updatedProduct = Product("1","Modificado",15.0,5)

        whenever(mockRepository.getAllProducts()).thenReturn(listOf(updatedProduct))

        viewModel.updateProduct(updatedProduct)
        advanceUntilIdle()

        verify(mockRepository).updateProduct(updatedProduct)
        verify(mockRepository).getAllProducts()
        assertEquals(listOf(updatedProduct), viewModel.products.value)
    }

    // -------------------- DELETE --------------------
    @Test
    fun `deleteProduct borra y recarga lista`() = runTest {
        val deleted = Product("1","Eliminar",10.0,1)

        whenever(mockRepository.getAllProducts()).thenReturn(emptyList())

        viewModel.deleteProduct(deleted)
        advanceUntilIdle()

        verify(mockRepository).deleteProduct(deleted)
        verify(mockRepository).getAllProducts()
        assertTrue(viewModel.products.value!!.isEmpty())
    }

    // -------------------- getProductById directo --------------------
    @Test
    fun `getProductById consulta repo y devuelve resultado`() = runTest {
        val productRef = "direct"
        val expected = Product(productRef,"Directo",3.0,2)

        whenever(mockRepository.getProductById(productRef)).thenReturn(expected)

        val result = viewModel.getProductById(productRef)
        advanceUntilIdle()

        verify(mockRepository).getProductById(productRef)
        assertEquals(expected, result)
    }
>>>>>>> Stashed changes
}
