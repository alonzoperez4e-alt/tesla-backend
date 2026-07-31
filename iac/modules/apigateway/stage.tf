resource "aws_apigatewayv2_stage" "default" {
  name = "$default"
  api_id = aws_apigatewayv2_api.main.id
  auto_deploy = true
}