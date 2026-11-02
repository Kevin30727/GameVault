package cl.iei093.gamevault;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;
import cl.iei093.gamevault.database.VideojuegoDAO;
import cl.iei093.gamevault.modelo.Videojuego;
import cl.iei093.gamevault.modelo.ValidadorVideojuego;

/** Formulario compartido para CREATE y UPDATE: el ID del Intent determina el modo. */
public class FormularioJuegoActivity extends Activity {
    private EditText titulo;
    private Spinner plataforma, genero, estado;
    private RatingBar puntuacion;
    private VideojuegoDAO dao;
    private long idJuego;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_formulario_juego);
        dao = new VideojuegoDAO(this);
        titulo = findViewById(R.id.campoTitulo);
        plataforma = findViewById(R.id.campoPlataforma);
        genero = findViewById(R.id.campoGenero);
        estado = findViewById(R.id.campoEstado);
        puntuacion = findViewById(R.id.campoPuntuacion);
        configurarSpinner(plataforma, R.array.plataformas);
        configurarSpinner(genero, R.array.generos);
        configurarSpinner(estado, R.array.estados);
        // -1 significa "registro nuevo"; un ID positivo significa "editar".
        idJuego = getIntent().getLongExtra("juego_id", -1);
        findViewById(R.id.botonCancelar).setOnClickListener(v -> finish());
        findViewById(R.id.botonGuardar).setOnClickListener(v -> guardar());
        if (idJuego > 0) cargarEdicion();
    }

    /** Carga opciones predefinidas para reducir entradas inválidas. */
    private void configurarSpinner(Spinner spinner, int recurso) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, recurso,
                R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /** Selecciona un valor existente al recuperar datos para edición. */
    private void seleccionar(Spinner spinner, String valor) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(valor)) { spinner.setSelection(i); return; }
        }
    }

    /** Busca el registro por ID y carga sus datos en el formulario. */
    private void cargarEdicion() {
        Videojuego j = dao.buscarPorId(idJuego);
        if (j == null) { Toast.makeText(this, "El juego ya no existe", Toast.LENGTH_SHORT).show(); finish(); return; }
        ((android.widget.TextView)findViewById(R.id.tituloFormulario)).setText("Editar videojuego");
        titulo.setText(j.getTitulo());
        seleccionar(plataforma, j.getPlataforma());
        seleccionar(genero, j.getGenero());
        seleccionar(estado, j.getEstado());
        puntuacion.setRating(j.getPuntuacion());
    }

    /** Valida antes de guardar. Si falla una regla, sale sin llamar al DAO. */
    private void guardar() {
        // Normalizamos espacios antes de comprobar longitud y obligatoriedad.
        String nombre = ValidadorVideojuego.normalizarTitulo(titulo.getText().toString());
        // RETURN impide que el programa continúe con datos inválidos.
        if (nombre.isEmpty()) { titulo.setError("El título es obligatorio"); titulo.requestFocus(); return; }
        if (nombre.length() < 2) { titulo.setError("Mínimo 2 caracteres"); titulo.requestFocus(); return; }
        if (nombre.length() > 100) { titulo.setError("Máximo 100 caracteres"); titulo.requestFocus(); return; }
        // RatingBar produce float; la puntuación persistida es INTEGER.
        int valor = Math.round(puntuacion.getRating());
        if (valor < 1 || valor > 5) {
            Toast.makeText(this, "Selecciona una puntuación entre 1 y 5", Toast.LENGTH_LONG).show(); return;
        }
        if (plataforma.getSelectedItem() == null || genero.getSelectedItem() == null || estado.getSelectedItem() == null) {
            Toast.makeText(this, "Debes elegir plataforma, género y estado", Toast.LENGTH_LONG).show(); return;
        }
        Videojuego juego = new Videojuego(idJuego, nombre,
                plataforma.getSelectedItem().toString(), genero.getSelectedItem().toString(),
                estado.getSelectedItem().toString(), valor);
        try {
            // Segunda barrera de validación: reglas independientes de la interfaz.
            ValidadorVideojuego.validar(juego);
            // El mismo formulario llama a UPDATE o INSERT según el ID recibido.
            if (idJuego > 0) {
                if (dao.actualizar(juego) != 1) { Toast.makeText(this, "No se pudo actualizar", Toast.LENGTH_SHORT).show(); return; }
                Toast.makeText(this, "Videojuego actualizado", Toast.LENGTH_SHORT).show();
            } else {
                long nuevoId = dao.insertar(juego);
                if (nuevoId <= 0) throw new SQLiteException("No se creó el registro");
                Toast.makeText(this, "Videojuego registrado", Toast.LENGTH_SHORT).show();
            }
            finish();
        // Errores de reglas Java: se informa al usuario sin cerrar la pantalla.
        } catch (IllegalArgumentException e) {
            new AlertDialog.Builder(this).setTitle("Datos inválidos")
                .setMessage(e.getMessage()).setPositiveButton("Corregir", null).show();
        // Rechazo de restricciones SQL (por ejemplo UNIQUE o CHECK).
        } catch (SQLiteConstraintException e) {
            new AlertDialog.Builder(this).setTitle("Registro no permitido")
                .setMessage("El juego ya está registrado en esa plataforma o incumple una restricción de SQLite.")
                .setPositiveButton("Corregir", null).show();
        // Otros fallos de almacenamiento: no se informa un guardado exitoso.
        } catch (SQLiteException e) {
            new AlertDialog.Builder(this).setTitle("Error al guardar")
                .setMessage("No se pudo guardar el registro en SQLite.")
                .setPositiveButton("Aceptar", null).show();
        }
    }
}
