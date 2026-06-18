resource "aws_ecs_task_definition" "api" {
  family = "${var.project_name}-${var.environment}-api-task"
  network_mode = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu = "512"
  memory = "1024"

  execution_role_arn = aws_iam_role.ecs_execution_role.arn
  task_role_arn = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name = "backend"
      image = "jmalloc/echo-server:latest" # placeholder para el health check
      essential = true
      portMappings = [
        {
          containerPort = 8080
          hostPort = 8080
          protocol = "tcp"
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group" = aws_cloudwatch_log_group.ecs_logs.name
          "awslogs-region" = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}