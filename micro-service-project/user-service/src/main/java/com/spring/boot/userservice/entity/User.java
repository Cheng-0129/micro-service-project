package com.spring.boot.userservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/13 16:41
 */
@Data
@TableName("t_user")
public class User {

	@TableId(type = IdType.AUTO)
	private Long id;
	private Long userId;
	private String name;
	private String password;
	private Integer age;
	private String email;
	private Timestamp createTime;
	private Timestamp updateTime;
}
