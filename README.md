# Despliegue Local

## Arquitectura
* **Bootstrap**: Capa fundacional única (S3, DynamoDB, OIDC). No modificar.
* **Entornos**: Capas dependientes según etapa (`dev`, `qa`, `prod`).

## Requisitos
* Bootstrap desplegado y credenciales AWS válidas.
* Docker Desktop, Terraform, Java, Maven e IntelliJ instalados.

## Pasos de Despliegue

### 1. Levantar Servicios
* Iniciar PostgreSQL, Redis y RabbitMQ:
```bash
docker compose up -d
```

### 2. Desplegar DEV
* Quitar la extensión `.example` de `terraform.tfvars.example`.

* Inicializar y aplicar Terraform:
```bash
cd iac/environments/dev
terraform init
terraform apply
```

### 3. Configurar IDE
* Copiar `cognito_issuer_uri` generado por Terraform.
* Añadirlo como variable de entorno en IntelliJ (`Edit Configurations`).

### 4. Ejecutar Backend
* Iniciar la aplicación mediante IntelliJ o con:
```bash
mvn spring-boot:run
```

## Problemas Comunes
* **Error en `terraform init`**: Verificar con el administrador que existan el Bucket, la Tabla Lock y los Roles de Bootstrap en AWS.
