package com.spring.boot.userservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.spring.boot.commoncore.constant.FeignHeaders;
import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.commoncore.util.ExceptionUtil;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.userservice.dto.feign.OrderCreateFeignDTO;
import com.spring.boot.userservice.dto.UserCreateDTO;
import com.spring.boot.userservice.dto.UserQueryDTO;
import com.spring.boot.userservice.dto.UserUpdateDTO;
import com.spring.boot.userservice.feign.OrderClient;
import com.spring.boot.userservice.service.UserService;
import com.spring.boot.userservice.vo.feign.OrderFeignVO;
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

import static com.spring.boot.commoncore.result.ResultCode.FEIGN_ERROR;

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
			description = "创建新用户，成功返回操作成功，失败返回 10001（用户添加失败）")
	@PostMapping("/add")
	public Result<Void> add(@RequestBody @Valid UserCreateDTO userCreateDTO) {

		log.info("【用户模块】添加新用户，请求参数：{}", userCreateDTO);

		userService.addUser(userCreateDTO);

		return Result.success("添加成功");
	}

	@Operation(summary = "删除用户",
			description = "根据用户ID删除用户，成功返回操作成功，用户不存在则返回 10002（用户不存在）")
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

	@Operation(summary = "修改用户信息",
			description = "根据用户ID更新用户信息，成功返回操作成功，用户不存在返回 10002，数据未变更返回 10003")
	@PutMapping("/update")
	public Result<Void> update(@RequestBody @Valid UserUpdateDTO userUpdateDTO) {

		log.info("【用户模块】更新用户信息，请求参数：{}", userUpdateDTO.getUserId());

		userService.updateUser(userUpdateDTO);

		return Result.success("更新成功");
	}

	@Operation(summary = "查询用户详情",
			description = "根据用户ID查询用户详细信息，包含ID、姓名、年龄、邮箱、创建时间，用户不存在返回 10002")
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

	@Operation(summary = "分页查询用户",
			description = "支持按条件分页查询用户列表，无匹配数据返回空列表")
	@GetMapping("/page")
	public Result<PageVO<UserVO>> getUserPage(@Valid UserQueryDTO query) {

		log.info("【用户模块】分页查询用户信息，请求参数：{}", query);

		PageVO<UserVO> pageVO = userService.getUserPage(query);

		return Result.success(pageVO, "查询成功");
	}

	@Operation(summary = "下订单",
			description = "传入用户ID、商品ID、数量，调用订单模块创建订单并扣减库存。" +
					"下单失败返回 1002（远程调用失败），触发熔断/限流返回 10004/10005")
	@PostMapping("/order")
	@SentinelResource(
			value = "userOrder",                                     // 资源名称
			blockHandler = "handleBlock",                        // 限流/降级后的处理方法
			blockHandlerClass = UserBlockHandler.class,          // 降级处理类
			fallback = "handleFallback",                         // 业务异常后的兜底方法
			fallbackClass = UserBlockHandler.class)              // 兜底处理类
	public Result<OrderFeignVO> order(@RequestBody @Valid OrderCreateFeignDTO orderCreateFeignDTO) {

		log.info("【用户模块】接收到下单请求，userId={}, productId={}, num={}",
				orderCreateFeignDTO.getUserId(),
				orderCreateFeignDTO.getProductId(),
				orderCreateFeignDTO.getNum());

		try {
			Result<OrderFeignVO> result = orderClient.createOrder(orderCreateFeignDTO, FeignHeaders.SOURCE_USER_SERVICE);

			if (result == null || result.isFail() || result.getData() == null) {
				log.warn("【用户模块】下单失败，userId={}, productId={}, num={}",
						orderCreateFeignDTO.getUserId(), orderCreateFeignDTO.getProductId(), orderCreateFeignDTO.getNum());
				throw BusinessException.of(FEIGN_ERROR, "下单失败，请稍后重试");
			}

			OrderFeignVO orderFeignVO = result.getData();
			log.info("【用户模块】下单成功，订单号：{}", orderFeignVO.getOrderNo());

			return Result.success(orderFeignVO, "下单成功");
		} catch (RuntimeException e) {
			Throwable cause = ExceptionUtil.unwind(e);
			if (cause instanceof BusinessException bizEx) {
				log.warn("【用户模块】业务异常：{}", bizEx.getMessage());
				return Result.fail(bizEx.getCode(), bizEx.getMessage());
			}
			throw e;
		}
	}
}
