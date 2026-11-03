 
JAVA TRAINING PROGRAM · TALLER PRÁCTICO
Conceptos CI/CD, DevOps y Pipelines
Caso CambioExpress: construir el pipeline de una API de tipo de cambio

Objetivo	Construir el pipeline de la API de CambioExpress: integración continua en GitHub Actions, testing continuo con un quality gate de cobertura, un Jenkinsfile al estilo OCD y un workflow de despliegue por ambientes al estilo Deploy Go.
Duración	75 a 90 minutos
Nivel	Introductorio
Requisitos	JDK 17 o superior, Maven 3.9+ y un editor (IntelliJ IDEA o VS Code). No necesitas Docker, Jenkins ni una cuenta de GitHub.
Entregable	<apellido>_cambioexpress.zip con el proyecto completo, sin la carpeta target/
Evaluación	Validador automático, 20 puntos (4 requerimientos × 5 puntos)
1. Contexto del caso
CambioExpress es una casa de cambio digital: publica el tipo de cambio del día y cotiza operaciones de compra y venta de dólares y euros. La API ya funciona y tiene pruebas, pero cada versión se compila y se instala a mano. El equipo va a adoptar el modelo de entrega del banco: integración continua en GitHub Actions, pruebas automáticas con un quality gate de cobertura, un Jenkinsfile para los jobs que ejecuta OCD y un workflow de despliegue por ambientes al estilo Deploy Go. Tu tarea es construir ese pipeline.
Método	URL	Qué hace
GET	/api/tipos-cambio	Tipos de cambio del día (USD y EUR)
GET	/api/tipos-cambio/{moneda}	Tipo de cambio de una moneda; 404 si no existe
POST	/api/cotizaciones	Cotiza una operación: 201 con el monto en soles
GET	/api/cotizaciones/{id}	Consulta una cotización; 404 si no existe

Operación	Qué tipo de cambio se aplica
COMPRA	El cliente compra la moneda extranjera y paga en soles al tipo de venta de la casa (USD: 3.786).
VENTA	El cliente vende la moneda extranjera y recibe soles al tipo de compra de la casa (USD: 3.712).

El proyecto base ya trae la API, sus pruebas y el esqueleto del pipeline. Las marcas TODO RN-x indican qué completar:
cambioexpress-base/
|-- Jenkinsfile                      TODO RN-3 (solo trae la etapa Compilar)
|-- .github/workflows/ci.yml         TODO RN-1 y RN-2 (solo trae el nombre)
|-- .github/workflows/cicd.yml       TODO RN-4 (solo trae el nombre)
|-- devops/desplegar.sh              despliegue simulado (ya está listo)
|-- pom.xml                          TODO RN-2 (JaCoCo) y TODO RN-4 (versión)
`-- src/
    |-- main/java/com/nttdata/cambioexpress/   API hexagonal completa
    `-- test/java/com/nttdata/cambioexpress/
        |-- domain/model/TipoCambioTest.java   TODO RN-2 (clase vacía)
        |-- application/service/CambioServiceTest.java
        |-- infrastructure/adapter/in/rest/CambioControllerIT.java
        `-- Validador.java                     califica el taller (no modificar)
2. Preparación
1.	Descomprime cambioexpress-base.zip en una ruta corta, por ejemplo C:\talleres\cambioexpress-base, y ábrelo en tu editor.
2.	Ejecuta mvn clean verify: corren 3 pruebas unitarias (Surefire) y 8 de integración (Failsafe), y termina en BUILD SUCCESS.
3.	Ejecuta el Validador: mvn -q test -Dtest=Validador. El proyecto base parte con 1 / 20.
4.	Opcional: levanta la API con mvn spring-boot:run y cotiza en Postman para conocer el negocio.

POST http://localhost:8080/api/cotizaciones
Content-Type: application/json
{ "moneda": "USD", "operacion": "COMPRA", "monto": 100 }
 
-> 201 Created      Location: /api/cotizaciones/C-0001
{ "id": "C-0001", "moneda": "USD", "operacion": "COMPRA", "monto": 100.00,
  "tipoCambio": 3.786, "montoSoles": 378.60 }

Cómo vas a ver tu avance
▪  El Validador lee tus archivos (YAML, Jenkinsfile y pom.xml) y ejecuta tus pruebas de TipoCambioTest. No necesita Jenkins ni GitHub.
▪  mvn clean verify en tu máquina hace lo mismo que el job de CI: si pasa en local, el pipeline tiene lo que necesita.
▪  Opcional: si subes el proyecto a un repositorio personal de prueba en GitHub, verás los workflows correr en la pestaña Actions. Nunca subas código de un cliente.
3. Requerimientos
Los nombres de archivos, jobs, etapas, ambientes, variables y credenciales se dan exactos: el Validador los busca tal cual.
RN-1 · Integración continua en GitHub Actions
Descripción. Completa .github/workflows/ci.yml para que cada cambio se compile y se pruebe automáticamente, con la convención de ramas del equipo: ramas feature creadas desde master.
Criterios de aceptación
▪	Archivo .github/workflows/ci.yml con name: ci pipeline.
▪	Se dispara con push a las ramas feature/** y con pull_request hacia master.
▪	Permisos mínimos: permissions con contents: read; ningún permiso write.
▪	Un job en ubuntu-latest con timeout-minutes entre 1 y 30.
▪	Pasos: actions/checkout, actions/setup-java con distribution: temurin, java-version: '17' y cache: maven, y un paso que ejecute mvn -B -ntp verify.
▪	Prohibido saltar pruebas: nada de -DskipTests, -Dmaven.test.skip ni -DskipITs.

Comprobación
mvn -q test -Dtest=Validador
 
RN-1  Integracion continua: ci.yml
  [OK] RN-1a  Se dispara con push a feature/** y PR a master       +1
  ...
  [OK] RN-1e  timeout-minutes en cada job (1 a 30)                 +1
 
Opcional (repositorio de prueba): git push -u origin feature/CX-101
-> Actions: "ci pipeline" en verde

RN-2 · Testing continuo: gate de cobertura, reportes y pruebas del dominio
Descripción. Agrega el quality gate de cobertura, publica los reportes de pruebas en cada ejecución y escribe las pruebas unitarias de la regla de negocio principal: TipoCambio.convertir.
Criterios de aceptación
▪	En pom.xml: jacoco-maven-plugin (versión 0.8.12) con los goals prepare-agent, report (fase verify) y check.
▪	Regla del check: counter LINE, value COVEREDRATIO y minimum 0.80 o más.
▪	En ci.yml: un paso actions/upload-artifact que publique target/surefire-reports/ (puedes sumar target/failsafe-reports/ y target/site/jacoco/) con if: always(), para que los reportes se publiquen aunque las pruebas fallen.
▪	En src/test/java/com/nttdata/cambioexpress/domain/model/TipoCambioTest.java: al menos 4 métodos @Test, todos en verde, con los casos obligatorios de la tabla.

Casos de TipoCambioTest (USD: compra 3.712, venta 3.786)
#	Caso	Datos	Resultado esperado
1	Compra de dólares usa el tipo de venta	COMPRA, 100	378.60
2	Venta de dólares usa el tipo de compra	VENTA, 100	371.20
3	Redondea a 2 decimales	COMPRA, 10.55	39.94 (escala 2)
4	Monto sobre el límite	COMPRA, 10000.01	ReglaDeNegocioException: «El monto supera el límite de 10000.00 por operación»
5	Recomendado: monto en cero	VENTA, 0	ReglaDeNegocioException: «El monto debe ser mayor que cero»
6	Recomendado: datos inválidos al construir	moneda "usd", compra 0, o venta igual o menor que compra	IllegalArgumentException al construir TipoCambio

Comprobación
mvn clean verify
[INFO] Tests run: 7 (o mas), Failures: 0, Errors: 0, Skipped: 0   (Surefire)
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0          (Failsafe)
[INFO] All coverage checks have been met.
[INFO] BUILD SUCCESS
 
Reporte: target/site/jacoco/index.html
Clase TipoCambio: 67 % de lineas sin tus pruebas -> 100 % con los 6 casos

RN-3 · Jenkinsfile al estilo OCD
Descripción. En el modelo OCD, el pase a certificación y producción ejecuta jobs de Jenkins. Completa el Jenkinsfile con el pipeline declarativo que usaría ese job.
Criterios de aceptación
▪	Pipeline declarativo: pipeline { agent ... } (ya está en la base).
▪	Etapas con estos nombres exactos y en este orden: Compilar, Pruebas y cobertura, Publicar artefacto, Desplegar a cert.
▪	Pruebas y cobertura ejecuta mvn -B -ntp verify, sin saltar pruebas, y publica los reportes JUnit (target/*-reports/*.xml) en un bloque post que corre siempre (always).
▪	options con un timeout (por ejemplo, 20 minutos).
▪	Publicar artefacto archiva el jar target/cambioexpress-*.jar.
▪	Desplegar a cert corre solo en la rama master y ejecuta bash devops/desplegar.sh cert target/cambioexpress-*.jar con el token leído de Jenkins: withCredentials con la credencial de ID cambioexpress-deploy-token en la variable DEPLOY_TOKEN.
▪	Ningún token ni contraseña escrito en el archivo.

Comprobación
mvn -q test -Dtest=Validador
 
RN-3  Jenkinsfile al estilo OCD
  [OK] RN-3a  Jenkinsfile declarativo: pipeline { agent ... }      +1
  ...
  [OK] RN-3e  Desplegar solo en master y con withCredentials       +1
 
Con Jenkins (opcional, job Multibranch Pipeline):
Compilar -> Pruebas y cobertura -> Publicar artefacto -> Desplegar a cert

RN-4 · Despliegue continuo al estilo Deploy Go
Descripción. Completa .github/workflows/cicd.yml para construir el jar una sola vez y desplegar ese mismo jar en dev y luego en cert, con las convenciones de Deploy Go.
Criterios de aceptación
▪	name: cicd pipeline; se ejecuta a mano (workflow_dispatch) y con push a master.
▪	Job build: ejecuta mvn -B -ntp verify y sube el jar con actions/upload-artifact (artefacto cambioexpress-jar, ruta target/cambioexpress-*.jar).
▪	Job deploy-dev: needs: build y environment: dev.
▪	Job deploy-cert: needs: deploy-dev, environment: cert y solo en master: if: github.ref == 'refs/heads/master'.
▪	Los jobs de despliegue descargan el jar con actions/download-artifact y ejecutan bash devops/desplegar.sh <ambiente> dist/cambioexpress-*.jar. No vuelven a compilar.
▪	El token llega en la variable DEPLOY_TOKEN desde ${{ secrets.DEPLOY_TOKEN }}. Ningún secreto escrito en ci.yml ni en cicd.yml.
▪	Convención Deploy Go: en pom.xml, versión fija 1.0.0, sin -SNAPSHOT (cada versión se marca con un tag vX.Y.Z).

Comprobación
mvn -q test -Dtest=Validador
 
RN-4  CD al estilo Deploy Go: cicd.yml
  [OK] RN-4a  Job build: mvn verify y sube el .jar                 +1
  ...
  [OK] RN-4e  pom.xml con version X.Y.Z, sin -SNAPSHOT             +1
 
Opcional (repositorio de prueba, environments dev y cert):
Actions > cicd pipeline > Run workflow
build OK -> deploy-dev OK -> deploy-cert esperando aprobacion

4. Cómo se evalúa
Requerimiento	Puntos	Qué revisa el Validador
RN-1	5	Disparadores, permisos de solo lectura, Java 17 con caché, mvn -B verify sin saltar pruebas y timeout.
RN-2	5	JaCoCo con prepare-agent y check de 80 % de líneas, reportes publicados con if: always() y TipoCambioTest con 4 pruebas o más en verde.
RN-3	5	Pipeline declarativo, las 4 etapas en orden, mvn verify con reportes JUnit siempre, timeout y despliegue solo en master con withCredentials.
RN-4	5	Build que sube el jar, deploy-cert con needs y solo master, despliegues que descargan y no recompilan, token por secrets y versión sin -SNAPSHOT.

Ejecútalo desde la carpeta del proyecto con mvn -q test -Dtest=Validador. Cada línea suma 1 punto; si falta algo, la línea indica qué:
RN-3  Jenkinsfile al estilo OCD
  [OK] RN-3a  Jenkinsfile declarativo: pipeline { agent ... }      +1
  [--] RN-3b  Etapas Compilar > Pruebas > Publicar > Desplegar      0  (falta stage('Pruebas y cobertura'))
  [--] RN-3d  options { timeout(...) }                              0  (falta timeout(...) dentro de options { })
  ...
PUNTAJE: 1 / 20
5. Entrega
▪	Comprime la carpeta del proyecto como <apellido>_cambioexpress.zip sin la carpeta target/.
▪	Revisa que el zip incluya la carpeta .github/ (en algunos sistemas las carpetas que empiezan con punto quedan ocultas).
▪	Antes de entregar: mvn clean verify en verde y el Validador con tu puntaje final.

Pistas
▪  En GitHub Actions las ramas se filtran dentro de cada evento: on > push > branches.
▪  Un paso con if: always() corre aunque los pasos anteriores hayan fallado.
▪  Cada job corre en una máquina limpia: el job de despliegue necesita actions/checkout para tener devops/desplegar.sh y actions/download-artifact para tener el jar.
▪  El check de JaCoCo corre en la fase verify: mvn test no lo ejecuta.
▪  En el Jenkinsfile, when va dentro de la etapa, antes de steps. YAML y Groovy son sensibles a llaves e indentación: usa 2 espacios en YAML, nunca tabs.

