resource "aws_security_group" "alb" {
  name = "${var.project_name}-${var.environment}-alb-sg"
  description = "Permite trafico HTTP/HTTPS solo desde CloudFront"
  vpc_id = var.vpc_id

  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    prefix_list_ids = [data.aws_ec2_managed_prefix_list.cloudfront.id]
  }

  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    prefix_list_ids = [data.aws_ec2_managed_prefix_list.cloudfront.id]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-alb-sg"
  }
}