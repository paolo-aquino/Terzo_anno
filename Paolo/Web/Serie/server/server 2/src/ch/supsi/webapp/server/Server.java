package ch.supsi.webapp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;

public class Server {

	private static ServerSocket serverSocket;
	private final static int PORT = 8080;
	private final static String CONTENT_LENGTH_HEADER = "Content-Length";
	private final static String LINEBREAK = "\r\n";
	private final static String DESKTOP_PATH = System.getProperty("user.home") + File.separator + "Desktop";

	public static void main(String[] args) throws Exception {
		serverSocket = new ServerSocket(PORT);
		System.out.println("Server avviato sulla porta : " + PORT);
		System.out.println("-------------------------------------");

		while (true) {
			Socket clientSocket = serverSocket.accept();
			clientSocket.setSoTimeout(200);
			handleRequest(clientSocket);
			clientSocket.close();
		}
	}

	public static void handleRequest(Socket socket) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			OutputStream out = socket.getOutputStream();

			Request request = readRequest(in);

			//here we have to check if the static resource has been called, and therefore provide it
			if (request != null){
				System.out.println(request.allRequest);

				if (request.resource.equals("/test.html")){
					System.out.println("trying to load test.html");
					serveStaticFile(out, "test.html");
				}else {
					Content responseBody = handleResponseContent(request);
					produceResponse(out, responseBody);
				}
			}

			out.flush();
			out.close();
			in.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	private static Request readRequest(BufferedReader input) throws IOException {
		String firstline = input.readLine();
		if (firstline != null) {
			System.out.println("----------------- " + new Date() + " --------------");
			boolean isPost = firstline.startsWith("POST");
			return getRequest(input, firstline, isPost);
		}
		return null;
	}

	private static Request getRequest(BufferedReader input, String line, boolean isPost) throws NumberFormatException, IOException {
		StringBuilder rawRequest = new StringBuilder();
		rawRequest.append(line);
		String resource = line.substring(line.indexOf(' ')+1, line.lastIndexOf(' '));
		int contentLength = 0;
		while (!(line = input.readLine()).equals("")) {
			rawRequest.append('\n' + line);
			if (line.startsWith(CONTENT_LENGTH_HEADER))
				contentLength = Integer.parseInt(line.substring(CONTENT_LENGTH_HEADER.length()+2));
		}
		String body = "";
		if (isPost) {
			body = getBody(input, contentLength);
			rawRequest.append("\n\n" + body);
		}
		return new Request(rawRequest.toString(), resource, body, isPost);
	}

	private static String getBody(BufferedReader bf, int length) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++)
			sb.append((char) bf.read());
		return sb.toString();
	}

	/*
	 * Usare questo metodo per gestire la richiesta ricevuta e produrre un 
	 * contenuto (txt, html, ...) da dare come corpo nella risposta
	 * 
	 */
	private static Content handleResponseContent(Request request) {
		String html = "<!DOCTYPE html>" + LINEBREAK
				+ "<html>" + LINEBREAK
				+ "<head>" + LINEBREAK
				+ "<meta charset=\"UTF-8\">" + LINEBREAK
				+ "<title>Trial</title>" + LINEBREAK
				+ "</head>" + LINEBREAK
				+ "<body>" + LINEBREAK
				+ "My first HTML5 document" + LINEBREAK
				+ "<br>" + LINEBREAK
				+ LocalDateTime.now().toString() + LINEBREAK
				+ "<p>What is your name ?</p>" + LINEBREAK
				+ "<form method=\"POST\">" + LINEBREAK
				+ "<input name=\"name\" type=\"text\" />" + LINEBREAK
				+ "<input type=\"submit\"/>" + LINEBREAK
				+ "</form>" + LINEBREAK
				+ "</body>" + LINEBREAK
				+ "</html>" + LINEBREAK;

		return new Content(html.getBytes());
	}

	private static void serveStaticFile(OutputStream output, String fileName) throws IOException{
		String filePath = DESKTOP_PATH + File.separator + fileName;
		File file = new File(filePath);

		if (file.exists()){
			byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

			//Inizializzazione degli header
			String HTTP_version = "HTTP/1.1 ";
			String status_code = "200 OK";
			String server = "Server: localhost:8080";
			String content_type = "Content-Type: text/html";
			String content_length = "Content-Length: " + fileContent.length;
			char newLine = '\n';

			output.write((HTTP_version + status_code + newLine + server + newLine + content_type + newLine + content_length + newLine + LINEBREAK).getBytes());
			output.write(fileContent);
		} else {
			//no file Has been found, therefore we need to return a 404 not found
			//Inizializzazione degli header
			String HTTP_version = "HTTP/1.1 ";
			String status_code = "404 Not Found";
			String server = "Server: localhost:8080";
			String content_type = "Content-Type: text/html";
			String content_length = "Content-Length: 0";
			char newLine = '\n';

			output.write((HTTP_version + status_code + newLine + server + newLine + content_type + newLine + content_length + newLine + LINEBREAK).getBytes());
		}
	}

	/*
	 * Usare questo metodo per scrivere l'intera risposta HTTP (prima linea+headers+body)
	 * 
	 */
	private static void produceResponse(OutputStream output, Content responseContent) throws IOException 
	{
		String statusCode = "HTTP/1.1 200 OK" + LINEBREAK;
		String contentType = "text/html;charset=UTF-8" + LINEBREAK;
		String contentLength = "Content-length: " + responseContent.length + LINEBREAK;

		output.write(statusCode.getBytes());
		output.write(contentType.getBytes());
		output.write(contentLength.getBytes());
		output.write(LINEBREAK.getBytes());

		// usare la variabile LINEBREAK per andare a capo
		output.write(new String(responseContent.content).getBytes());
	}

}