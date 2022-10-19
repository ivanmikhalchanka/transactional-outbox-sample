package com.github.ivanmikhalchanka.transactionaloutbox.web.rest;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import com.github.ivanmikhalchanka.transactionaloutbox.PostgresIntegrationTest;
import com.github.ivanmikhalchanka.transactionaloutbox.entity.UserStatus;
import com.github.ivanmikhalchanka.transactionaloutbox.web.rest.dto.UserRegisterRequest;
import com.github.ivanmikhalchanka.transactionaloutbox.web.rest.dto.UserStatusChangeRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ExtendWith({DBUnitExtension.class, MockitoExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
class UserResourceIntegrationTest implements PostgresIntegrationTest {
  private static final ObjectMapper MAPPER =
      new ObjectMapper().registerModule(new JavaTimeModule());

  private static final String EMAIL = "test@sample.com";

  @Autowired JdbcTemplate jdbcTemplate;
  @Autowired MockMvc mockMvc;

  @Test
  @ExpectedDataSet(
      value = "/dbunit/user/user-created-expected.xml",
      compareOperation = CompareOperation.CONTAINS)
  void testUserCreated() throws Exception {
    jdbcTemplate.update("ALTER SEQUENCE app_user_id_seq RESTART WITH 1");
    UserRegisterRequest request = new UserRegisterRequest(EMAIL, "test-admin");

    ResultActions response =
        mockMvc.perform(
            post("/api/users/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)));

    response
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.email", is(EMAIL)))
        .andExpect(jsonPath("$.status", is("REGISTERED")));
  }

  @Test
  @DataSet("/dbunit/user/user-updated-initial.xml")
  @ExpectedDataSet(
      value = "/dbunit/user/user-updated-expected.xml",
      compareOperation = CompareOperation.CONTAINS)
  void testUserStatusChanged() throws Exception {
    int userId = 2;
    UserStatusChangeRequest request =
        new UserStatusChangeRequest(UserStatus.ACTIVE, "test-admin-2");

    ResultActions response =
        mockMvc.perform(
            post("/api/users/v1/{userId}/status", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)));

    response
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(userId)))
        .andExpect(jsonPath("$.status", is(UserStatus.ACTIVE.name())));
  }
}
