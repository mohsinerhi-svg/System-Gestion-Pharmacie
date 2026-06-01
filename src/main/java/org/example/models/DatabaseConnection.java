package org.example.models;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe gérant la connexion à la base de données PostgreSQL (Pattern Singleton avec .env)
 */
public class DatabaseConnection {

    private static Connection connection = null;

    /**
     * Méthode pour obtenir la connexion active.
     * @return L'objet Connection
     */
    public static Connection getConnection() {
        try {
            // S'il n'y a pas de connexion ou si la connexion existante a été fermée
            if (connection == null || connection.isClosed()) {
                Dotenv dotenv = Dotenv.load();
                String url = dotenv.get("DB_URL");
                String user = dotenv.get("DB_USER");
                String password = dotenv.get("DB_PASSWORD");

                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Connexion à PostgreSQL réussie de manière sécurisée (.env) !");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur : Driver PostgreSQL introuvable.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur : Impossible de se connecter à la base de données.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur : Fichier .env introuvable ou mal configuré.");
            e.printStackTrace();
        }
        return connection;
    }
}