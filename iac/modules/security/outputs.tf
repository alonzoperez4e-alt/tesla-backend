output "alb_sg_id" {
  description = "ID del Security Group para el ALB"
  value = aws_security_group.alb.id
}

output "ecs_sg_id" {
  description = "ID del Security Group para las tareas ECS"
  value = aws_security_group.ecs_tasks.id
}

output "database_sg_id" {
  description = "ID del Security Group para la base de datos"
  value = aws_security_group.database.id
}

output "redis_sg_id" {
  description = "ID del Security Group para Redis"
  value = aws_security_group.redis.id
}

output "mq_sg_id" {
  description = "ID del Security Group para MQ"
  value = aws_security_group.mq.id
}