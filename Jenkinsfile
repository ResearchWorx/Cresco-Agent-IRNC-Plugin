#!groovy

node {
    stage 'Checkout'
    git url: 'https://github.com/ResearchWorx/Cresco-Agent-IRNC-RESTful-Plugin.git'

    stage 'Build'
    sh 'mvn clean package'
}