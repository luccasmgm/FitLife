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
import com.example.fitlife.model.entity.Exercicio;

/**
 * Adapter do RecyclerView para a lista de exercícios.
 *
 * Usa ListAdapter (em vez do RecyclerView.Adapter simples) porque ele
 * integra com DiffUtil: compara a lista antiga com a nova automaticamente
 * e anima apenas os itens que mudaram, em vez de redesenhar a lista inteira.
 * Isso é especialmente importante aqui porque a lista muda toda vez que
 * o usuário troca o filtro do Spinner.
 *
 * O padrão de callback (interface OnExercicioClickListener) mantém o
 * Adapter desacoplado da Activity — ele não sabe o que acontece ao clicar,
 * só notifica quem estiver ouvindo.
 */
public class ExercicioAdapter extends ListAdapter<Exercicio, ExercicioAdapter.ExercicioViewHolder> {

    public interface OnExercicioClickListener {
        void onExercicioClick(Exercicio exercicio);
    }

    private final OnExercicioClickListener listener;

    public ExercicioAdapter(OnExercicioClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /**
     * DiffUtil compara itens por ID (são o mesmo exercício?) e por conteúdo
     * (os dados mudaram?). Sem isso, qualquer update na lista causaria um
     * "flash" visual porque todos os itens seriam redesenhados.
     */
    private static final DiffUtil.ItemCallback<Exercicio> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Exercicio>() {
                @Override
                public boolean areItemsTheSame(@NonNull Exercicio a, @NonNull Exercicio b) {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Exercicio a, @NonNull Exercicio b) {
                    return a.getNome().equals(b.getNome())
                            && a.getTipo() == b.getTipo();
                }
            };

    @NonNull
    @Override
    public ExercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercicio, parent, false);
        return new ExercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExercicioViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ExercicioViewHolder extends RecyclerView.ViewHolder {

        private final TextView textNome;
        private final TextView textGrupoMuscular;
        private final TextView textTipo;

        ExercicioViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome          = itemView.findViewById(R.id.textNomeExercicio);
            textGrupoMuscular = itemView.findViewById(R.id.textGrupoMuscular);
            textTipo          = itemView.findViewById(R.id.textTipoExercicio);
        }

        void bind(Exercicio exercicio, OnExercicioClickListener listener) {
            textNome.setText(exercicio.getNome());

            // Grupo muscular só existe para musculação
            if (exercicio.getGrupoMuscular() != null) {
                textGrupoMuscular.setText(exercicio.getGrupoMuscular());
                textGrupoMuscular.setVisibility(View.VISIBLE);
            } else {
                textGrupoMuscular.setVisibility(View.GONE);
            }

            textTipo.setText(rotuloDeTipo(exercicio.getTipo()));

            itemView.setOnClickListener(v -> listener.onExercicioClick(exercicio));
        }

        private String rotuloDeTipo(Exercicio.TipoExercicio tipo) {
            switch (tipo) {
                case MUSCULACAO: return "Musculação";
                case CARDIO:     return "Cardio";
                case MOBILIDADE: return "Mobilidade";
                default:         return "";
            }
        }
    }
}
