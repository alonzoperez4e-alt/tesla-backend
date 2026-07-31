resource "aws_security_group" "vpc_link" {
  name = "${var.project_name}-${var.environment}-vpclink-sg"
  description = "Permite a API Gateway alcanzar a Fargate"
  vpc_id = var.vpc_id
}

resource "aws_security_group_rule" "vpc_link_egress" {
  type = "egress"
  from_port = 8080
  to_port = 8080
  protocol = "tcp"
  cidr_blocks = [var.vpc_cidr]
  security_group_id = aws_security_group.vpc_link.id
}