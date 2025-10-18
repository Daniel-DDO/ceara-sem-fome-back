create table administrador (
    id varchar(255) not null primary key,
    nome varchar(255),
    email varchar(255),
    senha varchar(255),
    cpf varchar(255),
    telefone varchar(255),
    genero varchar(255),
    data_nascimento date
);

create table beneficiario (
    id varchar(255) not null primary key,
    nome varchar(255),
    email varchar(255),
    senha varchar(255),
    cpf varchar(255),
    telefone varchar(255),
    genero varchar(255),
    data_nascimento date
);

create table comerciante (
    id varchar(255) not null primary key,
    nome varchar(255),
    email varchar(255),
    senha varchar(255),
    cpf varchar(255),
    telefone varchar(255),
    genero varchar(255),
    data_nascimento date
);

create table entregador (
    id varchar(255) not null primary key,
    nome varchar(255),
    email varchar(255),
    senha varchar(255),
    cpf varchar(255),
    telefone varchar(255),
    genero varchar(255),
    data_nascimento date
);

create table estabelecimento (
    id varchar(255) not null primary key,
    nome varchar(255),
    comerciante_id varchar(255) not null,
    constraint fk_estabelecimento_comerciante foreign key (comerciante_id)
        references comerciante(id)
);

create table verification_token (
    id bigserial not null primary key,
    token varchar(255) not null unique,
    user_email varchar(255) not null,
    tipo_pessoa varchar(255) not null check (tipo_pessoa in ('ADMINISTRADOR','BENEFICIARIO','COMERCIANTE','ENTREGADOR')),
    nome varchar(255) not null,
    cpf varchar(255),
    data_nascimento date,
    genero varchar(255),
    senha_criptografada varchar(255),
    telefone varchar(255),
    expiry_date timestamp(6) not null
);
