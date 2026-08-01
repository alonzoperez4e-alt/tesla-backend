variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, qa, prod)"
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block para la VPC"
  type        = string
}

variable "public_subnets_cidr" {
  description = "CIDR blocks para las subredes públicas"
  type        = list(string)
}

variable "database_subnets_cidr" {
  description = "CIDR blocks para las subredes de base de datos"
  type        = list(string)
}
