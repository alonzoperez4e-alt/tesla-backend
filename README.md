# Tesla Backend

Plataforma backend basada en **Spring Boot 4** y **Java 21**, diseñada para desplegarse en **AWS (ECS Fargate)** utilizando una arquitectura de infraestructura modular con **Terraform**.

## Arquitectura

* **App:** Java 21, Spring Boot, Spring Security (OAuth2/JWT con Cognito), PostgreSQL.
* **Infraestructura (AWS):** VPC, API Gateway (HTTP) + VPC Link, ECS (Fargate) con Cloud Map, ECR, RDS, CloudFront y Cognito.

> **Nota:** ElastiCache (Redis), Amazon MQ, el ALB y el NAT Gateway se eliminaron en la optimización FinOps. El ranking se agrega directamente desde PostgreSQL y el chat de grupos usa un broker STOMP en memoria (válido solo con una única tarea ECS).
* **IaC:** Terraform estructurado por módulos (`networking`, `security`, `database`, `compute`, `edge`, `cognito`) y entornos (`dev`, `qa`, `prod`). Incluye una capa fundacional (`bootstrap`).
* **CI/CD:** GitHub Actions con autenticación OIDC hacia AWS. La validación (tests + JaCoCo, SonarCloud y Checkov) corre en `ci.yml` sobre cada Pull Request; el despliegue continuo a ECS corre en `deploy-dev.yml` al hacer merge a `develop`.

## Prerrequisitos

* [Java 21](https://aws.amazon.com/corretto/) y Maven.
* [Docker Desktop](https://www.docker.com/products/docker-desktop/).
* [Terraform CLI](https://developer.hashicorp.com/terraform/downloads) (v1.0+).
* [AWS CLI](https://aws.amazon.com/cli/) configurado con credenciales válidas.

---

## Despliegue en Local

Para el entorno de desarrollo local, utilizaremos contenedores para los servicios de respaldo (bases de datos, colas) y Terraform para aprovisionar los servicios cloud estrictamente necesarios (como Amazon Cognito).

### 1. Levantar Servicios Base (Docker)

Levanta PostgreSQL localmente (el análisis de calidad corre en SonarCloud vía CI, no localmente):

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

### FASE 2: Validación y Despliegue de Aplicación (GitHub Actions)

El pipeline está separado en dos workflows:

* **`ci.yml`** (Continuous Integration): se dispara en cada **Pull Request** hacia `develop`/`main`. Corre `mvn verify` (tests + cobertura JaCoCo), análisis en SonarCloud y escaneo de IaC con Checkov. **No despliega.** Es el gate de calidad.
* **`deploy-dev.yml`** (Continuous Deployment): se dispara al hacer **merge/push a `develop`**. Construye el `.jar`, publica la imagen en ECR y actualiza el servicio ECS. Asume código ya validado en el PR.

Para que GitHub Actions funcione, configura lo siguiente en **Settings > Secrets and variables > Actions** de tu repositorio en GitHub:

**Variables (`Repository variables`):**

* `ECR_REPOSITORY`: Nombre del repositorio creado en ECR (ej. `tesla-backend-dev-backend`).
* `ECS_CLUSTER`: Nombre del clúster (ej. `tesla-backend-dev-cluster`).
* `ECS_SERVICE`: Nombre del servicio (ej. `tesla-backend-dev-service`).
* `TASK_FAMILY`: Familia de la tarea (ej. `tesla-backend-dev-api-task`).
* `AWS_ROLE_ARN`: El ARN del rol OIDC que arrojó el módulo `bootstrap` al hacer apply.

**Secretos (`Repository secrets`):**

* `SONAR_TOKEN`: Tu token de autenticación para SonarCloud.

> **Nota sobre credenciales de BD y colas:** `DB_PASSWORD` y `MQ_PASSWORD` **ya no se inyectan desde GitHub**. Terraform los almacena en **AWS SSM Parameter Store** (`SecureString`) a partir de las variables definidas en `terraform.tfvars`, y la task definition de ECS los lee vía `secrets`/`valueFrom`. Así las contraseñas nunca viajan por el pipeline ni quedan en texto plano en la definición de tarea.

**Flujo:**

1. Abres un Pull Request hacia `develop`.
2. `ci.yml` corre tests + cobertura, SonarCloud y Checkov. El PR solo puede mergearse si el gate pasa.
3. Al mergear a `develop`, `deploy-dev.yml` construye el `.jar` y publica la imagen Docker en Amazon ECR usando el `Dockerfile` optimizado.
4. Se inyecta la nueva imagen en la definición de tarea de ECS (con los secretos resueltos desde SSM) y se reinician los contenedores Fargate automáticamente sin tiempo de inactividad (Zero Downtime).

## Ventana de disponibilidad (18:00 - 24:00, hora de Perú)

El backend **no está encendido las 24 horas**. Para recortar el coste horario, cuatro
programaciones de EventBridge Scheduler (`iac/modules/scheduler/`) apagan y encienden los
dos únicos recursos que facturan por hora:

| Hora de Lima | Acción                                          |
| ------------ | ----------------------------------------------- |
| 17:40        | Arranca la instancia RDS                        |
| 18:00        | El servicio ECS pasa a 1 tarea                  |
| 00:00        | El servicio ECS vuelve a 0 tareas               |
| 00:10        | Se detiene la instancia RDS                     |

Fuera de esa franja, CloudFront responde **503** en `/api/*` con un JSON explicativo en
lugar del error genérico de API Gateway.

**Levantar el entorno a mano** (por ejemplo para depurar de madrugada). El cierre
programado de esa misma noche lo devolverá a 0, no hace falta apagarlo:

```bash
ENTORNO=dev  # dev | qa | prod

aws rds start-db-instance --db-instance-identifier "tesla-backend-$ENTORNO-db"
aws rds wait db-instance-available --db-instance-identifier "tesla-backend-$ENTORNO-db"

aws ecs update-service \
  --cluster "tesla-backend-$ENTORNO-cluster" \
  --service "tesla-backend-$ENTORNO-service" \
  --desired-count 1
```

El pipeline de CD hace exactamente esto en su primer job (`activar`), así que **no hace
falta encender nada antes de desplegar**.

**Volver a 24 horas** sin destruir las programaciones: `ventana_habilitada = false` en el
`terraform.tfvars` del entorno (las deja en estado `DISABLED` y quita el 503 del borde).
Si en cambio se quiere **cambiar el horario**, hay que mover con él dos cosas que dependen
de la franja: las ventanas de backup/mantenimiento de RDS (`iac/modules/database/bd.tf`) y
el cron del snapshot semanal (`app.ranking.snapshot.cron`).