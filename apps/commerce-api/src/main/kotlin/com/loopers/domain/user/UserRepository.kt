package com.loopers.domain.user

interface UserRepository {
    fun save(user: UserModel): UserModel
    fun findByLoginId(loginId: String): UserModel?
    fun existsByLoginId(loginId: String): Boolean
}
