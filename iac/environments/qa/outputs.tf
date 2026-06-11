output "qa_cognito_user_pool_id" {
  value = module.cognito.user_pool_id
  description = "ID del User Pool de Cognito para application-qa.properties"
}

output "qa_cognito_user_pool_client_id" {
  value = module.cognito.user_pool_client_id
  description = "ID del cliente para el frontend"
}

output "qa_s3_bucket_name" {
  value       = module.storage.bucket_name
  description = "Nombre del bucket S3 para application.properties"
}

output "qa_cloudfront_url" {
  value       = module.storage.cloudfront_domain
  description = "URL de CloudFront para application.properties"
}