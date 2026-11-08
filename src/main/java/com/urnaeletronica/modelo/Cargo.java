package com.urnaeletronica.modelo;

/** Cargos na ordem de votação, com o campo correspondente no Firestore. */
public enum Cargo {
    GOVERNADOR("Governador", "voto_proporcional"),
    PRESIDENTE("Presidente", "voto_majoritario");

    private final String nome;
    private final String campoVoto;

    Cargo(String nome, String campoVoto) {
        this.nome = nome;
        this.campoVoto = campoVoto;
    }

    public String getNome() {
        return nome;
    }

    public String getCampoVoto() {
        return campoVoto;
    }

    /** Converte o texto do banco em Cargo (ou null se não reconhecer). */
    public static Cargo deTexto(String texto) {
        if (texto == null) return null;
        for (Cargo c : values()) {
            if (c.nome.equalsIgnoreCase(texto.trim())) return c;
        }
        return null;
    }
}