variable "project_name" {
  description = "Nombre del proyecto"
  type = string
}

variable "environment" {
  description = "Entorno de despliegue (dev, qa, prod)"
  type = string
}

variable "aws_region" {
  description = "Región de AWS donde se desplegarán los recursos"
  type        = string
}

variable "vpc_id" {
  description = "ID de la VPC donde se desplegarán los recursos de cómputo"
  type = string
}

variable "public_subnets" {
  description = "Lista de subredes públicas para los recursos de cómputo"
  type = list(string)
}

variable "private_subnets" {
  description = "Lista de subredes privadas para los recursos de cómputo"
  type = list(string)
}

variable "alb_sg_id" {
  description = "ID del grupo de seguridad para el ALB"
  type = string
}

variable "ecs_sg_id" {
  description = "ID del grupo de seguridad para los servicios de ECS"
  type = string
}

variable "postgres_endpoint" {
  description = "Endpoint de la base de datos PostgreSQL"
  type = string
}

variable "postgres_username" {
  description = "Nombre de usuario para la base de datos PostgreSQL"
  type = string
}

variable "db_password" {
  description = "Password de PostgreSQL. Se persiste en SSM Parameter Store (SecureString) y se inyecta a la task via 'secrets', no en texto plano."
  type = string
  sensitive = true
}

variable "mq_password" {
  description = "Password de Amazon MQ. Se persiste en SSM Parameter Store (SecureString) y se inyecta a la task via 'secrets', no en texto plano."
  type = string
  sensitive = true
}

variable "redis_endpoint" {
  description = "Endpoint de la base de datos Redis"
  type = string
}

variable "cognito_issuer_uri" {
  description = "URI del issuer de Cognito para la autenticación JWT"
  type = string
}

variable "cognito_user_pool_id" {
  description = "ID del User Pool de Cognito, usado por el backend para llamar a las Admin APIs (AdminCreateUser, etc.)"
  type = string
}

variable "cognito_user_pool_arn" {
  description = "ARN del User Pool de Cognito, para acotar los permisos IAM del task role"
  type = string
}

variable "mq_username" {
  description = "Nombre de usuario para el servicio de mensajería"
  type = string
}

variable "mq_endpoint" {
  description = "Endpoint del servicio de mensajería"
  type = string
}

variable "alb_secret_token" {
  description = "Token secreto para validar la conexion entre CloudFront y el ALB"
  type = string
}

variable "s3_images_bucket_name" {
  description = "Nombre del bucket S3 para almacenamiento de imágenes"
  type = string
}

variable "cdn_public_base_url" {
  description = "URL base publica del CDN (CloudFront) usada por el backend para resolver URLs de imagenes"
  type = string
}

variable "cors_allowed_origins" {
  description = "Lista de origins permitidos para CORS, separados por coma"
  type        = string
}