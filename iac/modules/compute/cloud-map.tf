# Descubrimiento de servicios para la integracion privada de API Gateway.
#
# Se usa un namespace DNS *privado* (no uno HTTP): la integracion HTTP_PROXY de
# API Gateway a traves de un VPC Link resuelve el destino via Cloud Map y necesita
# IP y puerto. Con red awsvpc el puerto no viaja en un registro A, por lo que el
# servicio publica registros SRV, que si llevan host+puerto. Un namespace HTTP no
# publica DNS en absoluto y la integracion no encontraria targets.
resource "aws_service_discovery_private_dns_namespace" "main" {
  name        = "${var.project_name}-${var.environment}.local"
  description = "Namespace privado de Cloud Map para el backend de Tesla"
  vpc         = var.vpc_id
}

resource "aws_service_discovery_service" "backend" {
  name = "backend"

  dns_config {
    namespace_id   = aws_service_discovery_private_dns_namespace.main.id
    routing_policy = "MULTIVALUE"

    dns_records {
      type = "SRV"
      ttl  = 60
    }
  }

  # Sin health_check_custom_config a proposito. ECS registra igualmente las
  # tareas con AWS_INIT_HEALTH_STATUS = HEALTHY (verificado en dev) y AWS deja
  # el campo a null. Declarar el bloque vacio no lo envia a la API pero si crea
  # una diferencia permanente en cada plan y, al ser ForceNew, forzaba
  # reemplazar el servicio en cada apply.
}
