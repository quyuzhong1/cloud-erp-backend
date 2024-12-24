pipeline {
	agent any
	environment {
		harborUser = 'admin'
		harborPassword = 'Root@1234cn'
		harborAddress = '172.16.100.92:5000'
		harborRepo = 'sdc-erp'
	}

    stages {
        stage('拉取git仓库代码') {
            steps {
                checkout scmGit(branches: [[name: '${TAG}']], extensions: [], userRemoteConfigs: [[credentialsId: '7843edca-11b6-441e-8222-0d1f21ae600f', url: 'http://172.16.100.11:8993/erp-group/cloud-erp-backend.git']])
            }
        }
        stage('引用settings.xml') {
            steps {
                script {
                    // 获取 Managed File 中的 settings.xml 内容
                    def settingsXmlContent = managedFiles([file: 'settings.test'])[0].content

                    // 将 content 写入工作空间中的 settings.xml 文件
                    writeFile file: 'settings.xml', text: settingsXmlContent
                }
            }
        }
        stage('通过Maven构建项目') {
            steps {
                sh '/var/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/maven_3.5/bin/mvn -f pom.xml clean install -U -pl com.erp.server:erp-server-admin -am -Pdev -Dmaven.test.skip=true'
            }
        }
        stage('通过docker制作自定义镜像') {
            steps {
                sh '''ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-admin:${TAG}"
ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-admin/ && \\
    docker build -t ${harborAddress}/${harborRepo}/erp-server-admin:${TAG} ."'''
            }
        }
        stage('将自定义镜像推送到harbor') {
            steps {
                sh '''docker login -u ${harborUser} -p ${harborPassword} ${harborAddress}
docker push ${harborAddress}/${harborRepo}/erp-server-admin:${TAG}'''
            }
        }
        stage('远程执行k8s-master的kubectl命令') {
            steps {
                sh '''scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-admin/service.yaml root@172.16.100.60:/k8s-yaml/erp-server-admin
ssh -tt root@172.16.100.60 "sed -i \\"s|\\\\\\${tag}|${TAG}|g\\" /k8s-yaml/erp-server-admin/service.yaml"
ssh -tt root@172.16.100.60 "sed -i \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server-admin/service.yaml"
ssh -tt root@172.16.100.60 "/usr/bin/kubectl delete -f /k8s-yaml/erp-server-admin/service.yaml || true"
ssh -tt root@172.16.100.60 "/usr/bin/kubectl apply -f /k8s-yaml/erp-server-admin/service.yaml"'''
            }
        }

    }
}
