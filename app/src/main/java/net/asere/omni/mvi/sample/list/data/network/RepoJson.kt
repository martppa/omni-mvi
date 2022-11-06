package net.asere.omni.mvi.sample.list.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.asere.omni.mvi.sample.shared.domain.extension.empty
import net.asere.omni.mvi.sample.shared.domain.model.Repo

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