package cc.darhao.jigsaw.entity;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class PackageInfoProperty {

	private SimpleStringProperty name;
	
	private SimpleStringProperty protocol;
	
	private SimpleIntegerProperty length;
	
	public PackageInfoProperty(PackageInfo info) {
		name = new SimpleStringProperty(info.getName());
		protocol = new SimpleStringProperty(info.getProtocol());
		length = new SimpleIntegerProperty(info.getLength());
	}

	public String getName() {
		return name.get();
	}

	public String getProtocol() {
		return protocol.get();
	}

	public int getLength() {
		return length.get();
	}
	
}
