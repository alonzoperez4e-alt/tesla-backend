output "ecr_repository_url" {
  description = "La URL del repositorio ECR"
  value       = aws_ecr_repository.backend.repository_url
}

output "service_discovery_arn" {
  description = "ARN del servicio de Cloud Map donde se registran las tareas ECS. Lo consume la integracion privada de API Gateway."
  value       = aws_service_discovery_service.backend.arn
}
