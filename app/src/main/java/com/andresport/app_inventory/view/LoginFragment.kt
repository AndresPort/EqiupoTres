package com.andresport.app_inventory.view

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.andresport.app_inventory.R
import com.andresport.app_inventory.viewmodel.LoginViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

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

        // ViewModel
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // UI Components
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout)
        loginButton = view.findViewById(R.id.loginButton)
        registerButton = view.findViewById(R.id.registerButton)

        setupUI()

        loginButton.setOnClickListener { loginUser() }
        registerButton.setOnClickListener { registerUser() }
    }

    // -------------------------
    //   CONFIGURACIÓN GENERAL
    // -------------------------

    private fun setupUI() {
        loginButton.isEnabled = false
        registerButton.isEnabled = false

        emailEditText.addTextChangedListener { updateButtonsState() }
        passwordEditText.addTextChangedListener {
            updateButtonsState()
            validatePassword(it.toString())
        }
    }

    // -------------------------
    //   VALIDACIONES
    // -------------------------

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
        val enabled = emailEditText.text?.isNotEmpty() == true &&
                (passwordEditText.text?.length ?: 0) >= 6 &&
                passwordInputLayout.error == null

        loginButton.isEnabled = enabled
        registerButton.isEnabled = enabled

        val color = if (enabled) Color.WHITE else Color.parseColor("#B0B0B0")
        val style = if (enabled) Typeface.BOLD else Typeface.NORMAL

        loginButton.setTextColor(color)
        loginButton.setTypeface(null, style)

        registerButton.setTextColor(color)
        registerButton.setTypeface(null, style)
    }

    // -------------------------
    //   LOGIN
    // -------------------------

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
                navigateToHome()
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

    // -------------------------
    //   REGISTRO
    // -------------------------

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
                navigateToHome()
            } else {
                Toast.makeText(requireContext(), "Error en el registro", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToHome() {
        val action = LoginFragmentDirections.actionLoginFragmentToInventarioFragment()
        findNavController().navigate(action)
    }
}
