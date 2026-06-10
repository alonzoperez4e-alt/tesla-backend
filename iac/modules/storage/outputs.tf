output "bucket_name" {
  description = "Nombre exacto del bucket para el application.properties del backend"
  value       = aws_s3_bucket.media_bucket.id
}

output "cloudfront_domain" {
  description = "URL de CloudFront para el frontend y backend"
  value       = "https://${aws_cloudfront_distribution.cdn.domain_name}"
}