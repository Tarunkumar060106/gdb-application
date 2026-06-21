package com.gdb.account.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

// TODO: MOD9-CR-01: MockMvc Integration Tests.
// Trainee task: Write integration tests for AccountController using @WebMvcTest.
// Write tests for GET /api/v1/accounts/{accountNumber} and POST /api/v1/accounts/savings.
// Verify status codes (200, 201, 400, 422) and response body payloads.
@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAccountByNumber_NotFound() throws Exception {
        // Trainee: Write a test case here that mocks the service and asserts 404 response
    }
}
