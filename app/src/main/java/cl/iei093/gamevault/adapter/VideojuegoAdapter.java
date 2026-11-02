package cl.iei093.gamevault.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import cl.iei093.gamevault.R;
import cl.iei093.gamevault.modelo.Videojuego;

/** Adaptador: convierte cada videojuego en una tarjeta visual de RecyclerView. */
public class VideojuegoAdapter extends RecyclerView.Adapter<VideojuegoAdapter.JuegoHolder> {
    // Callback: entrega a MainActivity el ID de la tarjeta seleccionada.
    public interface AlSeleccionarJuego { void alSeleccionar(long id); }
    private final List<Videojuego> juegos = new ArrayList<>();
    private final AlSeleccionarJuego listener;

    public VideojuegoAdapter(AlSeleccionarJuego listener) { this.listener = listener; }

    /** Cambia la lista visible al aplicar búsquedas o después de editar. */
    public void reemplazar(List<Videojuego> nuevos) {
        juegos.clear();
        juegos.addAll(nuevos);
        notifyDataSetChanged();
    }

    // Infla el layout XML de una tarjeta (solo cuando RecyclerView lo necesita).
    @NonNull @Override public JuegoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_videojuego, parent, false);
        return new JuegoHolder(view);
    }

    // Enlaza los datos del juego con las vistas de la tarjeta.
    @Override public void onBindViewHolder(@NonNull JuegoHolder holder, int position) {
        Videojuego juego = juegos.get(position);
        holder.titulo.setText(juego.getTitulo());
        holder.metadatos.setText(juego.getPlataforma() + "  •  " + juego.getGenero());
        holder.estado.setText(juego.getEstado() + "  •  " + juego.getPuntuacion() + "/5 ★");
        holder.itemView.setOnClickListener(v -> listener.alSeleccionar(juego.getId()));
    }

    // RecyclerView pregunta cuántos registros se deben mostrar.
    @Override public int getItemCount() { return juegos.size(); }

    // ViewHolder mantiene referencias a las vistas y evita búsquedas repetitivas.
    static class JuegoHolder extends RecyclerView.ViewHolder {
        final TextView titulo, metadatos, estado;
        JuegoHolder(View v) {
            super(v);
            titulo = v.findViewById(R.id.itemTitulo);
            metadatos = v.findViewById(R.id.itemMetadatos);
            estado = v.findViewById(R.id.itemEstado);
        }
    }
}
