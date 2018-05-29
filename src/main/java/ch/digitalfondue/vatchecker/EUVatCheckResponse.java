package ch.digitalfondue.vatchecker;

public class EUVatCheckResponse {

    private final boolean isValid;
    private final String name;
    private final String address;

    public EUVatCheckResponse(boolean isValid, String name, String address) {
        this.isValid = isValid;
        this.name = name;
        this.address = address;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
