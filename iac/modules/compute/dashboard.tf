# Panel unico de observabilidad: metricas de aplicacion (ALB), de infraestructura
# (ECS) y una vista de logs (ERROR/WARN) en un solo dashboard de CloudWatch.
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${var.project_name}-${var.environment}"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
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
        type   = "metric"
        x      = 12
        y      = 0
        width  = 12
        height = 6
        properties = {
          title  = "ALB - Requests y latencia"
          region = var.aws_region
          view   = "timeSeries"
          period = 60
          metrics = [
            ["AWS/ApplicationELB", "RequestCount", "LoadBalancer", aws_lb.api.arn_suffix, { stat = "Sum" }],
            ["AWS/ApplicationELB", "TargetResponseTime", "LoadBalancer", aws_lb.api.arn_suffix, { stat = "Average", yAxis = "right" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "ALB - Codigos de error"
          region = var.aws_region
          view   = "timeSeries"
          stat   = "Sum"
          period = 60
          metrics = [
            ["AWS/ApplicationELB", "HTTPCode_Target_5XX_Count", "LoadBalancer", aws_lb.api.arn_suffix],
            ["AWS/ApplicationELB", "HTTPCode_Target_4XX_Count", "LoadBalancer", aws_lb.api.arn_suffix],
            ["AWS/ApplicationELB", "HTTPCode_ELB_5XX_Count", "LoadBalancer", aws_lb.api.arn_suffix]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "ALB - Hosts saludables / no saludables"
          region = var.aws_region
          view   = "timeSeries"
          stat   = "Maximum"
          period = 60
          metrics = [
            ["AWS/ApplicationELB", "HealthyHostCount", "LoadBalancer", aws_lb.api.arn_suffix, "TargetGroup", aws_lb_target_group.api.arn_suffix],
            ["AWS/ApplicationELB", "UnHealthyHostCount", "LoadBalancer", aws_lb.api.arn_suffix, "TargetGroup", aws_lb_target_group.api.arn_suffix]
          ]
        }
      },
      {
        type   = "log"
        x      = 0
        y      = 12
        width  = 24
        height = 6
        properties = {
          title  = "Logs recientes (ERROR / WARN)"
          region = var.aws_region
          view   = "table"
          query  = "SOURCE '${aws_cloudwatch_log_group.ecs_logs.name}' | fields @timestamp, @message | filter @message like /(?i)(error|warn)/ | sort @timestamp desc | limit 50"
        }
      },
      # --- Metricas de aplicacion (Micrometer -> CloudWatch) ---
      # Usan expresiones SEARCH por nombre/namespace para tolerar la cardinalidad
      # de dimensiones (uri, status, pool, id...) sin enumerarlas.
      {
        type   = "metric"
        x      = 0
        y      = 18
        width  = 12
        height = 6
        properties = {
          title   = "App - JVM Heap usado (bytes)"
          region  = var.aws_region
          view    = "timeSeries"
          stacked = true
          period  = 60
          metrics = [
            [{ expression = "SEARCH('Namespace=\"${local.app_metrics_namespace}\" MetricName=\"jvm.memory.used.value\" area=\"heap\"', 'Average', 60)", id = "heap", label = "Heap por pool" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 18
        width  = 12
        height = 6
        properties = {
          title  = "App - HikariCP conexiones"
          region = var.aws_region
          view   = "timeSeries"
          period = 60
          metrics = [
            [{ expression = "SEARCH('Namespace=\"${local.app_metrics_namespace}\" MetricName=\"hikaricp.connections.active.value\"', 'Average', 60)", id = "hk_active", label = "Activas" }],
            [{ expression = "SEARCH('Namespace=\"${local.app_metrics_namespace}\" MetricName=\"hikaricp.connections.idle.value\"', 'Average', 60)", id = "hk_idle", label = "Idle" }],
            [{ expression = "SEARCH('Namespace=\"${local.app_metrics_namespace}\" MetricName=\"hikaricp.connections.pending.value\"', 'Average', 60)", id = "hk_pending", label = "Pending" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 24
        width  = 12
        height = 6
        properties = {
          title  = "App - HTTP requests y latencia"
          region = var.aws_region
          view   = "timeSeries"
          period = 60
          metrics = [
            [{ expression = "SUM(SEARCH('Namespace=\"${local.app_metrics_namespace}\" MetricName=\"http.server.requests.count\"', 'Sum', 60))", id = "http_req", label = "Requests/min" }],
            [{ expression = "MAX(SEARCH('Namespace=\"${local.app_metrics_namespace}\" MetricName=\"http.server.requests.max\"', 'Average', 60))", id = "http_lat", label = "Latencia max", yAxis = "right" }]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 24
        width  = 12
        height = 6
        properties = {
          title  = "App - GC pause y CPU del proceso"
          region = var.aws_region
          view   = "timeSeries"
          period = 60
          metrics = [
            [{ expression = "SUM(SEARCH('Namespace=\"${local.app_metrics_namespace}\" MetricName=\"jvm.gc.pause.sum\"', 'Average', 60))", id = "gc_pause", label = "GC pause (sum)" }],
            [{ expression = "SEARCH('Namespace=\"${local.app_metrics_namespace}\" MetricName=\"process.cpu.usage.value\"', 'Average', 60)", id = "proc_cpu", label = "CPU proceso", yAxis = "right" }]
          ]
        }
      }
    ]
  })
}
