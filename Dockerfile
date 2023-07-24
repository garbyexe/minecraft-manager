FROM eclipse-temurin:17-jre-alpine
LABEL authors="michael"

WORKDIR "/opt/minecraft"

EXPOSE 25565
CMD ["java", "-jar", "/opt/minecraft/minecraft.jar"]

USER 1000