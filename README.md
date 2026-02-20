# ⚙️ Fastfood — Worker de Processamento de Vídeos

[![Release - Build, Quality Gate and Deploy](https://github.com/FIAP-SOAT-G129/hackathon-video-processing-worker-fase5/actions/workflows/release.yml/badge.svg)](https://github.com/FIAP-SOAT-G129/hackathon-video-processing-worker-fase5/actions/workflows/release.yml)

![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

Este repositório implementa o **Worker de Processamento de Vídeos** da aplicação **Fastfood**, desenvolvido em **Java 21 com Spring Boot 3 e Spring AMQP**. Ele é responsável por consumir mensagens de uma fila RabbitMQ, processar vídeos (extrair frames usando FFmpeg) e gerar arquivos ZIP com os frames, além de notificar o status do processamento.

---

## 🧾 Objetivo do Projeto

Consumir requisições de processamento de vídeo de uma fila RabbitMQ, realizar a extração de frames de vídeos utilizando a ferramenta FFmpeg, compactar os frames em um arquivo ZIP e, por fim, publicar o resultado do processamento em outra fila RabbitMQ para que o Microserviço de Vídeos possa atualizar o status e disponibilizar o resultado.

> 📚 **Wiki do Projeto:** <br/> > https://github.com/FIAP-SOAT-G129/.github/wiki/Fase-5

---

## 🚀 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3**
- **Spring AMQP** (Mensageria assíncrona com RabbitMQ)
- **RabbitMQ** (Mensageria assíncrona)
- **FFmpeg** (Processamento de vídeo e extração de frames)
- **Maven** (Gerenciamento de dependências)
- **Docker & Docker Compose** (Containerização e orquestração)

---

## 🧩 Domínios Gerenciados

| Entidade      | Descrição                                                                          |
|:--------------|:-----------------------------------------------------------------------------------|
| **Video**     | Metadados do vídeo, incluindo status de processamento e caminhos de armazenamento. |
| **VideoFile** | Registro dos arquivos físicos associados (Original e ZIP resultante).              |

---

## 🧠 Arquitetura

> 🚧 Em construção - Arquitetura Hexagonal (Ports and Adapters)

---

## ⚙️ Como Rodar o Projeto

### ✅ Pré-requisitos
- `Java 21` (opcional, para rodar fora do container)
- `Maven` (opcional, para rodar fora do container)
- `Docker` (para rodar em container)
- `Docker Compose` (para orquestrar containers)

### 🔧 Configuração

A aplicação já vem configurada com valores padrão no `application.yml` para funcionar com o Docker Compose. Caso deseje alterar, as principais variáveis de ambiente são:

```env
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

LOCAL_STORAGE_PATH=/tmp/zips
```

### 🐳 Executando o projeto com Docker Compose

No terminal, navegue até a raiz do projeto e execute:

```bash
docker-compose up --build
```

Os serviços de infraestrutura estarão acessíveis em:
- **RabbitMQ Management:** `localhost:15672` (guest/guest)

#### ⏹️ Parando os containers

Para parar e remover os containers, execute:

```bash
docker-compose down
```

---

## 🧪 Testes e Qualidade de Código

O projeto adota boas práticas de testes e qualidade de código, com foco em cobertura e comportamento previsível. Inclui testes de unidade utilizando:

- **JUnit 5**
- **Mockito**

### ▶️ Executando os testes

```bash
# Executar todos os testes
mvn test

# Executar testes com relatório de cobertura
mvn clean verify
```

---

## 👥 Equipe

Desenvolvido pela equipe **FIAP SOAT - G129** como parte do projeto de Arquitetura de Software.

---

## 📄 Licença

Este projeto é parte de um trabalho acadêmico da FIAP.

