/* CearaSemFome-3 */

CREATE TABLE IF NOT EXISTS endereco (
    id VARCHAR(255) PRIMARY KEY,
    cep VARCHAR(255),
    logradouro VARCHAR(255),
    numero VARCHAR(255),
    bairro VARCHAR(255),
    municipio VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8)
    );

CREATE TABLE IF NOT EXISTS carrinho (
    id VARCHAR(255) PRIMARY KEY,
    status VARCHAR(255),
    criacao DATE,
    modificacao DATE
    );

CREATE TABLE IF NOT EXISTS administrador (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255),
    cpf VARCHAR(20) UNIQUE,
    email VARCHAR(255),
    senha VARCHAR(255),
    data_nascimento DATE,
    telefone VARCHAR(255),
    genero VARCHAR(255),
    status VARCHAR(255),
    lgpd_accepted BOOLEAN
    );

CREATE TABLE IF NOT EXISTS comerciante (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255),
    cpf VARCHAR(250) UNIQUE,
    email VARCHAR(255),
    senha VARCHAR(255),
    data_nascimento DATE,
    telefone VARCHAR(255),
    genero VARCHAR(255),
    status VARCHAR(255),
    lgpd_accepted BOOLEAN
    );

CREATE TABLE IF NOT EXISTS beneficiario (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255),
    cpf VARCHAR(20) UNIQUE,
    email VARCHAR(255),
    senha VARCHAR(255),
    data_nascimento DATE,
    telefone VARCHAR(255),
    genero VARCHAR(255),
    status VARCHAR(255),
    numero_cadastro_social VARCHAR(255),
    carrinho_id VARCHAR(255),
    endereco_id VARCHAR(255),
    lgpd_accepted BOOLEAN,
    FOREIGN KEY (endereco_id) REFERENCES endereco (id),
    FOREIGN KEY (carrinho_id) REFERENCES carrinho (id)
    );

CREATE TABLE IF NOT EXISTS entregador (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255),
    cpf VARCHAR(20) UNIQUE,
    email VARCHAR(255),
    senha VARCHAR(255),
    data_nascimento DATE,
    telefone VARCHAR(255),
    genero VARCHAR(255),
    status VARCHAR(255),
    endereco_id VARCHAR(255),
    lgpd_accepted BOOLEAN,
    FOREIGN KEY (endereco_id) REFERENCES endereco (id)
    );

CREATE TABLE IF NOT EXISTS estabelecimento (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255),
    endereco_id VARCHAR(255),
    comerciante_id VARCHAR(255),
    FOREIGN KEY (endereco_id) REFERENCES endereco (id),
    FOREIGN KEY (comerciante_id) REFERENCES comerciante (id)
    );

CREATE TABLE IF NOT EXISTS produto (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255),
    lote VARCHAR(255),
    descricao VARCHAR(255),
    preco NUMERIC,
    quantidade_estoque INTEGER,
    status VARCHAR(255),
    imagem VARCHAR(255),
    tipo_imagem VARCHAR(255),
    comerciante_id VARCHAR(255),
    FOREIGN KEY (comerciante_id) REFERENCES comerciante (id)
    );

CREATE TABLE IF NOT EXISTS conta (
    id VARCHAR(255) PRIMARY KEY,
    numero_conta VARCHAR(255),
    agencia VARCHAR(255),
    saldo NUMERIC,
    beneficiario_id VARCHAR(255),
    FOREIGN KEY (beneficiario_id) REFERENCES beneficiario (id)
    );

CREATE TABLE IF NOT EXISTS produto_carrinho (
    id VARCHAR(255) PRIMARY KEY,
    carrinho_id VARCHAR(255),
    produto_id VARCHAR(255),
    quantidade INTEGER,
    FOREIGN KEY (carrinho_id) REFERENCES carrinho (id),
    FOREIGN KEY (produto_id) REFERENCES produto (id)
    );

CREATE TABLE IF NOT EXISTS produto_estabelecimento (
    id VARCHAR(255) PRIMARY KEY,
    produto_id VARCHAR(255),
    estabelecimento_id VARCHAR(255),
    FOREIGN KEY (produto_id) REFERENCES produto (id),
    FOREIGN KEY (estabelecimento_id) REFERENCES estabelecimento (id)
    );

CREATE TABLE IF NOT EXISTS verification_token (
    token VARCHAR(255) PRIMARY KEY,
    user_email VARCHAR(255),
    expiry_date TIMESTAMP,
    nome VARCHAR(255),
    cpf VARCHAR(20),
    senha_criptografada VARCHAR(255),
    data_nascimento DATE,
    telefone VARCHAR(255),
    genero VARCHAR(255),
    tipo_pessoa VARCHAR(255),
    lgpd_accepted BOOLEAN
    );
