resource "aws_mq_broker" "rabbitmq" {
  broker_name        = "${var.project_name}-${var.environment}-mq"
  engine_type        = "RabbitMQ"
  engine_version     = "4.2"
  host_instance_type = "mq.m7g.medium"

  auto_minor_version_upgrade = true

  deployment_mode = var.environment == "prod" ? "ACTIVE_STANDBY_MULTI_AZ" : "SINGLE_INSTANCE"

  publicly_accessible = false
  security_groups = [var.mq_sq_id]

  subnet_ids = var.environment == "prod" ? [var.database_subnets[0], var.database_subnets[1]] : [var.database_subnets[0]]

  user {
    password = var.mq_password
    username = var.mq_username
  }

  logs {
    general = true
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-mq"
  }
}

