resource "aws_db_instance" "postgres" {
  identifier             = "${var.project_name}-${var.environment}-db"
  engine                 = "postgres"
  engine_version         = "17"
  instance_class         = "db.t3.micro"
  allocated_storage      = 20

  db_name                = "tesladb"
  username               = var.db_username
  password               = var.db_password

  db_subnet_group_name   = var.database_subnet_group_name
  vpc_security_group_ids = [var.database_sg_id]

  multi_az               = true

  skip_final_snapshot    = var.environment != "prod" ? true : false
  publicly_accessible    = false
  storage_encrypted      = true

  deletion_protection    = var.environment == "prod" ? true : false

  auto_minor_version_upgrade = true

  tags = {
    Name = "${var.project_name}-${var.environment}-postgres"
  }
}