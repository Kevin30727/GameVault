package cl.iei093.gamevault.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Crea y versiona SQLite. Separar el esquema de las operaciones CRUD. */
public class GameVaultDbHelper extends SQLiteOpenHelper {
    public static final String NOMBRE_BD = "gamevault.db";
    // Aumentar la versión dispara onUpgrade en instalaciones ya existentes.
    public static final int VERSION_BD = 2;
    public static final String TABLA = "videojuegos";

    // RESTRICCIONES SQL: NOT NULL (obligatorio), CHECK (dominio/rango),
    // UNIQUE + NOCASE (evita título+plataforma repetidos sin distinguir mayúsculas).
    private static final String CREAR_TABLA = "CREATE TABLE " + TABLA + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "titulo TEXT NOT NULL CHECK(length(trim(titulo)) BETWEEN 2 AND 100 AND titulo = trim(titulo)), " +
            "plataforma TEXT NOT NULL CHECK(plataforma IN ('PC','PlayStation','Xbox','Nintendo Switch','Móvil','Otra')), " +
            "genero TEXT NOT NULL CHECK(genero IN ('Acción','Aventura','RPG','Estrategia','Deportes','Simulación','Indie','Otro')), " +
            "estado TEXT NOT NULL CHECK(estado IN ('Pendiente','Jugando','Completado')), " +
            "puntuacion INTEGER NOT NULL CHECK(typeof(puntuacion) = 'integer' AND puntuacion BETWEEN 1 AND 5), " +
            "UNIQUE(titulo COLLATE NOCASE, plataforma COLLATE NOCASE))";

    public GameVaultDbHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    /** Solo se llama cuando SQLite crea el archivo de BD por primera vez. */
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA);
    }

    /** Migración de versión 1 a 2 sin borrar registros válidos. */
    @Override public void onUpgrade(SQLiteDatabase db, int anterior, int nueva) {
        if (anterior < 2) {
            // La V1 no exigía valores de catálogo ni unicidad. Se conservan los datos
            // compatibles y se señala un conflicto si existen registros inválidos.
            // SQLiteOpenHelper realiza la actualización dentro de una transacción.
            db.execSQL("ALTER TABLE " + TABLA + " RENAME TO videojuegos_anterior");
            db.execSQL(CREAR_TABLA);
            // Copiar datos antiguos fuerza su validación contra las nuevas restricciones.
            db.execSQL("INSERT INTO " + TABLA + " (id, titulo, plataforma, genero, estado, puntuacion) " +
                    "SELECT id, titulo, plataforma, genero, estado, puntuacion FROM videojuegos_anterior");
            db.execSQL("DROP TABLE videojuegos_anterior");
        }
    }
}
