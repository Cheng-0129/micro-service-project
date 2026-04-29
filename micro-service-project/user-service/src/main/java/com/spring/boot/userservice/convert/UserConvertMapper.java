package com.spring.boot.userservice.convert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.userservice.dto.UserCreateDTO;
import com.spring.boot.userservice.dto.UserUpdateDTO;
import com.spring.boot.userservice.entity.User;
import com.spring.boot.userservice.vo.UserVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserConvertMapper {

	User toEntity(UserCreateDTO userCreateDTO);
	User toEntity(UserUpdateDTO userUpdateDTO);

	UserVO toVO(User user);
	List<UserVO> toVOList(List<User> userList);
	PageVO<UserVO> toPageVO(IPage<User> userPage);
}
