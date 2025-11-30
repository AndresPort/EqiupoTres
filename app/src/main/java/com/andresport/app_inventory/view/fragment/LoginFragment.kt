package com.andresport.app_inventory.view.fragment

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.andresport.app_inventory.R
// ¡ESTA ES LA IMPORTACIÓN QUE FALTABA!
import com.andresport.app_inventory.view.fragment.LoginFragmentDirections
import com.andresport.app_inventory.viewmodel.LoginViewModel
import com.andresport.app_inventory.widget.InventoryWidgetProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment() {

    // CAMBIO 2: Usar el ViewModel de la actividad
    private val viewModel: LoginViewModel by activityViewModels()

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CAMBIO 3: Eliminar la lógica duplicada de SessionManager
        // El ViewModel y el fragmento de inicio se encargarán de la navegación automática.

        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout)
        loginButton = view.findViewById(R.id.loginButton)
        registerButton = view.findViewById(R.id.registerButton)

        setupUI()

        loginButton.setOnClickListener { loginUser() }
        registerButton.setOnClickListener { registerUser() }
    }

    private fun setupUI() {
        // ... (El resto de tu código no necesita cambios)
        loginButton.isEnabled = false
        registerButton.isEnabled = false

        emailEditText.addTextChangedListener { updateButtonsState() }
        passwordEditText.addTextChangedListener {
            updateButtonsState()
            validatePassword(it.toString())
        }
    }

    private fun validatePassword(input: String) {
        when {
            input.any { !it.isDigit() } -> {
                passwordInputLayout.error = "Solo números"
            }
            input.length < 6 -> {
                passwordInputLayout.error = "Mínimo 6 dígitos"
                passwordInputLayout.setBoxStrokeColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
                )
            }
            else -> {
                passwordInputLayout.error = null
                passwordInputLayout.setBoxStrokeColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                )
            }
        }
    }

    private fun updateButtonsState() {
        val email = emailEditText.text?.toString().orEmpty()
        val password = passwordEditText.text?.toString().orEmpty()

        val enabled = email.isNotEmpty() &&
                password.length >= 6 &&
                password.all { it.isDigit() }

        loginButton.isEnabled = enabled
        registerButton.isEnabled = enabled

        val color = if (enabled) Color.WHITE else Color.parseColor("#B0B0B0")
        val style = if (enabled) Typeface.BOLD else Typeface.NORMAL

        loginButton.setTextColor(color)
        loginButton.setTypeface(null, style)

        registerButton.setTextColor(color)
        registerButton.setTypeface(null, style)
    }


    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        if (email.isEmpty() || password.length < 6) {
            Toast.makeText(requireContext(), "Login incorrecto", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.login(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Login exitoso", Toast.LENGTH_SHORT).show()
                handleLoginSuccess()
            } else {
                val errorMessage = when (task.exception?.message) {
                    "The email address is badly formatted." -> "Formato de correo inválido"
                    "There is no user record corresponding to this identifier." -> "Usuario no registrado"
                    "The password is invalid or the user does not have a password." -> "Contraseña incorrecta"
                    else -> "Login incorrecto"
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        if (email.isEmpty() || password.length < 6) {
            Toast.makeText(requireContext(), "Error en el registro", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.register(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show()
                handleLoginSuccess()
            } else {
                Toast.makeText(requireContext(), "Error en el registro", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLoginSuccess() {
        // CAMBIO 4: Esta llamada ahora actualizará el estado del ViewModel compartido
        viewModel.onAuthenticationSuccess()

        updateAllWidgets()
        navigateToHome()
    }

    private fun navigateToHome() {
        // No es necesario observar, ya que el ViewModel compartido notificará al InventarioFragment
        // para que navegue si es necesario, pero tras el login, la navegación es explícita.
        // También, el estado AUTHENTICATED evitará el bucle de "logout".
        val action = LoginFragmentDirections.actionLoginFragmentToInventarioFragment()
        findNavController().navigate(action)
    }

    private fun updateAllWidgets() {
        // ... (tu lógica del widget se mantiene igual)
        val context = context ?: return
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, InventoryWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            val intent = Intent(context, InventoryWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    }
}
