variable "prefix" {
  description = "Prefijo para los nombres de los recursos"
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block para la VPC"
  type = string
}

variable "public_subnets_cidr" {
  description = "CIDR blocks para las subredes públicas"
  type = list(string)
}

variable "private_subnets_cidr" {
  description = "CIDR blocks para las subredes privadas"
  type = list(string)
}

variable "database_subnets_cidr" {
    description = "CIDR blocks para las subredes de base de datos"
  type = list(string)
}
