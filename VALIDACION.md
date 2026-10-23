# Validación real · 22 de octubre de 2026

## Resultados

| Comprobación ejecutada | Resultado |
| --- | --- |
| `mvnw.cmd test -DlocalIntegration=true` | 16 pruebas, 0 fallos, 0 errores, 0 omitidas |
| `LinkServiceTest` | 10 pruebas correctas |
| `LinkControllerTest` | 5 pruebas correctas |
| `LocalInfrastructureTest` | 1 prueba correcta con MongoDB y Redis reales; 34,48 s |
| `npm run build` | Correcto; Vite 7.3.7, 32 módulos transformados |
| `npm ci` y auditoría npm | Instalación reproducible; 0 vulnerabilidades reportadas |
| `npm run smoke` | Correcto en Chrome headless; sin errores JavaScript de página |
| Reinicio real de Spring Boot | Mismo ID y URL original recuperados; redirección 302 correcta |
| Revisión visual | Capturas de escritorio 1280 px y móvil 390 px inspeccionadas |

Resumen original de Surefire: [pruebas-java.txt](docs/pruebas-java.txt). Los informes completos de la ejecución están en `backend/target/surefire-reports/` (carpeta generada, excluida de Git).

## Qué se verificó realmente

- MongoDB y Redis locales respondieron a ping; servicios Windows `MongoDB` y `Redis` estaban en ejecución.
- La prueba real creó una base MongoDB aleatoria y una clave Redis aislada; verificó escritura, índice único, reconstrucción del repositorio y lectura permanente.
- Se guardó el listado durante 30 segundos reales. Tras 2,1 segundos la consulta provino de Redis y no renovó el TTL; después de 30,6 segundos la clave expiró, MongoDB conservó el enlace y se inició un nuevo ciclo al consultar.
- Chrome creó un enlace desde React usando la API real y comprobó su copia al portapapeles.
- La URL corta devolvió 302 con `Location` correcto y un código inexistente devolvió 404. La prueba no siguió la redirección al sitio externo.
- Recargar la página redujo el TTL en lugar de reiniciarlo. Al expirar, la fila permaneció visible y la URL corta continuó respondiendo 302. Actualizar recuperó el listado desde MongoDB.
- La vista móvil no desbordó horizontalmente la página; la tabla dispone de desplazamiento horizontal interno para sus cuatro columnas.
- Se detuvo y arrancó de nuevo el proceso Spring Boot del proyecto y se comprobó que el registro persistía y la redirección seguía funcionando.

## Capturas guardadas

- [Crear y copiar · escritorio](docs/capturas/create-desktop.png)
- [Listado con TTL restante · escritorio](docs/capturas/list-desktop.png)
- [Listado · móvil](docs/capturas/list-mobile.png)

## Alcance y observaciones

Java 21.0.12.1, Node 22.14.0, npm 10.9.2, Spring Boot 3.5.16 y Maven 3.9.9. Los dos fallos iniciales detectados por las pruebas (caché vacía en memoria y precisión temporal BSON) se corrigieron antes de la ejecución final satisfactoria.

No se reiniciaron los servicios compartidos de MongoDB o Redis. La independencia de la caché se verificó con la expiración real de Redis y con un reinicio real del backend. No se declara haber comprobado un reinicio de los servidores de base de datos ni una caída de alimentación. La persistencia ante estos eventos depende también de la instalación y configuración del servicio MongoDB.

La instalación directa de node_modules en J: falló con errores de escritura de Google Drive. Se resolvió compilando y ejecutando el frontend en una copia temporal local mediante `start-frontend.ps1`; los fuentes y el lockfile están en el proyecto. El script evita reinstalar dependencias cuando el lockfile no cambió. Si cambia, hay que detener Vite antes de reinstalar para no bloquear esbuild.exe.

El test de navegador dejó un enlace de demostración hacia `https://example.com/parcial?...` en la base de desarrollo. Las bases y claves temporales de la prueba de integración se limpiaron al finalizar.

No se recibieron imágenes de los bocetos. Se implementó la distribución descrita en el enunciado y se verificó visualmente la interfaz generada.