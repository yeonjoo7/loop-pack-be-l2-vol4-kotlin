package com.loopers.domain.user

import com.loopers.infrastructure.user.UserJpaRepository
import com.loopers.utils.DatabaseCleanUp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
class UserServiceIntegrationTest @Autowired constructor(
    private val userService: UserService,
    private val userJpaRepository: UserJpaRepository,
    private val databaseCleanUp: DatabaseCleanUp,
) {
    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }

    @DisplayName("회원가입할 때,")
    @Nested
    inner class SignUp {
        @DisplayName("회원 정보와 암호화된 비밀번호를 저장한다.")
        @Test
        fun savesUserWithEncryptedPassword() {
            // arrange
            val command = UserService.SignUpCommand(
                loginId = "loopers01",
                password = "Pass1234!",
                name = "홍길동",
                birthDate = LocalDate.of(1990, 1, 1),
                email = "loopers@example.com",
            )

            // act
            userService.signUp(command)

            // assert
            val user = userJpaRepository.findByLoginId("loopers01")
            assertAll(
                { assertThat(user).isNotNull() },
                { assertThat(user?.password).isNotEqualTo("Pass1234!") },
                { assertThat(user?.authenticate("Pass1234!")).isTrue() },
            )
        }
    }

    @DisplayName("내 정보를 조회할 때,")
    @Nested
    inner class GetProfile {
        @DisplayName("로그인 ID와 비밀번호가 일치하면 회원 정보를 반환한다.")
        @Test
        fun returnsProfile_whenCredentialsAreValid() {
            // arrange
            userService.signUp(signUpCommand())

            // act
            val user = userService.getProfile(loginId = "loopers01", password = "Pass1234!")

            // assert
            assertAll(
                { assertThat(user.loginId).isEqualTo("loopers01") },
                { assertThat(user.maskedName()).isEqualTo("홍길*") },
            )
        }
    }

    private fun signUpCommand(): UserService.SignUpCommand {
        return UserService.SignUpCommand(
            loginId = "loopers01",
            password = "Pass1234!",
            name = "홍길동",
            birthDate = LocalDate.of(1990, 1, 1),
            email = "loopers@example.com",
        )
    }
}
