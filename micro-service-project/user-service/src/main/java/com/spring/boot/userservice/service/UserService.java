package com.spring.boot.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.userservice.dto.UserCreateDTO;
import com.spring.boot.userservice.dto.UserQueryDTO;
import com.spring.boot.userservice.dto.UserUpdateDTO;
import com.spring.boot.userservice.entity.User;
import com.spring.boot.userservice.vo.UserVO;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/13 16:45
 */
public interface UserService extends IService<User> {

	void addUser(UserCreateDTO userCreateDTO);
	void deleteById(Long id);
	void updateUser(UserUpdateDTO userUpdateDTO);
	UserVO getById(Long id);
	PageVO<UserVO> getUserPage(UserQueryDTO query);
}
