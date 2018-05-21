pipeline {
    agent {
        docker {
            image 'maven:3-alpine'
            args '-v /root/.m2:/root/.m2'
        }
    }
    stages {
        stage ('Start') {
            steps {
                // send build started notifications
                slackSend (color: '#FFFF00', message: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
            }
        }
        stage('Build') {
            steps {
                withMaven(
                        maven: 'M3',
                        options: [findbugsPublisher(), checkstyle(), pmd(), junitPublisher(ignoreAttachments: false)]
                ) {
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
        stage('Deliver') {
            steps {
                sh './jenkins/scripts/deliver.sh'
            }
            post {
                slackSend color: 'good', message: 'Message from Jenkins Pipeline'
            }
        }
    }
}
