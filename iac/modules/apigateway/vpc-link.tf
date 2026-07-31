resource "aws_apigatewayv2_vpc_link" "main" {
  name = "${var.project_name}-${var.environment}-vpc-link"
  security_group_ids = [var.vpc_link_sg_id]
  subnet_ids = var.public_subnets
}