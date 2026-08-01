output "ecs_sg_id" {
  description = "ID del Security Group para las tareas ECS"
  value       = aws_security_group.ecs_tasks.id
}

output "database_sg_id" {
  description = "ID del Security Group para la base de datos"
  value       = aws_security_group.database.id
}

output "vpc_link_sg_id" {
  description = "ID del Security Group del VPC Link de API Gateway. Es el unico origen aceptado por el SG de las tareas ECS."
  value       = aws_security_group.vpc_link.id
}
