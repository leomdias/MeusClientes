package com.example.meusclientes;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClienteAdapter adapter;
    private List<String[]> listaDados = new ArrayList<>();
    private MonitorRede monitorRede;
    private static final String CHANNEL_ID = "CANAL_CLIENTES";

    // --- lÓGICA DE PERMISSÃO DE NOTIFICAÇÃO ---

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(MainActivity.this, "✅ Acesso a notificações concedido!", Toast.LENGTH_LONG).show();
                    // Agora que a permissão foi concedida, podemos criar o canal de notificação
                    criarCanalNotificacao();
                } else {
                    Toast.makeText(MainActivity.this, "❌ Acesso a notificações negado. Notificações não serão exibidas.", Toast.LENGTH_LONG).show();
                }
            });

    // --- MÉTODOS DO CICLO DE VIDA ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Configurar a Lista (RecyclerView)
        recyclerView = findViewById(R.id.listaClientes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClienteAdapter(this, listaDados);
        recyclerView.setAdapter(adapter);

        //  Configurar o Botão
        Button btnCarregar = findViewById(R.id.btnCarregar);
        btnCarregar.setOnClickListener(v -> baixarDadosDaInternet());

        //  Registrar o Monitor de Rede (BroadcastReceiver)
        monitorRede = new MonitorRede();
        registerReceiver(monitorRede, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //  solicitar permissão de notificação e criar o canal
        askNotificationPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Para de monitorar a rede quando fechar
        unregisterReceiver(monitorRede);
    }

    // --- MÉTODOS DE PERMISSÃO ---

    private void askNotificationPermission() {
        // A partir do Android 13 (API 33), a permissão precisa ser solicitada em tempo de execução
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Permissão já concedida. Cria o canal imediatamente.
                Toast.makeText(this, "🔔 Notificações já ativas.", Toast.LENGTH_SHORT).show();
                criarCanalNotificacao();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Racional: Exibe um Toast antes de solicitar a permissão
                Toast.makeText(this, "Precisamos de permissão para enviar notificações importantes.", Toast.LENGTH_LONG).show();
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);

            } else {

                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {

            Toast.makeText(this, "Notificações ativadas por padrão.", Toast.LENGTH_SHORT).show();
            criarCanalNotificacao();
        }
    }
    

    private void baixarDadosDaInternet() {
        Toast.makeText(this, "Conectando...", Toast.LENGTH_SHORT).show();

        // THREAD: Executa em segundo plano
        new Thread(() -> {
            try {
                // conexão com a API (Dados falsos de teste)
                URL url = new URL("https://jsonplaceholder.typicode.com/users");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // leitura e Processamento (Omitido o detalhe para brevidade)
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder resposta = new StringBuilder();
                String linha;
                while ((linha = reader.readLine()) != null) {
                    resposta.append(linha);
                }
                reader.close();


                JSONArray jsonArray = new JSONArray(resposta.toString());
                List<String[]> novaLista = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String nome = obj.getString("name");
                    String email = obj.getString("email");
                    String site = obj.getString("website");
                    novaLista.add(new String[]{nome, email, site});
                }

                //  Voltar para a Thread Principal para atualizar a tela
                new Handler(Looper.getMainLooper()).post(() -> {
                    listaDados.clear();
                    listaDados.addAll(novaLista);
                    adapter.notifyDataSetChanged();

                    // Verifica se pode enviar notificação antes de chamar
                    if (isNotificationAllowed()) {
                        enviarNotificacao(); // Mostra notificação na barra superior
                    } else {
                        Toast.makeText(MainActivity.this, "Permissão de notificação negada. Não foi possível notificar.", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(MainActivity.this, "Erro ao baixar dados.", Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // --- LÓGICA DE NOTIFICAÇÃO ---

    private void criarCanalNotificacao() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Atualizações", NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void enviarNotificacao() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Download Completo")
                .setContentText("A lista de clientes foi atualizada.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    private boolean isNotificationAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }
}