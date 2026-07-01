output "alb_dns_name" {
  description = "El DNS publico del Application Load Balancer"
  value       = aws_lb.api.dns_name
}

output "ecr_repository_url" {
  description = "La URL del repositorio ECR"
  value       = aws_ecr_repository.backend.repository_url
}

output "ecs_cluster_name" {
  description = "Nombre del cluster ECS"
  value       = aws_ecs_cluster.main.name
}

output "ecs_service_name" {
  description = "Nombre del servicio ECS"
  value       = aws_ecs_service.api.name
}

output "alb_arn_suffix" {
  description = "ARN suffix del ALB, usado en las dimensiones de las alarmas de CloudWatch"
  value       = aws_lb.api.arn_suffix
}

output "target_group_arn_suffix" {
  description = "ARN suffix del Target Group, usado en las dimensiones de las alarmas de CloudWatch"
  value       = aws_lb_target_group.api.arn_suffix
}