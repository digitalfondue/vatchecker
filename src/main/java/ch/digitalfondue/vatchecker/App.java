package ch.digitalfondue.vatchecker;


public class App {
    public static void main(String[] args) {
        System.err.println(EUVatChecker.doCheck("IT", "00950501007"));

        System.err.println(EUVatChecker.doCheck("IT", "00950501000"));

        System.err.println(EUVatChecker.doCheck("AAAAAA", "009505010075353"));
    }
}
