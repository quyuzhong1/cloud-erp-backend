FROM 172.16.100.90:5000/library/openjdk:8u342-jdk
MAINTAINER sdc-erp

# 设置工作目录
WORKDIR /app

# 安装必要工具：curl jq（轻量且无交互）
RUN yum -y install epel-release && \
    yum -y install jq curl && \
    yum clean all && \
    rm -rf /var/cache/yum

# 将构建的JAR文件复制到容器中的工作目录
COPY target/*jar /app/app.jar

# 运行Spring Boot应用
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]