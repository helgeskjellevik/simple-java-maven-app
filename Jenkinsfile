pipeline {
    agent {
        docker {
            image 'maven:3-alpine'
            args '-v /root/.m2:/root/.m2'
        }
    }
    options {
        buildDiscarder(logRotator(numToKeepStr:'5'))
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        ansiColor colorMapName: 'XTerm'
    }
    triggers {
        //Build every 50 minutes
        cron("*/50 * * * *")
    }
    tools {
        maven 'M3'
        //sonarScanner: 'SonarQube-Scanner'
    }
    environment {
        responseStatus=0

        //Use Pipeline Utility Steps plugin to read information from pom.xml into env variables
        IMAGE = readMavenPom().getArtifactId()
        VERSION = readMavenPom().getVersion()
        FLYWAY_NAME = 'flyway-5.0.7'
        FLYWAY_CREDENTIALS = '997c1182-01ac-4ebd-9b58-65c864eaf7f2'
        FLYWAY_DB_URL = 'jdbc:mariadb://db:3306/fagdag'
        FLYWAY_LOCATION = 'filesystem:/home/utv2/simple-java-maven-app/src/main/resources/sql'
    }
    stages {
        stage('Sonar') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    echo 'sonarqube'
                    sh 'mvn sonar:sonar'
                }
            }
        }
        stage("Quality Gate") {
            steps {
                //sleep 2
                timeout(time: 1, unit: 'MINUTES') {
                    // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                    // true = set pipeline to UNSTABLE, false = don't
                    // Requires SonarQube Scanner for Jenkins 2.7+
                    echo 'qualitygate'
                    waitForQualityGate abortPipeline: false
                }
            }
        }
        stage ('Start') {
            steps {
                slackSend (color: '#FFFF00', message: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL}) (${env.CHANGE_AUTHOR}) (${env.VERSION})")
            }
        }
        stage('Checkout') {
            steps {
                echo 'checkout done automatically from jenkins job config'
                //checkout scm
            }
        }
        stage('Build') {
            steps {

                withMaven(
                        //maven: 'M3',
                        options: [findbugsPublisher(), checkstyle(), pmd(), junitPublisher(ignoreAttachments: false)]
                ) {
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
        stage('Parallell test') {
            steps {
                parallel(
                    one: {
                        echo "I'm on the first branch!"
                    },
                    two: {
                        echo "I'm on the second branch!"
                    },
                    three: {
                        echo "I'm on the third branch!"
                        echo "But you probably guessed that already."
                    }
                )
            }
        }

        stage('Database migration') {
            steps {
                //flywayrunner installationName: "${env.FLYWAY_NAME}", flywayCommand: 'clean', credentialsId: "${env.FLYWAY_CREDENTIALS}", url: "${env.FLYWAY_DB_URL}", locations: "${env.FLYWAY_LOCATION}", commandLineArgs: ''
                flywayrunner installationName: "${env.FLYWAY_NAME}", flywayCommand: 'info', credentialsId: "${env.FLYWAY_CREDENTIALS}", url: "${env.FLYWAY_DB_URL}", locations: "${env.FLYWAY_LOCATION}", commandLineArgs: ''

                timeout(time: 1, unit: 'MINUTES') {
                    input message: 'Does migration look ok? (Click "Proceed" to continue)'
                    flywayrunner installationName: "${env.FLYWAY_NAME}", flywayCommand: 'migrate', credentialsId: "${env.FLYWAY_CREDENTIALS}", url: "${env.FLYWAY_DB_URL}", locations: "${env.FLYWAY_LOCATION}", commandLineArgs: ''
                }
            }
        }
        stage('Validate') {
            steps {
                script {
                    def response = httpRequest timeout: 200,
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
        //always {
        //    step([$class: 'Mailer',
        //          recipients: "helge.skjellevik@gmail.com",
        //          sendToIndividuals: true])

        //mail(from: "helge.skjellevik@gmail.com",
        //        to: "helge.skjellevik@gmail.com",
        //        subject: "Jenkins notification",
        //        body: "Blah Blah")
        //}

        success {
            slackSend (color: '#00FF00', message: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }

        failure {
            slackSend (color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }
    }
}
