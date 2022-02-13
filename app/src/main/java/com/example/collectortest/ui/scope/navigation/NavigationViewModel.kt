package com.example.collectortest.ui.scope.navigation

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val navigation: MapboxNavigation
) : ViewModel() {

    private var _state = MutableLiveData<NavigationViewState>()
    val state: LiveData<NavigationViewState>
        get() = _state

    private var _routeState = MutableLiveData<NavigationRoutesState>()
    val routeState: LiveData<NavigationRoutesState>
        get() = _routeState

    private val locationObserver = object : LocationObserver {

        override fun onNewRawLocation(rawLocation: Location) {
            _state.postValue(NavigationViewState.Data(rawLocation.latitude, rawLocation.longitude))
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
        }
    }

    private val callback = object : RouterCallback {
        override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
            _routeState.postValue(NavigationRoutesState.Error)
        }

        override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
            _routeState.postValue(NavigationRoutesState.Error)
        }

        override fun onRoutesReady(routes: List<DirectionsRoute>, routerOrigin: RouterOrigin) {
            _routeState.postValue(NavigationRoutesState.Data(routes))
        }
    }

    @SuppressLint("MissingPermission")
    fun startSession() {
        navigation.startTripSession(true)
    }

    fun stopSession() {
        navigation.stopTripSession()
    }

    fun setRoute() {
        navigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(
                    listOf(
                        (state.value as NavigationViewState.Data).getPoint(),
                        Point.fromLngLat( 24.014446,49.835039)
                    )
                ).build(),
            callback
        )
    }

    fun subscribeToLocation() {
        navigation.registerLocationObserver(locationObserver)
    }

    override fun onCleared() {
        super.onCleared()
        navigation.unregisterLocationObserver(locationObserver)
    }
}