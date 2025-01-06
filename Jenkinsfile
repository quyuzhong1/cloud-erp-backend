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
                            sh "/var/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/maven_3.5/bin/mvn -s ${env.MY_SETTINGS_XML} clean package -T 8C -U '-Dmaven.test.skip=true'"
                        }
                    }
                }
            }
        }
        stage('通过docker制作自定义镜像') {
            steps {
                sh '''
                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-admin:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-admin/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-admin:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-k8s:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-k8s/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-k8s:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-bi:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-bi/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-bi:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-auth:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-auth/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-auth:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-dmp:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-dmp/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-dmp:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-file:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-file/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-file:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-mrp:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-mrp/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-mrp:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-msg:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-msg/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-msg{TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-oms:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-oms/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-oms:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-plm:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-plm/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-plm:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-scm:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-scm/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-scm:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-srm:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-srm/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-srm:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-sys:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-sys/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-sys:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-tms:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-tms/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-tms:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-wms:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-wms/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-wms:${TAG} ."

                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-server-workflow:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-workflow/ && \\
                docker build -t ${harborAddress}/${harborRepo}/erp-server-workflow:${TAG} ."
                '''
            }
        }
        stage('将自定义镜像推送到harbor') {
            steps {
                sh '''docker login -u ${harborUser} -p ${harborPassword} ${harborAddress}
                docker push ${harborAddress}/${harborRepo}/erp-server-admin:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-k8s:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-bi:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-auth:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-dmp:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-file:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-mrp:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-msg:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-oms:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-plm:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-scm:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-srm:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-sys:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-tms:${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-wms${TAG}
                docker push ${harborAddress}/${harborRepo}/erp-server-workflow:${TAG}
                '''
            }
        }
        stage('远程执行k8s-master的kubectl命令') {
            steps {
                sh '''
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-admin/erp-server-admin.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-k8s/erp-server-k8s.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-auth/erp-server-auth.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-dmp/erp-server-dmp.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-file/erp-server-file.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-mrp/erp-server-mrp.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-msg/erp-server-msg.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-oms/erp-server-oms.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-plm/erp-server-plm.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-scm/erp-server-scm.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-srm/erp-server-srm.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-sys/erp-server-sys.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-tms/erp-server-tms.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-wms/erp-server-wms.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-workflow/erp-server-workflow.yaml root@172.16.100.60:/k8s-yaml/erp-server
                    scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/erp-server-bi/erp-server-bi.yaml root@172.16.100.60:/k8s-yaml/erp-server

                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-admin.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-k8s.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-auth.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-dmp.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-file.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-mrp.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-msg.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-oms.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-plm.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-scm.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-srm.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-sys.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-tms.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-wms.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-workflow.yaml"
                    ssh -tt root@172.16.100.60 "sed -i -e \\"s|\\\\\\${tag}|${TAG}|g\\" -e \\"s|\\\\\\${namespace}|${NAMESPACE}|g\\" /k8s-yaml/erp-server/erp-server-bi.yaml"

                    ssh -tt root@172.16.100.60 "/usr/bin/kubectl delete -f /k8s-yaml/erp-server/erp-server-admin.yaml -f /k8s-yaml/erp-server/erp-server-k8s.yaml -f /k8s-yaml/erp-server/erp-server-bi.yaml -f /k8s-yaml/erp-server/erp-server-auth.yaml -f /k8s-yaml/erp-server/erp-server-dmp.yaml -f /k8s-yaml/erp-server/erp-server-file.yaml -f /k8s-yaml/erp-server/erp-server-mrp.yaml -f /k8s-yaml/erp-server/erp-server-msg.yaml -f /k8s-yaml/erp-server/erp-server-oms.yaml -f /k8s-yaml/erp-server/erp-server-plm.yaml -f /k8s-yaml/erp-server/erp-server-scm.yaml -f /k8s-yaml/erp-server/erp-server-srm.yaml -f /k8s-yaml/erp-server/erp-server-sys.yaml -f /k8s-yaml/erp-server/erp-server-tms.yaml -f /k8s-yaml/erp-server/erp-server-wms.yaml -f /k8s-yaml/erp-server/erp-server-workflow.yaml || true"

                    ssh -tt root@172.16.100.60 "/usr/bin/kubectl apply -f /k8s-yaml/erp-server/erp-server-admin.yaml -f /k8s-yaml/erp-server/erp-server-k8s.yaml -f /k8s-yaml/erp-server/erp-server-bi.yaml -f /k8s-yaml/erp-server/erp-server-auth.yaml -f /k8s-yaml/erp-server/erp-server-dmp.yaml -f /k8s-yaml/erp-server/erp-server-file.yaml -f /k8s-yaml/erp-server/erp-server-mrp.yaml -f /k8s-yaml/erp-server/erp-server-msg.yaml -f /k8s-yaml/erp-server/erp-server-oms.yaml -f /k8s-yaml/erp-server/erp-server-plm.yaml -f /k8s-yaml/erp-server/erp-server-scm.yaml -f /k8s-yaml/erp-server/erp-server-srm.yaml -f /k8s-yaml/erp-server/erp-server-sys.yaml -f /k8s-yaml/erp-server/erp-server-tms.yaml -f /k8s-yaml/erp-server/erp-server-wms.yaml -f /k8s-yaml/erp-server/erp-server-workflow.yaml"
                    '''
            }
        }
    }
}
