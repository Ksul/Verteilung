package de.schulte.testverteilung;

public class FileEntry {

	public FileEntry(String name, byte[] data) {
		super();
		this.name = name;
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public byte[] getData() {
		return data;
	}

	String name;

	byte[] data;

	
}
