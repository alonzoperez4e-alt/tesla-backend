# Namespace de las metricas de aplicacion (Micrometer -> CloudWatch). Debe
# coincidir con management.cloudwatch.metrics.export.namespace del perfil prod.
locals {
  app_metrics_namespace = "Tesla/Backend-${var.environment}"
}

# Topic SNS para notificar alarmas. Si se define alarm_email se suscribe por correo
# (requiere confirmar la suscripcion desde el email).
resource "aws_sns_topic" "alerts" {
  name = "${var.project_name}-${var.environment}-alerts"
}

resource "aws_sns_topic_subscription" "alerts_email" {
  count     = var.alarm_email != "" ? 1 : 0
  topic_arn = aws_sns_topic.alerts.arn
  protocol  = "email"
  endpoint  = var.alarm_email
}

# --- Alarmas del ALB ---

resource "aws_cloudwatch_metric_alarm" "alb_5xx" {
  alarm_name          = "${var.project_name}-${var.environment}-alb-5xx"
  alarm_description   = "Errores 5XX devueltos por el target (backend) via ALB"
  namespace           = "AWS/ApplicationELB"
  metric_name         = "HTTPCode_Target_5XX_Count"
  statistic           = "Sum"
  period              = 60
  evaluation_periods  = 5
  threshold           = 5
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"

  dimensions = {
    LoadBalancer = aws_lb.api.arn_suffix
    TargetGroup  = aws_lb_target_group.api.arn_suffix
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]
}

resource "aws_cloudwatch_metric_alarm" "alb_unhealthy_hosts" {
  alarm_name          = "${var.project_name}-${var.environment}-alb-unhealthy-hosts"
  alarm_description   = "Hay targets no saludables detras del ALB"
  namespace           = "AWS/ApplicationELB"
  metric_name         = "UnHealthyHostCount"
  statistic           = "Maximum"
  period              = 60
  evaluation_periods  = 3
  threshold           = 1
  comparison_operator = "GreaterThanOrEqualToThreshold"
  treat_missing_data  = "notBreaching"

  dimensions = {
    LoadBalancer = aws_lb.api.arn_suffix
    TargetGroup  = aws_lb_target_group.api.arn_suffix
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]
}

# --- Alarmas del servicio ECS ---

resource "aws_cloudwatch_metric_alarm" "ecs_cpu_high" {
  alarm_name          = "${var.project_name}-${var.environment}-ecs-cpu-high"
  alarm_description   = "CPU del servicio ECS por encima del 85%"
  namespace           = "AWS/ECS"
  metric_name         = "CPUUtilization"
  statistic           = "Average"
  period              = 60
  evaluation_periods  = 5
  threshold           = 85
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"

  dimensions = {
    ClusterName = aws_ecs_cluster.main.name
    ServiceName = aws_ecs_service.api.name
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]
}

resource "aws_cloudwatch_metric_alarm" "ecs_memory_high" {
  alarm_name          = "${var.project_name}-${var.environment}-ecs-memory-high"
  alarm_description   = "Memoria del servicio ECS por encima del 85%"
  namespace           = "AWS/ECS"
  metric_name         = "MemoryUtilization"
  statistic           = "Average"
  period              = 60
  evaluation_periods  = 5
  threshold           = 85
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"

  dimensions = {
    ClusterName = aws_ecs_cluster.main.name
    ServiceName = aws_ecs_service.api.name
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]
}

# --- Alarmas de aplicacion (metricas Micrometer -> CloudWatch) ---
# CloudWatch NO soporta expresiones SEARCH en alarmas (solo en dashboards), por lo
# que se referencian metricas directas con dimensiones exactas. Las metricas solo
# se exportan en prod (perfil prod); en dev la alarma queda sin datos y
# treat_missing_data=notBreaching la mantiene en OK.

# Saturacion del pool de conexiones: hay peticiones esperando una conexion libre
# a la BD (hikaricp.connections.pending > 0 sostenido). Sintoma temprano de
# cuello de botella en BD antes de que se disparen timeouts/errores 5XX.
# La dimension 'pool' es el nombre del pool HikariCP (spring.datasource.hikari.pool-name).
resource "aws_cloudwatch_metric_alarm" "app_hikari_pending" {
  alarm_name          = "${var.project_name}-${var.environment}-app-hikari-pending"
  alarm_description   = "Conexiones en espera del pool HikariCP (saturacion de BD)"
  namespace           = local.app_metrics_namespace
  metric_name         = "hikaricp.connections.pending.value"
  statistic           = "Maximum"
  period              = 60
  evaluation_periods  = 3
  threshold           = 0
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"

  dimensions = {
    pool = "TeslaHikariPool"
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]
}

# Nota: la alarma de heap JVM se descarto porque requeriria SEARCH (no soportado en
# alarmas) o enumerar los pools de memoria por 'id', cuyos nombres dependen del GC
# activo (G1/Serial) y la harian fragil. La presion de memoria del contenedor ya la
# cubre la alarma 'ecs-memory-high' (MemoryUtilization), que es lo que gatilla el
# reinicio de la task. El heap sigue visible en el widget del dashboard (que si
# soporta SEARCH).
