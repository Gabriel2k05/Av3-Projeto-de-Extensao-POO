package com.urnaeletronica.dados;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.urnaeletronica.modelo.Candidato;
import com.urnaeletronica.modelo.Cargo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Única classe que conversa com o Firestore. */
public class FirebaseService {
    private final Firestore db;

    public FirebaseService() throws IOException {
        InputStream chave = FirebaseService.class.getResourceAsStream("/chave-firebase.json");
        if (chave == null) {
            throw new FileNotFoundException(
                "chave-firebase.json não encontrado em src/main/resources");
        }
        FirebaseOptions opcoes = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(chave))
                .build();
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(opcoes);
        }
        db = FirestoreClient.getFirestore();
    }

    /** Lê a coleção "candidatos". */
    public List<Candidato> buscarCandidatos() throws Exception {
        List<Candidato> lista = new ArrayList<>();
        for (QueryDocumentSnapshot doc : db.collection("candidatos").get().get().getDocuments()) {
            Cargo cargo = Cargo.deTexto(doc.getString("cargo"));
            String nome = doc.getString("nome");
            Long numero = doc.getLong("numero");
            String partido = doc.getString("partido");
            if (cargo == null || nome == null || numero == null) continue; // ignora documentos incompletos
            lista.add(new Candidato(nome, numero.intValue(), partido, cargo));
        }
        return lista;
    }

    /** Grava uma cédula: um documento em "votos" com os dois campos. */
    public void salvarVoto(long[] escolhas) throws Exception {
        Map<String, Object> cedula = new HashMap<>();
        for (Cargo c : Cargo.values()) {
            cedula.put(c.getCampoVoto(), escolhas[c.ordinal()]);
        }
        db.collection("votos").add(cedula).get();
    }

    /** Retorna o valor votado para o cargo em todas as cédulas. */
    public List<Long> buscarVotos(Cargo cargo) throws Exception {
        List<Long> votos = new ArrayList<>();
        for (QueryDocumentSnapshot doc : db.collection("votos").get().get().getDocuments()) {
            Long v = doc.getLong(cargo.getCampoVoto());
            if (v != null) votos.add(v);
        }
        return votos;
    }
}