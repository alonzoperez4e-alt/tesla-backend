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

# --- Ventana de disponibilidad ---
# El backend solo se mantiene encendido entre estas dos horas (hora de Lima); fuera
# de la franja el modulo scheduler apaga la task ECS y la instancia RDS, y
# CloudFront responde 503 en /api/*.

variable "ventana_hora_apertura" {
  description = "Hora de Lima (0-23) en la que se enciende el servicio"
  type        = number
  default     = 18
}

variable "ventana_hora_cierre" {
  description = "Hora de Lima (0-23) en la que se apaga el servicio. 0 equivale a las 24:00."
  type        = number
  default     = 0
}

variable "ventana_habilitada" {
  description = "Poner a false para volver a disponibilidad 24h sin destruir las programaciones."
  type        = bool
  default     = true
}
