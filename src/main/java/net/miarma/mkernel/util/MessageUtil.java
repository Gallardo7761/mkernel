package net.miarma.mkernel.util;

import net.miarma.mkernel.config.providers.MessageProvider;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    // Método para parsear placeholders
    public static String parsePlaceholders(String message, List<String> placeholders, List<String> values) {
        for (int i = 0; i < placeholders.size(); i++) {
            message = message.replace(placeholders.get(i), values.get(i));
        }
        return message;
    }

    // Método para convertir códigos de colores y parsear HEX
    public static String parseColors(String message) {
        return parseHexColors(message).replace('&', '§');
    }

    // Parser para códigos HEX
    public static String parseHexColors(String input) {
        String hexPattern = "&#[0-9A-Fa-f]{6}";
        Matcher matcher = Pattern.compile(hexPattern).matcher(input);
        while (matcher.find()) {
            String hexColor = matcher.group();
            input = input.replace(hexColor, convertHexToMinecraftColor(hexColor));
        }
        return input;
    }

    // Conversión de HEX a formato de color de Minecraft
    private static String convertHexToMinecraftColor(String hexColor) {
        StringBuilder minecraftColor = new StringBuilder("&x");
        for (char c : hexColor.substring(2).toCharArray()) {
            minecraftColor.append("&").append(c);
        }
        return minecraftColor.toString();
    }

    // Formateo básico de mensajes sin colores
    public static String formatMessageNoColors(String message, boolean prefix) {
        if (prefix) {
            message = addPrefix(message);
        }
        return message;
    }

    // Formateo básico de mensajes para consola
    public static String formatMessageConsole(String message, boolean removePrefix) {
        if (removePrefix) {
            message = message.replace("[P]", "");
        }
        return message;
    }

    // Formateo básico de mensajes con placeholders
    public static String formatMessageNoColors(String message, boolean prefix, List<String> placeholders, List<String> values) {
        message = formatMessageNoColors(message, prefix);
        return parsePlaceholders(message, placeholders, values);
    }

    // Formateo con colores y prefijo
    public static String formatMessage(String message, boolean prefix) {
        if (prefix) {
            message = addPrefix(message);
        }
        return parseColors(message);
    }

    // Formateo con colores, prefijo y placeholders
    public static String formatMessage(String message, boolean prefix, List<String> placeholders, List<String> values) {
        message = formatMessage(message, prefix);
        return parsePlaceholders(message, placeholders, values);
    }

    // Método para añadir el prefijo
    private static String addPrefix(String message) {
        return message.replace("[P]", MessageProvider.getPrefix());
    }

    // Envío de mensajes
    public static void sendMessage(CommandSender sender, String message, boolean prefix) {
        sender.sendMessage(formatMessage(message, prefix));
    }

    // Envío de mensajes con placeholders
    public static void sendMessage(CommandSender sender, String message, boolean prefix, List<String> placeholders, List<String> values) {
        sender.sendMessage(formatMessage(message, prefix, placeholders, values));
    }
}
