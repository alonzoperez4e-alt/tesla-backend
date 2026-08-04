variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, prod)"
  type        = string
}

variable "vpc_link_sg_id" {
  description = "ID del grupo de seguridad para el VPC Link"
  type        = string
}

variable "public_subnets" {
  description = "Subredes donde se crean las ENIs del VPC Link"
  type        = list(string)
}

variable "service_discovery_arn" {
  description = "ARN del servicio de Cloud Map exportado desde el módulo compute"
  type        = string
}
