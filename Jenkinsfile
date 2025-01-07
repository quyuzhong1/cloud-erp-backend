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
        stage('通过docker制作自定义镜像') {
            steps {
                script {
                    def erpfile = readFile('erp_file.txt')
                    def lines = erpfile.split("\n")
                    for (line in lines) {
                        // 确保正确的插值
                        sh """
                        ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/${line}:${TAG} || true"
                        ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/${line}/ && docker build -t ${harborAddress}/${harborRepo}/${line}:${TAG} ."
                        """
                    }
                    // 最后构建erp-gateway镜像
                    sh """
                    ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-gateway:${TAG} || true"
                    ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-gateway && docker build -t ${harborAddress}/${harborRepo}/erp-gateway:${TAG} ."
                    """
                }
            }
        }
        stage('将自定义镜像推送到harbor') {
            steps {
                sh "docker login -u ${harborUser} -p ${harborPassword} ${harborAddress}"
                script {
                    def erpfile = readFile('erp_file.txt')
                    def lines = erpfile.split("\n")
                    for (line in lines) {
                        sh "docker push ${harborAddress}/${harborRepo}/${line}:${TAG}"
                    }
                }
                // 推送erp-gateway镜像
                sh "docker push ${harborAddress}/${harborRepo}/erp-gateway:${TAG}"
            }
        }
        stage('远程执行k8s-master的kubectl命令') {
            steps {
                script {
                    def erpfile = readFile('erp_file.txt')
                    def lines = erpfile.split("\n")
                    for (line in lines) {
                        sh """
                        scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/${line}/${line}.yaml root@172.16.100.60:/k8s-yaml/erp-server
                        ssh -tt root@172.16.100.60 "sed -i -e 's|\${tag}|${TAG}|g' -e 's|\${namespace}|${NAMESPACE}|g' /k8s-yaml/erp-server/${line}.yaml"
                        """
                    }
                }
                // 最后操作erp-gateway的yaml
                sh """
                scp /var/jenkins_home/workspace/${JOB_NAME}/erp-gateway/erp-gateway.yaml root@172.16.100.60:/k8s-yaml/erp-server
                ssh -tt root@172.16.100.60 "sed -i -e 's|\${tag}|${TAG}|g' -e 's|\${namespace}|${NAMESPACE}|g' /k8s-yaml/erp-server/erp-gateway.yaml"
                ssh -tt root@172.16.100.60 "/usr/bin/kubectl delete -f /k8s-yaml/erp-server/ || true"
                ssh -tt root@172.16.100.60 "/usr/bin/kubectl apply -f /k8s-yaml/erp-server/"
                """
            }
        }
    }
}
