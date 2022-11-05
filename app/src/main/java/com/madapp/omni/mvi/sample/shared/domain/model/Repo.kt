package com.madapp.omni.mvi.sample.shared.domain.model

data class Repo(
    val id: Long,
    val name: String,
    val description: String,
    val owner: String,
    val ownerAvatar: String,
    val fork: Boolean
)