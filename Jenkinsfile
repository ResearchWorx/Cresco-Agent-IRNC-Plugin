#!groovy

node {
    stage 'Build and Test'
    env.PATH = "${tool 'Maven 3'}/bin:${env.PATH}"
    git url: 'http://github.com/researchworx/Cresco-Agent-IRNC-RESTful-Plugin.git'
    sh "mvn clean package"
}