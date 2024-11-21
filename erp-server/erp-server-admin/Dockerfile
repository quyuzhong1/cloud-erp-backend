# docker基础镜像
FROM 172.16.100.92:5000/library/java-8-docker:latest
MAINTAINER sdc-erp

# 设置工作目录
WORKDIR /app

# 更换为国内的镜像源，例如阿里云的源
RUN sed -i 's|http://archive.ubuntu.com/ubuntu|http://mirrors.aliyun.com/ubuntu|g' /etc/apt/sources.list && \
    sed -i 's|http://security.ubuntu.com/ubuntu|http://mirrors.aliyun.com/ubuntu|g' /etc/apt/sources.list && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y tzdata && \
    apt-get clean

# 设置时区与宿主机一致，假设宿主机的时区是 Asia/Shanghai
ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone

# 将构建的JAR文件复制到容器中的工作目录
COPY target/*jar /app/app.jar

# 运行Spring Boot应用
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
