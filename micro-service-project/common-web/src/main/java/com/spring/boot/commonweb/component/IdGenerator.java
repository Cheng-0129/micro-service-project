package com.spring.boot.commonweb.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/15 09:40
 */
@Slf4j
public class IdGenerator {

	private final JdbcTemplate jdbcTemplate;
	private final String tableName;
	private final int step;

	private long currentId;   // 当前发到哪个号
	private long endId;       // 这批号的终点
	private final Object lock = new Object();

	public IdGenerator(JdbcTemplate jdbcTemplate, String tableName, int step) {
		this.jdbcTemplate = jdbcTemplate;
		this.tableName = tableName;
		this.step = step;
		this.currentId = 0;
		this.endId = 0;
	}

	public Long nextId() {
		synchronized (lock) {
			if (currentId >= endId) {
				fetchBatch();
			}
			return currentId++;
		}
	}

	private void fetchBatch() {
		log.debug("开始获取号段，tableName={}, step={}", tableName, step);
		Long startId = jdbcTemplate.queryForObject(
				"UPDATE biz_id_counter " +
						"SET current_max_id = current_max_id + ? " +
						"WHERE table_name = ? " +
						"RETURNING current_max_id",
				Long.class,
				step,
				tableName
		);
// startId 是更新后的 current_max_id（已经是加完 step 的了）
// 起点 = startId - step + 1
		long realStartId = startId - step + 1;
		this.currentId = realStartId;
		this.endId = realStartId + step;
		log.debug("号段获取成功，tableName={}, startId={}, endId={}", tableName, startId, endId - 1);
	}
}
