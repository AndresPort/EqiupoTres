package com.andresport.app_inventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map // <--- ¡AÑADE ESTA LÍNEA!
import com.andresport.app_inventory.repository.AuthenticationRepository
import com.andresport.app_inventory.util.Event
import com.andresport.app_inventory.utils.SessionManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

// ... el resto de tu archivo se mantiene igual

sealed class AuthenticationState {
    object AUTHENTICATED : AuthenticationState()
    object UNAUTHENTICATED : AuthenticationState()
    data class AUTH_ERROR(val message: String) : AuthenticationState()
}
  
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthenticationRepository()
    private val sessionManager = SessionManager(application)

    private val _authenticationState = MutableLiveData<AuthenticationState>()
    val authenticationState: LiveData<AuthenticationState> = _authenticationState

    // Ahora el compilador debería reconocer '.map' sin problemas
    val navigationToLoginState: LiveData<Event<AuthenticationState>> = _authenticationState.map {
        Event(it)
    }

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage


    fun login(email: String, password: String): Task<AuthResult> {
        return authRepository.login(email, password)
    }

    fun register(email: String, password: String): Task<AuthResult> {
        return authRepository.register(email, password)
    }

    fun checkUserLoggedIn() {
        val token = sessionManager.fetchAuthToken()
        if (!token.isNullOrEmpty()) {
            _authenticationState.value = AuthenticationState.AUTHENTICATED
        } else {
            _authenticationState.value = AuthenticationState.UNAUTHENTICATED
        }
    }

    fun onAuthenticationSuccess() {
        sessionManager.saveAuthToken("user_logged_in")
        _authenticationState.value = AuthenticationState.AUTHENTICATED
    }

    fun onAuthenticationFailureOrError(message: String) {
        _toastMessage.value = message
    }

    fun logout() {
        sessionManager.clearAuthToken()
        _authenticationState.value = AuthenticationState.UNAUTHENTICATED
    }
}
