# Prometheus - 10 Queries de Exemplo

Queries para demonstrar no projeto Tickets API. Acesse: `http://localhost:9090`

---

## 1. Status da API

```promql
up{application="api-tickets"}
```

> Retorna `1` se a API estiver respondendo, `0` se estiver down.

---

## 2. Total de Requisições por Endpoint

```promql
sum by (uri, method) (http_server_requests_seconds_count{application="api-tickets"})
```

> Quantidade total de requisições agrupadas por endpoint e método HTTP.

---

## 3. Taxa de Requisições por Segundo

```promql
rate(http_server_requests_seconds_count{application="api-tickets"}[1m])
```

> Quantas requisições por segundo a API está recebendo (último minuto).

---

## 4. Latência P95 por Endpoint

```promql
histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket{application="api-tickets"}[1m])))
```

> 95% das requisições respondem abaixo deste tempo (em segundos) por endpoint.

---

## 5. Taxa de Erros HTTP (5xx)

```promql
sum(rate(http_server_requests_seconds_count{application="api-tickets", status="500"}[1m]))
```

> Percentual de requisições que retornaram erro 5xx.

---

## 6. Requisições com Status 404

```promql
sum(rate(http_server_requests_seconds_count{application="api-tickets", status="404"}[1m]))
```

> Taxa de requisições com endpoint não encontrado.

---

## 7. Uso de Memória JVM (Heap)

```promql
jvm_memory_used_bytes{application="api-tickets", area="heap"} / 1024 / 1024
```

> Memória heap usada pela JVM em MB.

---

## 8. Uso de CPU

```promql
rate(process_cpu_usage{application="api-tickets"}[1m]) * 100
```

> Percentual de uso da CPU pelo processo da aplicação.

---

## 9. Threads Ativas

```promql
jvm_threads_live_threads{application="api-tickets"}
```

> Número total de threads ativas na JVM.

---

## 10. Top 5 Endpoints com Mais Tráfego

```promql
topk(5, sum by (uri) (rate(http_server_requests_seconds_count{application="api-tickets"}[5m])))
```

> 5 endpoints com maior taxa de requisições nos últimos 5 minutos.
