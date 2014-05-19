package de.schulte.testverteilung;

/**
 * Die Klasse realisiert einen internen Speicher für einegelesene Dokumente
 */
public class FileEntry {

	public FileEntry(String name, byte[] data) {
		super();
		this.name = name;
		this.data = data;
	}

    public FileEntry(String name, byte[] data, String extractedData) {
        super();
        this.name = name;
        this.data = data;
        this.extractedData = extractedData;
    }

	public String getName() {
		return name;
	}

	public byte[] getData() {
		return data;
	}

    public String getExtractedData(){
        return extractedData;
    }

    public void setExtractedData(String extractedData) {
        this.extractedData = extractedData;
    }

	String name;

	byte[] data;

    String extractedData;

	
}
