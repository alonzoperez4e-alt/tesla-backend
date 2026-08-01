resource "aws_apigatewayv2_integration" "backend" {
  api_id = aws_apigatewayv2_api.main.id

  integration_type   = "HTTP_PROXY"
  integration_uri    = var.service_discovery_arn
  integration_method = "ANY"

  connection_type = "VPC_LINK"
  connection_id   = aws_apigatewayv2_vpc_link.main.id

  timeout_milliseconds = 15000
}
