variable "project_name" {
  description = "Nombre del proyecto"
  type = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, qa, prod)"
  type = string
}

variable "alb_dns_name" {
  description = "El DNS publico del Load Balancer para conectarlo a CloudFront"
  type = string
}

variable "alb_secret_token" {
  description = "Token secreto para validar la conexion entre CloudFront y el ALB"
  type = string
}