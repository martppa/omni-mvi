package net.asere.omni.mvi.sample.shared.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import net.asere.omni.mvi.sample.shared.domain.model.Repo

@Parcelize
data class RepoModel(
    val id: Long,
    val name: String,
    val description: String,
    val owner: String,
    val ownerAvatar: String,
    val fork: Boolean
) : Parcelable

fun Repo.asPresentation() = RepoModel(
    id = id,
    name = name,
    description = description,
    owner = owner,
    ownerAvatar = ownerAvatar,
    fork = fork,
)