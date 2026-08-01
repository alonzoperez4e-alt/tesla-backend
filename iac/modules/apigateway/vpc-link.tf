# El VPC Link de un HTTP API no tiene cargo por hora (a diferencia del de REST
# API). Sus ENIs viven en las subredes publicas porque tras retirar el NAT
# Gateway no quedan subredes privadas en la VPC.
resource "aws_apigatewayv2_vpc_link" "main" {
  name               = "${var.project_name}-${var.environment}-vpc-link"
  security_group_ids = [var.vpc_link_sg_id]
  subnet_ids         = var.public_subnets
}
