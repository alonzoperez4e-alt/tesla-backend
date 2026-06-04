resource "aws_cognito_user_pool" "pool" {
  name = "${var.prefix}-user-pool"

  alias_attributes = ["email", "preferred_username"]
  auto_verified_attributes = ["email"]

  password_policy {
    minimum_length = 8
    require_uppercase = true
    require_lowercase = true
    require_numbers = true
    require_symbols = true
  }

  account_recovery_setting {
    recovery_mechanism {
      name = "verified_email"
      priority = 1
    }
  }

  schema {
    name = "given_name"
    attribute_data_type = "String"
    required = true
    mutable = true

    string_attribute_constraints {
      min_length = 1
      max_length = 100
    }
  }

  schema {
    name = "family_name"
    attribute_data_type = "String"
    required = true
    mutable = true

    string_attribute_constraints {
      min_length = 1
      max_length = 100
    }
  }
}

resource "aws_cognito_user_pool_client" "client" {
  name         = "${var.prefix}-web-client"
  user_pool_id = aws_cognito_user_pool.pool.id

  generate_secret = false
  allowed_oauth_flows = ["code"]
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_scopes = ["email", "openid", "profile"]
  supported_identity_providers = ["COGNITO"]

  callback_urls = var.allowed_callback_urls
  logout_urls = var.allowed_callback_urls

  access_token_validity = 15
  id_token_validity = 15
  refresh_token_validity = 30

  token_validity_units {
    access_token = "minutes"
    id_token = "minutes"
    refresh_token = "days"
  }
}

resource "aws_cognito_user_group" "administrador" {
  name         = "administrador"
  user_pool_id = aws_cognito_user_pool.pool.id
}

resource "aws_cognito_user_group" "alumno" {
  name         = "alumno"
  user_pool_id = aws_cognito_user_pool.pool.id
}