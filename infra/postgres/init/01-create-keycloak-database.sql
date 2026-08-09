-- Runs only on the first startup of an empty postgres volume.
-- Keycloak keeps its own schema; it must never share a database with the application.

CREATE DATABASE keycloak;
