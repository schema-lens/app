package dev.zemco.schemalens.auth

import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class RefreshTokenServiceImpl(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val refreshTokenGenerator: RefreshTokenGenerator,
    private val jwtConfiguration: JwtConfiguration,
) : RefreshTokenService {

    override fun createRefreshTokenFor(user: User): String =
        refreshTokenGenerator.generateRefreshToken().also {
            val createdAt = Instant.now()
            refreshTokenRepository.save(RefreshTokenEntry(
                token = it,
                userId = user.id!!,
                user = user,
                createdAt = createdAt,
                expiresAt = createExpirationDate(createdAt)
            ))
        }

    override fun useRefreshToken(token: String): User? {
        val entry = refreshTokenRepository.findByToken(token) ?: return null

        if (!entry.user.active || Instant.now() > entry.expiresAt) {
            return null
        }

        refreshTokenRepository.delete(entry)
        return entry.user
    }

    private fun createExpirationDate(createdAt: Instant): Instant =
        createdAt.plus(jwtConfiguration.refreshTokenExpirationMinutes, ChronoUnit.MINUTES)
}
