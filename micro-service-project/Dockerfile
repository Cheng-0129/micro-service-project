# Dockerfile（项目根目录）
FROM eclipse-temurin:17-jre-alpine AS builder
WORKDIR /build
# app.jar 由 CI 流水线从对应服务的 target 目录复制过来
COPY app.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 分层复制（利用 Docker 缓存加速）
COPY --from=builder /build/dependencies/ ./
COPY --from=builder /build/spring-boot-loader/ ./
COPY --from=builder /build/snapshot-dependencies/ ./
COPY --from=builder /build/application/ ./

USER appuser

# 各服务端口不同，由 docker-compose 映射
EXPOSE 8080

STOPSIGNAL SIGTERM

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-XX:+UseZGC", \
            "org.springframework.boot.loader.launch.JarLauncher"]