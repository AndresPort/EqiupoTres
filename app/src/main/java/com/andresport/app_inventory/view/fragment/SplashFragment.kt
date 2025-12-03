package com.andresport.app_inventory.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.andresport.app_inventory.R
import com.andresport.app_inventory.viewmodel.AuthenticationState
import com.andresport.app_inventory.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Un layout simple, podría ser solo un fondo o un ProgressBar
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            // Usamos un when para que sea más legible
            when (state) {
                is AuthenticationState.AUTHENTICATED -> {
                    // Si está autenticado, navega a Inventario y limpia la pila de navegación
                    findNavController().navigate(R.id.action_splashFragment_to_inventarioFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.splashFragment, true)
                            .build()
                    )
                }
                is AuthenticationState.UNAUTHENTICATED -> {
                    // Si NO está autenticado, navega a Login y limpia la pila
                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.splashFragment, true)
                            .build()
                    )
                }
                else -> {
                    // Manejar otros estados si es necesario, por ahora no hacemos nada
                }
            }
        }
    }
}
