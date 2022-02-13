package com.example.collectortest.ui.scope.navigation

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point

sealed class NavigationViewState {
    data class Data(val lat: Double, val lng: Double) : NavigationViewState() {
        fun getPoint(): Point = Point.fromLngLat(lng, lat)
    }

    object Error : NavigationViewState()
}

sealed class NavigationRoutesState {
    data class Data(val directions: List<DirectionsRoute>) : NavigationRoutesState()

    object Error : NavigationRoutesState()
}