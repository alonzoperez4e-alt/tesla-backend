terraform {
  backend "s3" {
    bucket = "tesla-bootstrap-terraform-state-050608055215"
    key    = "qa/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "tesla-bootstrap-terraform-locks"
    encrypt = true
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
}
