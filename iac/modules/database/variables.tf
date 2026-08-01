variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, qa, prod)"
  type        = string
}

variable "database_subnet_group_name" {
  description = "Nombre del grupo de subredes para la base de datos"
  type        = string
}

variable "database_sg_id" {
  description = "ID del grupo de seguridad para la base de datos"
  type        = string
}

variable "db_username" {
  description = "Nombre de usuario para la base de datos"
  type        = string
}

variable "db_password" {
  description = "Contraseña para la base de datos"
  type        = string
  sensitive   = true
}
