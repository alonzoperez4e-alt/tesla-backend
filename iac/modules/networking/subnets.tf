resource "aws_subnet" "public" {
  count = length(var.public_subnets_cidr)

  vpc_id = aws_vpc.main.id
  cidr_block = var.public_subnets_cidr[count.index]
  availability_zone = local.azs[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-${var.environment}-public-subnet-${count.index}"
  }
}

resource "aws_subnet" "database" {
  count = length(var.database_subnets_cidr)

  vpc_id = aws_vpc.main.id
  cidr_block = var.database_subnets_cidr[count.index]
  availability_zone = local.azs[count.index]

  tags = {
    Name = "${var.project_name}-${var.environment}-database-subnet-${count.index}"
  }
}