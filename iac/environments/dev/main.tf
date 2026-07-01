module "cognito" {
  source = "../../modules/cognito"

  aws_region = var.aws_region
  prefix = var.prefix
  allowed_callback_urls = concat(var.allowed_callback_urls, ["https://${module.edge.cloudfront_domain_name}/callback"])
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

module "database" {
  source                     = "../../modules/database"
  project_name               = "tesla-backend"
  environment                = var.environment

  database_subnet_group_name = module.networking.database_subnet_group_name
  database_subnets           = module.networking.database_subnets

  database_sg_id             = module.security.database_sg_id
  redis_sg_id                = module.security.redis_sg_id

  db_username                = var.db_username
  db_password                = var.db_password

  mq_sq_id                   = module.security.mq_sg_id
  mq_username               = var.mq_username
  mq_password               = var.mq_password
}

module "compute" {
  source = "../../modules/compute"
  project_name = "tesla-backend"
  environment = var.environment
  aws_region = var.aws_region
  vpc_id = module.networking.vpc_id
  public_subnets = module.networking.public_subnets
  private_subnets = module.networking.private_subnets
  alb_sg_id = module.security.alb_sg_id
  ecs_sg_id = module.security.ecs_sg_id
  postgres_endpoint = module.database.postgres_endpoint
  postgres_username = var.db_username
  redis_endpoint = module.database.redis_endpoint
  cognito_issuer_uri = "https://cognito-idp.${var.aws_region}.amazonaws.com/${module.cognito.user_pool_id}"
  mq_endpoint = module.database.mq_endpoint
  mq_username = var.mq_username
  alb_secret_token = var.alb_secret_token
  s3_images_bucket_name = module.edge.images_bucket_name
  cloudfront_domain_name = "https://${module.edge.cloudfront_domain_name}/storage"
}

module "edge" {
  source           = "../../modules/edge"
  project_name     = var.project_name
  environment      = var.environment
  alb_dns_name     = module.compute.alb_dns_name
  alb_secret_token = var.alb_secret_token
}