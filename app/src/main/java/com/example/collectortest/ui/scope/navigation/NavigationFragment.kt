package com.example.collectortest.ui.scope.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.collectortest.databinding.FragmentNavigationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavigationFragment : Fragment() {

    private var _binding: FragmentNavigationBinding? = null
    private val binding: FragmentNavigationBinding
        get() = checkNotNull(_binding)

    private val viewModel: NavigationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return FragmentNavigationBinding.inflate(inflater).apply {
            _binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)= with(binding) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.observe(viewLifecycleOwner) { processState(it) }
        viewModel.routeState.observe(viewLifecycleOwner) { processRouteState(it) }

        viewModel.subscribeToLocation()
        viewModel.startSession()

        nextBtn.setOnClickListener {
            findNavController().navigate(NavigationFragmentDirections.actionNavigationFragmentToPhotosFragment())
        }
        routeBtn.setOnClickListener { viewModel.setRoute() }
    }


    private fun processState(state: NavigationViewState) = with(binding) {
        when (state) {
            is NavigationViewState.Data -> {
                locationTv.text = "Your location: \nLatitude[${state.lat}]\n" +
                        "Longitude[${state.lng}]"
            }
            is NavigationViewState.Error -> {

            }
        }
    }

    private fun processRouteState(state: NavigationRoutesState) = with(binding) {
        when (state) {
            is NavigationRoutesState.Data -> routesTv.text = "Defined route: ${state.directions}"
            is NavigationRoutesState.Error -> routesTv.text = "Defined route: unknown}"
        }
    }

}