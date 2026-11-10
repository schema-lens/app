package dev.zemco.schemalens.admin

import dev.zemco.schemalens.validation.UsernameConstraint
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

data class AdminUserDto(
    val id: Long,
    val username: String,
    val email: String,
    val active: Boolean,
    val admin: Boolean,
)

data class AdminCreateUserDto(
    @field:NotBlank
    @field:Length(min = 4, max = 32)
    @field:UsernameConstraint
    val username: String,

    @field:NotBlank
    @field:Email
    @field:Length(max = 128)
    val email: String,

    @field:Length(min = 8)
    val password: String,

    val active: Boolean = true,
    val admin: Boolean = false,
)

data class AdminUpdateUserDto(
    @field:NotBlank
    @field:Email
    @field:Length(max = 128)
    val email: String,

    val active: Boolean,
    val admin: Boolean,

    @field:Length(min = 8)
    val password: String? = null,
)
