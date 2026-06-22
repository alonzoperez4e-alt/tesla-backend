variable "project_name" {
  description = "Nombre del proyecto"
  type = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, qa, prod)"
  type = string
}

variable "database_subnet_group_name" {
  description = "Nombre del grupo de subredes para la base de datos"
  type = string
}

variable "database_subnets" {
  description = "Lista de subredes para la base de datos"
  type = list(string)
}

variable "database_sg_id" {
  description = "ID del grupo de seguridad para la base de datos"
  type = string
}

variable "redis_sg_id" {
  description = "ID del grupo de seguridad para Redis"
  type = string
}

variable "db_username" {
  description = "Nombre de usuario para la base de datos"
  type = string
}

variable "db_password" {
  description = "Contraseña para la base de datos"
  type      = string
  sensitive = true
}

variable "mq_sq_id" {
  description = "ID del grupo de seguridad para el servicio de mensajería"
  type = string
}

variable "mq_username" {
  description = "Nombre de usuario para el servicio de mensajería"
  type = string
}

variable "mq_password" {
  description = "Contraseña para el servicio de mensajería"
  type = string
  sensitive = true
}

variable "mq_endpoint" {
  description = "Endpoint del servicio de mensajería"
  type = string
}