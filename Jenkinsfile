#!groovy

node {
    stage 'Checkout'
    git url: 'https://github.com/ResearchWorx/Cresco-Agent-IRNC-RESTful-Plugin.git'
    def mvnHome = tool 'Maven 3'
    stage 'Build'
    sh '${mvnHome}/bin/mvn clean package'
}