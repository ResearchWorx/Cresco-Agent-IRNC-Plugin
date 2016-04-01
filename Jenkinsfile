#!groovy

node {
    stage 'Checkout'
    git url: 'https://github.com/ResearchWorx/Cresco-Agent-IRNC-RESTful-Plugin.git'
    def mvnHome = tool 'M3'
    stage 'Build'
    sh '${mvnHome}/bin/mvn clean package'
}