package dev.zemco.schemalens.admin

import dev.zemco.schemalens.auth.EmailIsTakenException
import dev.zemco.schemalens.auth.User
import dev.zemco.schemalens.auth.UsernameIsTakenException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/user")
class AdminUserController(private val adminUserService: AdminUserService) {

    @GetMapping
    fun getUsers(): List<AdminUserDto> = adminUserService.getUsers().map { it.mapToDto() }

    @PostMapping
    fun createUser(@RequestBody @Valid dto: AdminCreateUserDto): AdminUserDto =
        adminUserService.createUser(dto).mapToDto()

    @PutMapping("/{userId}")
    fun updateUser(@PathVariable userId: Long, @RequestBody @Valid dto: AdminUpdateUserDto): AdminUserDto =
        adminUserService.updateUser(userId, dto).mapToDto()

    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Any> {
        adminUserService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(
        UsernameIsTakenException::class,
        EmailIsTakenException::class,
        AdminUserSelfModificationException::class,
    )
    fun handleConflictException(ex: RuntimeException) =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ex.message)

    private fun User.mapToDto() = AdminUserDto(id!!, username, email, active, admin)
}
