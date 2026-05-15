package com.spring.boot.userservice.controller;

import com.spring.boot.commoncore.result.Result;
import com.spring.boot.userservice.convert.UserConvertMapper;
import com.spring.boot.userservice.dto.LoginDTO;
import com.spring.boot.userservice.dto.UserCreateDTO;
import com.spring.boot.userservice.entity.User;
import com.spring.boot.userservice.service.UserService;
import com.spring.boot.userservice.util.JwtUtil;
import com.spring.boot.userservice.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Tag(name = "用户认证", description = "用户的登录、注册接口")
public class AuthController {

	@Resource
	private UserService userService;

	@Resource
	private UserConvertMapper userConvertMapper;

	@Autowired
	private JwtUtil jwtUtil;


	@Operation(summary = "用户登录")
	@PostMapping("/login")
	public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
		log.info("【用户模块】开始执行用户登录，请求参数：{}", dto);
		User user = userService.login(dto.getUsername(), dto.getPassword());
		String token = jwtUtil.generateToken(user.getUserId());
		LoginVO vo = userConvertMapper.toLoginVO(user);
		vo.setToken(token);
		log.info("【用户模块】用户登录成功，用户名：{}, userId：{}", dto.getUsername(), vo.getUserId());
		return Result.success(vo, "登录成功");
	}

	@Operation(summary = "用户注册")
	@PostMapping("/register")
	public Result<Void> register(@RequestBody @Valid UserCreateDTO dto) {
		log.info("【用户模块】开始执行用户注册，请求参数：{}", dto);
		userService.addUser(dto);
		log.info("【用户模块】用户注册成功，用户名：{}", dto.getName());
		return Result.success("注册成功");
	}
}
