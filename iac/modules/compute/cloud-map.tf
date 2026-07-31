resource "aws_service_discovery_http_namespace" "main" {
  name = "${var.project_name}-${var.environment}-namespace"
  description = "Namespace para descubrimiento de servicios de Tesla a traves de HTTP"
}

resource "aws_service_discovery_service" "backend" {
  name = "backend"
  namespace_id = aws_service_discovery_http_namespace.main.id
}