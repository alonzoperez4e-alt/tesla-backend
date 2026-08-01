variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, qa, prod)"
  type        = string
}

variable "aws_region" {
  description = "Región de AWS donde se desplegarán los recursos"
  type        = string
}

variable "prefix" {
  description = "Prefijo para los nombres de los recursos"
  type        = string
}

variable "allowed_callback_urls" {
  description = "URLs permitidas para el redireccionamiento después del login"
  type        = list(string)
}

variable "db_username" {
  description = "Usuario administrador de la base de datos"
  type        = string
  default     = "tesla_admin"
}

variable "db_password" {
  description = "Contraseña de la base de datos (Inyectada por entorno)"
  type        = string
  sensitive   = true
}

variable "origin_secret_token" {
  description = "Token para validar que el trafico viene de CloudFront"
  type        = string
  sensitive   = true
}

variable "extra_cors_origins" {
  description = "Origins adicionales de CORS (dev local, despliegues paralelos en Vercel, etc.)"
  type        = list(string)
  default     = ["http://localhost:5173"]
}

variable "alarm_email" {
  description = "Email para recibir las alarmas de CloudWatch (SNS). Vacio = sin suscripcion."
  type        = string
  default     = ""
}
