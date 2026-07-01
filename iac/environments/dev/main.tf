module "cognito" {
  source = "../../modules/cognito"

  aws_region            = var.aws_region
  prefix                = var.prefix
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
  environment  = var.environment
  vpc_id       = module.networking.vpc_id
}

module "database" {
  source       = "../../modules/database"
  project_name = "tesla-backend"
  environment  = var.environment

  database_subnet_group_name = module.networking.database_subnet_group_name
  database_subnets           = module.networking.database_subnets

  database_sg_id       = module.security.database_sg_id
  redis_sg_id          = module.security.redis_sg_id
  cache_retention_days = var.cache_retention_days

  db_username = var.db_username
  db_password = var.db_password

  mq_sq_id    = module.security.mq_sg_id
  mq_username = var.mq_username
  mq_password = var.mq_password
}

module "compute" {
  source             = "../../modules/compute"
  project_name       = "tesla-backend"
  environment        = var.environment
  aws_region         = var.aws_region
  vpc_id             = module.networking.vpc_id
  public_subnets     = module.networking.public_subnets
  private_subnets    = module.networking.private_subnets
  alb_sg_id          = module.security.alb_sg_id
  ecs_sg_id          = module.security.ecs_sg_id
  postgres_endpoint  = module.database.postgres_endpoint
  postgres_username  = var.db_username
  redis_endpoint     = module.database.redis_endpoint
  cognito_issuer_uri = "https://cognito-idp.${var.aws_region}.amazonaws.com/${module.cognito.user_pool_id}"
  mq_endpoint        = module.database.mq_endpoint
  mq_username        = var.mq_username
  alb_secret_token   = var.alb_secret_token
}

module "edge" {
  source           = "../../modules/edge"
  project_name     = "tesla-backend"
  environment      = var.environment
  alb_dns_name     = module.compute.alb_dns_name
  alb_secret_token = var.alb_secret_token
}

module "observability" {
  source = "../../modules/observability"

  project_name = "tesla-backend"
  environment  = var.environment

  alert_emails = var.alert_emails

  ecs_cluster_name        = module.compute.ecs_cluster_name
  ecs_service_name        = module.compute.ecs_service_name
  alb_arn_suffix          = module.compute.alb_arn_suffix
  target_group_arn_suffix = module.compute.target_group_arn_suffix
  desired_task_count      = 1

  postgres_instance_id = module.database.postgres_instance_id
  redis_cluster_id     = module.database.redis_cluster_id
}