import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class FullClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private String currentRoom = "#general";
    private RoomManager roomManager;

    public FullClientHandler(Socket socket, RoomManager roomManager) {
        this.socket = socket;
        this.roomManager = roomManager;
    }

    @Override
    public void run() {
        try {
            // Configurar timeout de lectura (30 segundos)[cite: 7]
            socket.setSoTimeout(30000);
            
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("200 OK - Bienvenido al JSMS. Usa CONNECT <user>");

            String rawMessage;
            while ((rawMessage = in.readLine()) != null) {
                Command cmd = ProtocolParser.parse(rawMessage);

                switch (cmd.getTipo()) {
                    case "CONNECT":
                        this.username = cmd.getPayload();
                        roomManager.unirseASala(currentRoom, this);
                        out.println("200 OK CONNECTED as " + username);
                        break;
                    case "JOIN":
                        this.currentRoom = cmd.getPayload();
                        roomManager.unirseASala(currentRoom, this);
                        out.println("JOINED " + currentRoom);
                        break;
                    case "MSG":
                        roomManager.broadcast(currentRoom, "MSG " + username + ": " + cmd.getPayload(), this);
                        out.println("MSG " + username + ": " + cmd.getPayload());
                        break;
                    case "QUIT":
                        out.println("BYE");
                        return;
                    default:
                        out.println("401 ERROR: " + cmd.getPayload());
                        break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout de socket alcanzado para cliente inactivo.");
        } catch (SocketException e) {
            System.err.println("Conexión resuelta o interrumpida abruptamente con el cliente.");
        } catch (IOException e) {
            System.err.println("Error de I/O en la sesión: " + e.getMessage());
        } finally {
            roomManager.removerDeSalas(this);
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar socket de forma limpia: " + e.getMessage());
            }
        }
    }

    public void enviarMensajeDirecto(String mensaje) {
        if (out != null) {
            out.println(mensaje);
        }
    }
}