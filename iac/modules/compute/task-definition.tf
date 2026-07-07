resource "aws_ecs_task_definition" "api" {
  family                   = "${var.project_name}-${var.environment}-api-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "init-volume-permissions"
      image     = "alpine:latest"
      essential = false
      user      = "root"
      command   = ["chmod", "777", "/tmp"]

      mountPoints = [
        {
          sourceVolume  = "tomcat-tmp"
          containerPath = "/tmp"
          readOnly      = false
        }
      ]
    },

    {
      name      = "backend"
      image     = "jmalloc/echo-server:latest"
      essential = true

      readonlyRootFilesystem = true

      dependsOn = [
        {
          containerName = "init-volume-permissions"
          condition     = "SUCCESS"
        }
      ]

      mountPoints = [
        {
          sourceVolume  = "tomcat-tmp"
          containerPath = "/tmp"
          readOnly      = false
        }
      ]

      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = var.environment
        },
        {
          name  = "COGNITO_ISSUER_URI"
          value = var.cognito_issuer_uri
        },
        {
          name  = "COGNITO_USER_POOL_ID"
          value = var.cognito_user_pool_id
        },
        {
          name  = "DB_URL"
          value = "jdbc:postgresql://${var.postgres_endpoint}/tesladb"
        },
        {
          name  = "DB_USER"
          value = var.postgres_username
        },
        {
          name  = "REDIS_HOST"
          value = var.redis_endpoint
        },
        {
          name  = "REDIS_PORT"
          value = "6379"
        },
        {
          name  = "MQ_HOST"
          value = var.mq_endpoint
        },
        {
          name  = "MQ_PORT"
          value = "61614"
        },
        {
          name  = "MQ_USERNAME"
          value = var.mq_username
        },
        {
          name  = "S3_IMAGE_BUCKET_NAME"
          value = var.s3_images_bucket_name
        },
        {
          name  = "CLOUDFRONT_STORAGE_URL"
          value = var.cloudfront_domain_name
        },
        {
          name  = "CORS_ALLOWED_ORIGINS"
          value = var.cors_allowed_origins
        }
      ]

      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
          protocol      = "tcp"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  volume {
    name = "tomcat-tmp"
  }
}
