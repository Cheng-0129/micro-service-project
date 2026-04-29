package com.spring.boot.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spring.boot.userservice.dto.UserQueryDTO;
import com.spring.boot.userservice.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/13 16:44
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

	IPage<User> selectUserPage(Page<User> page, @Param("query")UserQueryDTO query);
}
