# Panel unico de observabilidad: metricas de infraestructura (ECS) y una vista de
# logs (ERROR/WARN) en un solo dashboard de CloudWatch. Los widgets de ALB se
# eliminaron con el balanceador; los de API Gateway se anadiran cuando el modulo
# apigateway quede instanciado.
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${var.project_name}-${var.environment}"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 24
        height = 6
        properties = {
          title  = "ECS - CPU y Memoria (%)"
          region = var.aws_region
          view   = "timeSeries"
          stat   = "Average"
          period = 60
          metrics = [
            ["AWS/ECS", "CPUUtilization", "ClusterName", aws_ecs_cluster.main.name, "ServiceName", aws_ecs_service.api.name],
            ["AWS/ECS", "MemoryUtilization", "ClusterName", aws_ecs_cluster.main.name, "ServiceName", aws_ecs_service.api.name]
          ]
        }
      },
      {
        type   = "log"
        x      = 0
        y      = 6
        width  = 24
        height = 6
        properties = {
          title  = "Logs recientes (${var.environment == "dev" ? "INFO / WARN / ERROR" : "ERROR / WARN"})"
          region = var.aws_region
          view   = "table"
          query  = var.environment == "dev" ? "SOURCE '${aws_cloudwatch_log_group.ecs_logs.name}' | fields @timestamp, @message | filter @message like /(?i)(error|warn|info)/ | sort @timestamp desc | limit 50" : "SOURCE '${aws_cloudwatch_log_group.ecs_logs.name}' | fields @timestamp, @message | filter @message like /(?i)(error|warn)/ | sort @timestamp desc | limit 50"
        }
      }
    ]
  })
}
