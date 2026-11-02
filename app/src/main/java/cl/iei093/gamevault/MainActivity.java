package cl.iei093.gamevault;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cl.iei093.gamevault.adapter.VideojuegoAdapter;
import cl.iei093.gamevault.database.VideojuegoDAO;
import java.util.List;

/** Pantalla principal: presenta la biblioteca y conecta filtros con consultas SQLite. */
public class MainActivity extends Activity {
    private VideojuegoDAO dao;
    private VideojuegoAdapter adapter;
    private EditText campoBusqueda;
    private Spinner filtroEstado;
    private TextView mensajeVacio, resumen;

    // onCreate se ejecuta al crear la pantalla: enlaza Views, lista y eventos.
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // DAO: centraliza las operaciones de datos. La Activity NO escribe SQL.
        dao = new VideojuegoDAO(this);
        campoBusqueda = findViewById(R.id.campoBusqueda);
        filtroEstado = findViewById(R.id.filtroEstado);
        mensajeVacio = findViewById(R.id.mensajeVacio);
        resumen = findViewById(R.id.resumen);
        RecyclerView lista = findViewById(R.id.listaJuegos);
        lista.setLayoutManager(new LinearLayoutManager(this));
        // Al tocar una tarjeta, enviamos su ID mediante Intent para consultar el detalle.
        adapter = new VideojuegoAdapter(id -> {
            Intent intent = new Intent(this, DetalleJuegoActivity.class);
            intent.putExtra("juego_id", id);
            startActivity(intent);
        });
        lista.setAdapter(adapter);
        ArrayAdapter<CharSequence> estados = ArrayAdapter.createFromResource(this,
                R.array.filtros_estado, R.layout.spinner_item);
        estados.setDropDownViewResource(R.layout.spinner_dropdown_item);
        filtroEstado.setAdapter(estados);
        // Cada cambio de estado vuelve a consultar la base de datos.
        filtroEstado.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { cargarJuegos(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) { }
        });
        // TextWatcher ejecuta una búsqueda cada vez que cambia el texto.
        campoBusqueda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { cargarJuegos(); }
            @Override public void afterTextChanged(Editable s) { }
        });
        findViewById(R.id.botonAgregar).setOnClickListener(v ->
                startActivity(new Intent(this, FormularioJuegoActivity.class)));
    }

    // Se refresca al volver de agregar, editar o eliminar: evita mostrar datos obsoletos.
    @Override protected void onResume() {
        super.onResume();
        if (dao != null && adapter != null && filtroEstado != null) cargarJuegos();
    }

    /** Consulta según búsqueda/estado y sincroniza RecyclerView y contador. */
    private void cargarJuegos() {
        String filtro = filtroEstado.getSelectedItem() == null ? "Todos" : filtroEstado.getSelectedItem().toString();
        List<cl.iei093.gamevault.modelo.Videojuego> juegos = dao.listar(campoBusqueda.getText().toString(), filtro);
        adapter.reemplazar(juegos);
        mensajeVacio.setVisibility(juegos.isEmpty() ? View.VISIBLE : View.GONE);
        int total = dao.contarTotal();
        resumen.setText(total + " juegos en tu colección · " + juegos.size() + " visibles");
    }
}
