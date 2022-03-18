import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class WebServer implements Runnable {

    String defaultPath;
    final String errorFile = "404.html";
    final String indexFile = "index.html";
    int port;
    private Socket connect;
    static Boolean hasShown = false;

    public WebServer(Socket socket, int port, String folder) {
        if (port == 0) {
            System.out.println("Error port cannot be 0 - Selected default 8080");
            this.port = 8080;
        } else {
            this.port = port;
        }
        this.connect = socket;

        this.defaultPath = System.getProperty("user.dir") + folder;
    }

    public static void main(String[] args) {
        // set defaultport as 8080
        int selectedPort = 8080;
        String selectedPath = "";

        try {
            // take args for file

            // user selected port, defaults to 8080 otherwise
            selectedPort = Integer.parseInt(args[0].trim());

        } catch (NumberFormatException e) {
            showInstuctions();
            System.out.println("Error: portnumber not valid - Selected default 8080");

        } catch (ArrayIndexOutOfBoundsException s) {
            showInstuctions();
            System.out.println("Warning: No port selected as arg - Selected default 8080");

        }
        try {
            // This garbage will be foolproof.
            // if user does not inputs forwardslash / add
            String suppliedPath = args[1].trim();
            if (!suppliedPath.startsWith("/")) {
                suppliedPath = "/" + suppliedPath;
            }
            selectedPath += suppliedPath;

            // if path does not end with a forwardslash add it
            if (!selectedPath.endsWith("/")) {
                selectedPath += "/";
            }
            System.out.println(selectedPath);
            // if usersupplied file does not exist.
            if (!new File(System.getProperty("user.dir") + selectedPath).exists()) {
                selectedPath = checkDefaultPath();
            }
            // if user does not supply path input.
        } catch (ArrayIndexOutOfBoundsException s) {
            selectedPath = checkDefaultPath();
        }
        try {
            // TODO close at good place?
            ServerSocket serverCon = new ServerSocket(selectedPort);
            System.out.println("Started server at port: " + selectedPort + "\n");
            while (true) {
                WebServer myServer = new WebServer(serverCon.accept(), selectedPort,
                        selectedPath);

                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Server Connection error: " + e.getMessage());
        }
    }

    static void showInstuctions() {
        if (!hasShown) {
            System.out.println("Usage: WebServer [port] [serving_directory]");
            hasShown = true;
        }
    }

    // checks if default folder public is valid. Sadly a static method.
    // if so then it returns default public.
    private static String checkDefaultPath() {
        showInstuctions();
        System.out.println("Waring: No path selected as arg - Selected default /public/");
        if (!new File(System.getProperty("user.dir") + "/public/").exists()) {
            System.out.println("Error: default rootpath /public/ for webserver could not be found.");
            System.out.println("Did you move the class from the startfolder? \nExiting");
            System.exit(1);
        }
        System.out.println("Selected default path is: /public/");

        return "/public/";
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequest = "";
        Headers header = null;

        try {
            in = new BufferedReader(new InputStreamReader(connect.getInputStream())); // info grabbed from user
            out = new PrintWriter(connect.getOutputStream()); // send data to user
            dataOut = new BufferedOutputStream(connect.getOutputStream()); // send filedata to user
            header = new Headers(out, dataOut, connect);

            StringTokenizer parse = new StringTokenizer(in.readLine()); // read data from sent user
            String method = parse.nextToken().toUpperCase(); // get http method from client
            fileRequest = parse.nextToken().toLowerCase(); // user requested file/folder
            System.out.println("User sent method: " + method);

            System.out.println("user requested: " + fileRequest);
            // if not a get or a head return 500. Doublecheck with postman
            if (!method.equals("GET") && !method.equals("HEAD")) {
                header.returnServerError();

                // if it's a GET or a HEAD
            } else {
                // if user requests root, return index.html
                // will return 404 on folders that do not exist since it becomes
                // ./nofolder/index.html
                if (fileRequest.endsWith("/")) {
                    fileRequest += indexFile;
                } else if (!fileRequest.contains(".")) {
                    fileRequest += "/" + indexFile;
                }

                // 302 Found Redirect test.
                // if the user types in supersecret into url - Redirect to a special website
                // sadly triggers 404 aswell instead of breaking
                if (fileRequest.equals("/supersecret.html")) {
                    header.returnRedirect();
                }
                // if it is a normal get return 200 OK with file
                if (method.equals("GET")) {
                    header.dataDisplayer(defaultPath, fileRequest, "HTTP/1.1 200 OK");
                }

                System.out
                        .println("Content type of file: " + fileRequest + " - " + header.getFileType(fileRequest));

            }

        } catch (FileNotFoundException a) {
            try {
                // if 404 exists
                if (new File(defaultPath + errorFile).exists()) {
                    System.out.println("Sending to 404.html");
                    header.dataDisplayer(defaultPath, errorFile, "HTTP/1.1 404 File Not Found");
                    // else return static 404
                } else {
                    System.out.println("404.html not found - sending static page");
                    header.returnNotFound();
                }

            } catch (IOException ioe) {
                System.err.println("Error with file not found error: " + ioe.getMessage()); // error inception - should
                                                                                            // not happnen with if
                                                                                            // statment
            }

        } catch (IOException ioe) {
            System.err.println("Server error: " + ioe);
        } finally {
            // close all connections
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close();
            } catch (Exception e) {
                System.err.println("Error closing stream: " + e.getMessage());
            }

            System.out.println("Connection closed \n");
        }
    }

}
