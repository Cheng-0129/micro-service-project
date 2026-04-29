package com.spring.boot.commoncore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/27 10:44
 */
@Schema(description = "分页查询返回对象")
@Data
public class PageVO<T> {

	@Schema(description = "当前页码", example = "1")
	private Long current;

	@Schema(description = "每页条数", example = "10")
	private Long size;

	@Schema(description = "总记录数", example = "50")
	private Long total;

	@Schema(description = "总页数", example = "5")
	private Long pages;

	@Schema(description = "当前页数据列表")
	private List<T> records;
}
