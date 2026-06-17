output "vpc_id" {
  description = "ID de la VPC"
  value       = aws_vpc.main.id
}

output "public_subnets" {
  description = "IDs de las subredes públicas"
  value       = aws_subnet.public[*].id
}

output "private_subnets" {
  description = "IDs de las subredes privadas"
  value       = aws_subnet.private[*].id
}

output "database_subnets" {
  description = "IDs de las subredes de bases de datos"
  value       = aws_subnet.database[*].id
}

output "database_subnet_group_name" {
  description = "Nombre del grupo de subredes de base de datos"
  value       = aws_db_subnet_group.database.name
}