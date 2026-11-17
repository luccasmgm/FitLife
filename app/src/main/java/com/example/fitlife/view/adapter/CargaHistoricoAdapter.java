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
import com.example.fitlife.model.entity.CargaExercicio;

/**
 * Adapter do histórico de cargas na tela de detalhe do exercício.
 * Cada item mostra: data, carga (kg), séries × repetições.
 */
public class CargaHistoricoAdapter extends ListAdapter<CargaExercicio, CargaHistoricoAdapter.CargaViewHolder> {

    public CargaHistoricoAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<CargaExercicio> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CargaExercicio>() {
                @Override
                public boolean areItemsTheSame(@NonNull CargaExercicio a, @NonNull CargaExercicio b) {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull CargaExercicio a, @NonNull CargaExercicio b) {
                    return a.getCargaKg() == b.getCargaKg()
                            && a.getSeries() == b.getSeries()
                            && a.getRepeticoes() == b.getRepeticoes();
                }
            };

    @NonNull
    @Override
    public CargaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carga_historico, parent, false);
        return new CargaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CargaViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class CargaViewHolder extends RecyclerView.ViewHolder {

        private final TextView textData;
        private final TextView textCarga;
        private final TextView textSeriesRep;

        CargaViewHolder(@NonNull View itemView) {
            super(itemView);
            textData      = itemView.findViewById(R.id.textDataCarga);
            textCarga     = itemView.findViewById(R.id.textCargaKg);
            textSeriesRep = itemView.findViewById(R.id.textSeriesRep);
        }

        void bind(CargaExercicio carga) {
            textData.setText(carga.getData());
            textCarga.setText(carga.getCargaKg() + " kg");
            textSeriesRep.setText(carga.getSeries() + " séries × " + carga.getRepeticoes() + " reps");
        }
    }
}
