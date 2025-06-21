pipeline {
    agent any

    environment {
        SONAR_TOKEN = credentials('sonar-token')
    }

    stages {

        stage('Checkout Repos') {
            steps {
                echo 'Cloning Java EE Backend and Python API...'
                // Clone Java backend
                git branch: 'main',
                    url: 'https://github.com/AzizVerse/WorklfowLoan.git',
                    credentialsId: 'github-creds'

                // Clone Python ML API in subdirectory
                dir('loan-ml-api') {
                    git branch: 'main',
                        url: 'https://github.com/AzizVerse/loan-ml-api.git',
                        credentialsId: 'github-creds'
                }
            }
        }

        stage('Java Analysis & Tests') {
            steps {
                echo 'Running Java build, tests, static analysis, and code coverage...'
                sh '''
                    mvn clean verify \
                        checkstyle:checkstyle \
                        pmd:pmd \
                        jacoco:prepare-agent \
                        test \
                        jacoco:report
                '''
            }
        }

        stage('SonarCloud Analysis') {
            steps {
                echo 'Running SonarCloud static code analysis...'
                sh '''
                    mvn sonar:sonar \
                        -Dsonar.projectKey=AzizVerse_WorklfowLoan \
                        -Dsonar.organization=azizverse \
                        -Dsonar.host.url=https://sonarcloud.io \
                        -Dsonar.login=$SONAR_TOKEN
                '''
            }
        }

        stage('Python Lint & Tests') {
            steps {
                dir('loan-ml-api') {
                    echo 'Installing dependencies & running Python linting + tests...'
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
            echo 'Archiving reports and results...'

            // Java reports
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/site/jacoco/index.html', allowEmptyArchive: true
            archiveArtifacts artifacts: '**/target/site/checkstyle.html', allowEmptyArchive: true
            archiveArtifacts artifacts: '**/target/site/pmd.html', allowEmptyArchive: true

            // Python reports
            archiveArtifacts artifacts: 'loan-ml-api/htmlcov/**', allowEmptyArchive: true
        }
    }
}
