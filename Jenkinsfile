#!groovy

node {
    stage 'Checkout'
    checkout master
    def mvnHome = tool 'M3'
    stage 'Build'
    sh "${mvnHome}/bin/mvn clean package"
}