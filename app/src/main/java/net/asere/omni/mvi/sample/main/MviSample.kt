package net.asere.omni.mvi.sample.main

import android.app.Application
import net.asere.omni.mvi.sample.main.di.list.listDataModule
import net.asere.omni.mvi.sample.main.di.list.listDomainModule
import net.asere.omni.mvi.sample.main.di.list.listPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MviSample : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeDependency()
    }

    private fun initializeDependency() {
        startKoin {
            androidContext(this@MviSample)
            modules(
                listPresentationModule,
                listDataModule,
                listDomainModule
            )
        }
    }
}