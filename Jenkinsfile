#!groovy

node {
    git url: 'http://github.com/researchworx/Cresco-Agent-IRNC-RESTful-Plugin.git'
    def mvnHome = tool 'M3'
    sh "${mvnHome}/bin/mvn clean package"
}