package com.example.fitlife.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.model.entity.AlimentoConsumido;
import com.example.fitlife.model.entity.Refeicao;
import com.example.fitlife.viewmodel.NutricaoViewModel.RefeicaoItem;
import com.google.android.material.button.MaterialButton;

/**
 * Adapter da lista de refeições. Cada card mostra o tipo da refeição,
 * o horário sugerido, quanto já foi consumido em relação à meta e a
 * lista de alimentos registrados hoje.
 */
public class RefeicaoAdapter extends ListAdapter<RefeicaoItem, RefeicaoAdapter.RefeicaoViewHolder> {

    public interface OnAdicionarAlimentoListener {
        void onAdicionarAlimento(Refeicao refeicao);
    }

    private final OnAdicionarAlimentoListener listener;

    public RefeicaoAdapter(OnAdicionarAlimentoListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<RefeicaoItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RefeicaoItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull RefeicaoItem a, @NonNull RefeicaoItem b) {
                    return a.refeicao.getId() == b.refeicao.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull RefeicaoItem a, @NonNull RefeicaoItem b) {
                    return a.caloriasConsumidas == b.caloriasConsumidas
                            && a.alimentos.size() == b.alimentos.size()
                            && a.refeicao.getMetaCalorias() == b.refeicao.getMetaCalorias();
                }
            };

    @NonNull
    @Override
    public RefeicaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_refeicao, parent, false);
        return new RefeicaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RefeicaoViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    /** Rótulo amigável do tipo — público para a Activity reaproveitar no título do diálogo. */
    public static String rotuloDeTipo(Refeicao.TipoRefeicao tipo) {
        switch (tipo) {
            case CAFE_DA_MANHA: return "Café da manhã";
            case ALMOCO:        return "Almoço";
            case JANTAR:        return "Jantar";
            case SNACK:         return "Lanche";
            default:            return "";
        }
    }

    static class RefeicaoViewHolder extends RecyclerView.ViewHolder {

        private final TextView textTipo, textHorario, textProgresso, textAlimentos;
        private final MaterialButton btnAdicionar;

        RefeicaoViewHolder(@NonNull View itemView) {
            super(itemView);
            textTipo      = itemView.findViewById(R.id.textTipoRefeicao);
            textHorario   = itemView.findViewById(R.id.textHorarioRefeicao);
            textProgresso = itemView.findViewById(R.id.textProgressoRefeicao);
            textAlimentos = itemView.findViewById(R.id.textAlimentosRefeicao);
            btnAdicionar  = itemView.findViewById(R.id.btnAdicionarAlimento);
        }

        void bind(RefeicaoItem item, OnAdicionarAlimentoListener listener) {
            Refeicao refeicao = item.refeicao;

            textTipo.setText(rotuloDeTipo(refeicao.getTipo()));
            textHorario.setText(refeicao.getHorarioSugerido());
            textProgresso.setText(Math.round(item.caloriasConsumidas)
                    + " / " + Math.round(refeicao.getMetaCalorias()) + " kcal");

            if (item.alimentos.isEmpty()) {
                textAlimentos.setVisibility(View.GONE);
            } else {
                StringBuilder sb = new StringBuilder();
                for (AlimentoConsumido a : item.alimentos) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append("• ").append(a.getNomeAlimento());
                    if (a.getQuantidade() != null && !a.getQuantidade().isEmpty()) {
                        sb.append(" (").append(a.getQuantidade()).append(")");
                    }
                    sb.append(" — ").append(Math.round(a.getCalorias())).append(" kcal");
                }
                textAlimentos.setText(sb.toString());
                textAlimentos.setVisibility(View.VISIBLE);
            }

            btnAdicionar.setOnClickListener(v -> listener.onAdicionarAlimento(refeicao));
        }
    }
}
