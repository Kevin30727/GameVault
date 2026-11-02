package cl.iei093.gamevault.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cl.iei093.gamevault.modelo.Videojuego;
import cl.iei093.gamevault.modelo.ValidadorVideojuego;
import java.util.ArrayList;
import java.util.List;

/** DAO (Data Access Object): aísla INSERT, SELECT, UPDATE y DELETE de la UI. */
public class VideojuegoDAO {
    private final GameVaultDbHelper helper;

    public VideojuegoDAO(Context context) {
        helper = new GameVaultDbHelper(context.getApplicationContext());
    }

    /** ContentValues transforma atributos del objeto en columnas SQL. */
    private ContentValues valores(Videojuego juego) {
        ContentValues cv = new ContentValues();
        cv.put("titulo", juego.getTitulo());
        cv.put("plataforma", juego.getPlataforma());
        cv.put("genero", juego.getGenero());
        cv.put("estado", juego.getEstado());
        cv.put("puntuacion", juego.getPuntuacion());
        return cv;
    }

    /** INSERT: valida Java y deja que SQLite compruebe UNIQUE/CHECK. */
    public long insertar(Videojuego juego) {
        ValidadorVideojuego.validar(juego);
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.insertOrThrow(GameVaultDbHelper.TABLA, null, valores(juego));
    }

    /** UPDATE: solo afecta la fila cuyo ID coincide. */
    public int actualizar(Videojuego juego) {
        ValidadorVideojuego.validar(juego);
        if (juego.getId() <= 0) throw new IllegalArgumentException("ID inválido para editar.");
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(GameVaultDbHelper.TABLA, valores(juego), "id = ?",
                new String[]{String.valueOf(juego.getId())});
    }

    /** DELETE: exige ID positivo y devuelve cantidad de filas eliminadas. */
    public int eliminar(long id) {
        if (id <= 0) throw new IllegalArgumentException("ID inválido para eliminar.");
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(GameVaultDbHelper.TABLA, "id = ?", new String[]{String.valueOf(id)});
    }

    /** SELECT por ID: consulta parametrizada; null si no se encuentra. */
    public Videojuego buscarPorId(long id) {
        if (id <= 0) return null;
        SQLiteDatabase db = helper.getReadableDatabase();
        // try-with-resources cierra Cursor automáticamente, incluso si ocurre un error.
        try (Cursor c = db.query(GameVaultDbHelper.TABLA, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? desdeCursor(c) : null;
        }
    }

    /** SELECT dinámico: combina búsqueda LIKE, filtro WHERE y orden ORDER BY. */
    public List<Videojuego> listar(String texto, String estado) {
        List<Videojuego> resultado = new ArrayList<>();
        // "1=1" facilita agregar condiciones AND sin casos especiales.
        StringBuilder where = new StringBuilder("1=1");
        List<String> args = new ArrayList<>();
        if (texto != null && !texto.trim().isEmpty()) {
            where.append(" AND titulo LIKE ?");
            // ? + selectionArgs evita concatenar entradas de usuario dentro del SQL.
            args.add("%" + texto.trim() + "%");
        }
        if (estado != null && !estado.equals("Todos")) {
            where.append(" AND estado = ?");
            args.add(estado);
        }
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(GameVaultDbHelper.TABLA, null, where.toString(),
                args.toArray(new String[0]), null, null, "titulo COLLATE NOCASE ASC")) {
            while (c.moveToNext()) resultado.add(desdeCursor(c));
        }
        return resultado;
    }

    /** Desafío Actividad 4.1: recupera únicamente videojuegos con puntuación >= 4 ordenados descendentemente. */
    public List<Videojuego> listarDestacados() {
        List<Videojuego> resultado = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(GameVaultDbHelper.TABLA, null, "puntuacion >= ?",
                new String[]{"4"}, null, null, "puntuacion DESC, titulo COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                resultado.add(desdeCursor(c));
            }
        }
        return resultado;
    }

    /** COUNT(*) obtiene el total de registros, independientemente del filtro. */
    public int contarTotal() {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + GameVaultDbHelper.TABLA, null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    /** Mapea columnas del Cursor a un objeto Videojuego. */
    private Videojuego desdeCursor(Cursor c) {
        return new Videojuego(
            c.getLong(c.getColumnIndexOrThrow("id")),
            c.getString(c.getColumnIndexOrThrow("titulo")),
            c.getString(c.getColumnIndexOrThrow("plataforma")),
            c.getString(c.getColumnIndexOrThrow("genero")),
            c.getString(c.getColumnIndexOrThrow("estado")),
            c.getInt(c.getColumnIndexOrThrow("puntuacion")));
    }
}
