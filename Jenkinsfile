pipeline {
    agent any
    environment {
        harborCreds = credentials('harbor-credentials')
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
                            sh "/var/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/maven_3.5/bin/mvn -s ${env.MY_SETTINGS_XML} clean package -T 12C -U '-Dmaven.test.skip=true'"
                        }
                    }
                }
            }
        }
        stage('通过docker制作自定义镜像') {
            steps {
                script {
                    def erpfiles = ['erp-server-admin', 'erp-server-auth', 'erp-server-bi', 'erp-server-dmp', 'erp-server-file', 'erp-server-mrp', 'erp-server-msg', 'erp-server-oms', 'erp-server-plm', 'erp-server-scm', 'erp-server-srm', 'erp-server-sys', 'erp-server-tms', 'erp-server-wms', 'erp-server-workflow']
                    for (erpfile in erpfiles) {
                        sh """
                        ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/${erpfile}:${TAG} || true"
                        ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/${erpfile}/ && docker build -t ${harborAddress}/${harborRepo}/${erpfile}:${TAG} ."
                        """
                    }
                sh """
                ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/erp-gateway:${TAG} || true"
                ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-gateway && docker build -t ${harborAddress}/${harborRepo}/erp-gateway:${TAG} ."
                """
                }
            }
        }
        stage('将自定义镜像推送到harbor') {
            steps {
                sh "docker login -u ${harborCreds_USR} -p ${harborCreds_PSW} ${harborAddress}"
                script {
                    def erpfiles = ['erp-gateway', 'erp-server-admin', 'erp-server-auth', 'erp-server-bi', 'erp-server-dmp', 'erp-server-file', 'erp-server-mrp', 'erp-server-msg', 'erp-server-oms', 'erp-server-plm', 'erp-server-scm', 'erp-server-srm', 'erp-server-sys', 'erp-server-tms', 'erp-server-wms', 'erp-server-workflow']
                    for (erpfile in erpfiles) {
                        sh "docker push ${harborAddress}/${harborRepo}/${erpfile}:${TAG}"
                    }
                }
            }
        }
        stage('远程执行k8s-master的kubectl命令') {
            steps {
                sh "scp root@172.16.100.60:/k8s-yaml/erp-template/*.yaml root@172.16.100.60:/k8s-yaml/erptest-server"
                script {
                    def erpfiles = ['erp-gateway', 'erp-server-admin', 'erp-server-auth', 'erp-server-bi', 'erp-server-dmp', 'erp-server-file', 'erp-server-mrp', 'erp-server-msg', 'erp-server-oms', 'erp-server-plm', 'erp-server-scm', 'erp-server-srm', 'erp-server-sys', 'erp-server-tms', 'erp-server-wms', 'erp-server-workflow']
                    for (erpfile in erpfiles) {
                        sh """
                        ssh -tt root@172.16.100.60 "sed -i -e 's|\\\${tag}|${TAG}|g' -e 's|\\\${namespace}|${NAMESPACE}|g' /k8s-yaml/erptest-server/${erpfile}.yaml"
                        """
                    }
                }
                sh """
                ssh -tt root@172.16.100.60 "/usr/bin/kubectl delete -f /k8s-yaml/erptest-server/ || true"
                ssh -tt root@172.16.100.60 "/usr/bin/kubectl apply -f /k8s-yaml/erptest-server/"
                """
            }
        }
    }
}