output "postgres_endpoint" {
  description = "Endpoint de conexión para PostgreSQL"
  value       = aws_db_instance.postgres.endpoint
}
