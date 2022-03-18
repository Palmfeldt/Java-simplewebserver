import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

public class Headers {
    PrintWriter out = null;
    OutputStream dataOut = null;
    Socket connect = null;

    public Headers(PrintWriter out, OutputStream dataOut, Socket connect) throws IOException {
        this.connect = connect;
        this.out = out; // send data to user
        this.dataOut = dataOut; // send filedata to user
        
    }

     // if 404.html does not exist, make static one
     protected void returnNotFound() throws IOException {
        out.println("HTTP/1.1 404 File Not Found");
        out.println("Date: Fri, 31 Dec 1999 23:59:59 GMT");
        out.println("Content-Type: text/html");
        out.println("Content-Length: 81"); // size is incorrect but whatever
        out.println("Expires: Fri, 01 Jan 2021 00:59:59 GMT");
        out.println("Last-modified: Sat, 09 Aug 2020 14:21:40 GMT");
        out.println("");
        out.println("<TITLE>Error 404, 404 not found.</TITLE>");
        out.println("<P>Error 404 - and no 404.html found</P>");
        flushAll(out, dataOut);
        System.out.println("Current header: HTTP/1.1 404 File Not Found");
    }

    // Whoever wrote the RFC for HTTP would cry if they saw this
    // returns static 500 internal server error page
    protected void returnServerError() throws IOException {
        out.println("HTTP/1.1 500 Internal Server Error");
        out.println("Date: Fri, 31 Dec 1999 23:59:59 GMT");
        out.println("Content-type: text/html");
        out.println("Content-length: 160"); // size is incorrect but whatever
        out.println("Expires: Fri, 01 Jan 2021 00:59:59 GMT\r\n");
        out.println("Last-modified: Sat, 09 Aug 2020 14:21:40 GMT\r\n");
        out.println("");
        out.println("<TITLE>Error 500, Dont worry, not a SQL injection</TITLE>");
        out.println("<P>Error 500 - Dont worry, not a SQL injection</P>");
        flushAll(out, dataOut);
        System.out.println("Current header: HTTP/1.1 500 Internal Server Error");
    }

    // 302 redirect exeption
    protected void returnRedirect() throws IOException {
        out.println("HTTP/1.1 302 Found");
        out.println("Date: Fri, 31 Dec 1999 23:59:59 GMT");
        out.println("Content-type: text/html");
        out.println("Location: https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        out.println("Content-length: 160"); // size is incorrect but whatever
        out.println("Expires: Fri, 01 Jan 2021 00:59:59 GMT\r\n");
        out.println("Last-modified: Sat, 09 Aug 2020 14:21:40 GMT\r\n");
        out.println("");
        out.println("<TITLE>302 Found ;)</TITLE>");
        flushAll(out, dataOut);
        System.out.println("Current header: " + "HTTP/1.1 302 Found");
    }

    protected void flushAll(PrintWriter out, OutputStream dataOut) throws IOException {
        out.flush();
        dataOut.flush();
    }

       /**
     * Method sends data to the server via a file path
     * fileRequested
     * Used for loading pages with 200 OK and the non static 404 page
     */
    protected void dataDisplayer(String defaultPath, String fileName, String header)
            throws IOException {
        File file = new File(defaultPath + fileName);
        int fileLength = (int) file.length();
        byte[] fileData = readFileData(file, fileLength);
        // E.g. HTTP/1.1 404 File Not Found
        out.println(header);
        out.println("Date: " + new Date());
        out.println("Content-type: " + getFileType(defaultPath + fileName));
        out.println("Content-length: " + fileLength);
        out.println();
        out.flush();
        try {
            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
        System.out.println("Current header: " + header);
    }

    
    /**
     * Reads requested file and returns a byte array
     * Uses filestream
     * 
     * @param file       - file input
     * @param fileLength - file lenght - duh
     * @return returns filled data
     */
    protected byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

    /**
     * returns the MIME Type
     */
    protected String getFileType(String fileRequested) {
        // list of well known image types
        String[] imgTypes = { "png", "jpeg", "gif", "jpg" };

        String result = fileRequested.substring(fileRequested.lastIndexOf('.') + 1).trim();
        String returnStatement = "text/plain";
        if (fileRequested.endsWith(".html") || fileRequested.endsWith(".htm"))
            returnStatement = "text/html";
        // to slow of an operation?
        else if (Arrays.asList(imgTypes).contains(result)) {
            returnStatement = "image/png";
        }
        return returnStatement;
    }

    
}
