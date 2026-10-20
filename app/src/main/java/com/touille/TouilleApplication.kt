package com.touille

import android.app.Application
import com.touille.di.AppContainer

class TouilleApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
