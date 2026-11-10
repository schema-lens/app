package dev.zemco.schemalens.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserWrapperDetails(val user: User) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        if (user.admin) listOf(RoleGrantedAuthority(Role.ADMIN)) else listOf()

    override fun isEnabled(): Boolean = user.active
    override fun getUsername(): String = user.username
    override fun getPassword(): String = user.password

}
