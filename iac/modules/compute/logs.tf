resource "aws_cloudwatch_log_group" "ecs_logs" {
  name = "/ecs/${var.project_name}-${var.environment}/backend"
  retention_in_days = 7
}