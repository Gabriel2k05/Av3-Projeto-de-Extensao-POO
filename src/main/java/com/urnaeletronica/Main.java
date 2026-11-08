package com.urnaeletronica;

import com.urnaeletronica.dados.FirebaseService;
import com.urnaeletronica.negocio.Urna;
import com.urnaeletronica.visao.TelaUrna;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final Urna urna;
        try {
            urna = new Urna(new FirebaseService());
        } catch (Exception e) {
            System.err.println("Erro ao iniciar a urna: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Look and feel padrão do Java: respeita as cores dos botões em qualquer sistema
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignorada) { }
            new TelaUrna(urna);
        });

        System.out.println("=== URNA CIDADÃ ===");
        System.out.println("Digite o número do candidato | C = confirmar | R = corrigir | B = branco | * = apurar");

        // Loop do Scanner: roda até a apuração
        Scanner scanner = new Scanner(System.in);
        while (!urna.isEncerrada() && scanner.hasNextLine()) {
            String linha = scanner.nextLine().trim();
            for (char c : linha.toCharArray()) {
                if (c == '*') {
                    urna.apurar();   // a janela exibe o resultado e encerra o programa
                    break;
                } else if (c >= '0' && c <= '9') {
                    urna.digitar(c);
                } else if (c == 'c' || c == 'C') {
                    urna.confirmar();
                } else if (c == 'r' || c == 'R') {
                    urna.corrigir();
                } else if (c == 'b' || c == 'B') {
                    urna.votarBranco();
                }
            }
        }
    }
}