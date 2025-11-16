SELECT 'CREATE DATABASE dental_lab'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'dental_lab');

SELECT 'CREATE DATABASE dental_keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'dental_keycloak');
