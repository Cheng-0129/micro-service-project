package com.spring.boot.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.boot.orderservice.OrderServiceApplication;
import com.spring.boot.orderservice.dto.OrderCreateDTO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/6/8 11:13
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = OrderServiceApplication.class, loader = AnnotationConfigWebContextLoader.class)
@ActiveProfiles("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("OrderService 集成测试")
public class OrderServiceIntegrationTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	private static final Long TEST_USER_ID = 9999L;
	private static final Long TEST_PRODUCT_ID = 9999L;
	private static Long createdOrderNo;

	@Test
	@Order(1)
	@DisplayName("创建订单 - 成功")
	void createOrder_ShouldReturnSuccess() throws Exception {
		// 先注册用户
		String registerBody = "{\"name\":\"order_test_user\",\"password\":\"test123456\",\"age\":20,\"email\":\"order@test.com\"}";
		mockMvc.perform(post("/user/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registerBody));

		// 创建订单
		OrderCreateDTO dto = new OrderCreateDTO();
		dto.setUserId(TEST_USER_ID);
		dto.setProductId(TEST_PRODUCT_ID);
		dto.setNum(5);

		String result = mockMvc.perform(post("/order/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(0))
				.andExpect(jsonPath("$.data.orderNo").isNotEmpty())
				.andReturn()
				.getResponse()
				.getContentAsString();

		createdOrderNo = objectMapper.readTree(result)
				.get("data").get("orderNo").asLong();
	}

	@Test
	@Order(2)
	@DisplayName("查询订单 - 成功")
	void getOrder_ShouldReturnOrder() throws Exception {
		mockMvc.perform(get("/order/" + createdOrderNo))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(0))
				.andExpect(jsonPath("$.data.orderNo").value(createdOrderNo));
	}

	@Test
	@Order(3)
	@DisplayName("取消订单 - 成功")
	void cancelOrder_ShouldReturnSuccess() throws Exception {
		mockMvc.perform(put("/order/cancel/" + createdOrderNo))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(0));
	}

	@Test
	@Order(4)
	@DisplayName("查询不存在订单 - 返回失败")
	void getOrder_ShouldReturnError_WhenNotExists() throws Exception {
		mockMvc.perform(get("/order/99999999"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(30002));
	}
}
