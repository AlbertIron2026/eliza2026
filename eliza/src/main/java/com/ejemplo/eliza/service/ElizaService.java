package com.ejemplo.eliza.service;

import com.ejemplo.eliza.model.Message;
import java.util.Optional;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ElizaService {

    private final Random random = new Random();

    // Reflexión de pronombres: "mi trabajo" -> "tu trabajo"
    private static final Map<String, String> REFLEJOS = Map.of(
            "yo", "tú",
            "mi", "tu",
            "mis", "tus",
            "soy", "eres",
            "estoy", "estás",
            "me", "te");

    private record Rule(Pattern pattern, List<String> responses) {
    }

    private final List<Rule> rules = List.of(
            new Rule(Pattern.compile(".*\\bme llamo (\\w+).*", Pattern.CASE_INSENSITIVE),
                    List.of("Encantada de conocerte, $1. ¿Cómo te sientes hoy?")),
            new Rule(Pattern.compile(".*\\bestoy (.+)", Pattern.CASE_INSENSITIVE),
                    List.of("¿Por qué estás $1?", "¿Desde cuándo estás $1?")),
            new Rule(Pattern.compile(".*\\bme siento (.+)", Pattern.CASE_INSENSITIVE),
                    List.of("¿Qué te hace sentirte $1?", "¿Con qué frecuencia te sientes $1?")),
            new Rule(Pattern.compile(".*\\b(madre|padre|familia|pareja|trabajo)\\b.*", Pattern.CASE_INSENSITIVE),
                    List.of("Háblame más de tu $1.", "¿Cómo es tu relación con tu $1?")),
            new Rule(Pattern.compile(".*\\bporque (.+)", Pattern.CASE_INSENSITIVE),
                    List.of("¿Es esa la verdadera razón?", "¿Qué otras razones se te ocurren?")),
            new Rule(Pattern.compile(".*\\bno puedo (.+)", Pattern.CASE_INSENSITIVE),
                    List.of("¿Por qué crees que no puedes $1?", "¿Qué pasaría si pudieras $1?"))
                );

    private final List<String> defaults = List.of(
            "Cuéntame más.",
            "Entiendo. ¿Y eso cómo te hace sentir?",
            "Continúa, te escucho.");


   public String respond(String input, List<Message> history) {
    for (Rule rule : rules) {
        Matcher m = rule.pattern().matcher(input.trim());
        if (m.matches()) {
            String template = pick(rule.responses());
            return template.replace("$1", reflect(m.group(1)));
        }
    }
    // Ninguna regla coincide: intentamos usar la memoria
    return recall(history).orElse(pick(defaults));
}



    private String pick(List<String> options) {
        return options.get(random.nextInt(options.size()));
    }

private static final Pattern NOMBRE = Pattern.compile("\\bme llamo (\\w+)", Pattern.CASE_INSENSITIVE);
private static final Pattern TEMA = Pattern.compile("\\bmi (madre|padre|familia|pareja|trabajo)\\b", Pattern.CASE_INSENSITIVE);

private Optional<String> recall(List<Message> history) {
    String nombre = null;
    String tema = null;

    for (Message msg : history) {
        if (!"user".equals(msg.getSender())) continue;
        Matcher n = NOMBRE.matcher(msg.getText());
        if (n.find()) nombre = n.group(1);
        Matcher t = TEMA.matcher(msg.getText());
        if (t.find()) tema = t.group(1);
    }

    // Una de cada tres veces usamos la memoria, para no repetirnos
    if (random.nextInt(3) != 0) return Optional.empty();

    if (tema != null) {
        String saludo = nombre != null ? nombre + ", antes" : "Antes";
        return Optional.of(saludo + " mencionaste a tu " + tema + ". ¿Tiene relación con esto?");
    }
    if (nombre != null) {
        return Optional.of("Sigo aquí, " + nombre + ". Continúa.");
    }
    return Optional.empty();
}

    private String reflect(String text) {
        return Arrays.stream(text.split(" "))
                .map(w -> REFLEJOS.getOrDefault(w.toLowerCase(), w))
                .collect(Collectors.joining(" "));
    }
}