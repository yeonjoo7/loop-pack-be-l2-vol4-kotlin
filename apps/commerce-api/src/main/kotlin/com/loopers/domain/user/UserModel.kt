package com.loopers.domain.user

import com.loopers.domain.BaseEntity
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "users")
class UserModel(
    loginId: String,
    rawPassword: String,
    name: String,
    birthDate: LocalDate,
    email: String,
) : BaseEntity() {
    @Column(nullable = false, unique = true)
    var loginId: String = loginId
        protected set

    @Column(nullable = false)
    var password: String = encryptPassword(rawPassword, birthDate)
        protected set

    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var birthDate: LocalDate = birthDate
        protected set

    @Column(nullable = false)
    var email: String = email
        protected set

    init {
        validate(loginId = loginId, rawPassword = rawPassword, name = name, birthDate = birthDate, email = email)
    }

    fun maskedName(): String {
        return if (name.length <= 1) "*" else name.dropLast(1) + "*"
    }

    fun authenticate(rawPassword: String): Boolean {
        return PasswordEncryptor.matches(rawPassword, password)
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (!authenticate(currentPassword)) throw CoreException(ErrorType.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.")
        if (currentPassword == newPassword) throw CoreException(ErrorType.BAD_REQUEST, "현재 비밀번호는 사용할 수 없습니다.")
        this.password = encryptPassword(newPassword, birthDate)
    }

    private fun encryptPassword(rawPassword: String, birthDate: LocalDate): String {
        validatePassword(rawPassword, birthDate)
        return PasswordEncryptor.encrypt(rawPassword)
    }

    companion object {
        private val LOGIN_ID_REGEX = "^[A-Za-z0-9]+$".toRegex()
        private val NAME_REGEX = "^[가-힣A-Za-z ]+$".toRegex()
        private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        private val PASSWORD_REGEX = "^[A-Za-z0-9!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]{8,16}$".toRegex()

        private fun validate(
            loginId: String,
            rawPassword: String,
            name: String,
            birthDate: LocalDate,
            email: String,
        ) {
            if (!LOGIN_ID_REGEX.matches(loginId)) throw CoreException(ErrorType.BAD_REQUEST, "로그인 ID 형식이 올바르지 않습니다.")
            if (!NAME_REGEX.matches(name) || name.isBlank()) throw CoreException(ErrorType.BAD_REQUEST, "이름 형식이 올바르지 않습니다.")
            if (birthDate.isAfter(LocalDate.now())) throw CoreException(ErrorType.BAD_REQUEST, "생년월일 형식이 올바르지 않습니다.")
            if (!EMAIL_REGEX.matches(email)) throw CoreException(ErrorType.BAD_REQUEST, "이메일 형식이 올바르지 않습니다.")
            validatePassword(rawPassword, birthDate)
        }

        private fun validatePassword(rawPassword: String, birthDate: LocalDate) {
            if (!PASSWORD_REGEX.matches(rawPassword)) throw CoreException(ErrorType.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다.")
            val birthDateText = birthDate.toString().replace("-", "")
            if (rawPassword.contains(birthDateText)) throw CoreException(ErrorType.BAD_REQUEST, "비밀번호에 생년월일을 사용할 수 없습니다.")
        }
    }
}
