#!/usr/bin/env bash
# Build the jar, build the Docker image and push it to ECR.
# Required env: AWS_ACCOUNT_ID, AWS_REGION, ECR_REPO. Optional: IMAGE_TAG (default: git short sha)
set -euo pipefail

: "${AWS_ACCOUNT_ID:?}" "${AWS_REGION:?}" "${ECR_REPO:?}"
IMAGE_TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD)}"
REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
IMAGE="${REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"

./mvnw -B clean verify

aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$REGISTRY"

docker build -t "$IMAGE" .
docker push "$IMAGE"

echo "$IMAGE" > image.txt
echo "Pushed $IMAGE"
