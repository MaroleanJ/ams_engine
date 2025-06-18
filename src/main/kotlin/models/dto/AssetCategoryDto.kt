package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssetCategoryDto(
    val id: Int? = null,
    val name: String,
    val description: String? = null
)

@Serializable
data class CreateAssetCategoryRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class UpdateAssetCategoryRequest(
    val name: String,
    val description: String? = null
)