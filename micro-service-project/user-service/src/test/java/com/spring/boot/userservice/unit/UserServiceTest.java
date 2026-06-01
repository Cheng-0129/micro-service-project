package com.spring.boot.userservice.unit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spring.boot.commoncore.result.*;
import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commonweb.component.IdGenerator;
import com.spring.boot.userservice.convert.UserConvertMapper;
import com.spring.boot.userservice.dto.UserCreateDTO;
import com.spring.boot.userservice.dto.UserUpdateDTO;
import com.spring.boot.userservice.entity.User;
import com.spring.boot.userservice.mapper.UserMapper;
import com.spring.boot.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/26 11:09
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {

	@Mock
	private UserMapper userMapper;

	@Mock
	private UserConvertMapper userConvertMapper;

	@Mock
	private IdGenerator userIdGenerator;

	@Mock
	private PasswordEncoder passwordEncoder;

	private UserServiceImpl userService;

	// ============ 通用测试数据 ============
	private UserCreateDTO createDTO;
	private User mockUser;
	private static final Long TEST_USER_ID = 123456789L;
	private static final String RAW_PASSWORD = "123456";
	private static final String ENCODED_PASSWORD = "$2a$10$encoded";

	@BeforeEach
	void setUp() {
		// 手动创建实例，避免 @InjectMocks 对 MyBatis-Plus 父类处理不完整
		userService = new UserServiceImpl();

		// 通过反射把 Mock 注入到 Service 中
		ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
		ReflectionTestUtils.setField(userService, "userMapper", userMapper);
		ReflectionTestUtils.setField(userService, "userConvertMapper", userConvertMapper);
		ReflectionTestUtils.setField(userService, "userIdGenerator", userIdGenerator);
		ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);

		createDTO = new UserCreateDTO();
		createDTO.setName("testUser");
		createDTO.setPassword(RAW_PASSWORD);
		createDTO.setAge(25);
		createDTO.setEmail("test@example.com");

		mockUser = new User();
		mockUser.setUserId(TEST_USER_ID);
		mockUser.setName("testUser");
		mockUser.setPassword(ENCODED_PASSWORD);
		mockUser.setAge(25);
		mockUser.setEmail("test@example.com");
	}

	// ============ addUser 测试 ============
	@Nested
	@DisplayName("addUser - 用户注册")
	class AddUserTests {

		@Test
		@DisplayName("正常注册 → 密码被加密存储")
		void shouldEncodePasswordWhenAddUser() {
			when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
			when(userIdGenerator.nextId()).thenReturn(TEST_USER_ID);
			when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
			when(userConvertMapper.toEntity(any(UserCreateDTO.class))).thenReturn(mockUser);
			when(userMapper.insert(any(User.class))).thenReturn(1);

			userService.addUser(createDTO);

			verify(passwordEncoder).encode(RAW_PASSWORD);
			ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
			verify(userMapper).insert(userCaptor.capture());
			assertThat(userCaptor.getValue().getPassword()).isEqualTo(ENCODED_PASSWORD);
		}

		@Test
		@DisplayName("用户名已存在 → 抛出 USER_EXIST 异常")
		void shouldThrowWhenUserExists() {
			when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

			assertThatThrownBy(() -> userService.addUser(createDTO))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.USER_EXIST.getCode());
			verify(userMapper, never()).insert(any(User.class));
		}

		@Test
		@DisplayName("插入返回0 → 抛出 USER_ADD_FAILED 异常")
		void shouldThrowWhenInsertFails() {
			when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
			when(userIdGenerator.nextId()).thenReturn(TEST_USER_ID);
			when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
			when(userConvertMapper.toEntity(any(UserCreateDTO.class))).thenReturn(mockUser);
			when(userMapper.insert(any(User.class))).thenReturn(0);

			assertThatThrownBy(() -> userService.addUser(createDTO))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.USER_ADD_FAILED.getCode());
		}
	}

	// ============ login 测试 ============
	@Nested
	@DisplayName("login - 用户登录")
	class LoginTests {

		@Test
		@DisplayName("用户名密码正确 → 返回用户信息")
		void shouldReturnUserWhenLoginSuccess() {
			when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
			when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

			User result = userService.login("testUser", RAW_PASSWORD);

			assertThat(result).isNotNull();
			assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
			assertThat(result.getName()).isEqualTo("testUser");
		}

		@Test
		@DisplayName("用户不存在 → 抛出 USER_NOT_EXIST 异常")
		void shouldThrowWhenUserNotFound() {
			when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

			assertThatThrownBy(() -> userService.login("unknown", RAW_PASSWORD))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.USER_NOT_EXIST.getCode());
		}

		@Test
		@DisplayName("密码错误 → 抛出 USER_PASSWORD_ERROR 异常")
		void shouldThrowWhenPasswordWrong() {
			when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
			when(passwordEncoder.matches("wrongPassword", ENCODED_PASSWORD)).thenReturn(false);

			assertThatThrownBy(() -> userService.login("testUser", "wrongPassword"))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.USER_PASSWORD_ERROR.getCode());
		}
	}

	// ============ deleteById 测试 ============
	@Nested
	@DisplayName("deleteById - 删除用户")
	class DeleteByIdTests {

		@Test
		@DisplayName("用户存在 → 删除成功")
		void shouldDeleteWhenUserExists() {
			when(userMapper.deleteById(TEST_USER_ID)).thenReturn(1);

			userService.deleteById(TEST_USER_ID);

			verify(userMapper).deleteById(TEST_USER_ID);
		}

		@Test
		@DisplayName("用户不存在 → 抛出 USER_NOT_EXIST")
		void shouldThrowWhenUserNotFound() {
			when(userMapper.deleteById(TEST_USER_ID)).thenReturn(0);

			assertThatThrownBy(() -> userService.deleteById(TEST_USER_ID))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.USER_NOT_EXIST.getCode());
		}
	}

	// ============ updateUser 测试 ============
	@Nested
	@DisplayName("updateUser - 更新用户信息")
	class UpdateUserTests {

		@Test
		@DisplayName("用户存在且有变更 → 更新成功")
		void shouldUpdateWhenDataChanged() {
			UserUpdateDTO dto = new UserUpdateDTO();
			dto.setUserId(TEST_USER_ID);
			dto.setName("newName");
			dto.setAge(30);

			when(userMapper.selectById(TEST_USER_ID)).thenReturn(mockUser);
			when(userConvertMapper.toEntity(dto)).thenReturn(new User());
			when(userMapper.updateById(any(User.class))).thenReturn(1);

			userService.updateUser(dto);

			verify(userMapper).updateById(any(User.class));
		}

		@Test
		@DisplayName("数据无变更 → 抛出 DATA_NO_CHANGE")
		void shouldThrowWhenNoDataChanged() {
			UserUpdateDTO dto = new UserUpdateDTO();
			dto.setUserId(TEST_USER_ID);
			dto.setName("testUser");
			dto.setAge(25);
			dto.setEmail("test@example.com");

			when(userMapper.selectById(TEST_USER_ID)).thenReturn(mockUser);

			assertThatThrownBy(() -> userService.updateUser(dto))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.DATA_NO_CHANGE.getCode());
			verify(userMapper, never()).updateById(any(User.class));
		}
	}
}
