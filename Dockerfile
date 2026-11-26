FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY Main.java .
RUN javac Main.java
EXPOSE 8000
CMD ["java", "Main"]
