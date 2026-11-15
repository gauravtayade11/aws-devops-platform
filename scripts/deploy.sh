#!/usr/bin/env bash
# Deploy the image to EKS.
# Required env: AWS_REGION, EKS_CLUSTER. Image taken from $IMAGE or image.txt.
set -euo pipefail

: "${AWS_REGION:?}" "${EKS_CLUSTER:?}"
IMAGE="${IMAGE:-$(cat image.txt)}"

aws eks update-kubeconfig --region "$AWS_REGION" --name "$EKS_CLUSTER"

sed "s|IMAGE_PLACEHOLDER|${IMAGE}|g" k8s/deployment.yaml | kubectl apply -f -
kubectl apply -f k8s/service.yaml

kubectl rollout status deployment/aws-devops-app --timeout=180s
