package com.loopers.application.user

import com.loopers.domain.user.UserModel
import java.time.LocalDate

data class UserInfo(
    val loginId: String,
    val name: String,
    val birthDate: LocalDate,
    val email: String,
) {
    companion object {
        fun from(user: UserModel): UserInfo {
            return UserInfo(
                loginId = user.loginId,
                name = user.name,
                birthDate = user.birthDate,
                email = user.email,
            )
        }

        fun maskedFrom(user: UserModel): UserInfo {
            return UserInfo(
                loginId = user.loginId,
                name = user.maskedName(),
                birthDate = user.birthDate,
                email = user.email,
            )
        }
    }
}
