# Tesla Backend

Plataforma backend basada en **Spring Boot 3** y **Java 21**, diseñada para desplegarse en **AWS (ECS Fargate)** utilizando una arquitectura de infraestructura modular con **Terraform**.

## Arquitectura

* **App:** Java 21, Spring Boot, Spring Security (OAuth2/JWT con Cognito), PostgreSQL, Redis, RabbitMQ.
* **Infraestructura (AWS):** VPC, ALB, ECS (Fargate), ECR, RDS, ElastiCache, Amazon MQ, CloudFront y Cognito.
* **IaC:** Terraform estructurado por módulos (`networking`, `security`, `database`, `compute`, `edge`, `cognito`) y entornos (`dev`, `qa`, `prod`). Incluye una capa fundacional (`bootstrap`).
* **CI/CD:** GitHub Actions con autenticación OIDC hacia AWS, análisis en SonarCloud y despliegue continuo a ECS.

## Prerrequisitos

* [Java 21](https://aws.amazon.com/corretto/) y Maven.
* [Docker Desktop](https://www.docker.com/products/docker-desktop/).
* [Terraform CLI](https://developer.hashicorp.com/terraform/downloads) (v1.0+).
* [AWS CLI](https://aws.amazon.com/cli/) configurado con credenciales válidas.

---

## Despliegue en Local

Para el entorno de desarrollo local, utilizaremos contenedores para los servicios de respaldo (bases de datos, colas) y Terraform para aprovisionar los servicios cloud estrictamente necesarios (como Amazon Cognito).

### 1. Levantar Servicios Base (Docker)

Levanta PostgreSQL, Redis, RabbitMQ y SonarQube localmente:

```bash
docker compose up -d

```

### 2. Aprovisionar Dependencias Cloud (Terraform)

Debes desplegar el entorno `dev` en AWS para obtener el proveedor de identidad (Cognito) y otros recursos.

```bash
cd iac/environments/dev
cp terraform.tfvars.example terraform.tfvars
# Edita terraform.tfvars con contraseñas seguras y tus datos
cp backend.tfvars.example backend.tfvars
# Edita backend.tfvars con el bucket S3 y la tabla DynamoDB creados por el módulo bootstrap
terraform init -backend-config backend.tfvars
terraform apply

```

*Copia el valor de `dev_cognito_issuer_uri` que se imprime al finalizar.*

### 3. Ejecutar la Aplicación

Configura la variable de entorno de Cognito en tu terminal o IDE (IntelliJ/VS Code) y levanta el servidor:

```bash
export COGNITO_ISSUER_URI="<URL_COGNITO_OBTENIDA_EN_EL_PASO_ANTERIOR>"
export SPRING_PROFILES_ACTIVE=dev

./mvnw spring-boot:run

```

El servidor estará corriendo en `http://localhost:8080`.

> **Nota:** Si tienes problemas con el `terraform init`, asegúrate de que el módulo `bootstrap` (que crea el Bucket S3 y la tabla DynamoDB para el estado) haya sido desplegado previamente por tu administrador de nube.

---

## Despliegue en la Nube (AWS)

El despliegue en la nube es automatizado y se divide en **Infraestructura** y **Aplicación**.

### FASE 1: Despliegue de Infraestructura (Terraform)

**1. Capa Bootstrap (Solo la primera vez)**
Crea el Bucket S3 para guardar los estados (`.tfstate`), DynamoDB para bloqueos y los roles OIDC para que GitHub Actions pueda conectarse a AWS de forma segura sin contraseñas.

```bash
cd iac/bootstrap
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform apply

```

**2. Capa de Entorno (ej. Prod / Dev)**
Despliega todos los recursos de red, base de datos, caché y cómputo.

```bash
cd iac/environments/dev  # o prod, dev
cp terraform.tfvars.example terraform.tfvars
# Llena todas las variables requeridas (contraseñas de BD, colas, tokens, etc.)
terraform init
terraform apply

```

### FASE 2: Despliegue de Aplicación (GitHub Actions)

El proyecto incluye un pipeline en `.github/workflows/deploy-dev.yml` que reacciona a los pushes en la rama `develop` (o equivalentes para QA/Prod).

Para que GitHub Actions despliegue automáticamente el código en AWS ECS, debes configurar lo siguiente en **Settings > Secrets and variables > Actions** de tu repositorio en GitHub:

**Variables (`Repository variables`):**

* `ECR_REPOSITORY`: Nombre del repositorio creado en ECR (ej. `tesla-backend-dev-backend`).
* `ECS_CLUSTER`: Nombre del clúster (ej. `tesla-backend-dev-cluster`).
* `ECS_SERVICE`: Nombre del servicio (ej. `tesla-backend-dev-service`).
* `TASK_FAMILY`: Familia de la tarea (ej. `tesla-backend-dev-api-task`).
* `AWS_ROLE_ARN`: El ARN del rol OIDC que arrojó el módulo `bootstrap` al hacer apply.

**Secretos (`Repository secrets`):**

* `SONAR_TOKEN`: Tu token de autenticación para SonarCloud.
* `DB_PASSWORD`: La contraseña de PostgreSQL a inyectar en las tareas ECS.
* `MQ_PASSWORD`: La contraseña de RabbitMQ a inyectar en las tareas ECS.

**Flujo:**

1. Haces push a `develop`.
2. GitHub Actions analiza el código en SonarCloud.
3. Se construye el `.jar` empaquetando la aplicación.
4. Se compila y publica la imagen Docker en Amazon ECR usando el `Dockerfile` optimizado.
5. Se inyecta la nueva imagen en la definición de tarea de ECS y se reinician los contenedores Fargate automáticamente sin tiempo de inactividad (Zero Downtime).