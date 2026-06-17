package com.spring.boot.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.commonweb.component.IdGenerator;
import com.spring.boot.userservice.convert.UserConvertMapper;
import com.spring.boot.userservice.dto.UpdatePasswordDTO;
import com.spring.boot.userservice.dto.UserCreateDTO;
import com.spring.boot.userservice.dto.UserQueryDTO;
import com.spring.boot.userservice.dto.UserUpdateDTO;
import com.spring.boot.userservice.entity.User;
import com.spring.boot.userservice.mapper.UserMapper;
import com.spring.boot.userservice.service.UserService;
import com.spring.boot.userservice.vo.UserVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.spring.boot.commoncore.result.ResultCode.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/13 16:45
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

	@Resource
	private UserConvertMapper userConvertMapper;

	@Resource
	private UserMapper userMapper;

	@Autowired
	@Qualifier("userIdGenerator")
	private IdGenerator userIdGenerator;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Transactional
	public void addUser(UserCreateDTO userCreateDTO) {

		log.info("【用户模块】开始用户注册/创建，请求参数：{}", userCreateDTO);

		Long count = userMapper.selectCount(
				new LambdaQueryWrapper<User>().eq(User::getName, userCreateDTO.getName())
		);
		if (count > 0) {
			log.warn("【用户模块】添加用户失败，用户已存在，用户名：{}", userCreateDTO.getName());
			throw BusinessException.of(USER_EXIST);
		}

		User user = userConvertMapper.toEntity(userCreateDTO);

		Long userId = userIdGenerator.nextId();

		user.setUserId(userId);
		user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
		user.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
		user.setUpdateTime(Timestamp.valueOf(LocalDateTime.now()));

		int insert = baseMapper.insert(user);

		if (insert > 0) {
			log.info("【用户模块】新增用户成功，业务ID：{}，数据库影响行数：{}", userId, insert);
		} else {
			log.warn("【用户模块】新增用户失败，未插入数据，请求参数：{}", userCreateDTO);
			throw BusinessException.of(USER_ADD_FAILED);
		}
	}

	public void deleteById(Long id) {

		log.info("【用户模块】开始执行删除用户，请求参数：{}", id);

		int delete = baseMapper.deleteById(id);

		if (delete > 0) {
			log.info("【用户模块】删除用户成功，响应结果：{}，删除行数：{}", id, delete);
		} else {
			log.warn("【用户模块】删除用户失败，用户不存在，请求参数：{}", id);
			throw BusinessException.of(USER_NOT_EXIST);
		}
	}

	public void updateUser(UserUpdateDTO userUpdateDTO) {
		log.info("【用户模块】开始执行更新用户信息，请求参数：{}", userUpdateDTO);

		User oldUser = baseMapper.selectById(userUpdateDTO.getUserId());
		if (oldUser == null) {
			log.warn("【用户模块】更新用户信息失败，用户不存在：{}", userUpdateDTO.getUserId());
			throw BusinessException.of(USER_NOT_EXIST);
		}

		boolean hasChange = hasFieldChanged(userUpdateDTO.getName(), oldUser.getName()) ||
				hasFieldChanged(userUpdateDTO.getAge(), oldUser.getAge()) ||
				hasFieldChanged(userUpdateDTO.getEmail(), oldUser.getEmail());

		if (!hasChange) {
			log.warn("【用户模块】更新用户信息：本次提交数据与数据库一致，未产生数据变更，用户ID：{}，请求参数：{}", userUpdateDTO.getUserId(), userUpdateDTO);
			throw BusinessException.of(DATA_NO_CHANGE);
		}

		User newUser = userConvertMapper.toEntity(userUpdateDTO);
		newUser.setId(oldUser.getId());
		newUser.setUpdateTime(Timestamp.valueOf(LocalDateTime.now()));

		baseMapper.updateById(newUser);

		log.info("【用户模块】更新用户信息成功，响应结果：{}", userUpdateDTO.getUserId());
	}

	public void updatePassword(UpdatePasswordDTO updatePasswordDTO, Long userId) {

		log.info("【用户模块】开始执行更新用户密码，请求参数：{}", updatePasswordDTO);

		User existingUser = userMapper.selectOne(
						new LambdaQueryWrapper<User>()
								.select(User::getPassword, User::getId)
								.eq(User::getUserId, userId));

		// 2. 判空
		if (existingUser == null || existingUser.getPassword() == null) {
			log.warn("【用户模块】更新用户密码失败，用户不存在，用户ID：{}", userId);
			throw BusinessException.of(USER_NOT_EXIST);
		}

		String oldPassword = existingUser.getPassword();

		if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), oldPassword)) {
			log.warn("【用户模块】更新用户密码失败，密码错误，用户ID：{}", userId);
			throw BusinessException.of(USER_PASSWORD_ERROR);
		}

		if (updatePasswordDTO.getOldPassword().equals(updatePasswordDTO.getNewPassword())) {
			log.warn("【用户模块】更新用户密码失败，新密码不能与旧密码一致，用户ID：{}，请求参数：{}", userId, updatePasswordDTO);
			throw BusinessException.of(USER_PASSWORD_SAME);
		}

		User user = new User();
		user.setId(existingUser.getId());
		user.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
		user.setUpdateTime(Timestamp.valueOf(LocalDateTime.now()));

		baseMapper.updateById(user);

		log.info("【用户模块】更新用户密码成功，用户ID：{}", userId);
	}

	public UserVO getById(Long id) {

		log.info("【用户模块】开始执行查询用户信息，请求参数：{}", id);

		LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
				.select(
						User::getUserId,
						User::getName,
						User::getAge,
						User::getEmail,
						User::getCreateTime,
						User::getUpdateTime
				)
				.eq(User::getUserId, id);

		User user = baseMapper.selectOne(wrapper);

		if (user == null) {
			log.warn("【用户模块】用户信息查询失败，用户不存在，请求参数：{}", id);
			throw BusinessException.of(USER_NOT_EXIST, "用户[ " + id + " ]不存在");
		}

		UserVO userVO = userConvertMapper.toUserVO(user);

		log.info("【用户模块】用户信息查询成功，响应结果：{}", userVO);

		return userVO;
	}

	@Override
	public PageVO<UserVO> getUserPage(UserQueryDTO query) {

		log.info("【用户模块】开始执行分页查询用户信息，请求参数：{}", query);
		Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());

		IPage<User> userPage = userMapper.selectUserPage(page, query);

		PageVO<UserVO> pageVO = userConvertMapper.toPageVO(userPage);


		if (pageVO.getRecords().isEmpty()) {
			log.warn("【用户模块】分页查询无匹配用户数据");
		}

		log.info("【用户模块】分页查询用户信息成功，总条数：{}", userPage.getTotal());
		return pageVO;
	}

	@Override
	public User login(String username, String password) {

		log.info("【用户模块】开始执行用户登录，请求参数：username={}", username);
		User user = userMapper.selectOne(
				new LambdaQueryWrapper<User>().eq(User::getName, username)
		);
		if (user == null) {
			log.warn("【用户模块】用户登录失败，用户不存在，用户名：{}", username);
			throw BusinessException.of(USER_NOT_EXIST);
		}
		if (!passwordEncoder.matches(password, user.getPassword())) {
			log.warn("【用户模块】用户登录失败，密码错误，用户名：{}", username);
			throw BusinessException.of(USER_PASSWORD_ERROR);
		}
		return user;
	}

	// 工具方法
	private boolean hasFieldChanged(Object newValue, Object oldValue) {
		return newValue != null && !Objects.equals(newValue, oldValue);
	}
}
