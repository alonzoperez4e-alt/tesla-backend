variable "project_name" {
  description = "Nombre del proyecto"
  type = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, qa, prod)"
  type = string
}

variable "vpc_id" {
  description = "ID de la VPC donde se aplicarán las reglas de seguridad"
  type = string
}