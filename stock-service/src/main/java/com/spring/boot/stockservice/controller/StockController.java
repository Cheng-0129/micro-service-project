package com.spring.boot.stockservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.stockservice.dto.StockCreateDTO;
import com.spring.boot.stockservice.dto.StockQueryDTO;
import com.spring.boot.stockservice.dto.StockUpdateDTO;
import com.spring.boot.stockservice.service.StockValidationService;
import com.spring.boot.stockservice.vo.StockAddBackVO;
import com.spring.boot.stockservice.vo.StockDeductVO;
import com.spring.boot.stockservice.vo.StockVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/9 14:41
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/stock")
@Tag(name = "库存管理模块", description = "库存信息增删改查接口")
public class StockController {

	@Resource
	StockValidationService stockValidationService;

	@Operation(summary = "新增库存",
			description = "创建商品库存记录，成功返回操作成功，失败返回 20001（库存添加失败）")
	@PostMapping("/add")
	public Result<Void> addStock(@RequestBody @Valid StockCreateDTO stock) {

		log.info("【库存模块】新增库存信息，请求参数：stock={}", stock);
		stockValidationService.addStock(stock);
		log.info("【库存模块】新增库存信息成功，stock={}", stock);
		return Result.success("库存新增成功");
	}

	@Operation(summary = "查询库存",
			description = "根据商品ID查询库存信息，库存不存在返回 20002（库存不存在）")
	@GetMapping("/{productId}")
	public Result<StockVO> getStockByProductId(@PathVariable("productId")
	                                           @Parameter(
			                                           description = "产品ID",
			                                           example = "1")
	                                           @Min(value = 1, message = "ID必须大于0")
	                                           Long productId) {

		log.info("【库存模块】查询库存信息，请求参数：productId={}", productId);
		StockVO stockVO = stockValidationService.getStockByProductId(productId);
		log.info("【库存模块】查询库存信息成功，productId={}", productId);
		return Result.success(stockVO);
	}

	@Operation(summary = "修改库存信息",
			description = "根据商品ID更新库存信息，库存不存在返回 20002，更新失败返回 20003")
	@PutMapping("/{productId}")
	public Result<Void> updateStock(@PathVariable("productId")
	                                @Parameter(
			                                description = "产品ID",
			                                example = "1")
	                                @Min(value = 1, message = "ID必须大于0")
	                                Long productId,
	                                @RequestBody @Valid StockUpdateDTO stock) {

		log.info("【库存模块】更新库存信息，请求参数：productId={}, stock={}", productId, stock);
		stockValidationService.updateStockByProductId(productId, stock);
		log.info("【库存模块】更新库存信息成功，productId={}", productId);
		return Result.success("库存更新成功");
	}

	@Operation(summary = "删除库存",
			description = "根据商品ID删除库存记录，库存不存在返回 20002")
	@DeleteMapping("{productId}")
	public Result<Void> deleteStock(@PathVariable("productId")
	                                @Parameter(
			                                description = "产品ID",
			                                example = "1")
	                                @Min(value = 1, message = "ID必须大于0")
	                                Long productId) {

		log.info("【库存模块】删除库存信息，请求参数：productId={}", productId);
		stockValidationService.deleteStockByProductId(productId);
		log.info("【库存模块】删除库存信息成功，productId={}", productId);
		return Result.success("库存删除成功");
	}

	@Operation(summary = "扣减库存",
			description = "根据商品ID和数量扣减库存，成功返回商品信息和剩余库存。" +
					"库存不存在返回 20002，库存不足返回 20004。触发熔断/限流返回 20005/20006")
	@PostMapping("/deduct")
	@SentinelResource(
			value = "deductStock",
			blockHandler = "handleBlock",
			blockHandlerClass = StockBlockHandler.class,
			fallback = "handleFallback",
			fallbackClass = StockBlockHandler.class)
	public Result<StockDeductVO> deductStock(@RequestParam("productId")
	                                         @Parameter(
			                                         description = "产品ID",
			                                         example = "1")
	                                         @Min(value = 1, message = "ID必须大于0")
	                                         Long productId,
	                                         @RequestParam("num")
	                                         @Parameter(
			                                         description = "购买数量",
			                                         example = "20")
	                                         @Min(value = 1, message = "数量必须大于0")
	                                         Integer num) {

		log.info("【库存模块】扣减库存，请求参数：productId={}, num={}", productId, num);
		StockDeductVO stockDeductVO = stockValidationService.deductStock(productId, num);
		log.info("【库存模块】扣减库存成功，productId={}, num={}, stock={}, productName={}", productId, num, stockDeductVO.getStock(), stockDeductVO.getProductName());
		return Result.success(stockDeductVO);
	}

	@Operation(summary = "分页查询库存",
			description = "支持按条件分页查询库存列表，无匹配数据返回空列表")
	@GetMapping("/page")
	public Result<PageVO<StockVO>> getStockPage(@Valid StockQueryDTO query) {

		log.info("【库存模块】收到分页查询请求，参数：{}", query);
		PageVO<StockVO> pageVO = stockValidationService.getStockPage(query);
		log.info("【库存模块】分页查询响应，返回{}条", pageVO.getRecords().size());
		return Result.success(pageVO, "查询成功");
	}

	@Operation(summary = "回滚库存",
			description = "根据商品ID和数量回滚库存（取消订单时调用），成功返回商品信息和当前库存。库存不存在返回 20002")
	@PostMapping("/addBack")
	public Result<StockAddBackVO> addBackStock(@RequestParam("productId")
	                                           @Parameter(
			                                           description = "产品ID",
			                                           example = "1")
	                                           @Min(value = 1, message = "ID必须大于0")
	                                           Long productId,
	                                           @RequestParam("num")
	                                           @Parameter(
			                                           description = "购买数量",
			                                           example = "20")
	                                           @Min(value = 1, message = "数量必须大于0")
	                                           Integer num) {
		log.info("【库存模块】回滚库存，请求参数：productId={}, num={}", productId, num);
		StockAddBackVO stockAddBackVO = stockValidationService.addBackStock(productId, num);
		log.info("【库存模块】回滚库存成功，productId={}, num={}, stock={}, productName={}", productId, num, stockAddBackVO.getStock(), stockAddBackVO.getProductName());
		return Result.success(stockAddBackVO);
	}
}
