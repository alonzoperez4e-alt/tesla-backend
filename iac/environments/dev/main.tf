module "cognito" {
  source = "../../modules/cognito"

  prefix = var.prefix
  allowed_callback_urls = var.allowed_callback_urls
}
