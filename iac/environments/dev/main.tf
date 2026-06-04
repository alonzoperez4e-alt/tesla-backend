module "cognito" {
  source = "../../modules/cognito"

  prefix = local.prefix

  allowed_callback_urls = [
    "http://localhost:5173",
    "https://tesla-frontend-dev.vercel.app/callback"
  ]
}
