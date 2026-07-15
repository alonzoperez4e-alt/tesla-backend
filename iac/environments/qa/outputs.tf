output "qa_cognito_user_pool_id" {
  value = module.cognito.user_pool_id
  description = "ID del User Pool de Cognito para application-qa.properties"
}

output "qa_cognito_user_pool_client_id" {
  value = module.cognito.user_pool_client_id
  description = "ID del cliente para el frontend"
}

output "qa_cognito_issuer_uri" {
  value = "https://cognito-idp.${var.aws_region}.amazonaws.com/${module.cognito.user_pool_id}"
}

output "qa_cognito_user_pool_domain" {
  value = module.cognito.user_pool_domain
}

output "qa_postgres_endpoint" {
  value = module.database.postgres_endpoint
}

output "qa_redis_endpoint" {
  value = module.database.redis_endpoint
}

output "qa_api_base_url" {
  description = "URL base del API a consumir desde el Frontend"
  value       = "http://${module.compute.alb_dns_name}"
}

output "qa_ecr_repository_url" {
  value = module.compute.ecr_repository_url
}

output "qa_aplicacion_url" {
  description = "URL publica de la aplicacion (CDN)"
  value       = "https://${module.edge.cloudfront_domain_name}"
}

output "qa_s3_frontend_bucket" {
  value = module.edge.frontend_bucket_name
}

output "qa_s3_images_bucket" {
  value = module.edge.images_bucket_name
}