FROM 172.16.100.90:5000/library/openjdk:8u342-jdk
MAINTAINER sdc-erp

# 设置工作目录
WORKDIR /app

# 将构建的JAR文件复制到容器中的工作目录
COPY target/*jar /app/app.jar

# 运行Spring Boot应用
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]