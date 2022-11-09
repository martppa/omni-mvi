package net.asere.omni.mvi.sample.list.data.network.retrofit

import net.asere.omni.mvi.sample.list.data.network.RepoJson
import net.asere.omni.mvi.sample.list.data.network.Routes
import retrofit2.http.GET
import retrofit2.http.Query

interface RepoService {

    @GET(Routes.Repositories)
    suspend fun getRepositories(@Query("page") page: Int): List<RepoJson>
}