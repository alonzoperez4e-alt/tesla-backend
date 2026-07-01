variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, qa, prod)"
  type        = string
}

variable "alert_emails" {
  description = "Lista de emails del equipo que recibirán las alertas SNS (requieren confirmación manual)"
  type        = list(string)
}

variable "ecs_cluster_name" {
  description = "Nombre del cluster ECS (solo informativo / tagging, Container Insights se activa en el propio recurso del cluster)"
  type        = string
}

variable "ecs_service_name" {
  description = "Nombre del servicio ECS"
  type        = string
}

variable "alb_arn_suffix" {
  description = "ARN suffix del ALB para las dimensiones de CloudWatch"
  type        = string
}

variable "target_group_arn_suffix" {
  description = "ARN suffix del Target Group para las dimensiones de CloudWatch"
  type        = string
}

variable "desired_task_count" {
  description = "Cantidad deseada de tareas ECS detrás del ALB (umbral para la alarma de HealthyHostCount)"
  type        = number
  default     = 1
}

variable "postgres_instance_id" {
  description = "Identifier de la instancia RDS Postgres"
  type        = string
}

variable "rds_max_connections_threshold" {
  description = "Umbral de conexiones simultáneas a RDS (≈80% del max_connections de la instancia)"
  type        = number
  default     = 80
}

variable "redis_replication_group_id" {
  description = "Replication Group ID de ElastiCache Redis"
  type        = string
}