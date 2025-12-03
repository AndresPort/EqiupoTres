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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.andresport.app_inventory.R
import com.andresport.app_inventory.databinding.FragmentLoginBinding // Importar la clase de ViewBinding
import com.andresport.app_inventory.viewmodel.LoginViewModel
import com.andresport.app_inventory.widget.InventoryWidgetProvider
import dagger.hilt.android.AndroidEntryPoint

// 1. Anotar la clase para que Hilt pueda inyectar dependencias
@AndroidEntryPoint
class LoginFragment : Fragment() {

    // 2. Inyectar el ViewModel usando el delegado de Hilt
    private val viewModel: LoginViewModel by viewModels()

    // 3. Implementar View Binding para acceder a las vistas de forma segura
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar el layout usando View Binding
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // El chequeo de sesión existente se debe mover al ViewModel,
        // pero por ahora lo mantenemos aquí para no romper la lógica actual.
        // Lo ideal es que el ViewModel exponga un LiveData con el estado de la sesión.
        // val savedToken = sessionManager.fetchAuthToken() ...

        setupUI()

        binding.loginButton.setOnClickListener { loginUser() }
        binding.registerButton.setOnClickListener { registerUser() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiar la referencia al binding para evitar fugas de memoria
        _binding = null
    }

    private fun setupUI() {
        binding.loginButton.isEnabled = false
        binding.registerButton.isEnabled = false

        binding.emailEditText.addTextChangedListener { updateButtonsState() }
        binding.passwordEditText.addTextChangedListener {
            updateButtonsState()
            validatePassword(it.toString())
        }
    }

    private fun validatePassword(input: String) {
        when {
            input.any { !it.isDigit() } -> {
                binding.passwordInputLayout.error = "Solo números"
            }
            input.length < 6 -> {
                binding.passwordInputLayout.error = "Mínimo 6 dígitos"
                binding.passwordInputLayout.setBoxStrokeColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
                )
            }
            else -> {
                binding.passwordInputLayout.error = null
                binding.passwordInputLayout.setBoxStrokeColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                )
            }
        }
    }

    private fun updateButtonsState() {
        val email = binding.emailEditText.text?.toString().orEmpty()
        val password = binding.passwordEditText.text?.toString().orEmpty()

        val enabled = email.isNotEmpty() &&
                password.length >= 6 &&
                password.all { it.isDigit() }

        binding.loginButton.isEnabled = enabled
        binding.registerButton.isEnabled = enabled

        val color = if (enabled) Color.WHITE else Color.parseColor("#B0B0B0")
        val style = if (enabled) Typeface.BOLD else Typeface.NORMAL

        binding.loginButton.setTextColor(color)
        binding.loginButton.setTypeface(null, style)

        binding.registerButton.setTextColor(color)
        binding.registerButton.setTypeface(null, style)
    }

    private fun loginUser() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

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
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

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
        viewModel.onAuthenticationSuccess()
        handlePostLoginNavigation()
    }

    private fun navigateToHome() {
        // La clase de direcciones generada se mantiene igual
        val action = LoginFragmentDirections.actionLoginFragmentToInventarioFragment()
        findNavController().navigate(action)
    }

    private fun handlePostLoginNavigation() {
        val appWidgetId = activity?.intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val loginOrigin = activity?.intent?.getStringExtra(InventoryWidgetProvider.EXTRA_LOGIN_ORIGIN)

        updateAllWidgets()

        if (loginOrigin == InventoryWidgetProvider.ORIGIN_WIDGET_VISIBILITY) {
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                InventoryWidgetProvider.setBalanceVisibility(requireContext(), appWidgetId, true)
                InventoryWidgetProvider.requestWidgetUpdate(requireContext(), appWidgetId)
            }
            activity?.finish()
            return
        }

        navigateToHome()
    }

    private fun updateAllWidgets() {
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
