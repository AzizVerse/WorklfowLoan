pipeline {
    agent any

    environment {
        SONAR_TOKEN = credentials('sonar-token') // Add your SonarCloud token in Jenkins credentials
    }

    stages {
        stage('Checkout Java EE') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/AzizVerse/WorklfowLoan.git',
                    credentialsId: 'github-creds'
            }
        }

        stage('SonarCloud Analysis') {
            steps {
                echo 'Running SonarCloud analysis...'
                sh 'mvn verify sonar:sonar -Dsonar.projectKey=AzizVerse_WorklfowLoan -Dsonar.organization=azizverse -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN'
            }
        }

        stage('Package Java EE App') {
            steps {
                echo 'Packaging Java EE project...'
                sh 'mvn clean package'
            }
        }

        stage('Mockito Unit Tests') {
            steps {
                echo 'Running Java Unit Tests with Mockito...'
                sh 'mvn test'
            }
        }

        stage('Generate Java Code Coverage Report') {
            steps {
                echo 'Running JaCoCo Coverage...'
                sh 'mvn jacoco:prepare-agent test jacoco:report'
            }
        }

        stage('Checkout Python ML API') {
            steps {
                dir('loan-ml-api') {
                    git branch: 'main',
                        url: 'https://github.com/AzizVerse/loan-ml-api.git',
                        credentialsId: 'github-creds'
                }
            }
        }

        stage('Run Python Tests & Linting') {
            steps {
                dir('loan-ml-api') {
                    sh '''
                        python3 -m pip install --upgrade pip
                        python3 -m pip install -r requirements.txt
                        python3 -m pip install flake8 pytest pytest-cov
                        python3 -m flake8 main.py loan.py test_main.py
                        python3 -m pytest --cov=. --cov-report=html
                    '''
                }
            }
        }
    }

    post {
        always {
            echo 'Archiving test results and coverage reports...'
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/site/jacoco/index.html', allowEmptyArchive: true
            archiveArtifacts artifacts: 'loan-ml-api/htmlcov/**', allowEmptyArchive: true
        }
    }
}
