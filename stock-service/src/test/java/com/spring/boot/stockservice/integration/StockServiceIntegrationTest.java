package com.spring.boot.stockservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.boot.stockservice.StockServiceApplication;
import com.spring.boot.stockservice.dto.StockCreateDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/6/8 11:12
 */
@SpringBootTest(classes = {StockServiceApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("StockService 集成测试")
public class StockServiceIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private static Long productId;

	@Test
	@Order(1)
	@DisplayName("新增库存 - 成功")
	void addStock_ShouldReturnSuccess() throws Exception {
		StockCreateDTO dto = new StockCreateDTO();
		dto.setProductName("集成测试商品");
		dto.setStock(100);
		dto.setPrice(new BigDecimal("99.99"));

		String result = mockMvc.perform(post("/stock/add")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(0))
				.andReturn()
				.getResponse()
				.getContentAsString();

		// 查回来拿真实 productId
		String listResult = mockMvc.perform(get("/stock/page")
						.param("pageNum", "1")
						.param("pageSize", "10"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		productId = objectMapper.readTree(listResult)
				.get("data").get("records").get(0).get("productId").asLong();
	}

	@Test
	@Order(2)
	@DisplayName("查询库存 - 成功")
	void getStock_ShouldReturnStock() throws Exception {
		mockMvc.perform(get("/stock/" + productId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(0))
				.andExpect(jsonPath("$.data.stock").value(100))
				.andExpect(jsonPath("$.data.productName").value("集成测试商品"));
	}

	@Test
	@Order(3)
	@DisplayName("扣减库存 - 成功")
	void deductStock_ShouldReturnRemaining() throws Exception {
		mockMvc.perform(post("/stock/deduct")
						.param("productId", String.valueOf(productId))
						.param("num", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(0))
				.andExpect(jsonPath("$.data.stock").value(90));
	}

	@Test
	@Order(4)
	@DisplayName("扣减库存 - 库存不足返回失败")
	void deductStock_ShouldReturnError_WhenInsufficient() throws Exception {
		mockMvc.perform(post("/stock/deduct")
						.param("productId", String.valueOf(productId))
						.param("num", "9999"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(20004));
	}

	@Test
	@Order(5)
	@DisplayName("查询不存在库存 - 返回失败")
	void getStock_ShouldReturnError_WhenNotExists() throws Exception {
		mockMvc.perform(get("/stock/88888"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(20002));
	}
}
