// Shared library step (repo: jenkins-shared-lib, path vars/notifyBuild.groovy)
// Usage in any Jenkinsfile:  notifyBuild('FAILURE')
def call(String status) {
  def colors = [SUCCESS: 'good', UNSTABLE: 'warning', FAILURE: 'danger']
  def msg = "*${status}*: ${env.JOB_NAME} #${env.BUILD_NUMBER} " +
            "(branch `${env.BRANCH_NAME}`, env `${env.DEPLOY_ENV ?: 'n/a'}`)\n${env.BUILD_URL}"

  slackSend(channel: env.SLACK_CHANNEL ?: '#ci', color: colors.get(status, '#cccccc'), message: msg)

  if (status != 'SUCCESS') {
    emailext(
      subject: "[CI ${status}] ${env.JOB_NAME} #${env.BUILD_NUMBER}",
      mimeType: 'text/plain',
      body: """${msg}

Test report: ${env.BUILD_URL}testReport/
Console:     ${env.BUILD_URL}console
""",
      to: env.CI_EMAIL ?: '',
      recipientProviders: [culprits(), developers(), requestor()],
      attachLog: true,
      compressLog: true
    )
  }
}
