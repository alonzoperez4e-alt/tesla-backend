resource "aws_security_group" "ecs_tasks" {
  name        = "${var.project_name}-${var.environment}-ecs-sg"
  description = "Permite trafico entre tareas ECS y ALB"
  vpc_id      = var.vpc_id

  tags = {
    Name = "${var.project_name}-${var.environment}-ecs-sg"
  }
}

resource "aws_security_group_rule" "ecs_ingress_alb" {
  description              = "Permite trafico entrante desde el ALB al puerto 8080"
  type                     = "ingress"
  from_port                = 8080
  to_port                  = 8080
  protocol                 = "tcp"
  security_group_id        = aws_security_group.ecs_tasks.id
  source_security_group_id = aws_security_group.alb.id
}

resource "aws_security_group_rule" "ecs_egress_rds" {
  description              = "Permite trafico saliente de ECS hacia RDS en puerto 5432"
  type                     = "egress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  security_group_id        = aws_security_group.ecs_tasks.id
  source_security_group_id = aws_security_group.database.id
}

resource "aws_security_group_rule" "ecs_egress_mq" {
  description              = "Permite trafico saliente de ECS hacia MQ en puerto 5671"
  type                     = "egress"
  from_port                = 5671
  to_port                  = 5671
  protocol                 = "tcp"
  security_group_id        = aws_security_group.ecs_tasks.id
  source_security_group_id = aws_security_group.mq.id
}

resource "aws_security_group_rule" "ecs_egress_redis" {
  description              = "Permite trafico saliente de ECS hacia Redis en puerto 6379"
  type                     = "egress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  security_group_id        = aws_security_group.ecs_tasks.id
  source_security_group_id = aws_security_group.redis.id
}

resource "aws_security_group_rule" "ecs_egress_https" {
  description       = "Permite trafico HTTPS saliente de ECS hacia internet"
  type              = "egress"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  security_group_id = aws_security_group.ecs_tasks.id
  cidr_blocks       = ["0.0.0.0/0"]
}
