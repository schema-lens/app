package dev.zemco.schemalens.auth

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userService: UserService,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        userService.getUserByUsername(username)?.takeIf { it.active }?.let { UserWrapperDetails(it) }
            ?: throw UsernameNotFoundException("User with username '$username' not found")

}
