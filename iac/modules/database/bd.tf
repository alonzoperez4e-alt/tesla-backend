resource "aws_db_instance" "postgres" {
  identifier        = "${var.project_name}-${var.environment}-db"
  engine            = "postgres"
  engine_version    = "17"
  instance_class    = "db.t4g.micro"
  multi_az          = false
  allocated_storage = 20

  db_name  = "tesladb"
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = var.database_subnet_group_name
  vpc_security_group_ids = [var.database_sg_id]

  # Backups automaticos. Sin este argumento el valor por defecto de AWS es 0, es
  # decir backups desactivados y sin point-in-time recovery: combinado con
  # multi_az = false eso dejaba prod como punto unico de fallo con perdida total.
  # El almacenamiento de backups es gratuito hasta el tamano de allocated_storage
  # (20 GB), asi que 7 dias de retencion no anaden coste apreciable.
  # Ambas ventanas tienen que caer DENTRO de la franja de servicio (18:00-24:00 en
  # Lima = 23:00-05:00 UTC): con la instancia detenida RDS no ejecuta los backups
  # automaticos, asi que una ventana de madrugada dejaria de correr en silencio.
  # No pueden solaparse entre si y el backup necesita al menos 30 minutos.
  backup_retention_period = var.environment == "prod" ? 7 : 1
  backup_window           = "23:10-23:40"         # UTC = 18:10-18:40 en Lima
  maintenance_window      = "Tue:03:00-Tue:04:00" # UTC = lunes 22:00-23:00 en Lima
  copy_tags_to_snapshot   = true

  # En prod no se omite el snapshot final, asi que hay que darle un identificador:
  # sin el, un destroy fallaria. El nombre es estable, de modo que un segundo
  # destroy chocaria con el snapshot ya existente (situacion no habitual).
  skip_final_snapshot       = var.environment != "prod"
  final_snapshot_identifier = var.environment == "prod" ? "${var.project_name}-${var.environment}-final-snapshot" : null

  publicly_accessible = false
  storage_encrypted   = true

  deletion_protection = var.environment == "prod"

  auto_minor_version_upgrade = true

  tags = {
    Name = "${var.project_name}-${var.environment}-postgres"
  }
}