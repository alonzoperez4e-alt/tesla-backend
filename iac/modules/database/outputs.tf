output "postgres_endpoint" {
  description = "Endpoint de conexión para PostgreSQL"
  value       = aws_db_instance.postgres.endpoint
}

output "redis_endpoint" {
  description = "Endpoint de conexión para Redis"
  value       = aws_elasticache_cluster.redis.cache_nodes[0].address
}

output "mq_endpoint" {
  description = "Hostname del broker de mensajería (sin esquema ni puerto; ActiveMQ expone varios endpoints con distinto protocolo/puerto pero el mismo host)"
  value = regex("^[a-z0-9+]+://([^:/]+)", aws_mq_broker.rabbitmq.instances[0].endpoints[0])[0]
}