# Autoescalado del servicio ECS por seguimiento de objetivo (target tracking).
#
# Metrica: CPU promedio del servicio (ECSServiceAverageCPUUtilization).
# Se usa CPU y NO memoria a proposito: la JVM reserva el heap y no lo devuelve
# al SO, por lo que la memoria del contenedor queda alta y plana con o sin
# carga -> un scale-in por memoria casi nunca dispararia y se pagarian tareas
# de mas. La CPU sigue el trabajo real (peticiones), dando scale-out y scale-in
# limpios.
#
# min_capacity >= 2 mantiene tareas en las 2 AZs (alta disponibilidad); el
# scale-out agrega tareas ante carga (tolerancia a carga). El desired_count del
# servicio queda gobernado por este autoscaler (ver ignore_changes en service.tf).

resource "aws_appautoscaling_target" "ecs" {
  max_capacity       = var.max_capacity
  min_capacity       = var.min_capacity
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.api.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "ecs_cpu" {
  name               = "${var.project_name}-${var.environment}-cpu-target-tracking"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }

    target_value = var.cpu_target_value

    # Sube rapido ante carga, baja con calma para evitar oscilaciones (flapping).
    scale_out_cooldown = 60
    scale_in_cooldown  = 300
  }
}
