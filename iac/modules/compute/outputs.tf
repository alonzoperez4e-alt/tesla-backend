output "ecr_repository_url" {
  description = "La URL del repositorio ECR"
  value       = aws_ecr_repository.backend.repository_url
}

output "service_discovery_arn" {
  description = "ARN del servicio de Cloud Map donde se registran las tareas ECS. Lo consume la integracion privada de API Gateway."
  value       = aws_service_discovery_service.backend.arn
}

output "ecs_cluster_name" {
  description = "Nombre del cluster ECS. Lo usa el modulo scheduler en la llamada a UpdateService."
  value       = aws_ecs_cluster.main.name
}

output "ecs_service_name" {
  description = "Nombre del servicio ECS. Lo usa el modulo scheduler en la llamada a UpdateService."
  value       = aws_ecs_service.api.name
}

output "ecs_service_arn" {
  description = "ARN del servicio ECS, para acotar los permisos del rol del scheduler."
  value       = aws_ecs_service.api.id
}
