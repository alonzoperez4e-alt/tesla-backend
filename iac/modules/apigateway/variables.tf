variable "project_name" {
  description = "Nombre del proyecto"
  type = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, qa, prod)"
  type = string
}

variable "vpc_id" {
  description = "ID de la VPC donde se desplegarán los recursos de cómputo"
  type = string
}

variable "vpc_link_sg_id" {
  description = "ID del grupo de seguridad para vpc link"
  type = string
}

variable "public_subnets" {
  type = list(string)
}

variable "service_discovery_arn" {
  description = "ARN del servicio de Cloud Map exportado desde el módulo compute"
}