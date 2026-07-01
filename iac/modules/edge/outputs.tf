output "cloudfront_domain_name" {
  description = "URL principal de la aplicacion"
  value       = aws_cloudfront_distribution.cdn.domain_name
}

output "frontend_bucket_name" {
  description = "Nombre del bucket S3 para hacer despliegues del frontend"
  value       = aws_s3_bucket.frontend.id
}

output "images_bucket_name" {
  description = "Nombre del bucket S3 para almacenamiento de imagenes"
  value       = aws_s3_bucket.images.id
}

output "cloudfront_distribution_id" {
  description = "ID de la distribucion para invalidaciones de cache en el pipeline CI/CD"
  value       = aws_cloudfront_distribution.cdn.id
}

output "cloudfront_distribution_arn" {
  description = "ARN de la distribucion para configurar las bucket policies"
  value       = aws_cloudfront_distribution.cdn.arn
}