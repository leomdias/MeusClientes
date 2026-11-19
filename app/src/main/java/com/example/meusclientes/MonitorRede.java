package com.example.meusclientes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class MonitorRede extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Verifica se a ação foi mudança de conexão
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo == null || !networkInfo.isConnected()) {
                // TOAST: Avisa o usuário
                Toast.makeText(context, "ATENÇÃO: Você perdeu a conexão!", Toast.LENGTH_LONG).show();
            }
        }
    }
}