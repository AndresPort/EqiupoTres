package com.andresport.app_inventory.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // ¡CAMBIO IMPORTANTE! Usamos activityViewModels para compartir el ViewModel
import androidx.navigation.fragment.findNavController
import com.andresport.app_inventory.R
import com.andresport.app_inventory.databinding.FragmentLoginWindowBinding
import com.andresport.app_inventory.viewmodel.AuthenticationState // Importamos la nueva Sealed Class
import com.andresport.app_inventory.viewmodel.LoginViewModel
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginWindowBinding
    private val loginViewModel: LoginViewModel by activityViewModels()
    private lateinit var executor: Executor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginWindowBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        executor = Executors.newSingleThreadExecutor()

        controladores()
        observadorViewModel()

        loginViewModel.checkUserLoggedIn()
    }

    private fun controladores() {
        binding.lottieFingerprintScan.setOnClickListener {
            checkBiometricSupportAndAuthenticate()
        }
    }

    private fun observadorViewModel() {

        loginViewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthenticationState.AUTHENTICATED -> {

                    findNavController().navigate(R.id.action_LoginFragment_to_InventarioFragment)
                }
                is AuthenticationState.UNAUTHENTICATED -> {

                }

                else -> {

                }
            }
        }

        // Mantenemos el observador para mostrar mensajes de error o informativos.
        loginViewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Verifica si el dispositivo soporta autenticación biométrica y, si es así,
     * muestra el diálogo de autenticación.
     */
    private fun checkBiometricSupportAndAuthenticate() {
        val supportStatus = BiometricUtils.checkBiometricSupport(requireContext())

        if (supportStatus == BiometricManager.BIOMETRIC_SUCCESS) {
            showBiometricPrompt()
        } else {
            val message = BiometricUtils.getMessageForBiometricStatus(requireContext(), supportStatus)
            loginViewModel.onAuthenticationFailureOrError(message)
        }
    }

    /**
     * Muestra el diálogo de autenticación biométrica, configurando sus textos
     * y el comportamiento del callback.
     */
    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación con Biometría")
            .setSubtitle("Ingrese su huella digital")
            .setNegativeButtonText("Cancelar") // Corregido para tener un solo botón negativo
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            BiometricAuthCallback(loginViewModel)
        )

        biometricPrompt.authenticate(promptInfo)
    }
}

