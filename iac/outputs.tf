output "dev_cognito_user_pool_id" {
  value = module.cognito_dev.user_pool_id
  description = "ID del User Pool de Cognito para application-dev.properties"
}

output "dev_cognito_user_pool_client_id" {
  value = module.cognito_dev.user_pool_client_id
  description = "ID del cliente para el frontend"
}
