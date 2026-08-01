locals {
  vpc_cidr = "10.0.0.0/16"
}

module "cognito" {
  source = "../../modules/cognito"

  aws_region            = var.aws_region
  environment           = var.environment
  prefix                = var.prefix
  allowed_callback_urls = concat(var.allowed_callback_urls, ["https://${module.edge.cloudfront_domain_name}/callback"])
}

module "networking" {
  source = "../../modules/networking"

  project_name          = "tesla-backend"
  environment           = var.environment
  vpc_cidr              = local.vpc_cidr
  public_subnets_cidr   = ["10.0.1.0/24", "10.0.2.0/24"]
  database_subnets_cidr = ["10.0.21.0/24", "10.0.22.0/24"]
}

module "security" {
  source = "../../modules/security"

  project_name = "tesla-backend"
  environment  = var.environment
  vpc_id       = module.networking.vpc_id
  vpc_cidr     = local.vpc_cidr
}

module "database" {
  source       = "../../modules/database"
  project_name = "tesla-backend"
  environment  = var.environment

  database_subnet_group_name = module.networking.database_subnet_group_name
  database_sg_id             = module.security.database_sg_id

  db_username = var.db_username
  db_password = var.db_password
}

module "compute" {
  source                = "../../modules/compute"
  project_name          = "tesla-backend"
  environment           = var.environment
  aws_region            = var.aws_region
  vpc_id                = module.networking.vpc_id
  public_subnets        = module.networking.public_subnets
  ecs_sg_id             = module.security.ecs_sg_id
  postgres_endpoint     = module.database.postgres_endpoint
  postgres_username     = var.db_username
  db_password           = var.db_password
  origin_secret_token   = var.origin_secret_token
  cognito_issuer_uri    = "https://cognito-idp.${var.aws_region}.amazonaws.com/${module.cognito.user_pool_id}"
  cognito_user_pool_id  = module.cognito.user_pool_id
  cognito_user_pool_arn = module.cognito.user_pool_arn
  s3_images_bucket_name = module.edge.images_bucket_name
  cdn_public_base_url   = "https://${module.edge.cloudfront_domain_name}"
  cors_allowed_origins = join(",", concat(
    var.extra_cors_origins,
    ["https://${module.edge.cloudfront_domain_name}"]
  ))
  alarm_email = var.alarm_email
}

module "apigateway" {
  source = "../../modules/apigateway"

  project_name          = "tesla-backend"
  environment           = var.environment
  vpc_link_sg_id        = module.security.vpc_link_sg_id
  public_subnets        = module.networking.public_subnets
  service_discovery_arn = module.compute.service_discovery_arn
}

module "edge" {
  source       = "../../modules/edge"
  project_name = var.project_name
  environment  = var.environment

  api_gateway_domain_name = module.apigateway.api_domain_name
  origin_secret_token     = var.origin_secret_token
}
