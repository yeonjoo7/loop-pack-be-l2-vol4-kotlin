package com.loopers.application.user

import com.loopers.domain.user.UserService
import org.springframework.stereotype.Component

@Component
class UserFacade(
    private val userService: UserService,
) {
    fun signUp(command: UserService.SignUpCommand): UserInfo {
        return userService.signUp(command).let { UserInfo.from(it) }
    }

    fun getProfile(loginId: String, password: String): UserInfo {
        return userService.getProfile(loginId = loginId, password = password).let { UserInfo.maskedFrom(it) }
    }

    fun changePassword(loginId: String, currentPassword: String, newPassword: String) {
        userService.changePassword(loginId = loginId, currentPassword = currentPassword, newPassword = newPassword)
    }
}
