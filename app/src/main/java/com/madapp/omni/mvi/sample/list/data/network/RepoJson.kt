package com.madapp.omni.mvi.sample.list.data.network

import com.madapp.omni.mvi.sample.shared.domain.extension.empty
import com.madapp.omni.mvi.sample.shared.domain.model.Repo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RepoJson(
    val id: Long,
    val name: String,
    val description: String?,
    val owner: OwnerJson,
    val fork: Boolean
)

@JsonClass(generateAdapter = true)
data class OwnerJson(
    val login: String,
    @Json(name = "avatar_url")
    val avatarUrl: String
)

fun RepoJson.toDomain() = Repo(id, name, description ?: String.empty(), owner.login, owner.avatarUrl, fork)