package dev.zemco.schemalens.admin

import dev.zemco.schemalens.ResourceNotFoundException
import dev.zemco.schemalens.auth.*
import dev.zemco.schemalens.profile.ProfilePictureService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(rollbackFor = [Exception::class])
class AdminUserServiceImpl(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val profilePictureService: ProfilePictureService,
) : AdminUserService {

    @Transactional(readOnly = true)
    override fun getUsers(): List<User> = userRepository.findAll().sortedBy { it.id }

    override fun createUser(dto: AdminCreateUserDto): User {
        checkUsernameNotTaken(dto.username)
        checkEmailNotTaken(dto.email)

        return userRepository.save(User(
            username = dto.username,
            email = dto.email,
            password = passwordEncoder.encode(dto.password)!!,
            active = dto.active,
            admin = dto.admin,
        ))
    }

    override fun updateUser(id: Long, dto: AdminUpdateUserDto): User {
        val user = getUser(id)

        if (user.id == userService.getCurrentUser().id && (!dto.active || !dto.admin)) {
            throw AdminUserSelfModificationException()
        }

        if (!user.email.equals(dto.email, ignoreCase = true)) {
            checkEmailNotTaken(dto.email)
        }

        user.apply {
            email = dto.email
            active = dto.active
            admin = dto.admin
            dto.password?.let { password = passwordEncoder.encode(it)!! }
        }

        return userRepository.save(user)
    }

    override fun deleteUser(id: Long) {
        val user = getUser(id)

        if (user.id == userService.getCurrentUser().id) {
            throw AdminUserSelfModificationException()
        }

        userService.deleteUser(user)
        profilePictureService.removeProfilePicture(user.username)
    }

    private fun getUser(id: Long): User =
        userRepository.findById(id).orElseThrow { ResourceNotFoundException.withId("User", id) }

    private fun checkUsernameNotTaken(username: String) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw UsernameIsTakenException()
        }
    }

    private fun checkEmailNotTaken(email: String) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw EmailIsTakenException()
        }
    }
}
