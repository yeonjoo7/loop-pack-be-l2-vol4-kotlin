package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun signUp(command: SignUpCommand): UserModel {
        if (userRepository.existsByLoginId(command.loginId)) {
            throw CoreException(ErrorType.CONFLICT, "이미 가입된 로그인 ID입니다.")
        }

        return userRepository.save(
            UserModel(
                loginId = command.loginId,
                rawPassword = command.password,
                name = command.name,
                birthDate = command.birthDate,
                email = command.email,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun getProfile(loginId: String, password: String): UserModel {
        return authenticate(loginId, password)
    }

    @Transactional
    fun changePassword(loginId: String, currentPassword: String, newPassword: String) {
        val user = authenticate(loginId, currentPassword)
        user.changePassword(currentPassword = currentPassword, newPassword = newPassword)
    }

    private fun authenticate(loginId: String, password: String): UserModel {
        val user = userRepository.findByLoginId(loginId)
            ?: throw CoreException(ErrorType.NOT_FOUND, "회원을 찾을 수 없습니다.")
        if (!user.authenticate(password)) throw CoreException(ErrorType.BAD_REQUEST, "비밀번호가 일치하지 않습니다.")
        return user
    }

    data class SignUpCommand(
        val loginId: String,
        val password: String,
        val name: String,
        val birthDate: LocalDate,
        val email: String,
    )
}
