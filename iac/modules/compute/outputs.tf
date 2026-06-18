output "alb_dns_name" {
  description = "El DNS publico del Application Load Balancer"
  value       = aws_lb.api.dns_name
}

output "ecr_repository_url" {
  description = "La URL del repositorio ECR"
  value       = aws_ecr_repository.backend.repository_url
}
