
    create table administrador (
        data_nascimento date,
        cpf varchar(255),
        email varchar(255),
        genero varchar(255),
        id varchar(255) not null,
        nome varchar(255),
        senha varchar(255),
        telefone varchar(255),
        primary key (id)
    );

    create table beneficiario (
        data_nascimento date,
        cpf varchar(255),
        email varchar(255),
        genero varchar(255),
        id varchar(255) not null,
        nome varchar(255),
        senha varchar(255),
        telefone varchar(255),
        primary key (id)
    );

    create table comerciante (
        data_nascimento date,
        cpf varchar(255),
        email varchar(255),
        genero varchar(255),
        id varchar(255) not null,
        nome varchar(255),
        senha varchar(255),
        telefone varchar(255),
        primary key (id)
    );

    create table entregador (
        data_nascimento date,
        cpf varchar(255),
        email varchar(255),
        genero varchar(255),
        id varchar(255) not null,
        nome varchar(255),
        senha varchar(255),
        telefone varchar(255),
        primary key (id)
    );

    create table estabelecimento (
        id varchar(255) not null,
        nome varchar(255),
        primary key (id)
    );

    create table verification_token (
        expiry_date timestamp(6) not null,
        id bigserial not null,
        token varchar(255) not null unique,
        user_email varchar(255) not null,
        primary key (id)
    );
