# Monitoramento - Projeto Tickets API

Guia completo de monitoramento com Prometheus, Alertmanager e Grafana para a API REST de gerenciamento de usuários e pedidos.

---

## Arquitetura de Monitoramento

```
[Spring Boot App :8080] --scrape--> [Prometheus :9090] --alertas--> [Alertmanager :9093] --notify--> [Email]
       |
       --> [Grafana :3000]
```

| Serviço      | Porta | URL                          |
|--------------|-------|------------------------------|
| API          | 8080  | http://localhost:8080         |
| Prometheus   | 9090  | http://localhost:9090         |
| Grafana      | 3000  | http://localhost:3000         |
| Alertmanager | 9093  | http://localhost:9093         |

---

## 1. Prometheus

### O que é

Prometheus é uma ferramenta de monitoramento e alerting open-source que coleta métricas via HTTP scrape em intervalos configuráveis. Neste projeto, ele faz scrape dos endpoints de métricas expostos pelo Spring Boot Actuator.

### Configuração (`config/prometheus.yml`)

- **Scrape interval:** 5 segundos
- **Job:** `api-tickets-jobs`
- **Target:** `host.docker.internal:8080` (app Spring Boot na máquina host)
- **Metrics path:** `/actuator/prometheus`
- **Label:** `application: "api-tickets"`

### Endpoints Relevantes da API

| Endpoint                          | Descrição                       |
|-----------------------------------|----------------------------------|
| `/actuator/prometheus`            | Métricas no formato Prometheus   |
| `/actuator/metrics`               | Métricas no formato JSON         |
| `/actuator/health`                | Health check detalhado           |

### Queries do Prometheus para DEMO

Acesse o Prometheus em `http://localhost:9090` e use o menu **Status > Targets** para confirmar que o job `api-tickets-jobs` está UP.

#### Status do Target

```promql
up{application="api-tickets"}
```
> Retorna `1` se a aplicação estiver respondendo, `0` se estiver down.

#### Métricas de Requisições HTTP (Taxa de Requests por Segundo)

```promql
rate(http_server_requests_seconds_count{application="api-tickets"}[1m])
```
> Quantas requisições por segundo a API está recebendo, agrupadas por endpoint e método.

#### Métricas de Requisições HTTP (por Método HTTP)

```promql
http_server_requests_seconds_count{application="api-tickets", method="GET"}
```
> Total de requisições GET acumuladas desde o início da aplicação.

```promql
http_server_requests_seconds_count{application="api-tickets", method="POST"}
```
> Total de requisições POST acumuladas.

```promql
http_server_requests_seconds_count{application="api-tickets", method="PUT"}
```
> Total de requisições PUT acumuladas.

```promql
http_server_requests_seconds_count{application="api-tickets", method="DELETE"}
```
> Total de requisições DELETE acumuladas.

#### Métricas de Requisições HTTP (por Endpoint e Status Code)

```promql
http_server_requests_seconds_count{application="api-tickets", uri="/users", status="200"}
```
> Total de requisições GET `/users` com status 200 (OK).

```promql
http_server_requests_seconds_count{application="api-tickets", uri="/orders", status="200"}
```
> Total de requisições GET `/orders` com status 200.

```promql
http_server_requests_seconds_count{application="api-tickets", status="404"}
```
> Total de requisições que retornaram 404 (Not Found).

```promql
http_server_requests_seconds_count{application="api-tickets", status="500"}
```
> Total de requisições que retornaram 500 (Internal Server Error).

#### Latência de Resposta (P95)

```promql
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{application="api-tickets"}[1m]))
```
> 95% das requisições estão sendo respondidas abaixo deste tempo (em segundos).

#### Latência Média

```promql
rate(http_server_requests_seconds_sum{application="api-tickets"}[1m]) / rate(http_server_requests_seconds_count{application="api-tickets"}[1m])
```
> Tempo médio de resposta da API.

#### Percentual de Erros (Taxa de Erro)

```promql
sum(rate(http_server_requests_seconds_count{application="api-tickets", status=~"5.."}[1m])) / sum(rate(http_server_requests_seconds_count{application="api-tickets"}[1m])) * 100
```
> Porcentagem de requisições que retornaram status 5xx nos últimos 1 minuto.

#### Uso de Memória JVM

```promql
jvm_memory_used_bytes{application="api-tickets", area="heap"}
```
> Bytes usados na heap JVM.

```promql
jvm_memory_max_bytes{application="api-tickets", area="heap"}
```
> Limite máximo da heap JVM.

#### Uso de CPU

```promql
rate(process_cpu_usage{application="api-tickets"}[1m]) * 100
```
> Porcentagem de uso da CPU pelo processo Spring Boot.

#### Threads Ativas

```promql
jvm_threads_live_threads{application="api-tickets"}
```
> Número total de threads ativas na JVM.

#### Taxa de Coleta do Prometheus (por Job)

```promql
scrape_duration_seconds{job="api-tickets-jobs"}
```
> Tempo que o Prometheus leva para coletar as métricas da aplicação.

#### Requisições por Endpoint (Top 5 com Mais Tráfego)

```promql
topk(5, rate(http_server_requests_seconds_count{application="api-tickets"}[5m]))
```
> Top 5 endpoints com maior taxa de requisições nos últimos 5 minutos.

---

## 2. Alertmanager

### O que é

Alertmanager recebe alertas do Prometheus e roteia notificações por canais configurados (email, Slack, webhook, etc.). Neste projeto, está configurado para enviar emails via SMTP (Gmail).

### Configuração (`config/alertmanager.yml`)

- **Resolve timeout:** 5 minutos
- **Receiver:** `email-on-start`
- **SMTP:** `smtp.gmail.com:587` (TLS)
- **Group wait:** 10 segundos (aguarda antes de agrupar alertas)
- **Group interval:** 10 segundos (intervalo entre grupos)
- **Repeat interval:** 1 hora (repete alerta a cada hora se ativo)

### Regra de Alerta Configurada (`config/rules/alerts.yml`)

```yaml
groups:
  - name: application_startup
    rules:
      - alert: ApplicationStarted
        expr: up{application="api-tickets"} == 1
        for: 0s
        labels:
          severity: info
        annotations:
          summary: "Aplicação Iniciada"
          description: "A aplicação Spring Boot foi iniciada."
```

### Como Funciona

1. Prometheus verifica a expressão `up{application="api-tickets"} == 1` a cada 5s
2. Quando a aplicação sobe, a regra dispara imediatamente (`for: 0s`)
3. Prometheus envia o alerta para o Alertmanager
4. Alertmanager roteia para o receiver `email-on-start`
5. Um email é enviado com a notificação de que a aplicação iniciou

---

### Configuracao de Email (Gmail) - Passo a Passo

Para receber alertas por email, voce precisa configurar uma **Senha de Aplicativo** no Gmail. A senha normal do Google NAO funciona.

#### Passo 1: Criar Senha de Aplicativo no Google

1. Acesse https://myaccount.google.com/security
2. Clique em **Verificacao em duas etapas** (Two-Step Verification)
3. Ative se ainda nao estiver ativo
4. Volte a tela anterior e clique em **Senhas de aplicativo** (App passwords)
5. Selecione o app **Email** e o dispositivo **Outro (nome personalizado)**
6. Digite `Alertmanager` e clique em **Gerar**
7. O Google vai gerar algo como `xxxx xxxx xxxx xxxx` (16 caracteres)
8. **COPIE** essa senha - ela sera usada no alertmanager.yml

> IMPORTANTE: Anote essa senha. O Google so mostra uma vez.

#### Passo 2: Configurar o alertmanager.yml

Abra o arquivo `config/alertmanager.yml` e substitua os placeholders:

```yaml
global:
  resolve_timeout: 5m

route:
  receiver: 'email-on-start'
  group_by: ['alertname', 'job']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h

receivers:
  - name: 'email-on-start'
    email_configs:
      - to: 'SEU_EMAIL@gmail.com'              # <-- SEU EMAIL
        from: 'ALERT_EMAIL@gmail.com'           # <-- EMAIL QUE ENVIA (pode ser o mesmo)
        smarthost: 'smtp.gmail.com:587'
        auth_username: 'ALERT_EMAIL@gmail.com'  # <-- MESMO DO 'from'
        auth_password: 'xxxx xxxx xxxx xxxx'    # <-- SENHA DE APLICATIVO (16 chars)
        headers:
          Subject: '[ALERT Tickets API] - {{ .GroupLabels.alertname }}'
        tls_config:
          insecure_skip_verify: false
```

**Exemplo real** (usando o mesmo email para enviar e receber):

```yaml
receivers:
  - name: 'email-on-start'
    email_configs:
      - to: 'joao@gmail.com'
        from: 'joao@gmail.com'
        smarthost: 'smtp.gmail.com:587'
        auth_username: 'joao@gmail.com'
        auth_password: 'abcd efgh ijkl mnop'
        headers:
          Subject: '[ALERT Tickets API] - {{ .GroupLabels.alertname }}'
        tls_config:
          insecure_skip_verify: false
```

#### Passo 3: Reiniciar o Alertmanager

```bash
docker-compose restart alertmanager
```

#### Passo 4: Verificar se a Configuracao esta Correta

```bash
# Verificar status do Alertmanager
curl -s http://localhost:9093/api/v2/status | python -m json.tool

# Verificar receivers configurados
curl -s http://localhost:9093/api/v2/receivers | python -m json.tool

# Verificar alertas ativos
curl -s http://localhost:9090/api/v1/alerts | python -m json.tool
```

---

### Teste Completo: Enviar Email ao Iniciar a Aplicacao

Siga estes passos na ordem exata:

#### Passo 1: Verificar se o Alertmanager esta rodando

```bash
docker-compose ps alertmanager
```

> Deve mostrar `Up` na coluna Status.

#### Passo 2: Verificar se o Prometheus esta apontando para o Alertmanager

Acesse `http://localhost:9090/alerts`

> Deve mostrar a regra `ApplicationStarted` com status **pending** (se a app ainda nao subiu) ou **firing** (se ja subiu).

#### Passo 3: Parar tudo (para o teste ser limpo)

```bash
# Parar a aplicacao Spring Boot (se estiver rodando)
# Ctrl+C no terminal onde mvnw esta rodando

# Parar o Prometheus (para limpar alertas antigos)
docker-compose stop prometheus
docker-compose start prometheus
```

#### Passo 4: Verificar que NENHUM alerta esta ativo

Acesse `http://localhost:9090/alerts`

> Deve mostrar "No alert rules configured" ou lista vazia.

#### Passo 5: Subir a aplicacao Spring Boot

```bash
mvnw.cmd spring-boot:run
```

> Aguarde aparecer no console: `Started TicketsApplication in X seconds`

#### Passo 6: Verificar o alerta no Prometheus

Acesse `http://localhost:9090/alerts`

> O alerta `ApplicationStarted` deve aparecer com status **firing** (vermelho).

#### Passo 7: Verificar o alerta no Alertmanager

Acesse `http://localhost:9093/#/alerts`

> O alerta deve aparecer na lista de alertas ativos do Alertmanager.

#### Passo 8: Verificar o email

Abra a caixa de entrada do email configurado. Voce deve receber um email com:

```
Assunto: [ALERT Tickets API] - ApplicationStarted

Alerts Firing:
  ApplicationStarted
    Labels:
      alertname = ApplicationStarted
      application = api-tickets
      instance = host.docker.internal:8080
      job = api-tickets-jobs
      severity = info
    Annotations:
      description = A aplicação Spring Boot foi iniciada.
      summary = Aplicação Iniciada
    Source: http://localhost:9093
```

---

### Teste Rapido via CLI (sem subir a aplicacao)

Se voce quer testar o envio de email SEM precisar subir a aplicacao, use o CLI do Alertmanager:

```bash
curl -X POST http://localhost:9093/api/v2/alerts \
  -H "Content-Type: application/json" \
  -d '[
    {
      "labels": {
        "alertname": "TestAlert",
        "application": "api-tickets",
        "severity": "warning"
      },
      "annotations": {
        "summary": "Teste de Email",
        "description": "Este e um alerta de teste enviado diretamente para o Alertmanager."
      }
    }
  ]'
```

> Se a configuracao de email estiver correta, voce recebera o email em instantes.

#### Verificar se o alerta foi recebido

```bash
# Ver alertas ativos no Alertmanager
curl -s http://localhost:9093/api/v2/alerts | python -m json.tool
```

---

### Troubleshooting (Problemas Comuns)

#### "Erro de conexao SMTP" ou "authentication failed"

1. Verifique se a senha de aplicativo esta correta (16 caracteres, sem espacos extras)
2. Verifique se a Verificacao em Duas Etapas esta ativa no Google
3. Gere uma nova senha de aplicativo se necessário

#### "Email nao chegou"

1. Verifique a caixa de spam/lixo eletronico
2. Aguarde ate 1 minuto (pode haver delay no SMTP)
3. Verifique os logs do Alertmanager:
   ```bash
   docker-compose logs -f alertmanager
   ```

#### "Alerta nao aparece no Prometheus"

1. Verifique se a aplicacao esta rodando: `curl http://localhost:8080/actuator/health`
2. Verifique os targets: `http://localhost:9090/targets`
3. Verifique as regras: `http://localhost:9090/rules`

#### "Alerta aparece mas nao envia email"

1. Verifique os logs do Alertmanager:
   ```bash
   docker-compose logs -f alertmanager
   ```
2. Procure por erros como `level=error` ou `msg="Failed to send alert"`

---

### Para Testar o Alertmanager na DEMO

**1. Acesse a interface do Alertmanager:**

```
http://localhost:9093
```

**2. Verifique os targets de alerting no Prometheus:**

```
http://localhost:9090/alerts
```

> Aqui voce ve todos os alertas ativos e suas regras.

**3. Simule um alerta via CLI do Prometheus:**

```bash
# Verificar se o alerta ApplicationStarted esta ativo
curl -s http://localhost:9090/api/v1/alerts | python -m json.tool
```

**4. Verificar status do Alertmanager:**

```bash
curl -s http://localhost:9093/api/v2/status | python -m json.tool
```

**5. Verificar receivers configurados:**

```bash
curl -s http://localhost:9093/api/v2/receivers | python -m json.tool
```

**6. Alerta de demonstracao - Alertas de Silenciamento (para nao spam durante demo):**

Acesse `http://localhost:9093/#/silences` e crie uma silenciacao temporaria para evitar emails durante a demo.

### Criando Regras de Alerta Adicionais para a DEMO

Crie um novo arquivo `config/rules/alerts_demo.yml`:

```yaml
groups:
  - name: api_health
    rules:
      # Alerta quando a API está down
      - alert: APIDown
        expr: up{application="api-tickets"} == 0
        for: 10s
        labels:
          severity: critical
        annotations:
          summary: "API Tickets está DOWN"
          description: "A aplicação Spring Boot não está respondendo."

      # Alerta quando taxa de erro HTTP > 5%
      - alert: HighErrorRate
        expr: |
          (
            sum(rate(http_server_requests_seconds_count{application="api-tickets", status=~"5.."}[1m]))
            /
            sum(rate(http_server_requests_seconds_count{application="api-tickets"}[1m]))
          ) * 100 > 5
        for: 30s
        labels:
          severity: warning
        annotations:
          summary: "Taxa de erro HTTP elevada"
          description: "Mais de 5% das requisições estão retornando erro 5xx."

      # Alerta quando latência P95 > 2 segundos
      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{application="api-tickets"}[1m])) > 2
        for: 30s
        labels:
          severity: warning
        annotations:
          summary: "Latência P95 elevada"
          description: "95% das requisições estão demorando mais de 2 segundos."

      # Alerta quando memória JVM > 80%
      - alert: HighMemoryUsage
        expr: |
          (
            jvm_memory_used_bytes{application="api-tickets", area="heap"}
            /
            jvm_memory_max_bytes{application="api-tickets", area="heap"}
          ) * 100 > 80
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "Uso de memória JVM elevado"
          description: "Uso da heap JVM acima de 80%."
```

Para ativar, adicione ao `prometheus.yml`:

```yaml
rule_files:
  - 'rules/alerts.yml'
  - 'rules/alerts_demo.yml'
```

---

## 3. Grafana

### O que é

Grafana é uma plataforma de visualização e análise de dados. Neste projeto, ele é usado para criar dashboards com as métricas coletadas pelo Prometheus.

### Acesso

| Campo    | Valor   |
|----------|---------|
| URL      | http://localhost:3000 |
| Usuário  | `admin` |
| Senha    | `admin` |

> Na primeira vez que acessar, o Grafana pode pedir para alterar a senha padrão. Crie uma nova senha ou ignore para manter `admin`.

### Configurar a Fonte de Dados (Prometheus)

**Passo 1:** Acesse `http://localhost:3000`

**Passo 2:** Login com `admin` / `admin`

**Passo 3:** Clique no menu lateral em **Connections > Data sources** (ou use o atalho `Ctrl+Shift+D`)

**Passo 4:** Clique em **Add data source**

**Passo 5:** Selecione **Prometheus**

**Passo 6:** Configure:

| Campo          | Valor                            |
|----------------|----------------------------------|
| Name           | `Prometheus`                     |
| URL            | `http://prometheus:9090`         |
| Access         | `Server` (proxy)                 |
| Scrape interval| `5s`                             |

> **IMPORTANTE:** Use `http://prometheus:9090` (nome do container na rede Docker) e não `localhost:9090`.

**Passo 7:** Clique em **Save & Test**. Deve aparecer "Data source is working".

### Criar um Dashboard para DEMO

**Passo 1:** No menu lateral, clique em **Dashboards > New Dashboard** (ou use `+` > New Dashboard)

**Passo 2:** Clique em **Add visualization**

**Passo 3:** Selecione o data source **Prometheus**

**Passo 4:** Adicione painéis com as queries do Prometheus listadas na Seção 1

### Dashboard Sugerido para DEMO (Painéis)

Crie os seguintes painéis no dashboard:

#### Painel 1: Status da API

| Campo     | Valor                                           |
|-----------|-------------------------------------------------|
| Tipo      | Stat                                            |
| Query     | `up{application="api-tickets"}`                 |
| Título    | Status da API                                   |
| Thresholds| 0 = Red, 1 = Green                              |

#### Painel 2: Taxa de Requisições por Segundo

| Campo     | Valor                                           |
|-----------|-------------------------------------------------|
| Tipo      | Time series                                     |
| Query     | `rate(http_server_requests_seconds_count{application="api-tickets"}[1m])` |
| Título    | Requests por Segundo                            |
| Legend     | `{{method}} {{uri}}`                            |

#### Painel 3: Total de Requisições por Método

| Campo     | Valor                                           |
|-----------|-------------------------------------------------|
| Tipo      | Bar gauge                                       |
| Query (1) | `sum(http_server_requests_seconds_count{application="api-tickets", method="GET"})`    |
| Query (2) | `sum(http_server_requests_seconds_count{application="api-tickets", method="POST"})`   |
| Query (3) | `sum(http_server_requests_seconds_count{application="api-tickets", method="PUT"})`    |
| Query (4) | `sum(http_server_requests_seconds_count{application="api-tickets", method="DELETE"})` |
| Título    | Total de Requisições por Método HTTP            |

#### Painel 4: Latência P95

| Campo     | Valor                                           |
|-----------|-------------------------------------------------|
| Tipo      | Time series                                     |
| Query     | `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{application="api-tickets"}[1m]))` |
| Título    | Latência P95 (segundos)                         |
| Unit      | seconds (s)                                     |

#### Painel 5: Taxa de Erros (5xx)

| Campo     | Valor                                           |
|-----------|-------------------------------------------------|
| Tipo      | Stat                                            |
| Query     | `sum(rate(http_server_requests_seconds_count{application="api-tickets", status=~"5.."}[1m])) / sum(rate(http_server_requests_seconds_count{application="api-tickets"}[1m])) * 100` |
| Título    | Taxa de Erro HTTP (%)                           |
| Unit      | Percent (percent)                               |
| Thresholds| 0-2 = Green, 2-5 = Yellow, 5+ = Red             |

#### Painel 6: Uso de Memória JVM

| Campo     | Valor                                           |
|-----------|-------------------------------------------------|
| Tipo      | Time series                                     |
| Query     | `jvm_memory_used_bytes{application="api-tickets", area="heap"} / 1024 / 1024` |
| Título    | Memória JVM Usada (MB)                          |
| Unit      | Megabytes (MB)                                  |

#### Painel 7: Uso de CPU

| Campo     | Valor                                           |
|-----------|-------------------------------------------------|
| Tipo      | Time series                                     |
| Query     | `rate(process_cpu_usage{application="api-tickets"}[1m]) * 100` |
| Título    | Uso de CPU (%)                                  |
| Unit      | Percent (percent)                               |

#### Painel 8: Threads Ativas

| Campo     | Valor                                           |
|-----------|-------------------------------------------------|
| Tipo      | Stat                                            |
| Query     | `jvm_threads_live_threads{application="api-tickets"}` |
| Título    | Threads Ativas                                  |

### Variáveis de Dashboard (Opcional)

Para tornar o dashboard interativo, adicione variáveis:

1. Vá em **Dashboard Settings > Variables > Add variable**
2. Configure:

| Campo        | Valor                        |
|--------------|------------------------------|
| Name         | `application`                |
| Label        | Aplicação                    |
| Type         | Query                        |
| Query        | `label_values(up, application)` |
| Refresh      | On dashboard load            |

3. Use `$application` nas queries: `up{application="$application"}`

### Fluxo de Demonstração na DEMO

**Passo 1:** Inicie a stack de monitoramento

```bash
docker-compose up -d
```

**Passo 2:** Inicie a aplicação Spring Boot

```bash
mvnw.cmd spring-boot:run
```

**Passo 3:** Verifique o Prometheus

```
http://localhost:9090/targets
```
> Confirme que `api-tickets-jobs` está **UP** com last scrape recente.

**Passo 4:** Execute operações na API para gerar tráfego

```bash
# Criar 3 usuários
curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{"name":"João","email":"joao@email.com"}'
curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{"name":"Maria","email":"maria@email.com"}'
curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{"name":"Pedro","email":"pedro@email.com"}'

# Criar 3 pedidos
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"item":"Notebook","quantity":1,"value":4500.00,"user":{"idUser":1}}'
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"item":"Mouse","quantity":2,"value":89.90,"user":{"idUser":1}}'
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"item":"Teclado","quantity":1,"value":199.90,"user":{"idUser":2}}'

# Listar usuários
curl http://localhost:8080/users

# Listar pedidos
curl http://localhost:8080/orders

# Detalhe do usuário
curl http://localhost:8080/users/detalhe-usuario/1

# Detalhe do pedido
curl http://localhost:8080/orders/detalhe/1

# Forçar erro 404
curl -s http://localhost:8080/users/99999
```

**Passo 5:** Acesse o Grafana

```
http://localhost:3000
Login: admin / admin
```

> Os painéis devem mostrar dados em tempo real das requisições feitas.

**Passo 6:** Verifique os alertas

```
http://localhost:9090/alerts
```

> O alerta `ApplicationStarted` deve estar **active** (verde).

**Passo 7:** Verifique o Alertmanager

```
http://localhost:9093
```

> Se o email estiver configurado, uma notificação foi enviada quando a aplicação subiu.

---

## Resumo de URLs para a DEMO

| Ferramenta    | URL                               | Credenciais     |
|---------------|-----------------------------------|-----------------|
| API           | http://localhost:8080             | Sem autenticação|
| Prometheus    | http://localhost:9090             | Sem autenticação|
| Grafana       | http://localhost:3000             | admin / admin   |
| Alertmanager  | http://localhost:9093             | Sem autenticação|



http://prometheus:9090

---

## Comandos Rápidos

```bash
# Subir stack de monitoramento
docker-compose up -d

# Ver status dos containers
docker-compose ps

# Parar stack
docker-compose down

# Ver logs do Prometheus
docker-compose logs -f prometheus

# Ver logs do Grafana
docker-compose logs -f grafana

# Ver logs do Alertmanager
docker-compose logs -f alertmanager

# Compilar e rodar a aplicação
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```
