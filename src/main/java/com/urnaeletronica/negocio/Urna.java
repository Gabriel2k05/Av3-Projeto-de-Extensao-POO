package com.urnaeletronica.negocio;

import com.urnaeletronica.dados.FirebaseService;
import com.urnaeletronica.modelo.Candidato;
import com.urnaeletronica.modelo.Cargo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Lógica da urna. Métodos synchronized: console e interface usam o mesmo objeto. */
public class Urna {
    public static final long BRANCO = -1;
    public static final long NULO = -2;

    private final FirebaseService firebase;
    private final List<Candidato> candidatos;
    private final long[] escolhas = new long[Cargo.values().length];

    private int etapa = 0;           // índice do cargo atual
    private String digitos = "";
    private boolean branco = false;
    private boolean fim = false;     // mostrando a tela "FIM"
    private boolean encerrada = false;
    private String resultado = "";
    private Runnable observador;

    public Urna(FirebaseService firebase) throws Exception {
        this.firebase = firebase;
        this.candidatos = firebase.buscarCandidatos();
        if (candidatos.isEmpty()) {
            throw new IllegalStateException("Nenhum candidato válido encontrado no Firestore.");
        }
    }

    public void setObservador(Runnable observador) {
        this.observador = observador;
    }

    private void notificar() {
        if (observador != null) observador.run();
    }

    // ---------- ações da urna ----------

    public synchronized void digitar(char c) {
        if (encerrada || fim || branco) return;
        if (c < '0' || c > '9') return;
        if (digitos.length() < 2) {
            digitos += c;
            notificar();
        }
    }

    public synchronized void corrigir() {
        if (encerrada || fim) return;
        digitos = "";
        branco = false;
        notificar();
    }

    public synchronized void votarBranco() {
        if (encerrada || fim) return;
        if (digitos.isEmpty()) {          // como na urna real: só vale sem número digitado
            branco = true;
            notificar();
        }
    }

    public synchronized void confirmar() {
        if (encerrada || fim) return;

        long valor;
        if (branco) {
            valor = BRANCO;
        } else if (digitos.length() == 2) {
            Candidato c = getCandidatoAtual();
            valor = (c != null) ? c.getNumero() : NULO;
        } else {
            return; // número incompleto: nada a confirmar
        }

        escolhas[etapa] = valor;
        etapa++;
        digitos = "";
        branco = false;

        if (etapa == Cargo.values().length) {
            try {
                firebase.salvarVoto(escolhas);
            } catch (Exception e) {
                System.err.println("Erro ao salvar voto: " + e.getMessage());
            }
            fim = true;
            notificar();
            reiniciarDepoisDeUmTempo();
        } else {
            notificar();
        }
    }

    /** Mostra "FIM" por 2,5 s e libera a urna para o próximo eleitor. */
    private void reiniciarDepoisDeUmTempo() {
        new Thread(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException ignorada) { }
            synchronized (Urna.this) {
                if (encerrada) return;
                etapa = 0;
                fim = false;
                Arrays.fill(escolhas, 0);
            }
            notificar();
        }).start();
    }

    // ---------- apuração ----------

    public synchronized void apurar() {
        if (encerrada) return;
        encerrada = true;
        StringBuilder sb = new StringBuilder("===== RESULTADO DA ELEIÇÃO =====\n\n");
        try {
            for (Cargo cargo : Cargo.values()) {
                sb.append(apurarCargo(cargo)).append("\n");
            }
        } catch (Exception e) {
            sb.append("Erro ao consultar o banco de dados: ").append(e.getMessage());
        }
        resultado = sb.toString();
        notificar();
    }

    private String apurarCargo(Cargo cargo) throws Exception {
        List<Candidato> lista = new ArrayList<>();
        for (Candidato c : candidatos) {
            if (c.getCargo() == cargo) {
                c.zerarVotos();
                lista.add(c);
            }
        }

        int brancos = 0, nulos = 0;
        for (long v : firebase.buscarVotos(cargo)) {
            if (v == BRANCO) {
                brancos++;
            } else if (v == NULO) {
                nulos++;
            } else {
                for (Candidato c : lista) {
                    if (c.getNumero() == v) c.adicionarVoto();
                }
            }
        }

        int validos = 0;
        for (Candidato c : lista) validos += c.getVotos();
        int total = validos + brancos + nulos;

        Collections.sort(lista); // mais votado primeiro
        Locale br = Locale.forLanguageTag("pt-BR");
        StringBuilder sb = new StringBuilder("--- " + cargo.getNome().toUpperCase() + " ---\n");
        for (Candidato c : lista) {
            sb.append(String.format(br, "%s: %d voto(s) - %.2f%%%n",
                    c.getDescricao(), c.getVotos(), porcentagem(c.getVotos(), total)));
        }
        sb.append(String.format(br, "Brancos: %d - %.2f%%%n", brancos, porcentagem(brancos, total)));
        sb.append(String.format(br, "Nulos: %d - %.2f%%%n", nulos, porcentagem(nulos, total)));
        sb.append("Total de votos: ").append(total).append("\n");

        if (validos == 0) {
            sb.append("VENCEDOR: nenhum (sem votos válidos)\n");
        } else if (lista.size() > 1 && lista.get(0).getVotos() == lista.get(1).getVotos()) {
            sb.append("RESULTADO: EMPATE no primeiro lugar\n");
        } else {
            sb.append("VENCEDOR: ").append(lista.get(0).getDescricao()).append("\n");
        }
        return sb.toString();
    }

    private double porcentagem(int parte, int total) {
        return total == 0 ? 0 : parte * 100.0 / total;
    }

    // ---------- consultas para a interface ----------

    public synchronized Cargo getCargoAtual() {
        return etapa < Cargo.values().length ? Cargo.values()[etapa] : null;
    }

    public synchronized String getDigitos() { return digitos; }
    public synchronized boolean isBranco() { return branco; }
    public synchronized boolean isFim() { return fim; }
    public synchronized boolean isEncerrada() { return encerrada; }
    public synchronized String getResultado() { return resultado; }

    /** Candidato do número digitado no cargo atual (null se incompleto ou inexistente). */
    public synchronized Candidato getCandidatoAtual() {
        if (digitos.length() != 2) return null;
        int numero = Integer.parseInt(digitos);
        for (Candidato c : candidatos) {
            if (c.getCargo() == getCargoAtual() && c.getNumero() == numero) return c;
        }
        return null;
    }
}