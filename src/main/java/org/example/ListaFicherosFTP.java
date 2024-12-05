package org.example;

import java.io.*;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTP;

public class ListaFicherosFTP {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("ERROR: indicar como parámetros:");
            System.out.println("servidor usuario(opcional) contraseña(opcional)");
            System.exit(1);
        }
        String servidorFTP = args[0];

        String usuario = "anonymous", password = "";
        if (args.length >= 2) {
            usuario = args[1];
        }
        if (args.length >= 3) {
            password = args[2];
        }

        FTPClient clienteFTP = new FTPClient();

        try {
            clienteFTP.connect(servidorFTP);
            int codResp = clienteFTP.getReplyCode();
            if (!FTPReply.isPositiveCompletion(codResp)) {
                System.out.printf("ERROR: Conexión rechazada con código de respuesta %d.\n", codResp);
                System.exit(2);
            }

            clienteFTP.enterLocalPassiveMode();
            clienteFTP.setFileType(FTP.BINARY_FILE_TYPE);

            if (usuario != null && password != null) {
                boolean loginOK = clienteFTP.login(usuario, password);
                if (loginOK) {
                    System.out.printf("INFO: Login con usuario %s realizado.\n", usuario);
                }
                else {
                    System.out.printf("ERROR: Login con usuario %s rechazado.\n", usuario);
                    return;
                }
            }

            System.out.printf("INFO: Conexión establecida, mensaje de bienvenida del servidor:\n====\n%s\n====\n", clienteFTP.getReplyString());
            System.out.println("INFO: Intentando acceder al directorio 'desdeJava'...");

            if (!clienteFTP.changeWorkingDirectory("/desdeJava")){
                System.out.println("INFO: El directorio no existe, creando directorio...");
                clienteFTP.makeDirectory("/desdeJava");
                System.out.println("INFO: ¡Directorio creado!");
                clienteFTP.changeWorkingDirectory("/desdeJava");
            }

            try (FileInputStream fis = new FileInputStream("./src/main/java/org/example/desdeElPrograma.txt")) {
                if (clienteFTP.storeFile("desdeElPrograma.txt", fis)) {
                    System.out.println("INFO: Archivo subido correctamente.");
                } else {
                    System.out.println("ERROR: No se pudo subir el archivo.");
                }
            } catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
                e.getCause();
            }

            ListarElementos(clienteFTP);
            System.out.println();
            System.out.println("Contenido de 'desdeElPrograma.txt': ");
            try (InputStream inputStream = clienteFTP.retrieveFileStream("/desdeJava/desdeElPrograma.txt");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
                e.getCause();
            }
            System.out.println();

            if (!clienteFTP.changeWorkingDirectory("/")) {
                System.out.println("INFO: No se pudo cambiar al directorio raíz, intentamos con otro directorio...");
                // Intentamos cambiar al directorio home del usuario
                if (!clienteFTP.changeWorkingDirectory("~")) {
                    System.out.println("ERROR: No se pudo cambiar al directorio home o a otro directorio.");
                }

            }
            ListarElementos(clienteFTP);


        } catch (IOException e) {
            System.out.println("ERROR: conectando al servidor");
            e.getCause();
        } finally {
            try {
                clienteFTP.disconnect();
                System.out.println("INFO: conexión cerrada.");
            } catch (IOException e) {
                System.out.println("AVISO: no se pudo cerrar la conexión.");
            }
        }
    }

    private static void ListarElementos(FTPClient clienteFTP) throws IOException {
        FTPFile[] fichServ = clienteFTP.listFiles();
        int contador = 0;
        System.out.printf("INFO: Directorio actual en servidor: %s, contenidos: \n", clienteFTP.printWorkingDirectory());
        for(FTPFile f: fichServ) {
            String infoAdicFich = "";
            if(f.getType() == FTPFile.DIRECTORY_TYPE) {
                infoAdicFich = "/";
            }
            else if(f.getType() == FTPFile.SYMBOLIC_LINK_TYPE) {
                infoAdicFich = " -> " + f.getLink();
            }
            contador++;
            System.out.printf("%s %s%s\n", contador, f.getName(), infoAdicFich);

        }
    }
}