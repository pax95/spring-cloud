#!/usr/bin/groovy
// lets allow the VERSION_PREFIX to be specified as a parameter to the build
// but if not lets just default to 1.0
def versionPrefix = ""
try {
  versionPrefix = VERSION_PREFIX
} catch (Throwable e) {
  versionPrefix = "1.0"
}

def canaryVersion = "${versionPrefix}.${env.BUILD_NUMBER}"

node{
  git 'https://github.com/pax95/spring-cloud.git'

  echo 'NOTE: running pipelines for the first time will take longer as build and base docker images are pulled onto the node'
  kubernetes.pod('buildpod').withImage('fabric8/maven-builder')
      .withPrivileged(true)
      .withHostPathMount('/var/run/docker.sock','/var/run/docker.sock')
      .withEnvVar('DOCKER_CONFIG','/home/jenkins/.docker/')
      .withSecret('jenkins-docker-cfg','/home/jenkins/.docker')
      .withSecret('jenkins-maven-settings','/root/.m2')
      .withServiceAccount('jenkins')
      .inside {

    stage 'Canary Release'
    mavenCanaryRelease{
      version = canaryVersion
    }
  }
}
