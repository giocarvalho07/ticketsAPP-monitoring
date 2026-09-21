# Alertmanager - Guia Básico

---

## Como Funciona

```
[Prometheus] --alertas--> [Alertmanager] --notificação--> [Email]
```

1. O Prometheus avalia as **regras** em `config/rules/alerts.yml`
2. Quando uma regra dispara, envia o alerta para o **Alertmanager**
3. O Alertmanager roteia a notificação (email, no nosso caso)

---

## Arquivos de Configuração

| Arquivo | Função |
|---------|--------|
| `config/alertmanager.yml` | Configura o Alertmanager (receptor, SMTP, rotas) |
| `config/rules/alerts.yml` | Define as regras de alerta (expressões Prometheus) |

---

## Exemplo 1: API Down (Crítico)

Adicione em `config/rules/alerts.yml`:

```yaml
  - alert: APIDown
    expr: up{application="api-tickets"} == 0
    for: 10s
    labels:
      severity: critical
    annotations:
      summary: "API Tickets está DOWN"
      description: "A aplicação Spring Boot não está respondendo."
```

### O que faz

- **expr:** `up == 0` significa que o Prometheus não consegue acessar a app
- **for: 10s:** espera 10 segundos antes de disparar (evita falsos positivos)
- **severity: critical:** nível do alerta

### Como testar

1. Pare a aplicação Spring Boot
2. Aguarde 10 segundos
3. Acesse `http://localhost:9090/alerts` → alerta aparece como **firing**
4. Acesse `http://localhost:9093/#/alerts` → alerta aparece no Alertmanager
5. Email é enviado

---

## Exemplo 2: Taxa de Erros Alta (Warning)

Adicione em `config/rules/alerts.yml`:

```yaml
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
```

### O que faz

- **expr:** calcula o percentual de erros 5xx nos últimos 1 minuto
- **> 5:** dispara se mais de 5% das requisições derem erro
- **for: 30s:** aguarda 30 segundos para confirmar

### Como testar

1. Execute várias requisições com erro:
   ```bash
   for /L %i in (1,1,20) do curl -s http://localhost:8080/users/99999
   ```
2. Aguarde 30 segundos
3. Acesse `http://localhost:9090/alerts` → alerta aparece

---

## Reload das Regras

Após editar `config/rules/alerts.yml`, recarregue o Prometheus:

```bash
# Opção 1: Reiniciar o container
docker-compose restart prometheus

# Opção 2: Enviar sinal HUP (sem reiniciar)
curl -X POST http://localhost:9090/-/reload
```

---

## Ver Alertas

| URL | O que mostra |
|-----|--------------|
| `http://localhost:9090/alerts` | Regras e status no Prometheus |
| `http://localhost:9093/#/alerts` | Alertas recebidos pelo Alertmanager |
| `http://localhost:9093/#/silences` | Silenciamentos ativos |

---

## Silenciar Alertas (Útil para DEMO)

1. Acesse `http://localhost:9093/#/silences`
2. Clique em **Add Silence**
3. Configure um Match (ex: `alertname=ApplicationStarted`)
4. Defina a duração (ex: 2 horas)
5. Clique em **Create**

> Isso impede que o email seja enviado durante a demonstração.
