# Rol de ejecucion que EventBridge Scheduler asume para invocar las APIs de ECS y
# RDS. La condicion sobre aws:SourceAccount evita el problema del "confused deputy":
# sin ella, cualquier schedule de otra cuenta que conociera el ARN del rol podria
# pedir a Scheduler que lo asumiera.
data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "scheduler_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["scheduler.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [data.aws_caller_identity.current.account_id]
    }
  }
}

resource "aws_iam_role" "scheduler" {
  name               = "${var.project_name}-${var.environment}-scheduler-role"
  assume_role_policy = data.aws_iam_policy_document.scheduler_assume_role.json
}

data "aws_iam_policy_document" "scheduler_permissions" {
  statement {
    sid       = "EscalarServicioECS"
    effect    = "Allow"
    actions   = ["ecs:UpdateService"]
    resources = [var.ecs_service_arn]
  }

  statement {
    sid    = "ArrancarYPararRDS"
    effect = "Allow"
    actions = [
      "rds:StartDBInstance",
      "rds:StopDBInstance",
    ]
    resources = [var.db_instance_arn]
  }
}

resource "aws_iam_role_policy" "scheduler" {
  name   = "${var.project_name}-${var.environment}-scheduler-policy"
  role   = aws_iam_role.scheduler.id
  policy = data.aws_iam_policy_document.scheduler_permissions.json
}
