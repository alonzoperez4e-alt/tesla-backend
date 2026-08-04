output "postgres_endpoint" {
  description = "Endpoint de conexión para PostgreSQL"
  value       = aws_db_instance.postgres.endpoint
}

output "db_instance_identifier" {
  description = "Identificador de la instancia RDS. Lo usa el modulo scheduler para arrancarla y pararla."
  value       = aws_db_instance.postgres.identifier
}

output "db_instance_arn" {
  description = "ARN de la instancia RDS, para acotar los permisos del rol del scheduler."
  value       = aws_db_instance.postgres.arn
}
