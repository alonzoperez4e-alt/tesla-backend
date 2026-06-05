module "cognito" {
  source = "../../modules/cognito"

  aws_region = var.aws_region
  prefix = var.prefix
  allowed_callback_urls = var.allowed_callback_urls
}
