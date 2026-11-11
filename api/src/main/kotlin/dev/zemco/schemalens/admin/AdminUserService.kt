package dev.zemco.schemalens.admin

import dev.zemco.schemalens.auth.User

interface AdminUserService {
    fun getUsers(): List<User>
    fun createUser(dto: AdminCreateUserDto): User
    fun updateUser(id: Long, dto: AdminUpdateUserDto): User
    fun deleteUser(id: Long)
}
