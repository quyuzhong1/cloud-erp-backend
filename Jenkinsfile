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
                            sh "/var/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/maven_3.5/bin/mvn -s ${env.MY_SETTINGS_XML} clean package -T 12C -U '-Dmaven.test.skip=true'"
                        }
                    }
                }
            }
        }
        stage('通过SonarQube做代码质量检测') {
                    steps {
                        sh '/var/jenkins_home/sonar-scanner-4.5.0.2216/bin/sonar-scanner -Dsonar.host.url=http://172.16.100.90:9000/ -Dsonar.login=sqp_0ffcdfd391e376c42b8873aa08127a15f90c3084 -Dsonar.projectKey=uat-erp-backend -Dsonar.projectName=uat-erp-backend -Dsonar.language=java -Dsonar.scm.provider=git -Dsonar.sourceEncoding=UTF-8 -Dsonar.exclusions=**/*.class -Dsonar.java.binaries=erp-common/erp-common-business/target/classes,erp-common/erp-common-core/target/classes,erp-common/erp-common-message/target/classes,erp-gateway/target/classes,erp-model/erp-model-admin/target/classes,erp-model/erp-model-bi/target/classes,erp-model/erp-model-plm/target/classes,erp-model/erp-model-srm/target/classes,erp-model/erp-model-tms/target/classes,erp-model/erp-model-workflow/target/classes,erp-model/erp-model-dmp/target/classes,erp-model/erp-model-oms/target/classes,erp-model/erp-model-scm/target/classes,erp-model/erp-model-sys/target/classes,erp-model/erp-model-wms/target/classes,erp-model/erp-model-mrp/target/classes,erp-rpc/erp-rpc-auth/target/classes,erp-rpc/erp-rpc-dmp/target/classes,erp-rpc/erp-rpc-plm/target/classes,erp-rpc/erp-rpc-srm/target/classes,erp-rpc/erp-rpc-tms/target/classes,erp-rpc/erp-rpc-workflow/target/classes,erp-rpc/erp-rpc-bi/target/classes,erp-rpc/erp-rpc-file/target/classes,erp-rpc/erp-rpc-oms/target/classes,erp-rpc/erp-rpc-scm/target/classes,erp-rpc/erp-rpc-sys/target/classes,erp-rpc/erp-rpc-wms/target/classes,erp-rpc/erp-rpc-mrp/target/classes,erp-sdk/erp-sdk-msg-fs/target/classes,erp-sdk/erp-sdk-oms-aliexpress/target/classes,erp-sdk/erp-sdk-oms-amz-spapi/target/classes,erp-sdk/erp-sdk-oms-mercado/target/classes,erp-sdk/erp-sdk-oms-shopee/target/classes,erp-sdk/erp-sdk-oms-shopify/target/classes,erp-sdk/erp-sdk-oms-tiktok/target/classes,erp-sdk/erp-sdk-oms-walmart/target/classes,erp-sdk/erp-sdk-third-guanyi/target/classes,erp-sdk/erp-sdk-third-kingdee/target/classes,erp-sdk/erp-sdk-third-lingxing/target/classes,erp-sdk/erp-sdk-third-mabang/target/classes,erp-sdk/erp-sdk-third-qimen/target/classes,erp-sdk/erp-sdk-third-wangdian/target/classes,erp-sdk/erp-sdk-tms-aliexpress/target/classes,erp-sdk/erp-sdk-tms-baohong/target/classes,erp-sdk/erp-sdk-tms-batong/target/classes,erp-sdk/erp-sdk-tms-disifang/target/classes,erp-sdk/erp-sdk-tms-express/target/classes,erp-sdk/erp-sdk-tms-shopee/target/classes,erp-sdk/erp-sdk-tms-tiktok/target/classes,erp-sdk/erp-sdk-tms-tongyou/target/classes,erp-sdk/erp-sdk-tms-track123/target/classes,erp-sdk/erp-sdk-tms-ubi/target/classes,erp-sdk/erp-sdk-tms-weishi/target/classes,erp-sdk/erp-sdk-tms-yanwen/target/classes,erp-sdk/erp-sdk-tms-yuntu/target/classes,erp-sdk/erp-sdk-wms-antu/target/classes,erp-sdk/erp-sdk-wms-goodcang/target/classes,erp-sdk/erp-sdk-wms-iml/target/classes,erp-server/erp-server-admin/target/classes,erp-server/erp-server-auth/target/classes,erp-server/erp-server-bi/target/classes,erp-server/erp-server-dmp/target/classes,erp-server/erp-server-file/target/classes,erp-server/erp-server-msg/target/classes,erp-server/erp-server-oms/target/classes,erp-server/erp-server-plm/target/classes,erp-server/erp-server-scm/target/classes,erp-server/erp-server-srm/target/classes,erp-server/erp-server-sys/target/classes,erp-server/erp-server-tms/target/classes,erp-server/erp-server-wms/target/classes,erp-server/erp-server-workflow/target/classes,erp-server/erp-server-mrp/target/classes -Dsonar.sources=erp-common/erp-common-business/src,erp-common/erp-common-core/src,erp-common/erp-common-message/src,erp-gateway/src,erp-model/erp-model-admin/src,erp-model/erp-model-bi/src,erp-model/erp-model-msg/src,erp-model/erp-model-plm/src,erp-model/erp-model-srm/src,erp-model/erp-model-tms/src,erp-model/erp-model-workflow/src,erp-model/erp-model-dmp/src,erp-model/erp-model-oms/src,erp-model/erp-model-scm/src,erp-model/erp-model-sys/src,erp-model/erp-model-wms/src,erp-model/erp-model-mrp/src,erp-rpc/erp-rpc-auth/src,erp-rpc/erp-rpc-dmp/src,erp-rpc/erp-rpc-plm/src,erp-rpc/erp-rpc-srm/src,erp-rpc/erp-rpc-tms/src,erp-rpc/erp-rpc-workflow/src,erp-rpc/erp-rpc-bi/src,erp-rpc/erp-rpc-file/src,erp-rpc/erp-rpc-oms/src,erp-rpc/erp-rpc-scm/src,erp-rpc/erp-rpc-sys/src,erp-rpc/erp-rpc-wms/src,erp-rpc/erp-rpc-mrp/src,erp-sdk/erp-sdk-msg-fs/src,erp-sdk/erp-sdk-oms-aliexpress/src,erp-sdk/erp-sdk-oms-amz-spapi/src,erp-sdk/erp-sdk-oms-mercado/src,erp-sdk/erp-sdk-oms-shopee/src,erp-sdk/erp-sdk-oms-shopify/src,erp-sdk/erp-sdk-oms-tiktok/src,erp-sdk/erp-sdk-oms-walmart/src,erp-sdk/erp-sdk-third-guanyi/src,erp-sdk/erp-sdk-third-kingdee/src,erp-sdk/erp-sdk-third-lingxing/src,erp-sdk/erp-sdk-third-mabang/src,erp-sdk/erp-sdk-third-qimen/src,erp-sdk/erp-sdk-third-wangdian/src,erp-sdk/erp-sdk-tms-aliexpress/src,erp-sdk/erp-sdk-tms-baohong/src,erp-sdk/erp-sdk-tms-batong/src,erp-sdk/erp-sdk-tms-disifang/src,erp-sdk/erp-sdk-tms-express/src,erp-sdk/erp-sdk-tms-shopee/src,erp-sdk/erp-sdk-tms-tiktok/src,erp-sdk/erp-sdk-tms-tongyou/src,erp-sdk/erp-sdk-tms-track123/src,erp-sdk/erp-sdk-tms-ubi/src,erp-sdk/erp-sdk-tms-weishi/src,erp-sdk/erp-sdk-tms-yanwen/src,erp-sdk/erp-sdk-tms-yuntu/src,erp-sdk/erp-sdk-wms-antu/src,erp-sdk/erp-sdk-wms-goodcang/src,erp-sdk/erp-sdk-wms-iml/src,erp-server/erp-server-admin/src,erp-server/erp-server-auth/src,erp-server/erp-server-bi/src,erp-server/erp-server-dmp/src,erp-server/erp-server-file/src,erp-server/erp-server-msg/src,erp-server/erp-server-oms/src,erp-server/erp-server-plm/src,erp-server/erp-server-scm/src,erp-server/erp-server-srm/src,erp-server/erp-server-sys/src,erp-server/erp-server-tms/src,erp-server/erp-server-wms/src,erp-server/erp-server-workflow/src,erp-server/erp-server-mrp/src'
                    }
                }
        stage('通过docker制作自定义镜像') {
            steps {
                script {
                    def erpfile = readFile('erp_file.txt')
                    def lines = erpfile.split("\n")
                    for (line in lines) {
                        sh """
                        ssh -tt root@172.16.100.90 "docker rmi ${harborAddress}/${harborRepo}/${line}:${TAG} || true"
                        ssh -tt root@172.16.100.90 "cd /home/dockers/jenkins/jenkins_home/workspace/${JOB_NAME}/erp-server/${line}/ && docker build -t ${harborAddress}/${harborRepo}/${line}:${TAG} ."
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
                sh "docker login -u ${harborUser} -p ${harborPassword} ${harborAddress}"
                script {
                    def erpfile = readFile('erp_file.txt')
                    def lines = erpfile.split("\n")
                    for (line in lines) {
                        sh "docker push ${harborAddress}/${harborRepo}/${line}:${TAG}"
                    }
                }
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
                        scp /var/jenkins_home/workspace/${JOB_NAME}/erp-server/${line}/${line}.yaml root@172.16.100.60:/k8s-yaml/erpuat-server
                        ssh -tt root@172.16.100.60 "sed -i -e 's|\\\${tag}|${TAG}|g' -e 's|\\\${namespace}|${NAMESPACE}|g' /k8s-yaml/erpuat-server/${line}.yaml"
                        """
                    }
                }
                sh """
                scp /var/jenkins_home/workspace/${JOB_NAME}/erp-gateway/erp-gateway.yaml root@172.16.100.60:/k8s-yaml/erpuat-server
                ssh -tt root@172.16.100.60 "sed -i -e 's|\\\${tag}|${TAG}|g' -e 's|\\\${namespace}|${NAMESPACE}|g' /k8s-yaml/erpuat-server/erp-gateway.yaml"
                ssh -tt root@172.16.100.60 "/usr/bin/kubectl delete -f /k8s-yaml/erpuat-server/ || true"
                ssh -tt root@172.16.100.60 "/usr/bin/kubectl apply -f /k8s-yaml/erpuat-server/"
                """
            }
        }
    }
}