# syntax=docker/dockerfile:1.4
# 多阶段构建：容器内 Maven 编译 sample，无需宿主机安装 Maven

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 依赖层缓存：先复制 pom，源码变更时不重复拉依赖
COPY pom.xml .
COPY company-component-bom/pom.xml company-component-bom/
COPY common-exception-autoconfigure/pom.xml common-exception-autoconfigure/
COPY common-exception-spring-boot-starter/pom.xml common-exception-spring-boot-starter/
COPY common-auth-autoconfigure/pom.xml common-auth-autoconfigure/
COPY common-auth-spring-boot-starter/pom.xml common-auth-spring-boot-starter/
COPY common-auth-login-redis-autoconfigure/pom.xml common-auth-login-redis-autoconfigure/
COPY common-auth-login-redis-spring-boot-starter/pom.xml common-auth-login-redis-spring-boot-starter/
COPY common-log-autoconfigure/pom.xml common-log-autoconfigure/
COPY common-log-spring-boot-starter/pom.xml common-log-spring-boot-starter/
COPY common-dict-autoconfigure/pom.xml common-dict-autoconfigure/
COPY common-dict-spring-boot-starter/pom.xml common-dict-spring-boot-starter/
COPY company-component-samples/pom.xml company-component-samples/
COPY company-component-samples/sample-boot-app/pom.xml company-component-samples/sample-boot-app/

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B dependency:go-offline -pl company-component-samples/sample-boot-app -am -DskipTests \
    || mvn -B dependency:resolve -pl company-component-samples/sample-boot-app -am -DskipTests \
    || true

COPY . .

ARG SKIP_TESTS=1
RUN --mount=type=cache,target=/root/.m2 \
    if [ "${SKIP_TESTS}" = "1" ]; then \
      mvn -B package -pl company-component-samples/sample-boot-app -am -DskipTests; \
    else \
      mvn -B package -pl company-component-samples/sample-boot-app -am; \
    fi \
    && JAR="$(ls company-component-samples/sample-boot-app/target/sample-boot-app-*.jar | grep -v original | head -1)" \
    && cp "${JAR}" /build/app.jar

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /build/app.jar /app/app.jar

EXPOSE 18080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
