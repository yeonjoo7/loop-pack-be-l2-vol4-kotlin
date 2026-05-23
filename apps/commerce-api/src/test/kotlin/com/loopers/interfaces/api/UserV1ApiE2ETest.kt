package com.loopers.interfaces.api

import com.loopers.interfaces.api.user.UserV1Dto
import com.loopers.utils.DatabaseCleanUp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserV1ApiE2ETest @Autowired constructor(
    private val testRestTemplate: TestRestTemplate,
    private val databaseCleanUp: DatabaseCleanUp,
) {
    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }

    @DisplayName("POST /api/v1/users")
    @Nested
    inner class SignUp {
        @DisplayName("유효한 회원가입 요청이면 회원 정보를 반환한다.")
        @Test
        fun signsUpUser_whenRequestIsValid() {
            // arrange
            val request = signUpRequest()

            // act
            val responseType = object : ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>>() {}
            val response = testRestTemplate.exchange("/api/v1/users", HttpMethod.POST, HttpEntity(request), responseType)

            // assert
            assertAll(
                { assertThat(response.statusCode.is2xxSuccessful).isTrue() },
                { assertThat(response.body?.data?.loginId).isEqualTo("loopers01") },
                { assertThat(response.body?.data?.name).isEqualTo("홍길동") },
            )
        }

        @DisplayName("이미 가입된 로그인 ID 면 409 CONFLICT 응답을 받는다.")
        @Test
        fun returnsConflict_whenLoginIdAlreadyExists() {
            // arrange
            val request = signUpRequest()
            testRestTemplate.postForEntity("/api/v1/users", request, ApiResponse::class.java)

            // act
            val responseType = object : ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>>() {}
            val response = testRestTemplate.exchange("/api/v1/users", HttpMethod.POST, HttpEntity(request), responseType)

            // assert
            assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        }
    }

    @DisplayName("GET /api/v1/users/me")
    @Nested
    inner class GetProfile {
        @DisplayName("인증 헤더가 유효하면 마스킹된 내 정보를 반환한다.")
        @Test
        fun returnsMyProfile_whenHeadersAreValid() {
            // arrange
            testRestTemplate.postForEntity("/api/v1/users", signUpRequest(), ApiResponse::class.java)
            val headers = authHeaders()

            // act
            val responseType = object : ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>>() {}
            val response = testRestTemplate.exchange("/api/v1/users/me", HttpMethod.GET, HttpEntity<Any>(headers), responseType)

            // assert
            assertAll(
                { assertThat(response.statusCode.is2xxSuccessful).isTrue() },
                { assertThat(response.body?.data?.loginId).isEqualTo("loopers01") },
                { assertThat(response.body?.data?.name).isEqualTo("홍길*") },
                { assertThat(response.body?.data?.birthDate).isEqualTo(LocalDate.of(1990, 1, 1)) },
                { assertThat(response.body?.data?.email).isEqualTo("loopers@example.com") },
            )
        }
    }

    @DisplayName("PATCH /api/v1/users/me/password")
    @Nested
    inner class ChangePassword {
        @DisplayName("현재 비밀번호가 맞고 새 비밀번호가 유효하면 비밀번호를 변경한다.")
        @Test
        fun changesPassword_whenRequestIsValid() {
            // arrange
            testRestTemplate.postForEntity("/api/v1/users", signUpRequest(), ApiResponse::class.java)
            val request = UserV1Dto.ChangePasswordRequest(currentPassword = "Pass1234!", newPassword = "NewPass1!")

            // act
            val response = testRestTemplate.exchange(
                "/api/v1/users/me/password",
                HttpMethod.PATCH,
                HttpEntity(request, authHeaders()),
                ApiResponse::class.java,
            )

            // assert
            assertThat(response.statusCode.is2xxSuccessful).isTrue()
        }
    }

    private fun signUpRequest(): UserV1Dto.SignUpRequest {
        return UserV1Dto.SignUpRequest(
            loginId = "loopers01",
            password = "Pass1234!",
            name = "홍길동",
            birthDate = LocalDate.of(1990, 1, 1),
            email = "loopers@example.com",
        )
    }

    private fun authHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            set("X-Loopers-LoginId", "loopers01")
            set("X-Loopers-LoginPw", "Pass1234!")
        }
    }
}
