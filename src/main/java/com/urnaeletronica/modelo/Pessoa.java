package com.urnaeletronica.modelo;

public abstract class Pessoa {
    private final String nome;

    protected Pessoa(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public abstract String getDescricao();

    @Override
    public String toString() {
        return getDescricao();
    }
}