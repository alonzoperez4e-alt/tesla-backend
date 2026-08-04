variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, prod)"
  type        = string
}

variable "api_gateway_domain_name" {
  description = "Host del HTTP API de API Gateway (sin esquema), usado como origen del backend en CloudFront"
  type        = string
}

variable "origin_secret_token" {
  description = "Token secreto que CloudFront inyecta como X-Tesla-Origin-Token hacia el origen. La aplicacion lo valida para rechazar el trafico que no pase por el CDN."
  type        = string
  sensitive   = true
}

variable "hora_apertura" {
  description = "Hora de Lima (0-23) a partir de la cual /api/* llega al backend. Debe coincidir con la del modulo scheduler."
  type        = number
  default     = 18
}

variable "hora_cierre" {
  description = "Hora de Lima (0-23) a partir de la cual /api/* responde 503. 0 equivale a las 24:00. Debe coincidir con la del modulo scheduler."
  type        = number
  default     = 0

  validation {
    # La funcion del borde compara con un intervalo simple (apertura <= hora < cierre),
    # asi que no admite ventanas que crucen la medianoche.
    condition     = var.hora_cierre == 0 || var.hora_cierre > var.hora_apertura
    error_message = "La ventana no puede cruzar la medianoche: hora_cierre debe ser mayor que hora_apertura (o 0, que equivale a las 24:00)."
  }
}

variable "ventana_habilitada" {
  description = "Si es false no se asocia la funcion de horario y /api/* queda accesible las 24 horas."
  type        = bool
  default     = true
}
