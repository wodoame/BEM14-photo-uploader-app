#!/bin/bash
# Fetches infrastructure values from CloudFormation and sets them as
# GitHub Actions variables and secrets. Run this after any infra redeployment.
#
# Prerequisites: aws CLI (--profile admin), gh CLI (gh auth login)
set -e

REPO="wodoame/BEM14-photo-uploader-app"
STACK_NAME="photo-uploader-stack"
AWS_PROFILE="admin"
AWS_REGION="us-east-1"

root_output() {
  aws cloudformation describe-stacks --stack-name "$STACK_NAME" \
    --profile "$AWS_PROFILE" \
    --query "Stacks[0].Outputs[?OutputKey=='$1'].OutputValue" \
    --output text
}

child_stack() {
  aws cloudformation list-stack-resources --stack-name "$STACK_NAME" \
    --profile "$AWS_PROFILE" \
    --query "StackResourceSummaries[?LogicalResourceId=='$1'].PhysicalResourceId" \
    --output text
}

child_output() {
  aws cloudformation describe-stacks --stack-name "$1" \
    --profile "$AWS_PROFILE" \
    --query "Stacks[0].Outputs[?OutputKey=='$2'].OutputValue" \
    --output text
}

echo "Fetching values from CloudFormation..."

IAM_STACK=$(child_stack IAMStack)
DB_STACK=$(child_stack DatabaseStack)

ECR_REPO_URI=$(root_output ECRRepositoryUri)
ECR_REPO_NAME="${ECR_REPO_URI##*/}"

echo "Setting variables..."
gh variable set AWS_REGION            --body "$AWS_REGION"                            --repo "$REPO"
gh variable set ECR_REPOSITORY_NAME   --body "$ECR_REPO_NAME"                         --repo "$REPO"
gh variable set S3_BUCKET             --body "$(root_output ImageBucketName)"         --repo "$REPO"
gh variable set CLOUDFRONT_DOMAIN     --body "$(root_output CloudFrontDomainName)"    --repo "$REPO"
gh variable set ARTIFACT_BUCKET_NAME  --body "$(root_output ArtifactBucketName)"      --repo "$REPO"
gh variable set DB_HOST               --body "$(child_output "$DB_STACK" DBEndpoint)" --repo "$REPO"
gh variable set DB_PORT               --body "$(child_output "$DB_STACK" DBPort)"     --repo "$REPO"
gh variable set DB_NAME               --body "$(child_output "$DB_STACK" DBName)"     --repo "$REPO"

echo "Setting secrets..."
gh secret set AWS_ROLE_ARN                --body "$(child_output "$IAM_STACK" GitHubActionsRoleArn)"    --repo "$REPO"
gh secret set ECS_TASK_EXECUTION_ROLE_ARN --body "$(child_output "$IAM_STACK" ECSTaskExecutionRoleArn)" --repo "$REPO"
gh secret set ECS_TASK_ROLE_ARN           --body "$(child_output "$IAM_STACK" ECSTaskRoleArn)"          --repo "$REPO"
gh secret set DB_SECRET_ARN               --body "$(child_output "$DB_STACK" DBSecretArn)"              --repo "$REPO"

echo "Done."
