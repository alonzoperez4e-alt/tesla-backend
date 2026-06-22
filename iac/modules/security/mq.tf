resource "aws_security_group" "mq" {
  name = "${var.project_name}-${var.environment}-mq-sg"
  description = "Permitir trafico entre ECS y MQ"
  vpc_id = var.vpc_id

  ingress {
    from_port = 5671
    to_port = 5671
    protocol = "tcp"
    security_groups = [aws_security_group.ecs_tasks.id]
  }

  egress {
    from_port = 0
    to_port   = 0
    protocol  = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-mq-sg"
  }
}