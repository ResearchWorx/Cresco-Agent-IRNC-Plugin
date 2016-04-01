#!groovy

node {
    stage 'Build and Test'
    env.PATH = "${tool 'Maven 3'}/bin:${env.PATH}"
    git url: 'https://github.com/ResearchWorx/Cresco-Agent-IRNC-RESTful-Plugin.git'
    sh "mvn clean package"
}