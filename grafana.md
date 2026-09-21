# Grafana - Configurar Prometheus como Data Source

---

## 1. Acessar o Grafana

```
http://localhost:3000
```

| Campo | Valor |
|-------|-------|
| Usuário | `admin` |
| Senha | `admin` |

> Na primeira vez, o Grafana pede para alterar a senha padrão. Crie uma nova ou ignore para manter `admin`.

---

## 2. Adicionar Data Source

1. Clique no menu lateral: **Connections > Data sources**
2. Clique em **Add data source**
3. Selecione **Prometheus**
4. Configure:

| Campo | Valor |
|-------|-------|
| Name | `Prometheus` |
| URL | `http://prometheus:9090` |
| Access | `Server` (proxy) |
| Scrape interval | `5s` |

> Use `http://prometheus:9090` (nome do container na rede Docker), não `localhost:9090`.

5. Clique em **Save & Test**
6. Mensagem "Data source is working" confirma que está OK

---

## 3. Testar

Acesse **Explore** no menu lateral e selecione o data source **Prometheus**. Execute:

```promql
up{application="api-tickets"}
```

> Retorna `1` se a API estiver respondendo.
