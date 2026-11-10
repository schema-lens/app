package dev.zemco.schemalens.auth

import dev.zemco.schemalens.validation.UsernameConstraint
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Length

data class UserDto(
    val username: String,
    val email: String,
)

data class UserLoginDto(
    @field:NotNull
    @field:Length(min = 4, max = 32)
    @field:UsernameConstraint
    val username: String,

    @field:Length(min = 8)
    val password: String,
)

data class UserRegistrationDto(
    @field:NotNull
    @field:Length(min = 4, max = 32)
    @field:UsernameConstraint
    val username: String,

    @field:Length(min = 8)
    val password: String,

    @field:Email
    @field:NotNull
    @field:Length(max = 128)
    val email: String,
)

enum class UserRegistrationFailure {
    USERNAME_TAKEN,
    EMAIL_TAKEN,
}

data class UpdateUserDto(
    @field:Email
    @field:NotNull
    @field:Length(max = 128)
    val email: String,
)

data class ChangePasswordDto(
    val oldPassword: String,
    val newPassword: String,
)
