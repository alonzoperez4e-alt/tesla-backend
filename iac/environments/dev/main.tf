module "cognito" {
  source = "../../modules/cognito"

  aws_region = var.aws_region
  prefix = var.prefix
  allowed_callback_urls = var.allowed_callback_urls
}

module "networking" {
  source = "../../modules/networking"

  project_name          = "tesla-backend"
  environment           = var.environment
  vpc_cidr              = "10.0.0.0/16"
  public_subnets_cidr   = ["10.0.1.0/24", "10.0.2.0/24"]
  private_subnets_cidr  = ["10.0.11.0/24", "10.0.12.0/24"]
  database_subnets_cidr = ["10.0.21.0/24", "10.0.22.0/24"]
}

module "security" {
  source = "../../modules/security"

  project_name = "tesla-backend"
  environment = var.environment
  vpc_id = module.networking.vpc_id
}