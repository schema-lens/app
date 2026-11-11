package dev.zemco.schemalens.auth

import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtTokenFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val jwt = header.substring(7)
            val authentication = jwtService.createAuthenticationFrom(jwt)
            SecurityContextHolder.getContext().authentication = authentication
        } catch (ex: AuthenticationException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid user")
            return
        } catch (ex: JwtException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT")
            return
        }

        filterChain.doFilter(request, response)
    }

}
