Most small- and medium-sized projects have to solve a similar set of problems. 
This repository aims to be a starting point for developers who want to focus on 
implementing business logic right away by providing a robust architecture for a 
web application that includes a persistence layer and user management.  The 
template scales well with team size and application load, while utilizing best 
practices in software development.

See the code in action: https://crediblebadger.com/

This framework is based on AWS, Spring, Postgres, Liquibase, Velocity, and 
includes a docker-compose.yaml file that facilitates deployment in the cloud.
This default configuration runs the database and the application server on a
single machine for the sake of simplicity and fast deployments. However, 
horizontal scaling with multiple application server and a dedicated database 
server can be achieved with minimal changes to the configuration. The code was
therefore written with parallel execution in mind. Components like the database, 
the email rendering service or the cloud provider can be easily swapped.

The database is set up with the principle of least privilege in mind. The 
provided user groups enable easy management of developers and support users. 
Liquibase is used as a scalable approach to database development. The server 
communicates with the database using Hibernate and HQL, and is not dependent on 
the specific database technology used.

This repository features a user management system that securely stores encoded 
passwords. The system is also able to generate tokens that are sent out via AWS 
SES to verify the accounts or change passwords. These emails are rendered 
using Velocity. The application server provides verified and authenticated users 
with the ability to upload, list, and download files which are stored using AWS 
S3. Sessions are stored in the DB and therefore valid accross multiple 
application servers or after a server restart.

The front end is made with React with the static content being delivered 
directly by Spring.

If you want to address a small- or medium-sized business need with fewer than 
200k monthly active users or 20 developers, this project might be useful to you.

These environmental variables are required before the system can start up.
DB_ADMIN_PASSWORD - admin user is only used during initialization
DB_SERVICE_PASSWORD - Spring user, can be used to read/write from tables
DB_LIQUIBASE_PASSWORD - Liquibase user, can create tables
AWS_S3_BUCKET - AWS bucket for the storage of unstructured user data
SPRING_ADMIN_PASSWORD - Spring admin password

Local development using any cloud features requires an AWS account, the 
creation of a .aws/credentials file, a quick setup of Amazon SES and the 
creation of an S3 bucket. This project can be run using Amazon AWS Free Tier.