pipeline {
    agent any

    stages {
        stage('Checkout Java EE') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/AzizVerse/WorklfowLoan.git',
                    credentialsId: 'github-creds'
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
                sh 'mvn clean test'
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
                        python3 -m pytest --cov=.
                    '''
                }
            }
        }
    }
}
