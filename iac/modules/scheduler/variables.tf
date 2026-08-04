variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, qa, prod)"
  type        = string
}

variable "ecs_cluster_name" {
  description = "Nombre del cluster ECS que contiene el servicio a escalar"
  type        = string
}

variable "ecs_service_name" {
  description = "Nombre del servicio ECS a escalar entre 0 y 1 tareas"
  type        = string
}

variable "ecs_service_arn" {
  description = "ARN del servicio ECS, para acotar el permiso ecs:UpdateService"
  type        = string
}

variable "db_instance_identifier" {
  description = "Identificador de la instancia RDS a arrancar y parar"
  type        = string
}

variable "db_instance_arn" {
  description = "ARN de la instancia RDS, para acotar los permisos rds:Start/StopDBInstance"
  type        = string
}

variable "hora_apertura" {
  description = "Hora de Lima (0-23) a la que el servicio pasa a 1 tarea"
  type        = number
  default     = 18

  validation {
    condition     = var.hora_apertura >= 0 && var.hora_apertura <= 23 && floor(var.hora_apertura) == var.hora_apertura
    error_message = "hora_apertura debe ser un entero entre 0 y 23."
  }
}

variable "hora_cierre" {
  description = "Hora de Lima (0-23) a la que el servicio vuelve a 0 tareas. 0 equivale a las 24:00."
  type        = number
  default     = 0

  validation {
    condition     = var.hora_cierre >= 0 && var.hora_cierre <= 23 && floor(var.hora_cierre) == var.hora_cierre
    error_message = "hora_cierre debe ser un entero entre 0 y 23."
  }
}

variable "habilitado" {
  description = "Si es false las programaciones se crean en estado DISABLED, dejando el servicio disponible 24h sin destruir recursos."
  type        = bool
  default     = true
}

variable "zona_horaria" {
  description = "Zona horaria en la que se interpretan las expresiones cron. Debe coincidir con la del dominio (America/Lima)."
  type        = string
  default     = "America/Lima"
}
