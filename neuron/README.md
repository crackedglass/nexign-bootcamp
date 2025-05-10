# Billing System Project

This project simulates a mobile operator's billing system, consisting of several microservices.

## Project Structure

The project is a multi-module Maven build with the following services:

- `cdr-generator`: Emulates a network switch, generating Call Data Records (CDRs) and sending them to the BRT service via RabbitMQ.
- `brt-service`: Billing Real Time service. Stores subscriber and call information, interacts with HRS for tariff calculations, and manages subscriber balances.
- `hrs-service`: High-performance Rating Server. Manages tariff plans and calculates call costs.
- `crm-service`: Customer Relationship Management service. Provides an API for subscribers and managers to interact with the system.

## Technologies

- Java 17
- Spring Boot
- Spring Security, Data, Web, Cloud, AMQP
- Maven
- RabbitMQ
- PostgreSQL
- H2 Database (for cdr-generator)
- Liquibase
- Docker
- JUnit, Mockito, Spring Test

## Getting Started

(Instructions for building and running the project will be added here.)

## Service Interaction Diagram

(A diagram illustrating service interactions will be added here.)

## Database Schema

(Database schema details will be added here.)

## API Documentation (Swagger UI)

(Links to Swagger UI for each service will be added here.)

## Manual Service Startup

(Instructions for starting services manually will be added here.)

## Authentication/Authorization

(Details about authentication and roles will be added here.)

## Database Connection

(Details for connecting to databases will be added here.) 