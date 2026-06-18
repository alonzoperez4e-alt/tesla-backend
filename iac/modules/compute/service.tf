resource "aws_ecs_service" "api" {
  name = "${var.project_name}-${var.environment}-service"
  cluster = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count = 1
  launch_type = "FARGATE"
  health_check_grace_period_seconds = 120

  network_configuration {
    subnets = var.private_subnets
    security_groups = [var.ecs_sg_id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.api.arn
    container_name = "backend"
    container_port = 8080
  }

  depends_on = [aws_lb_listener.api]
}