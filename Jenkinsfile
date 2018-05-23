pipeline {
    agent {
        docker {
            image 'maven:3-alpine'
            args '-v /root/.m2:/root/.m2'
        }
    }
    options {
        // Only keep the 10 most recent builds
        buildDiscarder(logRotator(numToKeepStr:'5'))
        timestamps()
    }
    triggers {
        /*
          Restrict nightly builds to master branch, all others will be built on change only.
          Note: The BRANCH_NAME will only work with a multi-branch job using the github-branch-source
        */
        cron("*/50 * * * *")
    }
    tools {
        maven 'M3'
    }
    environment {
        responseStatus=0
    }
    stages {
        stage ('Start') {
            steps {
                slackSend (color: '#FFFF00', message: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
            }
        }
        stage('Build') {
            steps {

                withMaven(
                        //maven: 'M3',
                        options: [findbugsPublisher(), checkstyle(), pmd(), junitPublisher(ignoreAttachments: false)]
                ) {
                    //sh "mvn -B -DskipTests clean package checkstyle:checkstyle findbugs:findbugs pmd:pmd package"
                    //sh "export PATH=$MVN_CMD_DIR:$PATH && mvn -B -DskipTests clean package checkstyle:checkstyle findbugs:findbugs pmd:pmd package"
                    echo "$MVN_CMD"

                    sh "$MVN_CMD --version"
                    sh "$MVN_CMD -B -DskipTests clean package checkstyle:checkstyle findbugs:findbugs pmd:pmd package"
                }
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Validate') {
            steps {
                script {
                    def response = httpRequest timeout(time: 1, unit: 'MINUTES'),
                            //consoleLogResponseBody: true,
                            url: "https://jsonplaceholder.typicode.com/posts"

                    responseStatus = response.status

                    //println("Status: " + response.status)
                    //println("Content: " + response.content)
                }
            }
        }
        stage('Status') {
            when {
                expression {
                    return responseStatus == 200
                }
            }
            steps {
                echo 'Response 200'
            }
        }
        stage('Deliver') {
            steps {
                sh './jenkins/scripts/deliver.sh'
            }
        }
    }
    post {
        always {
            step([$class: 'Mailer',
                  //notifyEveryUnstableBuild: true,
                  recipients: "helge.skjellevik@gmail.com",
                  sendToIndividuals: true])
        }

        success {
            slackSend (color: '#00FF00', message: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }

        failure {
            slackSend (color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }
    }
}
