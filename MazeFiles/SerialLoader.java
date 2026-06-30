public class SerialLoader {
    private String fileName;

    public void deserialize(String path) {
        fileName = path;
    }

    public String getMazeName() {
        return fileName;
    }
}
