# provider.tf

# Configure the AWS Provider
# The provider block is used to configure the named provider, in this case aws.
AWS_ACCESS_KEY = AKIA57VDLKNGRL2ZVFVY
AWS_SECRET_KEY =

provider "aws" {
  region  = "us-east-1"  # Change to your preferred AWS region
  access_key = "your_aws_access_key"   # Replace with your AWS access key
  secret_key = "your_aws_secret_key"   # Replace with your AWS secret key
}
