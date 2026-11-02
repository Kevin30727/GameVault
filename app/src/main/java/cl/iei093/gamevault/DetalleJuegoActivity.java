package cl.iei093.gamevault;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import cl.iei093.gamevault.database.VideojuegoDAO;
import cl.iei093.gamevault.modelo.Videojuego;

/** Consulta un videojuego y permite navegar a su edición o confirmar eliminación. */
public class DetalleJuegoActivity extends Activity {
    private VideojuegoDAO dao;
    private long idJuego;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_detalle_juego);
        dao = new VideojuegoDAO(this);
        // Recibimos solo el ID, no una copia del objeto: SQLite sigue siendo la fuente.
        idJuego = getIntent().getLongExtra("juego_id", -1);
        if (idJuego <= 0) { finish(); return; }
        findViewById(R.id.botonVolver).setOnClickListener(v -> finish());
        findViewById(R.id.botonEditar).setOnClickListener(v -> {
            Intent i = new Intent(this, FormularioJuegoActivity.class);
            i.putExtra("juego_id", idJuego);
            startActivity(i);
        });
        findViewById(R.id.botonEliminar).setOnClickListener(v -> confirmarEliminacion());
    }

    // Tras editar, leemos otra vez la BD para reflejar los cambios actuales.
    @Override protected void onResume() {
        super.onResume();
        if (dao == null || idJuego <= 0) return;
        Videojuego j = dao.buscarPorId(idJuego);
        if (j == null) { Toast.makeText(this, "El videojuego ya no existe", Toast.LENGTH_SHORT).show(); finish(); return; }
        ((TextView)findViewById(R.id.detalleTitulo)).setText(j.getTitulo());
        ((TextView)findViewById(R.id.detallePlataforma)).setText("Plataforma: " + j.getPlataforma());
        ((TextView)findViewById(R.id.detalleGenero)).setText("Género: " + j.getGenero());
        ((TextView)findViewById(R.id.detalleEstado)).setText("Estado: " + j.getEstado());
        ((TextView)findViewById(R.id.detallePuntuacion)).setText("Puntuación: " + j.getPuntuacion() + "/5 ★");
    }

    /** AlertDialog exige confirmar antes de ejecutar DELETE (acción irreversible). */
    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar videojuego")
            .setMessage("¿Deseas eliminar este videojuego de forma permanente?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar", (dialog, which) -> {
                try {
                    // DELETE devuelve el número de filas afectadas: 1 es el éxito esperado.
                    if (dao.eliminar(idJuego) == 1) { Toast.makeText(this, "Videojuego eliminado", Toast.LENGTH_SHORT).show(); finish(); }
                    else Toast.makeText(this, "No se encontró el videojuego", Toast.LENGTH_SHORT).show();
                } catch (SQLiteException e) {
                    Toast.makeText(this, "Error de SQLite al eliminar", Toast.LENGTH_LONG).show();
                }
            }).show();
    }
}
