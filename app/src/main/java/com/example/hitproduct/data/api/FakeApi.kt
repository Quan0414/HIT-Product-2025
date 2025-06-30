package com.example.hitproduct.data.api

import com.example.hitproduct.data.model.User
import com.example.hitproduct.data.model.auth.LoginRequest
import com.example.hitproduct.data.model.auth.LoginResponse
import com.example.hitproduct.data.model.response.ApiResponse

class FakeApiService : ApiService {
    override suspend fun login(req: LoginRequest) = ApiResponse(
        success = true,
        message = "OK (fake)",
        data = LoginResponse(
            token = "fake-token",
            user = User("Nguyễn", "Văn A", "Fake", "Male", req.email, "1990-01-01")
        )
    )
}
