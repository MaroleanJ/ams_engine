package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.AssetCategoryDto
import com.techbros.models.dto.CreateAssetCategoryRequest
import com.techbros.models.dto.UpdateAssetCategoryRequest
import com.techbros.repositories.AssetCategoryRepository
import io.ktor.http.*

class AssetCategoryService(private val assetCategoryRepository: AssetCategoryRepository) {

    suspend fun createAssetCategory(request: CreateAssetCategoryRequest): AssetCategoryDto {
        validateAssetCategoryRequest(request.name)

        // Check if name already exists
        if (assetCategoryRepository.existsByName(request.name)) {
            throw ApiException("Asset category with name '${request.name}' already exists", HttpStatusCode.Conflict)
        }

        val categoryId = assetCategoryRepository.create(request)
        return getAssetCategoryById(categoryId)
    }

    suspend fun getAssetCategoryById(id: Int): AssetCategoryDto {
        return assetCategoryRepository.findById(id)
            ?: throw ApiException("Asset category not found", HttpStatusCode.NotFound)
    }

    suspend fun getAssetCategoryByName(name: String): AssetCategoryDto {
        return assetCategoryRepository.findByName(name)
            ?: throw ApiException("Asset category not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllAssetCategories(): List<AssetCategoryDto> {
        return assetCategoryRepository.findAll()
    }

    suspend fun updateAssetCategory(id: Int, request: UpdateAssetCategoryRequest): AssetCategoryDto {
        validateAssetCategoryRequest(request.name)

        // Check if category exists
        if (!assetCategoryRepository.exists(id)) {
            throw ApiException("Asset category not found", HttpStatusCode.NotFound)
        }

        // Check if name already exists (excluding current category)
        if (assetCategoryRepository.existsByName(request.name, excludeId = id)) {
            throw ApiException("Asset category with name '${request.name}' already exists", HttpStatusCode.Conflict)
        }

        val updated = assetCategoryRepository.update(id, request)
        if (!updated) {
            throw ApiException("Asset category not found", HttpStatusCode.NotFound)
        }

        return getAssetCategoryById(id)
    }

    suspend fun deleteAssetCategory(id: Int) {
        // Note: You might want to add validation here to check if the category is being used by any assets
        // before allowing deletion. This would require checking related tables.

        val deleted = assetCategoryRepository.delete(id)
        if (!deleted) {
            throw ApiException("Asset category not found", HttpStatusCode.NotFound)
        }
    }

    suspend fun searchAssetCategories(searchTerm: String): List<AssetCategoryDto> {
        if (searchTerm.isBlank()) {
            throw ApiException("Search term cannot be empty", HttpStatusCode.BadRequest)
        }

        return assetCategoryRepository.searchByName(searchTerm)
    }

    suspend fun getAssetCategoryCount(): Long {
        return assetCategoryRepository.getCount()
    }

    suspend fun validateCategoryExists(id: Int) {
        if (!assetCategoryRepository.exists(id)) {
            throw ApiException("Asset category not found", HttpStatusCode.NotFound)
        }
    }

    private fun validateAssetCategoryRequest(name: String) {
        when {
            name.isBlank() -> throw ApiException("Asset category name cannot be empty", HttpStatusCode.BadRequest)
            name.length > 100 -> throw ApiException("Asset category name is too long (max 100 characters)", HttpStatusCode.BadRequest)
            name.trim() != name -> throw ApiException("Asset category name cannot have leading or trailing whitespace", HttpStatusCode.BadRequest)
        }
    }
}