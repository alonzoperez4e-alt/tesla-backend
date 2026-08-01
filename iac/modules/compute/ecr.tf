resource "aws_ecr_repository" "backend" {
  name                 = "${var.project_name}-${var.environment}-backend"
  image_tag_mutability = "IMMUTABLE"
  force_delete         = var.environment != "prod" ? true : false

  image_scanning_configuration {
    scan_on_push = true
  }
}