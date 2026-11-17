
pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven'
    }

    environment {
        AWS_REGION = 'ap-southeast-2'
        ECR_REPO   = 'aws-devops-app'
        EKS_CLUSTER = 'aws-devops-eks'
        IMAGE_TAG  = "${BUILD_NUMBER}"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Initialize AWS') {
            steps {
                script {
                    env.AWS_ACCOUNT_ID = sh(
                        script: 'aws sts get-caller-identity --query Account --output text',
                        returnStdout: true
                    ).trim()

                    env.REGISTRY = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com"
                    env.IMAGE = "${env.REGISTRY}/${env.ECR_REPO}:${env.IMAGE_TAG}"
                }

                sh '''
                    echo "AWS Account: $AWS_ACCOUNT_ID"
                    echo "Region: $AWS_REGION"
                    echo "Image: $IMAGE"
                '''
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn -B clean verify'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t "$IMAGE" .'
            }
        }

        stage('Push to ECR') {
            steps {
                sh '''
                    aws ecr get-login-password --region "$AWS_REGION" |
                    docker login --username AWS --password-stdin "$REGISTRY"

                    docker push "$IMAGE"

                    docker logout "$REGISTRY"
                '''
            }
        }

        stage('Deploy to EKS (Helm)') {
            steps {
                sh '''
                    aws eks update-kubeconfig --region "$AWS_REGION" --name "$EKS_CLUSTER"

                    helm upgrade --install aws-devops-app helm/aws-devops-app \
                      --set image.repository="$REGISTRY/$ECR_REPO" \
                      --set image.tag="$IMAGE_TAG" \
                      --atomic --timeout 3m

                    kubectl get pods -l app=aws-devops-app
                '''
            }
        }
    }

    post {
        success {
            echo "Successfully pushed ${IMAGE}"
        }

        failure {
            echo 'Pipeline failed. Check the stage logs.'
        }

        always {
            sh 'docker image rm "$IMAGE" || true'
            cleanWs()
        }
    }
}
