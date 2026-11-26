package com.urnaeletronica.modelo;

/** Classe abstrata: base para qualquer pessoa do sistema. */
public abstract class Pessoa {
    private final String nome;

    protected Pessoa(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    /** Cada subclasse descreve a si mesma (polimorfismo). */
    public abstract String getDescricao();

    @Override
    public String toString() {
        return getDescricao();
    }
}