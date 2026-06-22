output "postgres_endpoint" {
  description = "Endpoint de conexión para PostgreSQL"
  value       = aws_db_instance.postgres.endpoint
}

output "redis_endpoint" {
  description = "Endpoint de conexión para Redis"
  value       = aws_elasticache_cluster.redis.cache_nodes[0].address
}

output "mq_endpoint" {
  description = "Endpoint de conexión para el servicio de mensajería"
  value = aws_mq_broker.rabbitmq.instances[0].endpoints[1]
}