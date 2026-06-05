output "dev_cognito_user_pool_id" {
  value = module.cognito.user_pool_id
  description = "ID del User Pool de Cognito para application-dev.properties"
}

output "dev_cognito_user_pool_client_id" {
  value = module.cognito.user_pool_client_id
  description = "ID del cliente para el frontend"
}

output "dev_cognito_issuer_uri" {
  value = "https://cognito-idp.${var.aws_region}.amazonaws.com/${module.cognito.user_pool_id}"
}

output "dev_cognito_user_pool_domain" {
  value = module.cognito.user_pool_domain
}