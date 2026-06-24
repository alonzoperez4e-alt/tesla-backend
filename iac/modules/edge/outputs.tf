output "cloudfront_domain_name" {
  description = "URL principal de la aplicacion"
  value       = aws_cloudfront_distribution.cdn.domain_name
}

output "frontend_bucket_name" {
  description = "Nombre del bucket S3 para hacer despliegues del frontend"
  value       = aws_s3_bucket.frontend.id
}