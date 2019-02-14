#!/usr/bin/env groovy

/**
  Perform a checkout from the SCM configuration on a folder inside the workspace,
  if branch, repo, and credentialsId are defined make a checkout using those parameters.

  gitCheckout()

  gitCheckout(basedir: 'sub-folder')

  gitCheckout(basedir: 'sub-folder', branch: 'master',
    repo: 'git@github.com:elastic/apm-pipeline-library.git',
    credentialsId: 'credentials-id',
    reference: '/var/lib/jenkins/reference-repo.git')

*/
def call(Map params = [:]){
  def basedir =  params.containsKey('basedir') ? params.basedir : "src"
  def repo =  params?.repo
  def credentialsId =  params?.credentialsId
  def branch =  params?.branch
  def reference = params?.reference

  def extensions = []

  if(reference != null){
    extensions.add([$class: 'CloneOption', depth: 1, noTags: false, reference: "${reference}", shallow: true])
    log(level: 'DEBUG', text: "gitCheckout: Reference repo enabled ${extensions.toString()}")
  }

  dir("${basedir}"){
    if(env?.BRANCH_NAME && branch == null){
      log(level: 'INFO', text: "gitCheckout: Checkout SCM ${env.BRANCH_NAME}")
      checkout scm
    } else if (branch && branch != ""
        && repo
        && credentialsId){
      log(level: 'INFO', text: "gitCheckout: Checkout ${branch} from ${repo} with credentials ${credentialsId}")
      checkout([$class: 'GitSCM', branches: [[name: "${branch}"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: extensions,
        submoduleCfg: [],
        userRemoteConfigs: [[
          refspec: '+refs/heads/*:refs/remotes/origin/* +refs/pull/*/head:refs/remotes/origin/pr/*',
          credentialsId: "${credentialsId}",
          url: "${repo}"]]])
    } else {
      error "No valid SCM config passed."
    }
    githubEnv()
    if(!isManualyTrigger()){
      githubPrCheckApproved()
    }
  }
}

def isManualyTrigger(){
  def ret = false
  if(currentBuild.getBuildCauses()?.findAll{ bc -> bc._class == 'hudson.model.Cause$UserIdCause'}?.size() >= 1){
    log(level: 'INFO', text: "gitCheckout: Build manually triggered")
    ret = true
  }
  return ret
}