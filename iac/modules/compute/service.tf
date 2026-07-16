resource "aws_ecs_service" "api" {
  name = "${var.project_name}-${var.environment}-service"
  cluster = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  # Valor inicial al crear el servicio; a partir de ahi lo gobierna el
  # autoscaling (ver ignore_changes y autoscaling.tf).
  desired_count = var.min_capacity
  launch_type = "FARGATE"
  health_check_grace_period_seconds = 180

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

  lifecycle {
    # task_definition: la imagen real la despliega el pipeline (deploy job).
    # desired_count: lo gobierna el autoscaling; no revertir en cada apply.
    ignore_changes = [task_definition, desired_count]
  }
}