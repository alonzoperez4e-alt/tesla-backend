output "sns_alerts_topic_arn" {
  description = "ARN del topic SNS de alertas, por si otro módulo o recurso necesita publicar en él"
  value       = aws_sns_topic.alerts.arn
}