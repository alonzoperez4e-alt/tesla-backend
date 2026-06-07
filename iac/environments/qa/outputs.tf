output "qa_cognito_user_pool_id" {
  value = module.cognito.user_pool_id
  description = "ID del User Pool de Cognito para application-qa.properties"
}

output "qa_cognito_user_pool_client_id" {
  value = module.cognito.user_pool_client_id
  description = "ID del cliente para el frontend"
}