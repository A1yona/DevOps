pipeline {
    agent{node('master')}
    stages {
        stage('Download project') {
            steps {
                script {
                    cleanWs()
                    withCredntials([
                            usernamePassword(credentialsId: 'srv_sudo',
                            usernameVariable: 'username',
                            passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop isng"
                            sh "echo '${password}' | sudo -S container rm isng"
                        } catch (Exception e) {
                            print 'container not exist, skip clean'
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
                              userRemoteConfigs                : [[credentialsId: 'AlyonaZaichenkoGit', url: 'https://github.com/A1yona/CICD.git']]])
                }
            }
        }
        stage('Run docker image'){
            steps {
                script {
                    withCredntials([
                            usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t zaichenko_a_nginx"
                        sh "echo '${password}' | sudo -S docker run -d -p 8456:80 --name isng -v /home/adminci/is_mount_dir:/stat_dir zaichenko_a_nginx"
                    }
                }
            }
        }
        stage('Get stats'){
            steps{
                script{
                    withCredntials([
                            usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker exec -t isng bash -c 'df -h > /stat_dir/stats.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t isng bash -c 'top -n 1 -b >> /stat_dir/stats.txt'"
                    }
                }

            }
        }
    }
}