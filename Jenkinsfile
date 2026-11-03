pipeline {
    agent any
    options {
        timeout(time: 20, unit: 'MINUTES')
    }
    stages {
        stage('Compilar') {
            steps {
                sh 'mvn -B -ntp compile'
            }
        }
        stage('Pruebas y cobertura') {
            steps {
                sh 'mvn -B -ntp verify'
            }
            post {
                always {
                    junit 'target/*-reports/*.xml'
                }
            }
        }
        stage('Publicar artefacto') {
            steps {
                archiveArtifacts artifacts: 'target/cambioexpress-*.jar'
            }
        }
        stage('Desplegar a cert') {
            when {
                branch 'master'
            }
            steps {
                withCredentials([string(credentialsId: 'cambioexpress-deploy-token', variable: 'DEPLOY_TOKEN')]) {
                    sh 'bash devops/desplegar.sh cert target/cambioexpress-*.jar'
                }
            }
        }
    }
}
