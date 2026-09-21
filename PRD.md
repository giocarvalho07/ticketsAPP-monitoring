# PRD - Projeto Tickets API

## Visão Geral

O projeto **Tickets** é uma API REST backend com Spring Boot para gerenciamento de
usuários e pedidos. Projeto didático do curso Mentoria Tech, com foco em APIs
REST e stack completa de monitoramento (Prometheus, Grafana, Alertmanager).
Não possui frontend. Arquitetura em camadas com banco MySQL.

---

## Stack Tecnológica

| Componente        | Tecnologia                    | Versão  |
|-------------------|-------------------------------|---------|
| Linguagem         | Java                          | 17      |
| Framework         | Spring Boot                   | 3.4.5   |
| Build Tool        | Maven (wrapper)               | 3.9.9   |
| ORM               | Spring Data JPA               | -       |
| Banco             | MySQL                         | -       |
| Métricas          | Prometheus                    | latest  |
| Dashboards        | Grafana                       | latest  |
| Alertas           | Alertmanager                  | latest  |
| Containerização   | Docker Compose                | 3.3     |

---

## Estrutura do Projeto

```
tickets/
├── config/                          # Monitoramento
│   ├── alertmanager.yml
│   ├── prometheus.yml
│   └── rules/alerts.yml
├── logs/                            # Logs da aplicação
├── src/main/java/com/tickets/
│   ├── TicketsApplication.java      # Entry point
│   ├── controller/
│   │   ├── OrderController.java
│   │   └── UserController.java
│   ├── domain/
│   │   ├── Order.java
│   │   └── User.java
│   ├── repository/
│   │   ├── OrderRepository.java
│   │   └── UserRepository.java
│   └── service/
│       ├── OrderService.java
│       └── UserService.java
├── src/main/resources/
│   ├── application.properties
│   └── application.yml
├── docker-compose.yml
├── pom.xml
└── mvnw / mvnw.cmd
```

---

## Modelo de Dados

### User (`users`) | Order (`orders`)

| User        | Tipo   | Restrições     | Order      | Tipo    | Restrições   |
|-------------|--------|----------------|------------|---------|--------------|
| idUser      | Long   | PK, auto-incr  | idOrder    | Long    | PK, auto-incr|
| name        | String | NOT NULL       | item       | String  | NOT NULL     |
| email       | String | NOT NULL, UNQ  | quantity   | Integer | -            |
|             |        |                | value      | Double  | -            |
|             |        |                | user_id    | Long    | FK, NOT NULL |

Relacionamento: **User 1:N Order** (OneToMany/ManyToOne bidirecional).

---

## Arquitetura em Camadas

```
[Cliente HTTP] → [Controller] → [Service] → [Repository] → [MySQL]
```

- **Controller** (`@RestController`): recebe HTTP, valida, retorna JSON
- **Service** (`@Service`): lógica de negócio, logs SLF4J, `@Transactional`
- **Repository** (`extends JpaRepository`): CRUD automático, consultas derivadas

## Endpoints da API

### UserController (`/users`)

| Método | Endpoint                        | Descrição                   |
|--------|---------------------------------|------------------------------|
| GET    | `/users`                        | Lista users (sem orders)     |
| GET    | `/users/email/{email}`          | Busca user por email         |
| GET    | `/users/detalhe-usuario/{id}`   | Detalhe do user com orders   |
| POST   | `/users`                        | Cria user                    |
| PUT    | `/users/{id}`                   | Atualiza user                |
| DELETE | `/users/{id}`                   | Deleta user                  |

### OrderController (`/orders`)

| Método | Endpoint                    | Descrição                   |
|--------|-----------------------------|------------------------------|
| GET    | `/orders`                   | Lista orders (sem user)      |
| GET    | `/orders/detalhe/{id}`      | Detalhe do order com user    |
| POST   | `/orders`                   | Cria order                   |
| PUT    | `/orders/{id}`              | Atualiza order               |
| DELETE | `/orders/{id}`              | Deleta order                 |

---

## Configuração do Banco (`application.properties`)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tickets?serverTimezone=America/Sao_Paulo
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Criar banco antes de rodar: `CREATE DATABASE IF NOT EXISTS tickets;`
O Hibernate cria tabelas automaticamente (`ddl-auto=update`).

---

## Configuração de Logs (`application.properties`)

```properties
# Logging
logging.file.name=logs/app.log
logging.level.com.tickets=INFO
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} %5p [%thread] %logger{36} - %msg%n
```

Os logs são gravados em `logs/app.log`.

---

## Stack de Monitoramento

| Serviço      | Porta | Config                  | Função                      |
|--------------|-------|-------------------------|------------------------------|
| Prometheus   | 9090  | config/prometheus.yml   | Scrape a cada 5s             |
| Grafana      | 3000  | -                       | Dashboards (admin/admin)     |
| Alertmanager | 9093  | config/alertmanager.yml | Notificações por email       |
| Actuator     | 8080  | application.yml         | /health, /metrics, /prometheus|

## Comandos para Executar

Pré-requisitos: Java 17 JDK, MySQL na porta 3306, Docker + Docker Compose

### Criar banco

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS tickets;"
```

### Compilar

```bash
./mvnw clean install          # Linux/Mac
mvnw.cmd clean install        # Windows
```

### Rodar aplicação

```bash
./mvnw spring-boot:run        # Linux/Mac
mvnw.cmd spring-boot:run      # Windows
java -jar target/tickets-0.0.1-SNAPSHOT.jar  # Via JAR
```

### Rodar testes

```bash
./mvnw test
```

### Monitoramento

```bash
docker-compose up -d          # Iniciar
docker-compose down           # Parar
docker-compose ps             # Status
```

---

## URLs dos Serviços

| Serviço      | URL                           |
|--------------|-------------------------------|
| API          | http://localhost:8080          |
| Prometheus   | http://localhost:9090          |
| Grafana      | http://localhost:3000          |
| Alertmanager | http://localhost:9093          |

---

## Fluxo de Execução

### 1. Preparação

```bash
java -version
mysql -u root -e "CREATE DATABASE IF NOT EXISTS tickets;"
```

### 2. Build e Execução

```bash
./mvnw clean install
./mvnw spring-boot:run
```

### 3. Monitoramento

```bash
docker-compose up -d
```

### 4. Testar Endpoints

```bash
# Criar usuário
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"João","email":"joao@email.com"}'

# Listar usuários
curl http://localhost:8080/users

# Criar pedido
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"item":"Notebook","quantity":1,"value":4500.00,"user":{"idUser":1}}'

# Detalhe do usuário com pedidos
curl http://localhost:8080/users/detalhe-usuario/1

# Detalhe do pedido com usuário
curl http://localhost:8080/orders/detalhe/1
```

---

## Formato dos Requests

```json
// Criar User:  { "name": "João Silva", "email": "joao@email.com" }
// Criar Order: { "item": "Notebook Dell", "quantity": 1, "value": 4500.00, "user": { "idUser": 1 } }
```

## Observações

1. Sem autenticação - API aberta
2. Sem frontend - Apenas backend REST
3. DDL automático - Hibernate cria tabelas
4. Lombok não usado - Getters/setters manuais
5. Nomes em PT-BR - Métodos e logs em Português
6. Serialização circular - Controllers removem relacionamentos manualmente
7. Alertmanager - Configurar senha de aplicativo Gmail (ver monitoraao.md)
8. Porta padrão - 8080
9. Logs - Gravados em `logs/app.log`
