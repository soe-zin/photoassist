package com.example.photographyassistant.domain

import java.time.Instant

/**
 * Domain model for project management.
 * 
 * This is used for organizing measurements into projects as specified in Task 15.
 * Projects provide a way to group related measurements for better organization and analysis.
 */
import kotlin.jvm.JvmInline

@JvmInline
value class ProjectId(
    private val id: String
) {
    override fun toString(): String = id
    
    companion object {
        fun generate(): ProjectId {
            return ProjectId(java.util.UUID.randomUUID().toString())
        }
    }
}

data class Project(
    val id: ProjectId,
    val name: String,
    val description: String = "",
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val isActive: Boolean = true
) {
    init {
        require(name.isNotBlank()) { "Project name cannot be empty" }
        require(name.length <= 100) { "Project name too long, maximum 100 characters" }
    }
    
    fun updateName(newName: String): Project {
        require(newName.isNotBlank()) { "Project name cannot be empty" }
        require(newName.length <= 100) { "Project name too long, maximum 100 characters" }
        return copy(name = newName, updatedAt = Instant.now())
    }
    
    fun archive() = copy(isActive = false, updatedAt = Instant.now())
    
    override fun toString(): String {
        return "Project(id=$id, name='$name', active=$isActive)"
    }
}