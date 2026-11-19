# Daniel-Yago-Ativ3
# Sistema Desktop de Gestão Veterinária

Projeto acadêmico (3ª Avaliação de LP). Java + Swing + MySQL (JDBC).

## Estrutura

```
GestaoVeterinaria/
├── Script.sql                    # cria banco, tabelas e dados de teste
├── config.properties.example     # modelo de credenciais (copiar para config.properties)
├── .gitignore
├── lib/                          # JARs (mysql-connector-j, futuramente FlatLaf)
├── relatorios/                   # destino dos relatórios .txt
├── docs/                         # diagramas e manual (Etapa 8)
└── src/
    ├── factory/                  # ConnectionFactory
    ├── modelo/                   # Cliente, Raca, Animal
    ├── dao/                      # ClienteDAO, RacaDAO, AnimalDAO
    ├── gui/                      # telas Swing
    ├── util/                     # ValidadorCPF, CalculadoraIdade, GeradorRelatorioTXT
    └── principal/                # Main
```

## Requisitos

- JDK 17 ou superior
- MySQL 8.0.16 ou superior (o script usa CHECK constraints)
- Driver JDBC: MySQL Connector/J (baixar o JAR em https://dev.mysql.com/downloads/connector/j/ e colocar em `lib/`)

## Como criar o banco

```
mysql -u SEU_USUARIO -p < Script.sql
```

Ou abra o `Script.sql` no MySQL Workbench e execute tudo. **O script apaga e recria o banco `gestao_veterinaria`.**

## Credenciais

Copie `config.properties.example` para `config.properties` e informe usuário e senha. Esse arquivo está no `.gitignore`.

## Status do desenvolvimento

- [x] Etapa 1 — Estrutura inicial e Script.sql
- [ ] Etapa 2 — ConnectionFactory e modelo
- [ ] Etapa 3 — Utilitários
- [ ] Etapa 4 — DAOs
- [ ] Etapa 5 — Testes do backend
- [ ] Etapa 6 — Interface Swing
- [ ] Etapa 7 — Integração e testes
- [ ] Etapa 8 — Diagramas e manual