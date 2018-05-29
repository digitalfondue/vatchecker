package ch.digitalfondue.vatchecker;


public class App {
    public static void main(String[] args) {
        System.err.println(from(EUVatChecker.doCheck("IT", "00950501007")));

        System.err.println(from(EUVatChecker.doCheck("IT", "00950501000")));

        System.err.println(from(EUVatChecker.doCheck("AAAAAA", "009505010075353")));
    }

    public static String from(EUVatCheckResponse resp) {
        return "EUVatCheckResponse{" +
                "isValid=" + resp.isValid() +
                ", name='" + resp.getName() + '\'' +
                ", address='" + resp.getAddress() + '\'' +
                '}';
    }
}
