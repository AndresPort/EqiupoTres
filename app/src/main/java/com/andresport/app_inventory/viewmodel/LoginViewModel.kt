package com.andresport.app_inventory.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.andresport.app_inventory.repository.IAuthenticationRepository
import com.andresport.app_inventory.util.Event
import com.andresport.app_inventory.utils.SessionManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class AuthenticationState {
    object AUTHENTICATED : AuthenticationState()
    object UNAUTHENTICATED : AuthenticationState()
    data class AUTH_ERROR(val message: String) : AuthenticationState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: IAuthenticationRepository,
    private val sessionManager: SessionManager,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _authenticationState = MutableLiveData<AuthenticationState>()
    val authenticationState: LiveData<AuthenticationState> = _authenticationState

    val navigationToLoginState: LiveData<Event<AuthenticationState>> = _authenticationState.map {
        Event(it)
    }

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    init {
        checkUserLoggedIn()
    }

    fun login(email: String, password: String): Task<AuthResult> {
        return authRepository.login(email, password)
    }

    fun register(email: String, password: String): Task<AuthResult> {
        return authRepository.register(email, password)
    }

    fun checkUserLoggedIn() {
        val token = sessionManager.fetchAuthToken()
        val firebaseUser = firebaseAuth.currentUser
        if (!token.isNullOrEmpty() || firebaseUser != null) {
            _authenticationState.value = AuthenticationState.AUTHENTICATED
            if (token.isNullOrEmpty() && firebaseUser != null) {
                sessionManager.saveAuthToken(firebaseUser.uid)
            }
        } else {
            _authenticationState.value = AuthenticationState.UNAUTHENTICATED
        }
    }

    fun onAuthenticationSuccess() {
        val token = firebaseAuth.currentUser?.uid ?: "user_logged_in"
        sessionManager.saveAuthToken(token)
        _authenticationState.value = AuthenticationState.AUTHENTICATED
    }

    fun onAuthenticationFailureOrError(message: String) {
        _toastMessage.value = message
    }

    fun logout() {
        firebaseAuth.signOut()
        sessionManager.clearAuthToken()
        _authenticationState.value = AuthenticationState.UNAUTHENTICATED
    }
}
