resource "aws_ecs_service" "api" {
  name            = "${var.project_name}-${var.environment}-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.public_subnets
    security_groups  = [var.ecs_sg_id]
    assign_public_ip = true
  }

  # Con registros SRV hay que indicar de que contenedor/puerto se toma el destino.
  service_registries {
    registry_arn   = aws_service_discovery_service.backend.arn
    container_name = "backend"
    container_port = 8080
  }

  # Corta los despliegues que no llegan a arrancar y devuelve el servicio a la
  # ultima revision que si funciono. Sin esto, una imagen que muere al arrancar
  # deja a ECS reintentando indefinidamente y el servicio sin tareas sanas.
  #
  # Como no hay balanceador, ECS solo cuenta tareas que no alcanzan el estado
  # RUNNING o que el HEALTHCHECK del contenedor marca como unhealthy. El umbral
  # es max(3, desired_count * 0.5), es decir 3 fallos con una sola tarea.
  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }
}