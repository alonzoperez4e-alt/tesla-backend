# Ventana de disponibilidad del backend.
#
# La plataforma solo se usa por las tardes, asi que fuera de la franja
# hora_apertura-hora_cierre (18:00-24:00 en Lima por defecto) se apagan los dos
# unicos recursos que facturan por hora: la task de Fargate y la instancia RDS.
#
# Se usa EventBridge Scheduler con "universal targets" (llamadas directas a la API
# del SDK) en lugar de Application Auto Scaling porque el mismo patron cubre ECS y
# RDS con un solo rol y una sola semantica, sin necesitar un scalable target.
#
# Las expresiones se escriben en hora de Lima gracias a schedule_expression_timezone.
# Peru no tiene horario de verano, pero declararlo explicitamente evita tener que
# razonar en UTC al leer el codigo.
#
# El servicio ECS tiene ignore_changes = [desired_count] (modules/compute/service.tf),
# de modo que un terraform apply posterior no revierte lo que haga el scheduler.

locals {
  # La base de datos arranca 20 minutos antes de la apertura: RDS tarda entre 3 y
  # 10 minutos en levantar una db.t4g.micro y la aplicacion no arranca si no puede
  # conectar. Como el desfase es menor que una hora, basta con restar una hora a la
  # apertura y fijar el minuto 40.
  hora_arranque_bd = (var.hora_apertura - 1 + 24) % 24

  cron_arrancar_bd = "cron(40 ${local.hora_arranque_bd} * * ? *)"
  cron_abrir       = "cron(0 ${var.hora_apertura} * * ? *)"
  cron_cerrar      = "cron(0 ${var.hora_cierre} * * ? *)"
  # La BD se para 10 minutos despues del cierre para no cortar conexiones vivas.
  cron_parar_bd = "cron(10 ${var.hora_cierre} * * ? *)"

  estado = var.habilitado ? "ENABLED" : "DISABLED"
}

resource "aws_scheduler_schedule_group" "ventana" {
  name = "${var.project_name}-${var.environment}-ventana"
}

resource "aws_scheduler_schedule" "arrancar_bd" {
  name        = "${var.project_name}-${var.environment}-arrancar-bd"
  description = "Arranca la instancia RDS antes de abrir la ventana de servicio"
  group_name  = aws_scheduler_schedule_group.ventana.name
  state       = local.estado

  schedule_expression          = local.cron_arrancar_bd
  schedule_expression_timezone = var.zona_horaria

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = "arn:aws:scheduler:::aws-sdk:rds:startDBInstance"
    role_arn = aws_iam_role.scheduler.arn

    # Ojo: los nombres del input son los del SDK, no los de la API query de RDS
    # (DbInstanceIdentifier, no DBInstanceIdentifier).
    input = jsonencode({
      DbInstanceIdentifier = var.db_instance_identifier
    })

    retry_policy {
      maximum_retry_attempts       = 3
      maximum_event_age_in_seconds = 3600
    }
  }
}

resource "aws_scheduler_schedule" "abrir" {
  name        = "${var.project_name}-${var.environment}-abrir-servicio"
  description = "Escala el servicio ECS a 1 tarea al abrir la ventana de servicio"
  group_name  = aws_scheduler_schedule_group.ventana.name
  state       = local.estado

  schedule_expression          = local.cron_abrir
  schedule_expression_timezone = var.zona_horaria

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = "arn:aws:scheduler:::aws-sdk:ecs:updateService"
    role_arn = aws_iam_role.scheduler.arn

    input = jsonencode({
      Cluster      = var.ecs_cluster_name
      Service      = var.ecs_service_name
      DesiredCount = 1
    })

    retry_policy {
      maximum_retry_attempts       = 3
      maximum_event_age_in_seconds = 3600
    }
  }
}

resource "aws_scheduler_schedule" "cerrar" {
  name        = "${var.project_name}-${var.environment}-cerrar-servicio"
  description = "Escala el servicio ECS a 0 tareas al cerrar la ventana de servicio"
  group_name  = aws_scheduler_schedule_group.ventana.name
  state       = local.estado

  schedule_expression          = local.cron_cerrar
  schedule_expression_timezone = var.zona_horaria

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = "arn:aws:scheduler:::aws-sdk:ecs:updateService"
    role_arn = aws_iam_role.scheduler.arn

    input = jsonencode({
      Cluster      = var.ecs_cluster_name
      Service      = var.ecs_service_name
      DesiredCount = 0
    })

    retry_policy {
      maximum_retry_attempts       = 3
      maximum_event_age_in_seconds = 3600
    }
  }
}

resource "aws_scheduler_schedule" "parar_bd" {
  name        = "${var.project_name}-${var.environment}-parar-bd"
  description = "Para la instancia RDS despues de cerrar la ventana de servicio"
  group_name  = aws_scheduler_schedule_group.ventana.name
  state       = local.estado

  schedule_expression          = local.cron_parar_bd
  schedule_expression_timezone = var.zona_horaria

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = "arn:aws:scheduler:::aws-sdk:rds:stopDBInstance"
    role_arn = aws_iam_role.scheduler.arn

    input = jsonencode({
      DbInstanceIdentifier = var.db_instance_identifier
    })

    retry_policy {
      maximum_retry_attempts       = 3
      maximum_event_age_in_seconds = 3600
    }
  }
}
