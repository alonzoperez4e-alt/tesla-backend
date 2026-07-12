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
# Usan expresiones SEARCH agregadas con SUM para resolver una sola serie
# (requisito de las alarmas sobre metric math). Si aun no llegan metricas, la
# busqueda queda vacia y treat_missing_data=notBreaching mantiene la alarma en OK.

# Saturacion del pool de conexiones: hay peticiones esperando una conexion libre
# a la BD (hikaricp.connections.pending > 0 sostenido). Sintoma temprano de
# cuello de botella en BD antes de que se disparen timeouts/errores 5XX.
resource "aws_cloudwatch_metric_alarm" "app_hikari_pending" {
  alarm_name          = "${var.project_name}-${var.environment}-app-hikari-pending"
  alarm_description   = "Conexiones en espera del pool HikariCP (saturacion de BD)"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 3
  threshold           = 0
  treat_missing_data  = "notBreaching"

  metric_query {
    id          = "pending"
    label       = "HikariCP pending"
    return_data = true
    expression  = "SUM(SEARCH('Namespace=\"${local.app_metrics_namespace}\" MetricName=\"hikaricp.connections.pending.value\"', 'Average', 60))"
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]
}

# Presion de memoria de la JVM: heap usado por encima de ~85% del heap maximo.
# Con memoria de task=1024 MB y -XX:MaxRAMPercentage=75, el heap maximo ronda los
# 768 MiB; el umbral (~653 MiB) avisa antes de que la MemoryUtilization del
# contenedor (alarma ecs-memory-high) llegue al limite y ECS reinicie la task.
resource "aws_cloudwatch_metric_alarm" "app_jvm_heap_high" {
  alarm_name          = "${var.project_name}-${var.environment}-app-jvm-heap-high"
  alarm_description   = "Heap de la JVM por encima de ~85% del maximo (riesgo de OOM)"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 5
  threshold           = 685128089 # ~653 MiB (85% de 768 MiB)
  treat_missing_data  = "notBreaching"

  metric_query {
    id          = "heap_used"
    label       = "Heap usado (bytes)"
    return_data = true
    expression  = "SUM(SEARCH('Namespace=\"${local.app_metrics_namespace}\" MetricName=\"jvm.memory.used.value\" area=\"heap\"', 'Average', 60))"
  }

  alarm_actions = [aws_sns_topic.alerts.arn]
  ok_actions    = [aws_sns_topic.alerts.arn]
}
