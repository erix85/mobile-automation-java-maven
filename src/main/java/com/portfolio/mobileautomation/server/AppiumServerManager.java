package com.portfolio.mobileautomation.server;

import com.portfolio.mobileautomation.config.FrameworkConfig;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.appium.java_client.service.local.AppiumDriverLocalService.buildService;

public final class AppiumServerManager {

    private static final Logger logger = LoggerFactory.getLogger(AppiumServerManager.class);
    private static AppiumDriverLocalService service;
    private static final FrameworkConfig CONFIG = ConfigFactory.create(FrameworkConfig.class);

    private AppiumServerManager() {
        // Private constructor to prevent instantiation
    }

    public static void startAppiumServer() {
        if (service == null || !service.isRunning()) {
            if (!isPortAvailable(CONFIG.appiumPort())) {
                logger.warn("El puerto {} ya está en uso. Intentando liberar el puerto antes de iniciar Appium Server...", CONFIG.appiumPort());
                killProcessOnPort(CONFIG.appiumPort());
                // Give a small delay for the port to actually free up
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupción al esperar la liberación del puerto", e);
                }

                int retryCount = 0;
                int maxRetries = 5;
                while (!isPortAvailable(CONFIG.appiumPort()) && retryCount < maxRetries) {
                    logger.warn("El puerto {} sigue en uso después de intentar liberarlo. Reintentando en 1 segundo...", CONFIG.appiumPort());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.error("Interrupción al esperar la liberación del puerto", e);
                    }
                    retryCount++;
                }

                if (!isPortAvailable(CONFIG.appiumPort())) {
                    logger.error("El puerto {} no se pudo liberar después de {} intentos. No se puede iniciar el servidor de Appium.", CONFIG.appiumPort(), maxRetries);
                    throw new RuntimeException("No se pudo liberar el puerto " + CONFIG.appiumPort() + " para iniciar el servidor de Appium.");
                }
            }

            logger.info("Iniciando Appium server...");
            AppiumServiceBuilder builder = new AppiumServiceBuilder()
                    .withIPAddress(CONFIG.appiumIp())
                    .usingPort(CONFIG.appiumPort())
                    .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
                    .withArgument(GeneralServerFlag.LOG_LEVEL, "info")
                    .withAppiumJS(new File(System.getProperty("user.home") + "/AppData/Roaming/npm/node_modules/appium/build/lib/main.js")); // Ajusta esta ruta si Appium está instalado en otro lugar

            try {
                service = buildService(builder);
                service.start();
                logger.info("Appium server iniciado en {}", service.getUrl());
            } catch (Exception e) {
                logger.error("Fallo al iniciar el servidor de Appium. Asegúrate de que Node.js y Appium estén correctamente instalados y las rutas sean correctas.", e);
                throw new RuntimeException("No se pudo iniciar el servidor de Appium.", e);
            }
        } else {
            logger.info("Appium server ya está corriendo en {}", service.getUrl());
        }
    }

    public static void stopAppiumServer() {
        if (service != null && service.isRunning()) {
            logger.info("Deteniendo Appium server...");
            service.stop();
            logger.info("Appium server detenido.");
        } else {
            logger.info("Appium server no está corriendo.");
        }
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Optional: set reuse address to true for faster port reuse
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void killProcessOnPort(int port) {
        String os = System.getProperty("os.name").toLowerCase();
        String command;
        String pid = findPidOnPort(port);

        if (pid.isEmpty()) {
            logger.info("No se encontró ningún proceso ocupando el puerto {}.", port);
            return;
        }

        logger.info("Intentando terminar el proceso con PID {} en el puerto {}.", pid, port);

        if (os.contains("win")) {
            command = "taskkill /PID " + pid + " /F";
        } else if (os.contains("nix") || os.contains("mac")) {
            command = "kill -9 " + pid;
        } else {
            logger.warn("Sistema operativo no soportado para matar procesos en puerto: {}", os);
            return;
        }

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            if (process.exitValue() == 0) {
                logger.info("Proceso con PID {} en puerto {} terminado exitosamente.", pid, port);
            } else {
                logger.warn("Fallo al terminar el proceso con PID {} en puerto {}. Código de salida: {}", pid, port, process.exitValue());
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.warn("  Error: {}", line);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Excepción al intentar terminar el proceso en el puerto {}: {}", port, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private static String findPidOnPort(int port) {
        String os = System.getProperty("os.name").toLowerCase();
        String pid = "";

        try {
            Process process;
            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("netstat -ano");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    Pattern pattern = Pattern.compile(":\\s*" + port + "\\s+.*?LISTENING\\s+(\\d+)");
                    while ((line = reader.readLine()) != null) {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            pid = matcher.group(1);
                            logger.debug("Found PID {} for port {} using netstat.", pid, port);
                            break;
                        }
                    }
                }
            } else if (os.contains("nix") || os.contains("mac")) {
                // Use lsof -t to get the PID directly
                process = Runtime.getRuntime().exec("lsof -t -i :" + port);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    pid = reader.readLine(); // lsof -t outputs PID on a single line
                    if (pid != null) {
                        pid = pid.trim();
                        logger.debug("Found PID {} for port {} using lsof.", pid, port);
                    }
                }
            } else {
                logger.warn("Sistema operativo no soportado para encontrar PID en puerto: {}", os);
            }
        } catch (IOException e) {
            logger.error("Excepción al ejecutar comando para encontrar PID en puerto {}: {}", port, e.getMessage());
        }

        return pid;
    }
}