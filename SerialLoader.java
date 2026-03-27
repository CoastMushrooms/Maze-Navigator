public class SerialLoader {
    private String fileName;

    public void deserialize(String path) {
        // Path can be like CaveData\\M1.ser or C30.ser; just remember last token.
        fileName = path;
    }

    public String getMazeName() {
        return fileName;
    }
}
