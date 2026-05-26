# FoodGuard

FoodGuard é um aplicativo mobile Android desenvolvido em Kotlin com o objetivo de auxiliar usuários no gerenciamento de alimentos, ajudando a reduzir desperdícios por meio do controle de validade, notificações e dicas de conservação.

O aplicativo permite cadastrar alimentos, acompanhar datas de vencimento, receber alertas sobre produtos próximos da validade e visualizar estatísticas relacionadas ao consumo e desperdício.

---

# Funcionalidades

## Gerenciamento de Alimentos

- Cadastro de alimentos com:
  - Nome
  - Data de validade
  - Data de compra
  - Quantidade
  - Categoria
  - Preço
  - Local de armazenamento
  - Data de lembrete
  - Imagem do alimento

## Controle de Validade

- Visualização de alimentos:
  - Próximos do vencimento
  - Já vencidos
- Sistema de lembretes
- Organização por abas de notificações

## Estatísticas e Insights

- Taxa de eficiência de consumo
- Estimativa de economia mensal
- Estatísticas de desperdício
- Comparação entre consumo e desperdício

## Sistema de Dicas de Conservação

O aplicativo possui um sistema interno (`ConservationAI`) que fornece recomendações de conservação baseadas no tipo de alimento cadastrado.

### Exemplos de recomendações:
- Conservação de bananas
- Armazenamento de pão
- Refrigeração de leite
- Conservação de vegetais

## Perfil do Usuário

- Cadastro e login
- Foto de perfil
- Gerenciamento de informações do usuário

---

# Tecnologias Utilizadas

- Kotlin
- Android SDK
- Room Database
- Arquitetura MVVM
- RecyclerView
- Fragments
- Material Design Components
- Coroutines
- Flow

---

# Estrutura do Projeto

```bash
app/
├── data/                # Banco de dados, DAO, Repository e Models
├── adapter/             # Adaptadores RecyclerView
├── viewmodel/           # ViewModels
├── res/                 # Layouts, drawables e recursos
├── ui fragments/        # Telas e navegação
```

---

# Componentes Principais

## Banco de Dados

O projeto utiliza Room Database para persistência local de dados.

### Entidades Principais

- `FoodItem`
- `User`

### DAOs

- `FoodDao`
- `UserDao`

---

# Telas do Aplicativo

- Login
- Cadastro
- Tela Inicial
- Notificações
- Dashboard de Insights
- Perfil
- Detalhes do Alimento

---

# Arquitetura

O projeto segue o padrão de arquitetura MVVM (Model-View-ViewModel).

## Model

Responsável por:
- Banco de dados
- Repositórios
- Entidades

## ViewModel

Responsável por:
- Gerenciamento de estado da interface
- Regras de negócio
- Observação de dados

## View

Responsável por:
- Activities
- Fragments
- Interação com o usuário

---

# Como Executar o Projeto

## Requisitos

- Android Studio
- Android SDK 33+
- Gradle

## Passos para execução

### 1. Clone o repositório

```bash
git clone https://github.com/Fernando-1909/FoodGuard.git
```

### 2. Acesse a pasta do projeto

```bash
cd FoodGuard
```

### 3. Abra o projeto no Android Studio

- Clique em:
  - `Open`
- Selecione a pasta do projeto

### 4. Aguarde a sincronização do Gradle

O Android Studio irá instalar automaticamente as dependências necessárias.

### 5. Execute o aplicativo

Você pode executar em:
- Emulador Android
- Dispositivo físico Android

---

# Melhorias Futuras

- Sincronização em nuvem
- Notificações push
- Recomendações inteligentes com IA
- Leitura de código de barras
- OCR para leitura automática de validade
- Melhorias no modo escuro
- Suporte para múltiplos usuários

---

# Autores

## Fernando Macedo da Costa
Desenvolvedor do projeto.

## José Inácio Mendes Ferreira
Desenvolvedor do projeto.

---

# Licença

Projeto desenvolvido para fins acadêmicos e educacionais.

---

# Repositório

https://github.com/Fernando-1909/FoodGuard
