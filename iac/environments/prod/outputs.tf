output "prod_cognito_user_pool_id" {
  value = module.cognito.user_pool_id
  description = "ID del User Pool de Cognito para application-prod.properties"
}

output "prod_cognito_user_pool_client_id" {
  value = module.cognito.user_pool_client_id
  description = "ID del cliente para el frontend"
}

output "prod_cognito_issuer_uri" {
  value = "https://cognito-idp.${var.aws_region}.amazonaws.com/${module.cognito.user_pool_id}"
}

output "prod_cognito_user_pool_domain" {
  value = module.cognito.user_pool_domain
}

output "prod_postgres_endpoint" {
  value = module.database.postgres_endpoint
}

output "prod_redis_endpoint" {
  value = module.database.redis_endpoint
}

output "prod_api_base_url" {
  description = "URL base del API a consumir desde el Frontend"
  value       = "http://${module.compute.alb_dns_name}"
}

output "prod_ecr_repository_url" {
  value = module.compute.ecr_repository_url
}

output "prod_aplicacion_url" {
  description = "URL publica de la aplicacion (CDN)"
  value       = "https://${module.edge.cloudfront_domain_name}"
}

output "prod_s3_frontend_bucket" {
  value = module.edge.frontend_bucket_name
}

output "prod_s3_images_bucket" {
  value = module.edge.images_bucket_name
}