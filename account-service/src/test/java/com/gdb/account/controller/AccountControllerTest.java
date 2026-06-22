package com.gdb.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdb.account.client.AuthClient;
import com.gdb.account.constants.AccountConstants;
import com.gdb.account.dto.response.AccountResponse;
import com.gdb.account.exception.AccountException;
import com.gdb.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AuthClient authClient;

    @BeforeEach
    void setUp() {
        AuthClient.TokenValidationResponse validResponse = AuthClient.TokenValidationResponse.builder()
                .isValid(true)
                .userId(1L)
                .loginId("admin")
                .role("ADMIN")
                .build();
        when(authClient.validateToken(any())).thenReturn(validResponse);
    }

    @Test
    public void testGetAccountByNumber_NotFound() throws Exception {
        when(accountService.getAccountByNumber(999L))
                .thenThrow(new AccountException("Account not found with number: 999",
                        AccountConstants.ACCOUNT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/accounts/999")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    public void testGetAccountByNumber_Success() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .accountNumber(100001L)
                .accountType("SAVINGS")
                .name("Test User")
                .balance(new BigDecimal("5000.00"))
                .isActive(true)
                .build();

        when(accountService.getAccountByNumber(100001L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/100001")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_number").value(100001))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    public void testCreateSavingsAccount_MissingFields_Returns422() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/savings")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testGetAllAccounts_ReturnsOk() throws Exception {
        when(accountService.getAllAccounts(any(), any(), any()))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/accounts")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAccountByNumber_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/100001"))
                .andExpect(status().isUnauthorized());
    }
}
