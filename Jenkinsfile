pipeline {
    agent any

    environment {
        SONAR_TOKEN = credentials('sona_token')
    }

    stages {

        stage('Checkout Repos') {
            steps {
                echo 'Cloning Java EE Backend and Python API...'
                git branch: 'main',
                    url: 'https://github.com/AzizVerse/WorklfowLoan.git',
                    credentialsId: 'github-cred'

                dir('loan-ml-api') {
                    git branch: 'main',
                        url: 'https://github.com/AzizVerse/loan-ml-api.git',
                        credentialsId: 'github-cred'
                }
            }
        }

        stage('Java Analysis & Tests') {
            steps {
                echo 'Running Java build, tests, static analysis, and code coverage...'
                bat '''
                    mvn clean verify ^
                        -Ddependency-check.skip=true ^
                        checkstyle:checkstyle ^
                        pmd:pmd ^
                        jacoco:prepare-agent ^
                        test ^
                        jacoco:report
                '''
            }
        }

        stage('SonarCloud Analysis') {
            steps {
                echo 'Running SonarCloud static code analysis...'
                bat """
                    mvn sonar:sonar ^
                        -Dsonar.projectKey=AzizVerse_WorklfowLoan ^
                        -Dsonar.organization=azizverse ^
                        -Dsonar.host.url=https://sonarcloud.io ^
                        -Dsonar.login=%SONAR_TOKEN%
                """
            }
        }

        stage('Python Lint & Tests') {
            steps {
                dir('loan-ml-api') {
                    echo 'Installing dependencies & running Python linting + tests...'
                    bat '''
    "C:\\Users\\HP\\AppData\\Local\\Programs\\Python\\Python313\\python.exe" -m pip install --upgrade pip
    "C:\\Users\\HP\\AppData\\Local\\Programs\\Python\\Python313\\python.exe" -m pip install -r requirements.txt
    "C:\\Users\\HP\\AppData\\Local\\Programs\\Python\\Python313\\python.exe" -m pip install flake8 pytest pytest-cov
    "C:\\Users\\HP\\AppData\\Local\\Programs\\Python\\Python313\\python.exe" -m flake8 main.py loan.py test_main.py
    "C:\\Users\\HP\\AppData\\Local\\Programs\\Python\\Python313\\python.exe" -m pytest --cov=. --cov-report=html
'''

                }
            }
        }

        stage('Security: OWASP Dependency Check') {
            steps {
                script {
                    try {
                        bat '''
                            mvn org.owasp:dependency-check-maven:check ^
                                -Dnvd.api.disabled=true ^
                                -Dformat=HTML
                        '''
                    } catch (err) {
                        echo "⚠️ OWASP Dependency Check failed — continuing build."
                    }
                }
            }
        }
      stage('Deploy to Nexus') {
    steps {
        withCredentials([usernamePassword(credentialsId: 'nexus-cred', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
            script {
                // Safely write settings.xml (avoid direct interpolation of secrets)
                def settingsXml = """
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>nexus</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
"""
                writeFile file: 'settings.xml', text: settingsXml

                // Get version from pom.xml
                def version = readMavenPom().version
                def url = "http://localhost:8081/repository/maven-releases/com/mybank/mybank/${version}/mybank-${version}.war"

                // Check if artifact already exists
                def status = bat(
                    script: """curl -u %NEXUS_USER%:%NEXUS_PASS% -s -o NUL -w "%{http_code}" "${url}" """,
                    returnStdout: true
                ).trim()

                if (status == "200") {
                    echo "✅ Artifact already exists in Nexus. Skipping deploy."
                } else {
                    echo "📦 Artifact not found. Deploying to Nexus..."
                    bat 'mvn deploy -s settings.xml -DskipTests'
                }
            }
        }
    }
}


        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                bat 'docker build -t azizdevops12/mybank:latest .'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-cred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    bat '''
                        docker login -u %DOCKER_USER% -p %DOCKER_PASS%
                        docker push azizdevops12/mybank:latest
                    '''
                }
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                echo 'Deploying containers with Docker Compose...'
                bat 'docker-compose down && docker-compose up -d --build'
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

            // OWASP security report
            archiveArtifacts artifacts: '**/target/dependency-check-report/dependency-check-report.html', allowEmptyArchive: true

            // Python reports
            archiveArtifacts artifacts: 'loan-ml-api/htmlcov/**', allowEmptyArchive: true
        }
    }
}
