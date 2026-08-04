variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, prod)"
  type        = string
}

variable "aws_region" {
  description = "Región de AWS donde se desplegarán los recursos"
  type        = string
}

variable "vpc_id" {
  description = "ID de la VPC donde se desplegarán los recursos de cómputo"
  type        = string
}

variable "public_subnets" {
  description = "Lista de subredes públicas para los recursos de cómputo"
  type        = list(string)
}

variable "ecs_sg_id" {
  description = "ID del grupo de seguridad para los servicios de ECS"
  type        = string
}

variable "postgres_endpoint" {
  description = "Endpoint de la base de datos PostgreSQL"
  type        = string
}

variable "postgres_username" {
  description = "Nombre de usuario para la base de datos PostgreSQL"
  type        = string
}

variable "db_password" {
  description = "Password de PostgreSQL. Se persiste en SSM Parameter Store (SecureString) y se inyecta a la task via 'secrets', no en texto plano."
  type        = string
  sensitive   = true
}

variable "origin_secret_token" {
  description = "Token que CloudFront inyecta como X-Tesla-Origin-Token. Se persiste en SSM (SecureString) y llega a la task como ORIGIN_TOKEN via 'secrets'."
  type        = string
  sensitive   = true
}

variable "cognito_issuer_uri" {
  description = "URI del issuer de Cognito para la autenticación JWT"
  type        = string
}

variable "cognito_user_pool_id" {
  description = "ID del User Pool de Cognito, usado por el backend para llamar a las Admin APIs (AdminCreateUser, etc.)"
  type        = string
}

variable "cognito_user_pool_arn" {
  description = "ARN del User Pool de Cognito, para acotar los permisos IAM del task role"
  type        = string
}

variable "s3_images_bucket_name" {
  description = "Nombre del bucket S3 para almacenamiento de imágenes"
  type        = string
}

variable "cdn_public_base_url" {
  description = "URL base publica del CDN (CloudFront) usada por el backend para resolver URLs de imagenes"
  type        = string
}

variable "cors_allowed_origins" {
  description = "Lista de origins permitidos para CORS, separados por coma"
  type        = string
}

variable "alarm_email" {
  description = "Email al que se suscriben las alarmas de CloudWatch (SNS). Vacio = sin suscripcion."
  type        = string
  default     = ""
}
