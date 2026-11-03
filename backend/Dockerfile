# ---------- Fase 1: build ----------
# Imagem com JDK completo, usada só para compilar. Ela não vai para a imagem final.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia primeiro só o wrapper e o pom.xml: assim a camada das dependências
# fica em cache e só é baixada de novo quando o pom.xml mudar.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Agora o código-fonte. Mudar o código não invalida o cache das dependências.
COPY src/ src/

# Os testes não rodam aqui: os de integração usam Testcontainers e precisam
# de Docker dentro do build. Quem roda os testes é o CI, a cada PR.
RUN ./mvnw -B -q package -DskipTests

# ---------- Fase 2: execução ----------
# Só o JRE: a imagem fica menor e sem compilador.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuário sem privilégios: se a aplicação for invadida, o invasor não é root.
RUN addgroup -S app && adduser -S app -G app

# O Spring Boot gera um único .jar executável (o original vira .jar.original).
COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8080

# MaxRAMPercentage: a JVM respeita o limite de memória do container, em vez de olhar a RAM da máquina.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
