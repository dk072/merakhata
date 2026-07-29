package com.merakhata.app

import com.merakhata.app.domain.auth.CloudAuthResult
import com.merakhata.app.domain.auth.CloudAuthService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AuthSecurityTest {

    @Test
    fun test1_randomCredentialsMustFail() = runBlocking {
        val result = CloudAuthService.signIn("random_991823@fake.com", "randomPass12399")
        assertTrue("Random credentials must be rejected", result is CloudAuthResult.Error)
        val error = result as CloudAuthResult.Error
        assertTrue(error.message.contains("No account found") || error.message.contains("register"))
    }

    @Test
    fun test2_unregisteredEmailMustFail() = runBlocking {
        val result = CloudAuthService.signIn("unregistered_test_user@gmail.com", "password123")
        assertTrue("Unregistered email must be rejected", result is CloudAuthResult.Error)
    }

    @Test
    fun test3_registeredEmailWrongPasswordMustFail() = runBlocking {
        val email = "security_user_${System.currentTimeMillis()}@merakhata.com"
        val correctPassword = "CorrectPassword123"
        val wrongPassword = "WrongPassword999"

        // 1. Register valid user
        val regResult = CloudAuthService.signUp(email, correctPassword, "Shop Owner", "Mera Store")
        assertTrue("Registration must succeed for new user", regResult is CloudAuthResult.Success)

        // 2. Login with wrong password
        val loginResult = CloudAuthService.signIn(email, wrongPassword)
        assertTrue("Login with wrong password MUST be rejected", loginResult is CloudAuthResult.Error)
        val error = loginResult as CloudAuthResult.Error
        assertTrue("Message must indicate incorrect password", error.message.contains("Incorrect Password"))
    }

    @Test
    fun test4_registeredEmailCorrectPasswordMustSucceed() = runBlocking {
        val email = "valid_user_${System.currentTimeMillis()}@merakhata.com"
        val password = "StrongPassword456"

        val regResult = CloudAuthService.signUp(email, password, "Trader", "Kiran Supermarket")
        assertTrue(regResult is CloudAuthResult.Success)

        val loginResult = CloudAuthService.signIn(email, password)
        assertTrue("Login with correct credentials MUST succeed", loginResult is CloudAuthResult.Success)
        val success = loginResult as CloudAuthResult.Success
        assertEquals(email.lowercase(), success.email)
        assertNotNull("User ID must be non-null", success.userId)
        assertNotNull("Token must be non-null", success.token)
    }

    @Test
    fun test5_emptyEmailOrPasswordMustFail() = runBlocking {
        val res1 = CloudAuthService.signIn("", "password")
        assertTrue("Empty email must fail", res1 is CloudAuthResult.Error)

        val res2 = CloudAuthService.signIn("user@test.com", "")
        assertTrue("Empty password must fail", res2 is CloudAuthResult.Error)
    }

    @Test
    fun test6_invalidEmailFormatMustFail() = runBlocking {
        val res = CloudAuthService.signIn("invalidemailformat", "password123")
        assertTrue("Invalid email format must fail", res is CloudAuthResult.Error)
    }

    @Test
    fun test7_existingEmailRegistrationMustFail() = runBlocking {
        val email = "duplicate_test_${System.currentTimeMillis()}@merakhata.com"
        val password = "Password123"

        val reg1 = CloudAuthService.signUp(email, password, "Owner 1", "Store 1")
        assertTrue("First registration must succeed", reg1 is CloudAuthResult.Success)

        val reg2 = CloudAuthService.signUp(email, password, "Owner 2", "Store 2")
        assertTrue("Duplicate registration MUST fail safely", reg2 is CloudAuthResult.Error)
        val error = reg2 as CloudAuthResult.Error
        assertTrue(error.message.contains("already exists"))
    }

    @Test
    fun test8_newValidAccountRegistrationMustCreateAccount() = runBlocking {
        val email = "new_account_${System.currentTimeMillis()}@merakhata.com"
        val password = "SecurePass789"

        val reg = CloudAuthService.signUp(email, password, "John Doe", "Doe Enterprises")
        assertTrue("New valid account creation must succeed", reg is CloudAuthResult.Success)
        val success = reg as CloudAuthResult.Success
        assertTrue(success.userId.startsWith("usr_"))
    }

    @Test
    fun test9_passwordHashingVerification() {
        val pwd = "mySecretPassword123"
        val hash1 = CloudAuthService.hashPassword(pwd)
        val hash2 = CloudAuthService.hashPassword(pwd)

        assertEquals("Hash must be deterministic", hash1, hash2)
        assertNotEquals("Plain text password must NEVER equal hash", pwd, hash1)
        assertEquals("SHA-256 hex string length must be 64 characters", 64, hash1.length)
    }
}
