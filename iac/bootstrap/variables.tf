variable "aws_region" {
  description = "Región de AWS donde bootstrap se desplegará"
  type        = string
}

variable "prefix" {
  description = "Prefijo para los nombres de los recursos"
  type        = string
}

variable "github_repo" {
  description = "Repositorio de GitHub para GitHub Actions"
  type        = string
}

variable "environments" {
  description = "Mapeo de entornos a ramas de GitHub para GitHub Actions"

  type = map(object({
    github_branch = string
  }))
  default = {
    "dev" = {
      github_branch = "develop"
    }
    "prod" = {
      github_branch = "main"
    }
  }
}