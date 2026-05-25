package com.spring.boot.userservice.controller;

import com.spring.boot.commoncore.result.Result;
import com.spring.boot.userservice.convert.UserConvertMapper;
import com.spring.boot.userservice.dto.LoginDTO;
import com.spring.boot.userservice.dto.TokenPair;
import com.spring.boot.userservice.dto.UserCreateDTO;
import com.spring.boot.userservice.entity.User;
import com.spring.boot.userservice.service.UserService;
import com.spring.boot.userservice.util.JwtUtil;
import com.spring.boot.userservice.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/14 11:33
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/user")
@Tag(name = "用户认证", description = "用户的登录、注册、登出接口")
public class AuthController {

	@Resource
	private UserService userService;

	@Resource
	private UserConvertMapper userConvertMapper;

	@Autowired
	private JwtUtil jwtUtil;


	@Operation(summary = "用户登录",
			description = "验证用户名和密码，成功返回用户信息和JWT令牌，用户不存在返回 10002，密码错误返回 10006")
	@PostMapping("/login")
	public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
		log.info("【用户模块】开始执行用户登录，请求参数：{}", dto);
		User user = userService.login(dto.getUsername(), dto.getPassword());
		// ✅ 生成双 Token
		String accessToken = jwtUtil.generateAccessToken(user.getUserId());
		String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

		LoginVO vo = userConvertMapper.toLoginVO(user);
		vo.setAccessToken(accessToken);
		vo.setRefreshToken(refreshToken);

		log.info("【用户模块】用户登录成功，用户名：{}, userId：{}", dto.getUsername(), vo.getUserId());
		return Result.success(vo, "登录成功");
	}

	@Operation(summary = "用户注册",
			description = "创建新用户账号，成功返回操作成功，用户名已存在返回 10007，添加失败返回 10004")
	@PostMapping("/register")
	public Result<Void> register(@RequestBody @Valid UserCreateDTO dto) {
		log.info("【用户模块】开始执行用户注册，请求参数：{}", dto);
		userService.addUser(dto);
		log.info("【用户模块】用户注册成功，用户名：{}", dto.getName());
		return Result.success("注册成功");
	}

	@Operation(summary = "用户登出",
			description = "将当前Token加入黑名单，使其立即失效")
	@PostMapping("/logout")
	public Result<Void> logout(
			@RequestHeader("Authorization") String accessToken,
			@RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken
	) {
		log.info("【用户模块】用户登出");

		jwtUtil.blacklistToken(accessToken);
		if (refreshToken != null && !refreshToken.isBlank()) {
			jwtUtil.removeRefreshToken(refreshToken);
		}

		log.info("【用户模块】用户登出成功，Token已加入黑名单");
		return Result.success("登出成功");
	}

	@Operation(summary = "刷新 Token",
			description = "使用 Refresh Token 获取新的 Access Token 和 Refresh Token（旧 Refresh Token 失效）")
	@PostMapping("/refresh")
	public Result<TokenPair> refreshToken(@RequestHeader("Authorization") String refreshToken) {
		log.info("【用户模块】开始刷新 Token");

		TokenPair newTokens = jwtUtil.refreshToken(refreshToken);
		log.info("【用户模块】Token 刷新成功");
		return Result.success(newTokens, "Token 刷新成功");
	}

	@Operation(summary = "注销所有设备",
			description = "作废该用户的所有Token，强制所有设备重新登录。需要有效的Access Token")
	@PostMapping("/logout/all")
	public Result<Void> logoutAllDevices(@RequestHeader("Authorization") String accessToken,
	                                     @Parameter(hidden = true) @RequestHeader("X-UserId") Long userId) {
		log.info("【用户模块】用户 {} 执行全局登出", userId);

		// 1. 将当前 Access Token 加入黑名单
		jwtUtil.blacklistToken(accessToken);

		// 2. 作废该用户的所有 Refresh Token
		jwtUtil.blacklistAllUserTokens(userId);

		log.info("【用户模块】用户 {} 全局登出成功", userId);
		return Result.success("所有设备已登出");
	}

}
