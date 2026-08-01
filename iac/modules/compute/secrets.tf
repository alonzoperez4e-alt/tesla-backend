# Secretos de la aplicación almacenados en SSM Parameter Store (SecureString).
# La task los consume via el bloque `secrets` de la definición (valueFrom = ARN),
# evitando exponerlos como variables de entorno en texto plano en la task def.
resource "aws_ssm_parameter" "db_password" {
  name        = "/${var.project_name}/${var.environment}/db-password"
  description = "Password de PostgreSQL para el backend (${var.environment})"
  type        = "SecureString"
  value       = var.db_password
}

# Mismo valor que CloudFront inyecta como cabecera X-Tesla-Origin-Token. Va por
# SSM y no como variable de entorno en claro porque es el secreto que impide
# alcanzar el backend saltandose el CDN por la URL execute-api.
resource "aws_ssm_parameter" "origin_token" {
  name        = "/${var.project_name}/${var.environment}/origin-token"
  description = "Token de origen compartido entre CloudFront y el backend (${var.environment})"
  type        = "SecureString"
  value       = var.origin_secret_token
}
