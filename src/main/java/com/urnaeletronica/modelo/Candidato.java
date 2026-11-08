package com.urnaeletronica.modelo;

public class Candidato extends Pessoa implements Comparable<Candidato> {
    private final int numero;
    private final String partido;
    private final Cargo cargo;
    private int votos;

    public Candidato(String nome, int numero, String partido, Cargo cargo) {
        super(nome);
        this.numero = numero;
        this.partido = partido;
        this.cargo = cargo;
    }

    public int getNumero() { return numero; }
    public String getPartido() { return partido; }
    public Cargo getCargo() { return cargo; }
    public int getVotos() { return votos; }

    public void adicionarVoto() { votos++; }
    public void zerarVotos() { votos = 0; }

    @Override
    public String getDescricao() {
        return getNome() + " (" + numero + ") - " + partido;
    }

    /** Ordem decrescente de votos. */
    @Override
    public int compareTo(Candidato outro) {
        return Integer.compare(outro.votos, this.votos);
    }
}