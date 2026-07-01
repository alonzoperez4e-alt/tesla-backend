terraform {
  backend "s3" {
    bucket         = "tesla-bootstrap-terraform-state-382876615060"
    key            = "dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "tesla-bootstrap-terraform-locks"
    encrypt        = true
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Environment = "dev"
      Project     = "tesla-backend"
      ManagedBy   = "Terraform"
    }
  }
}
