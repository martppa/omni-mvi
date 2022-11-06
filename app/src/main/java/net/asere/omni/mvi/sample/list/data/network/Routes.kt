package net.asere.omni.mvi.sample.list.data.network

import net.asere.omni.mvi.sample.BuildConfig

class Routes {
    companion object {
        const val Host = BuildConfig.API_URL
        const val Repositories = "/orgs/microsoft/repos"
    }
}