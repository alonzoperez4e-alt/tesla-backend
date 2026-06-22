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
      image = "jmalloc/echo-server:latest"
      essential = true

      environment = [
        {
          name = "SPRING_PROFILES_ACTIVE"
          value = var.environment
        },
        {
          name = "COGNITO_ISSUER_URI"
          value = var.cognito_issuer_uri
        },
        {
          name = "DB_URL"
          value = "jdbc:postgresql://${var.postgres_endpoint}/tesladb"
        },
        {
          name = "DB_USER"
          value = var.postgres_username
        },
        {
          name = "REDIS_HOST"
          value = var.redis_endpoint
        },
        {
          name  = "REDIS_PORT"
          value = "6379"
        },
        {name = "MQ_HOST"
        value = replace(replace(var.mq_endpoint, "ampqs://", ""), ":5671", "")
        },
        {
          name  = "MQ_PORT"
          value = "61614"
        },
        {
          name = "MQ_USERNAME"
          value = var.mq_username
        }
      ]

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