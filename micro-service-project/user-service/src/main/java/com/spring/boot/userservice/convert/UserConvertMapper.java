package com.spring.boot.userservice.convert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.userservice.dto.UpdatePasswordDTO;
import com.spring.boot.userservice.dto.UserCreateDTO;
import com.spring.boot.userservice.dto.UserUpdateDTO;
import com.spring.boot.userservice.entity.User;
import com.spring.boot.userservice.vo.LoginVO;
import com.spring.boot.userservice.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
		unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConvertMapper {

	void fillUser(UserCreateDTO userCreateDTO, @MappingTarget User user);
	void fillUser(UserUpdateDTO userUpdateDTO, @MappingTarget User user);

	void fillUserVO(User user, @MappingTarget UserVO userVO);
	void fillLoginVO(User user, @MappingTarget LoginVO loginVO);

	default User toEntity(UserCreateDTO userCreateDTO) {
		if (userCreateDTO == null) return null;
		User user = new User();
		fillUser(userCreateDTO, user);
		return user;
	}
	default User toEntity(UserUpdateDTO userUpdateDTO) {
		if (userUpdateDTO == null) return null;
		User user = new User();
		fillUser(userUpdateDTO, user);
		return user;
	}

	default UserVO toUserVO(User user) {
		if (user == null) return null;
		UserVO userVO = new UserVO();
		fillUserVO(user, userVO);
		return userVO;
	}

	default LoginVO toLoginVO(User user) {
		if (user == null) return null;
		LoginVO loginVO = new LoginVO();
		fillLoginVO(user, loginVO);
		return loginVO;
	}

	default List<UserVO> toVOList(List<User> userList) {
		if (userList == null) return null;
		return userList.stream().map(this::toUserVO).collect(Collectors.toList());
	}
	default PageVO<UserVO> toPageVO(IPage<User> userPage) {
		if (userPage == null) return null;
		PageVO<UserVO> pageVO = new PageVO<>();
		pageVO.setCurrent(userPage.getCurrent());
		pageVO.setSize(userPage.getSize());
		pageVO.setTotal(userPage.getTotal());
		pageVO.setPages(userPage.getPages());
		pageVO.setRecords(toVOList(userPage.getRecords()));
		return pageVO;
	}
}
