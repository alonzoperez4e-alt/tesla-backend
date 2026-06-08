Documentacion de feat/migrar_a_cognito

1. Crear docker compose para levantar una base de datos local para pruebas.

   ```bash
   docker-compose up -d
   ```

2. Configurar las variables de entorno para la conexión a la base de datos y Cognito en el archivo `application.properties` y `application-dev.properties`.

3. Crear la infraestructura de Cognito mediante Terraform (dev, qa y prod).

   ```bash
   cd iac/environments/dev

   terraform init
   terraform plan
   terraform apply
   ```

   4. Obtener los valores generados por Terraform indicados en la salida del comando `terraform apply` y configurar las variables de entorno.

      Los valores importantes son:

       * `dev_cognito_user_pool_id`
       * `dev_cognito_user_pool_client_id`
       * `dev_cognito_issuer_uri`
       * `dev_cognito_user_pool_domain`

5. Registrar los usuarios de la aplicación en Cognito de manera manual a traves de la consola web de AWS.

   Para cada usuario existente se debe:

    * Crear el usuario en el User Pool.
    * Asignarlo al grupo correspondiente (`administrador` o `alumno`).
    * Guardar el valor del campo `sub` de Cognito en la columna `cognito_sub` de la tabla `usuario`.

6. Configurar Postman simulando consultas por parte del frontend para autenticarse contra Cognito utilizando:

    * User Pool ID.
    * App Client ID.
    * Dominio de Cognito generado por Terraform.

7. Ejecutar el backend.

8. Validar la autenticación.

   El backend ya no genera ni valida JWT propios. Ahora:

    * Cognito emite los tokens.
    * Spring Security valida los tokens usando `COGNITO_ISSUER_URI`.
    * Los roles se obtienen desde el claim `cognito:groups`.
    * Los grupos soportados son:

        * `administrador`
        * `alumno`

9. Verificar los endpoints migrados.

   Los endpoints que anteriormente recibían `userId`, `studentId` o `creatorId` desde el frontend ahora obtienen la identidad directamente desde el JWT de Cognito mediante el componente `IdentityExtractor`.

10. Eliminar configuraciones antiguas de autenticación.

    Se removieron:

    * `JwtAuthFilter`
    * `JwtService`
    * `AuthenticationController`
    * `AuthenticationServiceImpl`
    * `CustomUserDetailsService`
    * `AuthenticationConfig`

    Toda la autenticación ahora depende de AWS Cognito y OAuth2 Resource Server de Spring Security.
11. Realizar pruebas de integración para validar el flujo completo de autenticación y autorización con Cognito.