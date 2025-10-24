--isso aqui em cima é por causa da reformulação do banco
DROP TABLE IF EXISTS produto_estabelecimento CASCADE;
DROP TABLE IF EXISTS item_carrinho CASCADE;
DROP TABLE IF EXISTS produto CASCADE;
DROP TABLE IF EXISTS estabelecimento CASCADE;
DROP TABLE IF EXISTS conta CASCADE;
DROP TABLE IF EXISTS carrinho CASCADE;
DROP TABLE IF EXISTS beneficiario CASCADE;
DROP TABLE IF EXISTS administrador CASCADE;
DROP TABLE IF EXISTS comerciante CASCADE;
DROP TABLE IF EXISTS entregador CASCADE;
DROP TABLE IF EXISTS verification_token CASCADE;


-- ADMINISTRADOR

CREATE TABLE administrador (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_nascimento DATE NOT NULL,
    telefone VARCHAR(30),
    genero VARCHAR(20),
    status VARCHAR(50) NOT NULL
);


-- BENEFICIARIO

CREATE TABLE beneficiario (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_nascimento DATE NOT NULL,
    telefone VARCHAR(30),
    genero VARCHAR(20),
    status VARCHAR(50) NOT NULL,
    numero_cadastro_social VARCHAR(100),
    carrinho_id VARCHAR(255)
);


-- COMERCIANTE

CREATE TABLE comerciante (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_nascimento DATE NOT NULL,
    telefone VARCHAR(30),
    genero VARCHAR(20),
    status VARCHAR(50) NOT NULL
);


-- ENTREGADOR

CREATE TABLE entregador (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_nascimento DATE NOT NULL,
    telefone VARCHAR(30),
    genero VARCHAR(20),
    status VARCHAR(50) NOT NULL
);


-- CARRINHO

CREATE TABLE carrinho (
    id VARCHAR(255) PRIMARY KEY,
    beneficiario_id VARCHAR(255),
    status VARCHAR(50),
    criacao TIMESTAMP,
    modificacao TIMESTAMP,
    FOREIGN KEY (beneficiario_id) REFERENCES beneficiario(id)
);


-- CONTA

CREATE TABLE conta (
    id VARCHAR(255) PRIMARY KEY,
    numero_conta VARCHAR(50),
    agencia VARCHAR(20),
    saldo NUMERIC(15,2),
    beneficiario_id VARCHAR(255) UNIQUE NOT NULL,
    FOREIGN KEY (beneficiario_id) REFERENCES beneficiario(id)
);


-- ESTABELECIMENTO

CREATE TABLE estabelecimento (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    endereco VARCHAR(255),
    comerciante_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (comerciante_id) REFERENCES comerciante(id)
);


-- PRODUTO

CREATE TABLE produto (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco_base NUMERIC(10,2),
    criador_id VARCHAR(255),
    status VARCHAR(50),
    FOREIGN KEY (criador_id) REFERENCES comerciante(id)
);


-- PRODUTO_ESTABELECIMENTO (associativa)

CREATE TABLE produto_estabelecimento (
    produto_id VARCHAR(255) NOT NULL,
    estabelecimento_id VARCHAR(255) NOT NULL,
    preco_venda NUMERIC(10,2) NOT NULL,
    estoque INT CHECK (estoque >= 0),
    PRIMARY KEY (produto_id, estabelecimento_id),
    FOREIGN KEY (produto_id) REFERENCES produto(id),
    FOREIGN KEY (estabelecimento_id) REFERENCES estabelecimento(id)
);


-- ITEM_CARRINHO

CREATE TABLE item_carrinho (
    id VARCHAR(255) PRIMARY KEY,
    carrinho_id VARCHAR(255) NOT NULL,
    produto_id VARCHAR(255) NOT NULL,
    quantidade INT NOT NULL CHECK (quantidade >= 0),
    preco_unitario NUMERIC(10,2),
    FOREIGN KEY (carrinho_id) REFERENCES carrinho(id),
    FOREIGN KEY (produto_id) REFERENCES produto(id)
);


-- VERIFICATION_TOKEN

CREATE TABLE verification_token (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    expiry_date TIMESTAMP NOT NULL
);
