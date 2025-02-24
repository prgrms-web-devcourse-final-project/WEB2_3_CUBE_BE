# 1. JDK 17이 포함된 경량 Linux 환경을 사용
FROM openjdk:17-jdk-slim

# 2. 작성자 정보
LABEL authors="jhyngu"

# 3. 작업 디렉토리 생성 (컨테이너 내부에서 실행할 폴더)
WORKDIR /app

# 4. JAR 파일을 컨테이너 내부로 복사
COPY build/libs/*.jar app.jar

COPY .env .

# 5. Spring Boot 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
