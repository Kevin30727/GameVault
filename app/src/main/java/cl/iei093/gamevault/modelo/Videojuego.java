package cl.iei093.gamevault.modelo;

/** Modelo: representa un registro de la tabla videojuegos como objeto Java. */
public class Videojuego {
    // Encapsulamiento: campos privados; getters permiten lectura controlada.
    private final long id;
    private final String titulo;
    private final String plataforma;
    private final String genero;
    private final String estado;
    private final int puntuacion;

    /** Constructor: crea el objeto con todos sus atributos. */
    public Videojuego(long id, String titulo, String plataforma, String genero, String estado, int puntuacion) {
        this.id = id;
        this.titulo = titulo;
        this.plataforma = plataforma;
        this.genero = genero;
        this.estado = estado;
        this.puntuacion = puntuacion;
    }
    public long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getPlataforma() { return plataforma; }
    public String getGenero() { return genero; }
    public String getEstado() { return estado; }
    public int getPuntuacion() { return puntuacion; }
}
