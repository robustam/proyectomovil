rootProject.name = "red-patrimonial-backend"

// Cada carpeta es un microservicio independiente (se ejecuta en su propio puerto
// y tiene su propia base de datos H2 local).
include(
    "ms-usuarios",  // puerto 8081 - usuarios, roles y permisos por nodo
    "ms-nodos",     // puerto 8082 - fichas patrimoniales, horarios y multimedia
    "ms-eventos",   // puerto 8083 - oferta cultural/litúrgica e inscripciones
    "ms-rutas",     // puerto 8084 - rutas, salidas pedagógicas y guías
    "ms-avisos"     // puerto 8085 - avisos comunitarios en tiempo real
)
