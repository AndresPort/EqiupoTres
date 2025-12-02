package com.andresport.app_inventory.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Esta clase es para gestionar la sesión del usuario a través de SharedPreferences.
 * Se encarga de guardar, recuperar y borrar el token de autenticación.
 */
@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {

    // Se inicializan las SharedPreferences en modo privado.
    private val prefs: SharedPreferences = context.getSharedPreferences("Auth", Context.MODE_PRIVATE)

    // Objeto compañero para definir constantes.
    companion object {
        // Clave para guardar y recuperar el token del usuario.
        const val USER_TOKEN = "user_token"
    }

    /**
     * La función recibe un token y lo guarda en SharedPreferences.
     * Esto mantiene la sesión del usuario activa.
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        // Se guarda el token con la clave USER_TOKEN.
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    /**
     * Esta función es para obtener el token de autenticación guardado.
     * Retorna el token si existe, de lo contrario retorna null.
     */
    fun fetchAuthToken(): String? {
        // Aquí se obtiene el token guardado.
        return prefs.getString(USER_TOKEN, null)
    }

    /**
     * Esta función es para borrar el token de autenticación.
     * Se utiliza para cerrar la sesión del usuario.
     */
    fun clearAuthToken() {
        val editor = prefs.edit()
        // Se remueve el token de SharedPreferences.
        editor.remove(USER_TOKEN)
        editor.apply()
    }
}
