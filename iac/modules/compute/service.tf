resource "aws_ecs_service" "api" {
  name = "${var.project_name}-${var.environment}-service"
  cluster = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count = 1
  launch_type = "FARGATE"

  network_configuration {
    subnets = var.public_subnets
    security_groups = [var.ecs_sg_id]
    assign_public_ip = true
  }

  service_registries {
    registry_arn = aws_service_discovery_service.backend.arn
  }

  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }
}