package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class UserModelTest {
    @DisplayName("회원을 생성할 때,")
    @Nested
    inner class Create {
        @DisplayName("유효한 정보가 주어지면 회원이 생성되고 비밀번호는 암호화된다.")
        @Test
        fun createsUser_whenValidFieldsAreProvided() {
            // arrange
            val password = "Pass1234!"

            // act
            val user = createUser(password = password)

            // assert
            assertAll(
                { assertThat(user.loginId).isEqualTo("loopers01") },
                { assertThat(user.name).isEqualTo("홍길동") },
                { assertThat(user.birthDate).isEqualTo(LocalDate.of(1990, 1, 1)) },
                { assertThat(user.email).isEqualTo("loopers@example.com") },
                { assertThat(user.password).isNotEqualTo(password) },
                { assertThat(user.authenticate(password)).isTrue() },
            )
        }

        @DisplayName("형식이 맞지 않는 값이 있으면 BAD_REQUEST 예외가 발생한다.")
        @Test
        fun throwsBadRequest_whenFieldFormatIsInvalid() {
            // arrange
            val invalidEmail = "invalid-email"

            // act
            val exception = assertThrows<CoreException> {
                createUser(email = invalidEmail)
            }

            // assert
            assertThat(exception.errorType).isEqualTo(ErrorType.BAD_REQUEST)
        }

        @DisplayName("비밀번호에 생년월일이 포함되면 BAD_REQUEST 예외가 발생한다.")
        @Test
        fun throwsBadRequest_whenPasswordContainsBirthDate() {
            // arrange
            val password = "19900101!"

            // act
            val exception = assertThrows<CoreException> {
                createUser(password = password)
            }

            // assert
            assertThat(exception.errorType).isEqualTo(ErrorType.BAD_REQUEST)
        }
    }

    @DisplayName("회원 이름을 마스킹할 때,")
    @Nested
    inner class MaskName {
        @DisplayName("마지막 글자를 * 로 바꾼다.")
        @Test
        fun masksLastCharacter() {
            // arrange
            val user = createUser(name = "홍길동")

            // act
            val result = user.maskedName()

            // assert
            assertThat(result).isEqualTo("홍길*")
        }
    }

    @DisplayName("비밀번호를 변경할 때,")
    @Nested
    inner class ChangePassword {
        @DisplayName("현재 비밀번호와 같은 새 비밀번호는 사용할 수 없다.")
        @Test
        fun throwsBadRequest_whenNewPasswordEqualsCurrentPassword() {
            // arrange
            val user = createUser(password = "Pass1234!")

            // act
            val exception = assertThrows<CoreException> {
                user.changePassword(currentPassword = "Pass1234!", newPassword = "Pass1234!")
            }

            // assert
            assertThat(exception.errorType).isEqualTo(ErrorType.BAD_REQUEST)
        }
    }

    private fun createUser(
        loginId: String = "loopers01",
        password: String = "Pass1234!",
        name: String = "홍길동",
        birthDate: LocalDate = LocalDate.of(1990, 1, 1),
        email: String = "loopers@example.com",
    ): UserModel {
        return UserModel(
            loginId = loginId,
            rawPassword = password,
            name = name,
            birthDate = birthDate,
            email = email,
        )
    }
}
