package com.example.collectortest.di

import android.content.Context
import com.example.collectortest.R
import com.example.collectortest.data.storage.Storage
import com.example.collectortest.database.Database
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FirebaseModule {


    @Singleton
    @Provides
    fun provideStorageRepository(): Storage = Storage()

    @Singleton
    @Provides
    fun provideDatabase(): Database = Database()

    @Singleton
    @Provides
    fun provideMapboxNavigation(@ApplicationContext context: Context): MapboxNavigation {
        val navigationOptions = NavigationOptions.Builder(context)
            .accessToken(context.getString(R.string.mapbox_access_token))
            .build()

        return MapboxNavigationProvider.create(navigationOptions)
    }

}