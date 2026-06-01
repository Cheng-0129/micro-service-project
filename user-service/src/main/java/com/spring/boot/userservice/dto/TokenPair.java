package com.spring.boot.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/25 09:52
 */
@Data
@AllArgsConstructor
public class TokenPair {
	private String accessToken;
	private String refreshToken;
}
