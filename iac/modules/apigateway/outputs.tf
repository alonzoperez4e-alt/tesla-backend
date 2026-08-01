output "api_endpoint" {
  description = "URL base del stage $default del HTTP API (con esquema)"
  value       = aws_apigatewayv2_api.main.api_endpoint
}

output "api_domain_name" {
  description = "Host del HTTP API sin esquema, para usarlo como origen de CloudFront"
  value       = replace(aws_apigatewayv2_api.main.api_endpoint, "https://", "")
}
