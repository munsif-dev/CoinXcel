provider "aws" {
  region = "us-east-1"
}

resource "aws_instance" "db_instance" {
  ami           = "ami-0c55b159cbfafe1f0"  
  instance_type = "t2.micro"
  key_name      = "your-key-pair"
  tags = {
    Name = "coinxcel-database"
  }
}

resource "aws_instance" "backend_instance" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  key_name      = "your-key-pair"
  tags = {
    Name = "coinxcel-backend"
  }
}

resource "aws_instance" "frontend_instance" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  key_name      = "your-key-pair"
  tags = {
    Name = "coinxcel-frontend"
  }
}

output "db_ip" {
  value = aws_instance.db_instance.public_ip
}

output "backend_ip" {
  value = aws_instance.backend_instance.public_ip
}

output "frontend_ip" {
  value = aws_instance.frontend_instance.public_ip
}
