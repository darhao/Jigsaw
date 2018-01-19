package cc.darhao.jigsaw.entity;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class FieldInfoProperty {

	private SimpleStringProperty name;
	
	private SimpleStringProperty value;
	
	private SimpleStringProperty type;
	
	private SimpleIntegerProperty position;
	
	private SimpleIntegerProperty length;

	
	public FieldInfoProperty(FieldInfo info) {
		name = new SimpleStringProperty(info.getName());
		value = new SimpleStringProperty(info.getValue());
		type = new SimpleStringProperty(info.getType());
		position = new SimpleIntegerProperty(info.getPosition());
		length = new SimpleIntegerProperty(info.getLength());
	}
	
	
	public String getName() {
		return name.get();
	}

	public String getValue() {
		return value.get();
	}

	public String getType() {
		return type.get();
	}

	public int getPosition() {
		return position.get();
	}

	public int getLength() {
		return length.get();
	}
	
}
