CREATE DATABASE cosmic;
CREATE USER cosmic_admin WITH CREATEROLE ENCRYPTED PASSWORD 'redsnailshell';
GRANT ALL PRIVILEGES ON DATABASE cosmic TO cosmic_admin;