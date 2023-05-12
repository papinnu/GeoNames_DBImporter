package org.papi.geonames.dbimporter;

public class App {

    private final Srtring fileName;

    public App(Srtring fileName) {
        this.fileName = fileName;
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Input file not passed!");
        }

        String file = args[0];
        App app = new App(file);

    }

    private void buildStructure(String filePath) {
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
