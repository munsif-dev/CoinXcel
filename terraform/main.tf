provider "aws" {
  region = "us-west-2"  # Specify your region
}

resource "aws_instance" "springboot_instance" {
  ami           = "ami-xxxxxxxxxxxxxxxxx"  # Replace with your AMI
  instance_type = "t2.micro"
  key_name      = "your-key-pair-name"     # Replace with your SSH key name

  tags = {
    Name = "SpringBootInstance"
  }
}

resource "aws_instance" "mysql_instance" {
  ami           = "ami-xxxxxxxxxxxxxxxxx"  # Replace with your AMI
  instance_type = "t2.micro"
  key_name      = "your-key-pair-name"     # Replace with your SSH key name

  tags = {
    Name = "MySQLInstance"
  }
}

output "springboot_instance_ip" {
  value = aws_instance.springboot_instance.public_ip
}

output "mysql_instance_ip" {
  value = aws_instance.mysql_instance.public_ip
}
