package com.example.meusclientes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meusclientes.DetalheActivity;

import java.util.List;

public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ViewHolder> {

    //lista que guarda os dados
    private List<String[]> listaDeClientes;
    private Context context;

    public ClienteAdapter(Context context, List<String[]> listaDeClientes) {
        this.context = context;
        this.listaDeClientes = listaDeClientes;
    }

    //cria o visual de cada linha, infla o XML
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new ViewHolder(view);
    }

    //cola os dados (texto) nos componentes visuais
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] cliente = listaDeClientes.get(position);

        holder.txtNome.setText(cliente[0]);
        holder.txtEmail.setText(cliente[1]);

        // INTENT: Configura o clique para abrir a outra tela
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalheActivity.class);
            // Passamos os dados para a próxima tela
            intent.putExtra("NOME", cliente[0]);
            intent.putExtra("EMAIL", cliente[1]);
            intent.putExtra("SITE", cliente[2]);
            context.startActivity(intent);
        });
    }

    //diz quantos itens tem na lista
    @Override
    public int getItemCount() {
        return listaDeClientes.size();
    }

    //classe auxiliar para achar os IDs no XML
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtEmail;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtEmail = itemView.findViewById(R.id.txtEmail);
        }
    }
}