package cl.iei093.gamevault.modelo;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Reglas del dominio, independientes de las pantallas y de SQLite. */
public final class ValidadorVideojuego {
    // Catálogos aceptados por la lógica de negocio: deben coincidir con los de SQLite y XML.
    private static final List<String> PLATAFORMAS = Arrays.asList("PC", "PlayStation", "Xbox", "Nintendo Switch", "Móvil", "Otra");
    private static final List<String> GENEROS = Arrays.asList("Acción", "Aventura", "RPG", "Estrategia", "Deportes", "Simulación", "Indie", "Otro");
    private static final List<String> ESTADOS = Arrays.asList("Pendiente", "Jugando", "Completado");

    // Constructor privado: clase utilitaria sin instancias.
    private ValidadorVideojuego() { }

    /** Comprueba reglas antes de INSERT/UPDATE; lanza excepción si falla. */
    public static void validar(Videojuego juego) {
        if (juego == null) throw new IllegalArgumentException("El videojuego es obligatorio.");
        String titulo = juego.getTitulo();
        if (titulo == null || titulo.trim().isEmpty()) throw new IllegalArgumentException("El título es obligatorio.");
        if (titulo.length() > 100) throw new IllegalArgumentException("El título no puede superar 100 caracteres.");
        if (titulo.length() < 2) throw new IllegalArgumentException("El título debe tener al menos 2 caracteres.");
        // Evita duplicados disimulados con espacios al inicio o final.
        if (!titulo.equals(titulo.trim())) throw new IllegalArgumentException("El título no debe tener espacios en los extremos.");
        // No admite saltos de línea ni otros caracteres de control.
        if (titulo.matches(".*[\\p{Cntrl}].*")) throw new IllegalArgumentException("El título contiene caracteres de control.");
        validarOpcion(juego.getPlataforma(), PLATAFORMAS, "plataforma");
        validarOpcion(juego.getGenero(), GENEROS, "género");
        validarOpcion(juego.getEstado(), ESTADOS, "estado");
        if (juego.getPuntuacion() < 1 || juego.getPuntuacion() > 5)
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 5.");
    }

    /** Rechaza valores fuera de las listas autorizadas. */
    private static void validarOpcion(String valor, List<String> opciones, String campo) {
        if (valor == null || !opciones.contains(valor))
            throw new IllegalArgumentException("Selecciona un valor válido para " + campo + ".");
    }

    /** trim elimina espacios extremos; replaceAll agrupa espacios consecutivos. */
    public static String normalizarTitulo(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }
}
