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
      values = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values = ["repo:${var.github_repo}:ref:refs/heads/${each.value.github_branch}"]
    }
  }
}

resource "aws_iam_role" "github_actions_roles" {
  for_each = var.environments

  name = "${var.prefix}-github-actions-role-${each.key}"

  assume_role_policy = data.aws_iam_policy_document.github_trust[each.key].json
}

resource "aws_iam_role_policy_attachment" "github_actions_admin" {
  for_each = var.environments

  role       = aws_iam_role.github_actions_roles[each.key].name

    policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}