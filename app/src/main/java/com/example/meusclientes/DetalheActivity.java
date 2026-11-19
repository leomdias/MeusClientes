package com.example.meusclientes;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetalheActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe);

        TextView txtNome = findViewById(R.id.detalheNome);
        TextView txtEmail = findViewById(R.id.detalheEmail);
        TextView txtSite = findViewById(R.id.detalheSite);
        Button btnVoltar = findViewById(R.id.btnVoltar);

        //Intent
        String nome = getIntent().getStringExtra("NOME");
        String email = getIntent().getStringExtra("EMAIL");
        String site = getIntent().getStringExtra("SITE");

        txtNome.setText(nome);
        txtEmail.setText(email);
        txtSite.setText(site);

        btnVoltar.setOnClickListener(v -> finish());
    }
}