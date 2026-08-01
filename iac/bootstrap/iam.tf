data "aws_iam_policy_document" "github_trust" {
  for_each = var.environments

  statement {
    effect = "Allow"

    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values = [
        "repo:${var.github_repo}:ref:refs/heads/${each.value.github_branch}",
        "repo:${var.github_repo}:environment:${each.key}"
      ]
    }
  }
}

resource "aws_iam_role" "github_actions_roles" {
  for_each = var.environments

  name = "${var.prefix}-github-actions-role-${each.key}"

  assume_role_policy = data.aws_iam_policy_document.github_trust[each.key].json
}

resource "aws_iam_policy" "github_actions_policy" {
  for_each = var.environments

  name        = "${var.prefix}-github-actions-policy-${each.key}"
  description = "Política de mínimo privilegio para GitHub Actions en el entorno ${each.key}"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [

      {
        Sid    = "TerraformStateBucketAccess"
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket",
          "s3:GetBucketVersioning",
          "s3:PutBucketVersioning",
          "s3:GetEncryptionConfiguration",
          "s3:PutEncryptionConfiguration"
        ]
        Resource = [
          "arn:aws:s3:::${var.prefix}-terraform-state-*",
          "arn:aws:s3:::${var.prefix}-terraform-state-*/*"
        ]
      },

      {
        Sid    = "TerraformLocksTableAccess"
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:DeleteItem",
          "dynamodb:DescribeTable",
          "dynamodb:CreateTable",
          "dynamodb:UpdateTable",
          "dynamodb:DescribeTimeToLive",
          "dynamodb:ListTagsOfResource",
          "dynamodb:TagResource"
        ]
        Resource = "arn:aws:dynamodb:${var.aws_region}:*:table/${var.prefix}-terraform-locks"
      },

      {
        Sid    = "DynamoDBAutoscalingAccess"
        Effect = "Allow"
        Action = [
          "application-autoscaling:RegisterScalableTarget",
          "application-autoscaling:DeregisterScalableTarget",
          "application-autoscaling:DescribeScalableTargets",
          "application-autoscaling:PutScalingPolicy",
          "application-autoscaling:DeleteScalingPolicy",
          "application-autoscaling:DescribeScalingPolicies"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "application-autoscaling:service-namespace" = "dynamodb"
          }
        }
      },

      {
        Sid    = "KMSKeyManagement"
        Effect = "Allow"
        Action = [
          "kms:CreateKey",
          "kms:DescribeKey",
          "kms:GetKeyPolicy",
          "kms:GetKeyRotationStatus",
          "kms:EnableKeyRotation",
          "kms:PutKeyPolicy",
          "kms:ScheduleKeyDeletion",
          "kms:ListResourceTags",
          "kms:TagResource"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "kms:CallerAccount" = "*"
          }
        }
      },

      {
        Sid    = "IAMRoleManagement"
        Effect = "Allow"
        Action = [
          "iam:CreateRole",
          "iam:DeleteRole",
          "iam:GetRole",
          "iam:UpdateRole",
          "iam:PassRole",
          "iam:ListRolePolicies",
          "iam:ListAttachedRolePolicies",
          "iam:AttachRolePolicy",
          "iam:DetachRolePolicy",
          "iam:TagRole",
          "iam:UntagRole"
        ]
        Resource = "arn:aws:iam::*:role/${var.prefix}-*"
      },
      {
        Sid    = "IAMPolicyManagement"
        Effect = "Allow"
        Action = [
          "iam:CreatePolicy",
          "iam:DeletePolicy",
          "iam:GetPolicy",
          "iam:GetPolicyVersion",
          "iam:ListPolicyVersions",
          "iam:CreatePolicyVersion",
          "iam:DeletePolicyVersion",
          "iam:TagPolicy"
        ]
        Resource = "arn:aws:iam::*:policy/${var.prefix}-*"
      },

      {
        Sid    = "OIDCProviderManagement"
        Effect = "Allow"
        Action = [
          "iam:CreateOpenIDConnectProvider",
          "iam:DeleteOpenIDConnectProvider",
          "iam:GetOpenIDConnectProvider",
          "iam:ListOpenIDConnectProviders",
          "iam:TagOpenIDConnectProvider"
        ]
        Resource = "arn:aws:iam::*:oidc-provider/token.actions.githubusercontent.com"
      },

      {
        Sid      = "STSCallerIdentity"
        Effect   = "Allow"
        Action   = ["sts:GetCallerIdentity"]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "github_actions_policy_attachment" {
  for_each = var.environments

  role       = aws_iam_role.github_actions_roles[each.key].name
  policy_arn = aws_iam_policy.github_actions_policy[each.key].arn
}