package com.urnaeletronica.visao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.urnaeletronica.modelo.Candidato;
import com.urnaeletronica.modelo.Cargo;
import com.urnaeletronica.negocio.Urna;

public class TelaUrna extends JFrame {
    private final Urna urna;
    private final JLabel lblTitulo = new JLabel();
    private final JLabel lblCargo = new JLabel();
    private final JLabel lblNumero = new JLabel();
    private final JLabel lblNome = new JLabel();
    private final JLabel lblPartido = new JLabel();
    private final JLabel lblAviso = new JLabel();
    private boolean mostrandoResultado = false;

    public TelaUrna(Urna urna) {
        super("Urna Cidadã");
        this.urna = urna;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        add(criarVisor(), BorderLayout.CENTER);
        add(criarTeclado(), BorderLayout.EAST);
        configurarAtalhoAsterisco();

        urna.setObservador(() -> SwingUtilities.invokeLater(this::atualizar));
        atualizar();

        setSize(800, 480);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel criarVisor() {
        JPanel visor = new JPanel(new GridLayout(6, 1, 0, 4));
        visor.setBackground(new Color(0xE8E8E8));
        visor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 0),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15))));

        lblTitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblCargo.setFont(new Font("SansSerif", Font.BOLD, 30));
        lblNumero.setFont(new Font("Monospaced", Font.BOLD, 30));
        lblNome.setFont(new Font("SansSerif", Font.PLAIN, 20));
        lblPartido.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblAviso.setFont(new Font("SansSerif", Font.ITALIC, 13));

        visor.add(lblTitulo);
        visor.add(lblCargo);
        visor.add(lblNumero);
        visor.add(lblNome);
        visor.add(lblPartido);
        visor.add(lblAviso);
        return visor;
    }

    private JPanel criarTeclado() {
        JPanel teclado = new JPanel(new BorderLayout(5, 10));
        teclado.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        teclado.setBackground(new Color(0x3A3A3A));

        JPanel numeros = new JPanel(new GridLayout(4, 3, 6, 6));
        numeros.setOpaque(false);
        numeros.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        for (int i = 1; i <= 9; i++) {
            numeros.add(botaoNumero(i));
        }
        numeros.add(new JLabel());
        numeros.add(botaoNumero(0));
        numeros.add(new JLabel());

        JPanel acoes = new JPanel(new GridLayout(1, 3, 6, 6));
        acoes.setOpaque(false);
        acoes.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        acoes.setPreferredSize(new Dimension(300, 70));
        acoes.add(criarBotao("BRANCO", Color.WHITE, Color.BLACK, urna::votarBranco));
        acoes.add(criarBotao("CORRIGIR", new Color(0xFF8C00), Color.BLACK, urna::corrigir));
        acoes.add(criarBotao("CONFIRMAR", new Color(0x2E9E3E), Color.BLACK, urna::confirmar));

        teclado.add(numeros, BorderLayout.CENTER);
        teclado.add(acoes, BorderLayout.SOUTH);
        teclado.setPreferredSize(new Dimension(340, 0));
        return teclado;
    }

    private JButton botaoNumero(int n) {
        return criarBotao(String.valueOf(n), new Color(0x1E1E1E), Color.WHITE,
                () -> urna.digitar((char) ('0' + n)));
    }

    private JButton criarBotao(String texto, Color fundo, Color frente, Runnable acao) {
        JButton b = new JButton(texto);
        b.setBackground(fundo);
        b.setForeground(frente);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, texto.length() > 1 ? 12 : 22));
        b.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        b.addActionListener(e -> acao.run());
        return b;
    }

    private void configurarAtalhoAsterisco() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke('*'), "apurar");
        getRootPane().getActionMap().put("apurar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                urna.apurar();
            }
        });
    }

    /** Redesenha o visor conforme o estado da urna*/
    private void atualizar() {
        if (urna.isEncerrada()) {
            if (!mostrandoResultado) {
                mostrandoResultado = true;
                mostrarResultado();
            }
            return;
        }

        if (urna.isFim()) {
            definirTextos("", "FIM", "", "", "", "Voto registrado. Obrigado!");
            return;
        }

        Cargo cargo = urna.getCargoAtual();
        String d = urna.getDigitos();
        String numero = "Número: " + (d.length() > 0 ? d.charAt(0) : '_')
                + " " + (d.length() > 1 ? d.charAt(1) : '_');

        if (urna.isBranco()) {
            definirTextos("SEU VOTO PARA", cargo.getNome().toUpperCase(), "",
                    "VOTO EM BRANCO", "", "CONFIRMAR para votar ou CORRIGIR para refazer");
        } else if (d.length() == 2) {
            Candidato c = urna.getCandidatoAtual();
            if (c != null) {
                definirTextos("SEU VOTO PARA", cargo.getNome().toUpperCase(), numero,
                        "Nome: " + c.getNome(), "Partido: " + c.getPartido(),
                        "CONFIRMAR para votar ou CORRIGIR para refazer");
            } else {
                definirTextos("SEU VOTO PARA", cargo.getNome().toUpperCase(), numero,
                        "NÚMERO ERRADO", "VOTO NULO",
                        "CONFIRMAR para votar nulo ou CORRIGIR para refazer");
            }
        } else {
            definirTextos("SEU VOTO PARA", cargo.getNome().toUpperCase(), numero,
                    "", "", "Digite o número do candidato");
        }
    }

    private void definirTextos(String titulo, String cargo, String numero,
                               String nome, String partido, String aviso) {
        lblTitulo.setText(titulo);
        lblCargo.setText(cargo);
        lblNumero.setText(numero);
        lblNome.setText(nome);
        lblPartido.setText(partido);
        lblAviso.setText(aviso);
    }

    /** Exibe o resultado e encerra o programa ao fechar a janela*/
    private void mostrarResultado() {
        String resultado = urna.getResultado();
        System.out.println(resultado);

        JTextArea area = new JTextArea(resultado, 24, 60);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Resultado da eleição", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
}