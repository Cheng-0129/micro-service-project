package com.spring.boot.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.boot.userservice.UserServiceApplication;
import com.spring.boot.userservice.dto.LoginDTO;
import com.spring.boot.userservice.dto.UserCreateDTO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/6/8 11:06
 */
@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserServiceApplication.class)
@ActiveProfiles("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("UserService 集成测试")
public class UserServiceIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
			.withDatabaseName("micro_service_project")
			.withUsername("postgres")
			.withPassword("root");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	private static final String USERNAME = "integration_test_user";
	private static final String PASSWORD = "test123456";

	@Test
	@Order(1)
	@DisplayName("用户注册 - 成功")
	void register_ShouldReturnSuccess() throws Exception {
		UserCreateDTO dto = new UserCreateDTO();
		dto.setName(USERNAME);
		dto.setPassword(PASSWORD);
		dto.setAge(25);
		dto.setEmail("integration@test.com");

		mockMvc.perform(post("/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(0))
				.andExpect(jsonPath("$.msg").value("注册成功"));
	}

	@Test
	@Order(2)
	@DisplayName("用户注册 - 重复用户名返回失败")
	void register_ShouldReturnError_WhenUserExists() throws Exception {
		UserCreateDTO dto = new UserCreateDTO();
		dto.setName(USERNAME);
		dto.setPassword(PASSWORD);
		dto.setAge(25);
		dto.setEmail("integration2@test.com");

		mockMvc.perform(post("/user/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(10007));
	}

	@Test
	@Order(3)
	@DisplayName("用户登录 - 成功返回Token")
	void login_ShouldReturnToken() throws Exception {
		LoginDTO dto = new LoginDTO();
		dto.setUsername(USERNAME);
		dto.setPassword(PASSWORD);

		mockMvc.perform(post("/user/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(0))
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
	}

	@Test
	@Order(4)
	@DisplayName("用户登录 - 密码错误返回失败")
	void login_ShouldReturnError_WhenWrongPassword() throws Exception {
		LoginDTO dto = new LoginDTO();
		dto.setUsername(USERNAME);
		dto.setPassword("wrongpassword");

		mockMvc.perform(post("/user/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(10006));
	}
}