# GUÍA: Implementación de Escudos - Grupo 1

## Situación actual

Se han creado **18 archivos drawable XML** con colores distintivos para cada equipo (placeholders).
Estos funcionan perfectamente y muestran colores únicos sin modificar la BD.

## Equipos implementados

1. Pontevedra CF
2. Bergantiños CF
3. UP Langreo
4. Coruxo FC
5. RC Deportivo Fabril
6. RS Gimnástica de Torrelavega
7. Real Avilés Industrial
8. Club Marino de Luanco
9. CD Numancia de Soria
10. Rayo Cantabria
11. Real Ávila CF
12. UM Escobedo
13. Real Valladolid Promesas
14. Salamanca CF UDS
15. CD Laredo
16. SD Compostela
17. CD Guijuelo
18. UD Llanera

## Para usar imágenes PNG reales

1. **Descargar imágenes de escudos**:
   - Buscar imágenes PNG de cada equipo (350x350px es ideal)
   - Guardarlas en: `app/src/main/res/drawable/`
   - Nombrar igual: `escudo_pontevedra.png`, `escudo_bergantinos.png`, etc.

2. **EscudosHelper.kt continuará funcionando automáticamente**:
   - El helper mapea nombres → recursos drawable
   - Si reemplazas los .xml con .png del mismo nombre, funciona sin cambiar código

## Ubicación de archivos drawable

```
app/src/main/res/drawable/
├── escudo_pontevedra.xml
├── escudo_bergantinos.xml
├── escudo_langreo.xml
├── escudo_coruxo.xml
├── escudo_deportivo_fabril.xml
├── escudo_gimnastica.xml
├── escudo_real_aviles.xml
├── escudo_marino.xml
├── escudo_numancia.xml
├── escudo_rayo_cantabria.xml
├── escudo_real_avila.xml
├── escudo_escobedo.xml
├── escudo_valladolid_promesas.xml
├── escudo_salamanca.xml
├── escudo_laredo.xml
├── escudo_compostela.xml
├── escudo_guijuelo.xml
└── escudo_llanera.xml
```

## Código que se actualizó

- ✅ **EscudosHelper.kt**: Mapper con los 18 equipos reales
- ✅ **EquiposActivity.kt**: Carga escudo en cada equipo
- ✅ **PartidosActivity.kt**: Carga escudos de ambos equipos
- ✅ **PartidoActivity.kt**: Muestra escudos en header del partido

## Nota importante

- Los XML placeholders son útiles para desarrollo/testing
- Cuando reemplaces con PNG, se ajustan automáticamente sin tocar código
- El sistema funciona sin BD en todas formas
