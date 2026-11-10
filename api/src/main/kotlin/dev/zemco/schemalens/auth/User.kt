package dev.zemco.schemalens.auth

import dev.zemco.schemalens.validation.UsernameConstraint
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Length

@Entity(name = "registered_user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @NotBlank
    @Length(min = 4, max = 32)
    @UsernameConstraint
    @Column(nullable = false, unique = true, length = 32)
    var username: String,

    @Column(name = "hashed_password", nullable = false, length = 60)
    var password: String,

    @Email
    @NotNull
    @Column(nullable = false, length = 128)
    var email: String,

    @Column(nullable = false)
    var active: Boolean = false,

    @Column(nullable = false)
    var admin: Boolean = false,
)
