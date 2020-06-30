pipeline {
    agent{node('master')}
    stages {
        stage('Download project') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                            usernameVariable: 'username',
                            passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop zav_pract"
                            sh "echo '${password}' | sudo -S container rm zav_pract"
                        } catch (Exception e) {
                            print 'Skip cleanup, container does not exist'
                        }
                    }
                }
                script {
                    echo 'Start download project'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: 'AlyonaZaichenkoGit', url: 'https://github.com/A1yona/DevOps.git']]])
                }
            }
        }
        stage('Run docker image'){
            steps {
                script {
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                            usernameVariable: 'username',
                            passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t zaichenko_a_nginx"
                        sh "echo '${password}' | sudo -S docker run -d -p 8156:80 --name zav_pract -v /home/adminci/zav_dir:/stat_dir zaichenko_a_nginx"
						currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        stage('Get stats'){
            steps{
                script{
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                            usernameVariable: 'username',
                            passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker exec -t zav_pract bash -c 'df -h > /stat_dir/stats.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t zav_pract bash -c 'top -n 1 -b >> /stat_dir/stats.txt'"
                    }
                }

            }
        }
		stage('Stop docker container'){
            steps{
                script{
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                            usernameVariable: 'username',
                            passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker stop zav_pract"
                        sh "echo '${password}' | sudo -S container rm zav_pract"
                    }
                }

            }
        }
    }
}