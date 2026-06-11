variable "prefix" {
  description = "Prefijo para los nombres de los recursos (ej. academia-tesla)"
  type        = string
}

variable "environment" {
  description = "Entorno actual de despliegue (dev, qa, prod)"
  type        = string
}