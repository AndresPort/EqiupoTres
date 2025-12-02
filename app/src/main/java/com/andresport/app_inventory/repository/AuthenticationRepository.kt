package com.andresport.app_inventory.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import javax.inject.Inject
import javax.inject.Singleton

// 1. SE DEFINE LA INTERFAZ
interface IAuthenticationRepository {
    fun login(email: String, password: String): Task<AuthResult>
    fun register(email: String, password: String): Task<AuthResult>
}


@Singleton
class AuthenticationRepository @Inject constructor() : IAuthenticationRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun login(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    override fun register(email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }
}
