package encoding;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * | Charaters         | nö                            |
 * | ISO-8859-1 Hex    | 6e f6                         |
 * | ISO-8859-1 Binär  | 0110 1110 1111 0110           |
 * | ISO-8859-1 Base64 | bvY=                          |
 * | UTF-8 Hex         | 6e c3 b6                      |
 * | UTF-8 Binär       | 0110 1110 1100 0011 1011 0110 |
 * | UTF-8 Base64      | bsO2, 27 44 14 54             |
 */
public class Base64Converter {

    private static final int BUFFER_SIZE = 2048;

    public static void main(String[] args) throws IOException, URISyntaxException {
        zipAndEncodeToBase64("/input.docx", "output.txt");
        decodeFromBase64AndUnzip("/output.txt");
        otherStuff();
    }

    private static void zipAndEncodeToBase64(String inputFileName, String outputFileName) throws URISyntaxException,
            IOException {
        URL url = Base64Converter.class.getResource(inputFileName);
        File inputFile = new File(url.toURI());
        File zipFile = new File("output.zip");
        try (DataInputStream dis = new DataInputStream(new FileInputStream(inputFile));
             ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {

            ZipEntry zipEntry = new ZipEntry(inputFile.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] ba = new byte[(int) inputFile.length()];
            dis.readFully(ba);
            zipOut.write(ba);
            zipOut.closeEntry();
        }
        try (DataInputStream dis = new DataInputStream(new FileInputStream(zipFile));
             FileOutputStream out = new FileOutputStream(outputFileName)) {

            int fileLength = (int) zipFile.length();
            byte[] ba = new byte[fileLength];
            dis.readFully(ba);
            byte[] dest = Base64.getEncoder().encode(ba);
            out.write(dest);
        }
        Files.delete(zipFile.toPath());
    }

    private static void decodeFromBase64AndUnzip(String inputFileName) throws URISyntaxException, IOException {
        URL url = Base64Converter.class.getResource(inputFileName);
        File inputFile = new File(url.toURI());
        File zipFile = new File("result.zip");
        try (DataInputStream dis = new DataInputStream(new FileInputStream(inputFile));
             FileOutputStream out = new FileOutputStream(zipFile)) {
            byte[] ba = new byte[(int) inputFile.length()];
            dis.readFully(ba);
            byte[] decodedBytes = Base64.getDecoder().decode(ba);
            out.write(decodedBytes);
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                try (FileOutputStream out = new FileOutputStream(entry.getName())) {
                    byte[] data = new byte[BUFFER_SIZE];
                    int count;
                    while ((count = zipIn.read(data, 0, BUFFER_SIZE)) > 0) {
                        out.write(data, 0, count);
                    }
                }
            }
        }
        Files.delete(zipFile.toPath());
    }

    private static void otherStuff() throws URISyntaxException, IOException {
        URL url = Base64Converter.class.getResource("/input.txt");
        File f = new File(url.toURI());

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "ISO-8859-1"))) {
            String line = br.readLine();
            System.out.println("line: " + line);
        }

        String input = "nö";
        byte[] inputBytes = input.getBytes(Charset.forName("ISO-8859-1"));
        String encodedString = Base64.getEncoder().encodeToString(inputBytes);
        System.out.println(encodedString);

        byte[] decodedBytes = Base64.getDecoder().decode(encodedString.getBytes());
        String decodedString = new String(decodedBytes, Charset.defaultCharset());
        System.out.println(decodedString);
    }

}
