# Monitoramento da Aplicação Tickets (ticketsAPP)

Este repositório contém as configurações e scripts para implementar o monitoramento da aplicação `ticketsAPP` utilizando Prometheus e Alertmanager. O objetivo é garantir a saúde, performance e disponibilidade da aplicação, além de fornecer alertas em caso de problemas.

## Arquitetura de Monitoramento

A arquitetura de monitoramento implementada utiliza as seguintes ferramentas:

* **Prometheus:** Responsável por coletar e armazenar métricas da aplicação `ticketsAPP`. As métricas são expostas pela aplicação através do Spring Boot Actuator.
* **Alertmanager:** Gerencia e roteia os alertas disparados pelo Prometheus, enviando notificações por e-mail.

## Pré-requisitos

Antes de começar, você precisará ter as seguintes ferramentas instaladas e configuradas:

* **Docker:** Para executar o Prometheus e Alertmanager em containers.
* **Docker Compose:** Para orquestrar os containers do Prometheus e Alertmanager.
* **Acesso a um servidor SMTP (Gmail utilizado como exemplo):** Para configurar as notificações do Alertmanager.
* **Uma aplicação Spring Boot (`ticketsAPP`) com o Spring Boot Actuator configurado para expor métricas no endpoint `/actuator/prometheus`.**

## Configuração

### 1. Arquivos de Configuração

Este repositório contém os seguintes arquivos de configuração:

* **`prometheus/prometheus.yml`:** Configuração do Prometheus para fazer scraping das métricas da aplicação `ticketsAPP` e definir regras de alerta.
* **`prometheus/rules/alerts.yml`:** Definição das regras de alerta do Prometheus, incluindo alertas de inicialização da aplicação e alta taxa de erros 4xx.
* **`alertmanager/alertmanager.yml`:** Configuração do Alertmanager para receber alertas do Prometheus e enviar notificações por e-mail.

### 2. Configuração do Prometheus (`prometheus/prometheus.yml`)

O arquivo de configuração do Prometheus está configurado para:

* Fazer scraping das métricas da aplicação `ticketsAPP` no endpoint `/actuator/prometheus` com um intervalo de 5 segundos.
* Carregar as regras de alerta definidas no arquivo `prometheus/rules/alerts.yml`.

```yaml
scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['localhost:8080'] # Ajuste para o endereço da sua aplicação

rule_files:
  - 'rules/*.yml' 
```



Observação: Ajuste o targets para o endereço e porta onde sua aplicação ticketsAPP está rodando.

3. Regras de Alerta do Prometheus (prometheus/rules/alerts.yml)
Este arquivo define as seguintes regras de alerta:

ApplicationStarted: Dispara um alerta de informação quando a aplicação api-tickets (ajuste o application conforme sua configuração) é iniciada.
High4xxErrorRate: Dispara um alerta de aviso se a taxa de erros HTTP 4xx (erros do cliente) na aplicação exceder 5% nos últimos 5 minutos.


```yaml
groups:
  - name: application_alerts
    rules:
      - alert: ApplicationStarted
        expr: up{application="api-tickets"} == 1
        for: 0s
        labels:
          severity: info
        annotations:
          summary: "Aplicação Iniciada"
          description: "A aplicação Spring Boot (job {{ $labels.job }}, instance {{ $labels.instance }}) foi iniciada."

      - alert: High4xxErrorRate
        expr: increase(http_server_requests_seconds_count{application="api-tickets", status=~"4.."}[5m]) / increase(http_server_requests_seconds_count{application="api-tickets"}[5m]) > 0.05
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "Alta Taxa de Erros 4xx"
          description: "A taxa de erros HTTP 4xx (erros do cliente) na aplicação está acima de 5%."
```
Observação: Ajuste o seletor up{application="api-tickets"} para corresponder aos labels expostos pela sua aplicação.

4. Configuração do Alertmanager (alertmanager/alertmanager.yml)
O arquivo de configuração do Alertmanager está configurado para:

Receber todos os alertas e roteá-los para o receiver email-on-start.
Enviar notificações por e-mail através do servidor SMTP do Gmail.


```yaml
global:
  resolve_timeout: 5m

route:
  receiver: 'email-on-start'

receivers:
  - name: 'email-on-start'
    email_configs:
      - to: 'seu_email@example.com' # Substitua pelo seu e-mail
        from: 'alertmanager@seu_dominio.com' # Ajuste o remetente
        smarthost: 'smtp.gmail.com:587' # Use o servidor SMTP do seu provedor
        auth_username: 'seu_email@example.com' # Seu nome de usuário do SMTP
        auth_password: 'sua_senha_ou_senha_de_aplicativo' # Sua senha do SMTP (ou senha de aplicativo para Gmail com 2FA)
        tls_config:
          insecure_skip_verify: false
```

Importante:

Substitua 'seu_email@example.com', 'alertmanager@seu_dominio.com', 'smtp.gmail.com:587', 'seu_email@example.com' e 'sua_senha_ou_senha_de_aplicativo' pelas suas informações reais.
Se você estiver usando o Gmail com a autenticação de dois fatores ativada, você precisará gerar uma senha de aplicativo e usá-la no lugar da sua senha normal.
Como Executar
Para iniciar o Prometheus e o Alertmanager utilizando Docker Compose:

Clone este repositório.

Certifique-se de ter o Docker e o Docker Compose instalados.

Navegue até o diretório raiz do repositório no seu terminal.

Execute o seguinte comando:

Bash

docker-compose up -d
Isso irá iniciar os containers do Prometheus e Alertmanager em segundo plano.

Acessando as Interfaces Web
* `prometheus`:* Acesse `http://localhost:9090` no seu navegador.
* `Alertmanager: `:* Acesse `http://localhost:9093` no seu navegador.
* `Actuator: `:* Acesse `http://localhost:8080/actuator` no seu navegador.
* `Grafana: `:* Acesse `http://localhost:3000` no seu navegador.
