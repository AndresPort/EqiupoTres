package com.andresport.app_inventory.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.andresport.app_inventory.repository.IAuthenticationRepository
import com.andresport.app_inventory.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    // Esta regla ejecuta todas las tareas de LiveData de forma síncrona.
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // --- Mocks de las dependencias ---
    @Mock
    private lateinit var mockAuthRepository: IAuthenticationRepository

    @Mock
    private lateinit var mockSessionManager: SessionManager

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    // La instancia del ViewModel que vamos a probar.
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        // Inicializamos el ViewModel con los mocks antes de cada prueba.
        viewModel = LoginViewModel(mockAuthRepository, mockSessionManager, mockFirebaseAuth)
    }

    @Test
    fun `onAuthenticationSuccess - el estado de autenticación cambia a AUTHENTICATED`() {
        // Arrange: Preparamos el escenario
        val uid = "test_uid"
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.uid).thenReturn(uid)

        // Act: Ejecutamos el método que queremos probar
        viewModel.onAuthenticationSuccess()

        // Assert: Verificamos que el resultado es el esperado
        verify(mockSessionManager).saveAuthToken(uid) // Se guardó el token
        assertEquals(AuthenticationState.AUTHENTICATED, viewModel.authenticationState.value) // El estado es AUTHENTICATED
    }

    @Test
    fun `onAuthenticationFailureOrError - se establece el mensaje de toast`() {
        // Arrange
        val errorMessage = "Error de autenticación"

        // Act
        viewModel.onAuthenticationFailureOrError(errorMessage)

        // Assert
        assertEquals(errorMessage, viewModel.toastMessage.value) // El mensaje de error se publica
    }

    @Test
    fun `checkUserLoggedIn - con token de sesión, el estado es AUTHENTICATED`() {
        // Arrange
        `when`(mockSessionManager.fetchAuthToken()).thenReturn("some_token")

        // Act
        viewModel.checkUserLoggedIn()

        // Assert
        assertEquals(AuthenticationState.AUTHENTICATED, viewModel.authenticationState.value)
    }

    @Test
    fun `checkUserLoggedIn - con usuario de Firebase, el estado es AUTHENTICATED`() {
        // Arrange
        val uid = "test_uid"
        `when`(mockSessionManager.fetchAuthToken()).thenReturn(null) // No hay token de sesión
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.uid).thenReturn(uid)

        // Act
        viewModel.checkUserLoggedIn()

        // Assert
        verify(mockSessionManager).saveAuthToken(uid) // Se guarda el nuevo token
        assertEquals(AuthenticationState.AUTHENTICATED, viewModel.authenticationState.value)
    }

    @Test
    fun `checkUserLoggedIn - sin usuario, el estado es UNAUTHENTICATED`() {
        // Arrange
        `when`(mockSessionManager.fetchAuthToken()).thenReturn(null)
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)

        // Act
        viewModel.checkUserLoggedIn()

        // Assert
        assertEquals(AuthenticationState.UNAUTHENTICATED, viewModel.authenticationState.value)
    }

    @Test
    fun `logout - el estado cambia a UNAUTHENTICATED`() {
        // Act
        viewModel.logout()

        // Assert
        verify(mockFirebaseAuth).signOut() // Se cierra sesión en Firebase
        verify(mockSessionManager).clearAuthToken() // Se limpia el token
        assertEquals(AuthenticationState.UNAUTHENTICATED, viewModel.authenticationState.value)
    }
}
