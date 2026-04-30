package com.spring.boot.stockservice.controller;

import com.spring.boot.commoncore.result.Result;
import com.spring.boot.stockservice.service.StockValidationService;
import com.spring.boot.stockservice.dto.StockCreateDTO;
import com.spring.boot.stockservice.vo.StockDeductVO;
import com.spring.boot.stockservice.dto.StockUpdateDTO;
import com.spring.boot.stockservice.vo.StockVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

	@Operation(
			summary = "新增库存",
			description = "传入库存信息，创建新商品库存并返回成功状态，若添加失败则返回20001。"
	)
	@PostMapping("/add")
	public Result<Void> addStock(@RequestBody @Valid StockCreateDTO stock) {

		log.info("【库存模块】新增库存信息，请求参数：stock={}", stock);

		stockValidationService.addStock(stock);

		log.info("【库存模块】新增库存信息成功，stock={}", stock);

		return Result.success("库存新增成功");
	}

	@Operation(
			summary = "根据ID查询库存信息",
			description = "传入产品ID，返回库存信息，若产品不存在则返回20002。"
	)
	@GetMapping("/{productId}")
	public Result<StockVO> getStockByProductId(@PathVariable("productId")
	                                           @Parameter(
			                                           name = "productId",
			                                           description = "产品ID",
			                                           example = "1",
			                                           required = true
	                                           )
	                                           @NotNull(message = "ID不能为空")
	                                           @Min(value = 1, message = "ID必须大于0")
	                                           Long productId) {

		log.info("【库存模块】查询库存信息，请求参数：productId={}", productId);

		StockVO stockVO = stockValidationService.getStockByProductId(productId);

		log.info("【库存模块】查询库存信息成功，productId={}", productId);

		return Result.success(stockVO);
	}

	@Operation(
			summary = "根据ID更改库存信息",
			description = "传入库存ID和新库存信息，若库存不存在则返回20002；若信息更新失败则返回20003"
	)
	@PutMapping("/{productId}")
	public Result<Void> updateStock(@PathVariable("productId")
	                                @Parameter(
			                                name = "productId",
			                                description = "产品ID",
			                                example = "1",
			                                required = true
	                                )
	                                @NotNull(message = "ID不能为空")
	                                @Min(value = 1, message = "ID必须大于0") Long productId,
	                                @RequestBody @Valid StockUpdateDTO stock) {

		log.info("【库存模块】更新库存信息，请求参数：productId={}, stock={}", productId, stock);

		stockValidationService.updateStockByProductId(productId, stock);

		log.info("【库存模块】更新库存信息成功，productId={}", productId);

		return Result.success("库存更新成功");
	}

	@Operation(
			summary = "根据ID删除对应商品库存",
			description = "传入库存ID删除对应商品库存，若库存不存在则返回20002"
	)
	@DeleteMapping("{productId}")
	public Result<Void> deleteStock(@PathVariable("productId")
	                                @Parameter(
			                                name = "productId",
			                                description = "产品ID",
			                                example = "1",
			                                required = true
	                                )
	                                @NotNull(message = "ID不能为空")
	                                @Min(value = 1, message = "ID必须大于0")
	                                Long productId) {

		log.info("【库存模块】删除库存信息，请求参数：productId={}", productId);

		stockValidationService.deleteStockByProductId(productId);

		log.info("【库存模块】删除库存信息成功，productId={}", productId);

		return Result.success("库存删除成功");
	}

	@Operation(summary = "扣减库存",
			description = "传入产品ID和数量，扣除对应数量的库存，成功则返回商品信息和库存余量，若库存不存在则返回20002，若库存不足则返回20004"
	)
	@PostMapping("/deduct")
	public Result<StockDeductVO> deductStock(@RequestParam("productId")
	                                         @Parameter(
			                                         name = "productId",
			                                         description = "产品ID",
			                                         example = "1",
			                                         required = true
	                                         )
	                                         @NotNull(message = "ID不能为空")
	                                         @Min(value = 1, message = "ID必须大于0")
	                                         Long productId,
	                                         @RequestParam("num")
	                                         @Parameter(
			                                         name = "num",
			                                         description = "购买数量",
			                                         example = "20",
			                                         required = true
	                                         )
	                                         @NotNull(message = "数量不能为空")
	                                         @Min(value = 1, message = "数量必须大于0")
	                                         Integer num) {
		log.info("【库存模块】扣减库存，请求参数：productId={}, num={}", productId, num);
		StockDeductVO stockDeductVO = stockValidationService.deductStock(productId, num);
		log.info("【库存模块】扣减库存成功，productId={}, num={}, stock={}, productName={}", productId, num, stockDeductVO.getStock(), stockDeductVO.getProductName());
		return Result.success(stockDeductVO);
	}
}
