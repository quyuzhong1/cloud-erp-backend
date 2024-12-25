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
        stage('maven部署项目') {
            steps {
                script {
                    configFileProvider([configFile(fileId: '06ffcde1-6631-4338-a346-9b040decb468', variable: 'MY_SETTINGS_XML')]) {
                        withEnv(["JAVA_HOME=/var/jenkins_home/tools/jdk1.8.0_301"]) {
                            sh "${JAVA_HOME}/bin/java -version"
                            //打包erp-server-admin里的pom.xml
                            sh "/var/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/maven_3.5/bin/mvn -s ${env.MY_SETTINGS_XML} clean install -U -pl com.erp.server:erp-server-admin -am -Pdev -Dmaven.test.skip=true"

                            //打包erp-server-k8s里的pom.xml
                            sh "/var/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/maven_3.5/bin/mvn -s ${env.MY_SETTINGS_XML} clean install -U -pl com.erp.server:erp-server-k8s -am -Pdev -Dmaven.test.skip=true"
                        }
                    }
                }
            }
        }
        stage('通过docker制作自定义镜像') {
            steps {
                sh '''
                //打包erp-server-admin里的pom.xml
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-admin/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-admin:${TAG} ."       //制作erp-server-k8s镜像

                //制作erp-server-k8s镜像
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-k8s/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-k8s:${TAG} ."
                '''
            }
        }
        stage('将自定义镜像推送到harbor') {
            steps {
                sh '''docker login -u ${harborUser} -p ${harborPassword} ${harborAddress}
                //上传erp-server-admin镜像
                docker push ${harborAddress}/${harborRepo}/erp-server-admin:${TAG}

                //上传erp-server-k8s镜像
                docker push ${harborAddress}/${harborRepo}/erp-server-k8s:${TAG}
                '''
            }
        }
        stage('远程执行k8s-master的kubectl命令') {
            steps {
                sh '''
                    //运行erp-server-admin命令
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-admin/service.yaml root@172.16.100.60:/k8s-yaml/erp-server-admin
                    ssh -tt root@172.16.100.60 "sed -i \\"s|\\\\\\${tag}|${TAG}|g\\" /k8s-yaml/erp-server-admin/service.yaml"
                    ssh -tt root@172.16.100.60 "sed -i \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server-admin/service.yaml"
                    ssh -tt root@172.16.100.60 "/usr/bin/kubectl delete -f /k8s-yaml/erp-server-admin/service.yaml || true"
                    ssh -tt root@172.16.100.60 "/usr/bin/kubectl apply -f /k8s-yaml/erp-server-admin/service.yaml"

                    //运行erp-server-k8s命令
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-k8s/service.yaml root@172.16.100.60:/k8s-yaml/erp-server-k8s
                    ssh -tt root@172.16.100.60 "sed -i \\"s|\\\\\\${tag}|${TAG}|g\\" /k8s-yaml/erp-server-k8s/service.yaml"
                    ssh -tt root@172.16.100.60 "sed -i \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server-k8s/service.yaml"
                    ssh -tt root@172.16.100.60 "/usr/bin/kubectl delete -f /k8s-yaml/erp-server-k8s/service.yaml || true"
                    ssh -tt root@172.16.100.60 "/usr/bin/kubectl apply -f /k8s-yaml/erp-server-k8s/service.yaml"
                    '''
            }
        }
    }
}
