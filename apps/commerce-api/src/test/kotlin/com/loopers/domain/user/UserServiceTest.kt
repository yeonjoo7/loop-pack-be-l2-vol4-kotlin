package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class UserServiceTest {
    @DisplayName("회원가입할 때,")
    @Nested
    inner class SignUp {
        @DisplayName("이미 가입된 로그인 ID 면 CONFLICT 예외가 발생한다.")
        @Test
        fun throwsConflict_whenLoginIdAlreadyExists() {
            // arrange
            val repository = InMemoryUserRepository()
            val service = UserService(repository)
            service.signUp(signUpCommand(loginId = "loopers01"))

            // act
            val exception = assertThrows<CoreException> {
                service.signUp(signUpCommand(loginId = "loopers01"))
            }

            // assert
            assertThat(exception.errorType).isEqualTo(ErrorType.CONFLICT)
        }
    }

    @DisplayName("비밀번호를 변경할 때,")
    @Nested
    inner class ChangePassword {
        @DisplayName("현재 비밀번호로 인증되면 새 비밀번호로 변경된다.")
        @Test
        fun changesPassword_whenCurrentPasswordIsValid() {
            // arrange
            val repository = InMemoryUserRepository()
            val service = UserService(repository)
            service.signUp(signUpCommand(password = "Pass1234!"))

            // act
            service.changePassword(
                loginId = "loopers01",
                currentPassword = "Pass1234!",
                newPassword = "NewPass1!",
            )

            // assert
            val user = repository.findByLoginId("loopers01")
            assertThat(user?.authenticate("NewPass1!")).isTrue()
        }
    }

    private fun signUpCommand(
        loginId: String = "loopers01",
        password: String = "Pass1234!",
    ): UserService.SignUpCommand {
        return UserService.SignUpCommand(
            loginId = loginId,
            password = password,
            name = "홍길동",
            birthDate = LocalDate.of(1990, 1, 1),
            email = "$loginId@example.com",
        )
    }

    private class InMemoryUserRepository : UserRepository {
        private val users = mutableMapOf<String, UserModel>()

        override fun save(user: UserModel): UserModel {
            users[user.loginId] = user
            return user
        }

        override fun findByLoginId(loginId: String): UserModel? {
            return users[loginId]
        }

        override fun existsByLoginId(loginId: String): Boolean {
            return users.containsKey(loginId)
        }
    }
}
