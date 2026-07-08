# Secretos de la aplicación almacenados en SSM Parameter Store (SecureString).
# La task los consume via el bloque `secrets` de la definición (valueFrom = ARN),
# evitando exponerlos como variables de entorno en texto plano en la task def.
resource "aws_ssm_parameter" "db_password" {
  name        = "/${var.project_name}/${var.environment}/db-password"
  description = "Password de PostgreSQL para el backend (${var.environment})"
  type        = "SecureString"
  value       = var.db_password
}

resource "aws_ssm_parameter" "mq_password" {
  name        = "/${var.project_name}/${var.environment}/mq-password"
  description = "Password de Amazon MQ (RabbitMQ) para el backend (${var.environment})"
  type        = "SecureString"
  value       = var.mq_password
}
