resource "aws_apigatewayv2_api" "main" {
  name          = "${var.project_name}-${var.environment}-api"
  protocol_type = "HTTP"

  # No se puede desactivar sin un dominio propio: la URL execute-api queda
  # publica y accesible saltandose CloudFront. Por eso la validacion de la
  # cabecera secreta tiene que hacerse en la aplicacion, no solo en el borde.
  disable_execute_api_endpoint = false
}
