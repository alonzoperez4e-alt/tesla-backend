resource "aws_cloudfront_origin_access_control" "spa_oac" {
  name                              = "${var.project_name}-${var.environment}-spa-oac"
  description                       = "Origin Access Control para el bucket del Frontend"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_origin_access_control" "images_oac" {
  name                              = "${var.project_name}-${var.environment}-images-oac"
  description                       = "Origin Access Control para el bucket de Imagenes"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_origin_request_policy" "api_policy" {
  name = "${var.project_name}-${var.environment}-api-policy"

  # Lista blanca deliberada. Dos ausencias importantes:
  #  - Host: reenviarlo rompe API Gateway, que exige su propio hostname.
  #  - X-Tesla-Origin-Token: no se reenvia el del visitante para que no pueda
  #    suplantarlo; CloudFront siempre inyecta el suyo via custom_header.
  headers_config {
    header_behavior = "whitelist"
    headers {
      items = [
        "Authorization",
        "Content-Type",
        "Accept",
        "Origin",
        "Access-Control-Request-Headers",
        "Access-Control-Request-Method",
      ]
    }
  }

  cookies_config {
    cookie_behavior = "all"
  }

  query_strings_config {
    query_string_behavior = "all"
  }
}

resource "aws_cloudfront_function" "spa_router" {
  name    = "${var.project_name}-${var.environment}-spa-router"
  runtime = "cloudfront-js-1.0"
  comment = "Reescribe rutas del SPA (sin extension) a /index.html sin depender de codigos de error del origen"
  publish = true
  code    = file("${path.module}/functions/spa-router.js")
}

# Fuera de la ventana de servicio no hay tareas ECS registradas en Cloud Map y API
# Gateway devolveria un error generico indistinguible de una caida. Esta funcion
# corta la peticion en el borde con un 503 explicito.
#
# Requiere el runtime 2.0: es el que soporta el objeto Date completo. La funcion
# spa_router se queda en 1.0, que le basta.
resource "aws_cloudfront_function" "service_hours" {
  count = var.ventana_habilitada ? 1 : 0

  name    = "${var.project_name}-${var.environment}-service-hours"
  runtime = "cloudfront-js-2.0"
  comment = "Responde 503 en /api/* fuera de la ventana de servicio"
  publish = true
  code = templatefile("${path.module}/functions/service-hours.js.tftpl", {
    hora_apertura = var.hora_apertura
    hora_cierre   = var.hora_cierre == 0 ? 24 : var.hora_cierre
  })
}

resource "aws_s3_bucket" "cf_logs" {
  bucket        = "${var.project_name}-cf-logs-${var.environment}"
  force_destroy = var.environment != "prod" ? true : false
}

resource "aws_s3_bucket_ownership_controls" "cf_logs" {
  bucket = aws_s3_bucket.cf_logs.id

  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_acl" "cf_logs" {
  bucket     = aws_s3_bucket.cf_logs.id
  acl        = "log-delivery-write"
  depends_on = [aws_s3_bucket_ownership_controls.cf_logs]
}
resource "aws_cloudfront_distribution" "cdn" {
  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = "index.html"
  price_class         = "PriceClass_100"

  logging_config {
    bucket          = aws_s3_bucket.cf_logs.bucket_domain_name
    prefix          = "cf-access-logs/"
    include_cookies = false
  }

  origin {
    domain_name              = aws_s3_bucket.frontend.bucket_regional_domain_name
    origin_id                = "S3-Frontend"
    origin_access_control_id = aws_cloudfront_origin_access_control.spa_oac.id
  }

  origin {
    domain_name = var.api_gateway_domain_name
    origin_id   = "APIGW-Backend"

    custom_origin_config {
      http_port  = 80
      https_port = 443
      # API Gateway solo acepta HTTPS; ademas el token secreto viaja en esta
      # conexion, asi que nunca debe ir en claro por internet publico.
      origin_protocol_policy = "https-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }

    custom_header {
      name  = "X-Tesla-Origin-Token"
      value = var.origin_secret_token
    }
  }

  origin {
    domain_name              = aws_s3_bucket.images.bucket_regional_domain_name
    origin_id                = "S3-Images"
    origin_access_control_id = aws_cloudfront_origin_access_control.images_oac.id
  }

  default_cache_behavior {
    target_origin_id       = "S3-Frontend"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["GET", "HEAD", "OPTIONS"]
    cached_methods         = ["GET", "HEAD"]
    compress               = true
    cache_policy_id        = "658327ea-f89d-4fab-a63d-7e88639e58f6"

    function_association {
      event_type   = "viewer-request"
      function_arn = aws_cloudfront_function.spa_router.arn
    }
  }

  ordered_cache_behavior {
    path_pattern           = "/api/*"
    target_origin_id       = "APIGW-Backend"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods         = ["GET", "HEAD"]
    compress               = true
    # CachingDisabled: las respuestas del API no se cachean en el borde.
    cache_policy_id          = "4135ea2d-6df8-44a3-9df3-4b5a84be39ad"
    origin_request_policy_id = aws_cloudfront_origin_request_policy.api_policy.id

    dynamic "function_association" {
      for_each = aws_cloudfront_function.service_hours

      content {
        event_type   = "viewer-request"
        function_arn = function_association.value.arn
      }
    }
  }

  # No hay behavior /ws-chat/*: API Gateway HTTP API no hace proxy de upgrades
  # WebSocket, por lo que el chat de grupos queda aplazado.

  ordered_cache_behavior {
    path_pattern           = "/images/*"
    target_origin_id       = "S3-Images"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["GET", "HEAD", "OPTIONS"]
    cached_methods         = ["GET", "HEAD"]
    compress               = true
    cache_policy_id        = "658327ea-f89d-4fab-a63d-7e88639e58f6"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    # checkov:skip=CKV2_AWS_42: Se utiliza el certificado por defecto ya que no se cuenta con un dominio propio.
    cloudfront_default_certificate = true
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-cdn"
    Environment = var.environment
  }
}