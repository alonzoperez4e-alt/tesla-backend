variable "prefix" {
  description = "Prefijo para los nombres de los recursos"
  type        = string
}

variable "allowed_callback_urls" {
  description = "URLs permitidas para el redireccionamiento después del login"
  type        = list(string)
}