package com.example.northfutbol;

public class EquipoClasificacion {

    // Nombre del equipo (EQUIPO.NOMBRE) 
    private String nombre;

    // Partidos jugados 
    private int pj;

    // Partidos ganados 
    private int pg;

    // Partidos empatados 
    private int pe;

    // Partidos perdidos 
    private int pp;

    // Goles a favor 
    private int gf;

    // Goles en contra 
    private int gc;

    // Diferencia de goles (GF - GC) 
    private int gd;

    // Puntos totales (PG * 3 + PE) 
    private int puntos;

    public EquipoClasificacion(String nombre, int pj, int pg, int pe, int pp, int gf, int gc, int gd, int puntos) {
        this.nombre = nombre;
        this.pj = pj;
        this.pg = pg;
        this.pe = pe;
        this.pp = pp;
        this.gf = gf;
        this.gc = gc;
        this.gd = gd;
        this.puntos = puntos;
    }

    public String getNombre() { return nombre; }
    public int getPj() { return pj; }
    public int getPg() { return pg; }
    public int getPe() { return pe; }
    public int getPp() { return pp; }
    public int getGf() { return gf; }
    public int getGc() { return gc; }
    public int getGd() { return gd; }
    public int getPuntos() { return puntos; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPj(int pj) { this.pj = pj; }
    public void setPg(int pg) { this.pg = pg; }
    public void setPe(int pe) { this.pe = pe; }
    public void setPp(int pp) { this.pp = pp; }
    public void setGf(int gf) { this.gf = gf; }
    public void setGc(int gc) { this.gc = gc; }
    public void setGd(int gd) { this.gd = gd; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
}