/*
 * Copyright (c) 2022 Visma Autopay AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
pipeline {
    agent {
        label 'compile'
    }

    environment {
        MAIN_BUILD_GOAL = 'verify'
        BRANCH_BUILD_GOAL = 'verify sonar:sonar'
    }

    tools {
        maven 'Maven 3'
        jdk 'JDK 11'
    }

    options {
        timestamps()
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Prepare env') {
            steps {
                echo 'Prepare environment...'
                script {
                    if (env.CHANGE_ID) { // pull request branch: PR-ddd
                        env.BRANCH_NAME = CHANGE_BRANCH
                        if (currentBuild.rawBuild.project.displayName == GIT_BRANCH) {
                            def jobName = GIT_BRANCH + '-' + CHANGE_BRANCH.replaceAll(/%../, '-').replace('.', '_').replaceAll(/\W/, '-')
                            currentBuild.rawBuild.project.setDisplayName(jobName)
                        }
                        env.JOB_NAME = JOB_NAME.replace(GIT_BRANCH, currentBuild.rawBuild.project.displayName)
                    }

                    env.PRETTY_JOB_NAME = env.JOB_NAME.replaceAll(/%../, '-').replace('/', ' » ')
                    env.SCM_BRANCH = env.BRANCH_NAME.replaceAll(/%../, '-').replace('.', '_').replaceAll(/\W/, '-')

                    if (env.BRANCH_NAME ==~ /main/) {
                        env.BUILD_GOAL = env.MAIN_BUILD_GOAL
                    } else {
                        env.BUILD_GOAL = env.BRANCH_BUILD_GOAL
                    }

                    env.BUILD_PROPERTIES = '''
                        -Dsonar.scm.disabled=false
                        -Dsonar.projectKey=AutoPay.HttpSignatures
                        "-Dsonar.projectName=AutoPay HTTP Signatures"
                    '''

                    if (env.CHANGE_ID) {
                        env.BUILD_PROPERTIES += """
                            -Dsonar.pullrequest.key=${env.CHANGE_ID}
                            -Dsonar.pullrequest.branch=${SCM_BRANCH}
                            -Dsonar.pullrequest.base=main
                        """
                    } else {
                        env.BUILD_PROPERTIES += """
                            -Dsonar.branch.name=${SCM_BRANCH}
                        """
                    }

                    env.BUILD_PROPERTIES = env.BUILD_PROPERTIES.replaceAll("[\\n\t ]+", " ");
                }

                sh 'printenv | sort'
            }
        }

        stage('Build') {
            steps {
                echo 'Build...'
                withSonarQubeEnv('Sonar Enterprise') {
                    sh "mvn clean ${BUILD_GOAL} -ntp -Pjacoco ${BUILD_PROPERTIES}"
                }
            }
        }
    }

    post {
        always {
            junit testResults: '**/surefire-reports/TEST-*.xml', allowEmptyResults: true
        }

        fixed {
            echo "Sending fixed message to Slack"
            slackSend(color: "${SLACK_COLOR_GOOD}",
                 channel: "${SLACK_CHANNEL}",
                 tokenCredentialId: "${SLACK_TOKENID}",
                 baseUrl: "${SLACK_BASE_URL}",
                 message: "*FIXED:* ${PRETTY_JOB_NAME} \n *More info at:* ${BUILD_URL}")
        }

        failure {
            echo "Sending failure message to Slack"
            slackSend(color: "${SLACK_COLOR_DANGER}",
                 channel: "${SLACK_CHANNEL}",
                 tokenCredentialId: "${SLACK_TOKENID}",
                 baseUrl: "${SLACK_BASE_URL}",
                 message: "*FAILED:* ${PRETTY_JOB_NAME} \n *More info at:* ${BUILD_URL}")
        }
    }
}
