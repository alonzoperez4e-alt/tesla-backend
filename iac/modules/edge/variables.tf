variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, qa, prod)"
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
