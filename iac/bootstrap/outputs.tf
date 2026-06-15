output "terraform_state_bucket" {
  value = aws_s3_bucket.terraform_state.bucket
}

output "terraform_locks_table" {
  value = aws_dynamodb_table.terraform_locks.name
}

output "github_actions_role_arn" {
  value = {
    for env, role in aws_iam_role.github_actions_roles :
    env => role.arn
  }
}
