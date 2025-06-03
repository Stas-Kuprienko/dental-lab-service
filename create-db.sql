SELECT 'CREATE DATABASE dental_lab'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'dental_lab');