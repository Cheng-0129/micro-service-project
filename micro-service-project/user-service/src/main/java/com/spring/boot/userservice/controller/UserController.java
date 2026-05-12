package com.spring.boot.userservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.commoncore.util.ExceptionUtil;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.userservice.dto.OrderCreateDTO;
import com.spring.boot.userservice.dto.UserCreateDTO;
import com.spring.boot.userservice.dto.UserQueryDTO;
import com.spring.boot.userservice.dto.UserUpdateDTO;
import com.spring.boot.userservice.feign.OrderClient;
import com.spring.boot.userservice.service.UserService;
import com.spring.boot.userservice.vo.OrderVO;
import com.spring.boot.userservice.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.spring.boot.commoncore.result.ResultCode.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/13 16:46
 */
@RestController
@RequestMapping("/user")
@Validated
@Slf4j
@Tag(name = "用户管理模块", description = "用户的增删改查、分页查询接口")
public class UserController {

	@Resource
	private UserService userService;

	@Resource
	private OrderClient orderClient;

	@Operation(summary = "新增用户",
			description = "传入用户基本信息，创建新用户并返回成功状态，若添加失败则返回10001。")
	@PostMapping("/add")
	public Result<Void> add(@RequestBody @Valid UserCreateDTO userCreateDTO) {

		log.info("【用户模块】添加新用户，请求参数：{}", userCreateDTO);

		userService.addUser(userCreateDTO);

		return Result.success("添加成功");
	}

	@Operation(summary = "根据ID删除用户",
			description = "传入用户ID，删除对应ID的用户，若用户不存在则返回10002。")
	@DeleteMapping("/delete/{id}")
	public Result<Void> delete(@PathVariable("id")
	                           @Parameter(
			                           description = "用户ID",
			                           example = "1")
	                           @Min(value = 1, message = "ID必须大于0")
	                           Long id) {

		log.info("【用户模块】删除用户，请求参数：{}", id);

		userService.deleteById(id);

		return Result.success("删除成功");
	}

	@Operation(summary = "更改用户信息",
			description = "传入用户ID和用户基本信息，更新对应ID的用户信息，若用户不存在则返回10002，若数据未改变则返回10003。")
	@PutMapping("/update")
	public Result<Void> update(@RequestBody @Valid UserUpdateDTO userUpdateDTO) {

		log.info("【用户模块】更新用户信息，请求参数：{}", userUpdateDTO.getUserId());

		userService.updateUser(userUpdateDTO);

		return Result.success("更新成功");
	}

	@Operation(summary = "根据ID查询用户",
			description = "传入用户ID，返回用户的详细信息（包括ID、姓名、年龄、邮箱、创建时间等），若用户不存在则返回10002。")
	@GetMapping("/get/{id}")
	public Result<UserVO> getById(@PathVariable("id")
	                              @Parameter(
			                              description = "用户ID",
			                              example = "1")
	                              @Min(value = 1, message = "ID必须大于0")
	                              Long id) {

		log.info("【用户模块】查询用户信息，请求参数：{}", id);

		UserVO userVO = userService.getById(id);

		return Result.success(userVO, "查询成功");
	}

	@Operation(summary = "分页查询用户信息",
			description = "传入分页参数和查询条件，返回分页后的用户列表，若用户不存在则返回空列表。")
	@GetMapping("/page")
	public Result<PageVO<UserVO>> getUserPage(@Valid UserQueryDTO query) {

		log.info("【用户模块】分页查询用户信息，请求参数：{}", query);

		PageVO<UserVO> pageVO = userService.getUserPage(query);

		return Result.success(pageVO, "查询成功");
	}

	@Operation(summary = "下订单",
			description = "传入订单信息，调用订单模块，创建订单。")
	@PostMapping("/order")
	@SentinelResource(value = "userCreateOrder",
			fallback = "userCreateOrderFallback",
			blockHandler = "userCreateOrderBlock")
	public Result<OrderVO> order(@RequestBody @Valid OrderCreateDTO orderCreateDTO) {

		log.info("【用户模块】接收到下单请求，userId={}, productId={}, num={}",
				orderCreateDTO.getUserId(),
				orderCreateDTO.getProductId(),
				orderCreateDTO.getNum());

		try {
			Result<OrderVO> result = orderClient.createOrder(orderCreateDTO);

			if (result == null || result.isFail() || result.getData() == null) {
				log.warn("【用户模块】下单失败，userId={}, productId={}, num={}",
						orderCreateDTO.getUserId(), orderCreateDTO.getProductId(), orderCreateDTO.getNum());
				throw BusinessException.of(FEIGN_ERROR, "下单失败，请稍后重试");
			}

			OrderVO orderVO = result.getData();
			log.info("【用户模块】下单成功，订单号：{}", orderVO.getOrderNo());

			return Result.success(orderVO, "下单成功");
		} catch (RuntimeException e) {
			Throwable cause = ExceptionUtil.unwind(e);
			if (cause instanceof BusinessException bizEx) {
				log.warn("【用户模块】业务异常：{}", bizEx.getMessage());
				return Result.fail(bizEx.getCode(), bizEx.getMessage());
			}
			throw e;
		}
	}
	public Result<OrderVO> userCreateOrderFallback(OrderCreateDTO dto, Throwable e) {
		Throwable cause = ExceptionUtil.unwind(e);
		// 业务异常：返回原来的错误码和提示
		if (cause instanceof BusinessException bizEx) {
			log.warn("【用户模块】业务异常：{}", bizEx.getMessage());
			return Result.fail(bizEx.getCode(), bizEx.getMessage());
		}
		// 系统异常：真正的降级
		log.error("【用户模块】下单降级，参数：{}，异常：{}", dto, cause.getMessage());
		return Result.fail(USER_DEGRADE, "下单失败，服务降级，请稍后重试");
	}
	public Result<OrderVO> userCreateOrderBlock(OrderCreateDTO dto, BlockException e) {
		log.warn("【用户模块】下单被限流/熔断，参数：{}", dto);
		return Result.fail(USER_FLOWING, "下单服务繁忙，请稍后重试");
	}
}
