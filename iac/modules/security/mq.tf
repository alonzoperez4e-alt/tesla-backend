resource "aws_security_group" "mq" {
  name        = "${var.project_name}-${var.environment}-mq-sg"
  description = "Permitir trafico entre ECS y MQ"
  vpc_id      = var.vpc_id

  tags = {
    Name = "${var.project_name}-${var.environment}-mq-sg"
  }
}

resource "aws_security_group_rule" "mq_ingress_ecs" {
  type                     = "ingress"
  from_port                = 5671
  to_port                  = 5671
  protocol                 = "tcp"
  security_group_id        = aws_security_group.mq.id
  source_security_group_id = aws_security_group.ecs_tasks.id
}