public class Command {
    private String tipo;
    private String payload;

    public Command(String tipo, String payload) {
        this.tipo = tipo;
        this.payload = payload;
    }

    public String getTipo() { return tipo; }
    public String getPayload() { return payload; }
}

public class ProtocolParser {
    public static Command parse(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return new Command("ERROR", "Comando vacío");
        }

        String[] partes = rawInput.trim().split(" ", 2);
        String comando = partes[0].toUpperCase();
        String argumento = partes.length > 1 ? partes[1].trim() : "";

        switch (comando) {
            case "CONNECT":
            case "JOIN":
            case "MSG":
            case "QUIT":
                return new Command(comando, argumento);
            default:
                return new Command("ERROR", "Comando desconocido: " + comando);
        }
    }
}