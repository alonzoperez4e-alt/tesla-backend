resource "aws_db_subnet_group" "database" {
  name = "${var.project_name}-${var.environment}-db-subnet-group"
  subnet_ids = aws_subnet.database[*].id
  description = "Grupo de subredes para las bases de datos de ${var.project_name} ${var.environment}"

  tags = {
    Name = "${var.project_name}-${var.environment}-db-subnet-group"
  }
}
