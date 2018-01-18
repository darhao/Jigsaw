package cc.darhao.jigsaw.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cc.darhao.dautils.api.ClassScanner;
import cc.darhao.dautils.api.DateUtil;
import cc.darhao.jigsaw.entity.FieldInfo;
import cc.darhao.jigsaw.entity.FieldInfoProperty;
import cc.darhao.jigsaw.entity.PackageInfo;
import cc.darhao.jigsaw.entity.PackageInfoProperty;
import cc.darhao.jiminal.annotation.Parse;
import cc.darhao.jiminal.annotation.Protocol;
import cc.darhao.jiminal.core.BasePackage;
import cc.darhao.jiminal.core.PackageParser;
import cc.darhao.jiminal.exception.PackageParseException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * <br>
 * <b>2018年1月15日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class MainController implements Initializable {
	
	private static final String CONFIG_FILE_NAME = "jigsaw.cfg";
	
	private static final String CONFIG_KEY_CLASS_PATH = "classPath";
	
	private Properties properties;
	
	private Logger logger = LogManager.getRootLogger();
	
	@FXML
	private TextField pathTf;
	@FXML
	private Button pathBt;
	@FXML
	private Label nameLb;
	@FXML
	private TextField serialNoTf;
	@FXML
	private Button randomBt;
	@FXML
	private TableView packageTb;
	@FXML
	private TableView fieldTb;
	@FXML
	private TableColumn packageNameCol;
	@FXML
	private TableColumn packageProtocolCol;
	@FXML
	private TableColumn packageLengthCol;
	@FXML
	private TableColumn fieldNameCol;
	@FXML
	private TableColumn fieldValueCol;
	@FXML
	private TableColumn fieldTypeCol;
	@FXML
	private TableColumn fieldPositionCol;
	@FXML
	private TableColumn fieldLengthCol;
	@FXML
	private RadioButton hexRb;
	@FXML
	private RadioButton binRb;
	@FXML
	private RadioButton decRb;
	@FXML
	private TextArea bytesTa;
	@FXML
	private Label stateLb;
	
	/**
	 * 扫描出来的通讯包类列表
	 */
	private List<Class> packageClasses;
	/**
	 * 扫描出来的通讯包类的对象列表
	 */
	private List<BasePackage> packageObjects;
	/**
	 * 包类观察者数据列表
	 */
	private ObservableList<PackageInfoProperty> packageInfoPropertiesList;
	/**
	 * 字段观察者数据列表
	 */
	private ObservableList<FieldInfoProperty> fieldInfoPropertiesList;
	
	
	private Stage primaryStage;

	
	public void initialize(URL location, ResourceBundle resources) {
		initConfigFile();
		initTable();
		initViewValue();
		initSerialNoTfListener();
		initBytesTaListener();
		initFormatRbsListener();
		initFieldValueColListener();
	}


	public void initFieldValueColListener() {
	}


	public void initFormatRbsListener() {
		ChangeListener<Boolean> listener = new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				serializePackageAndRefreshBytesTa();
			}
		};
		hexRb.selectedProperty().addListener(listener);
		decRb.selectedProperty().addListener(listener);
		binRb.selectedProperty().addListener(listener);
	}


	public void initBytesTaListener() {
		bytesTa.setWrapText(true);
		bytesTa.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				try {
					//文本解析成字节集
					List<Byte> bytes = new ArrayList<Byte>();
					for (String byteString : bytesTa.getText().split(" ")) {
						int i = 0;
						if(hexRb.isSelected()) {
							i = Integer.valueOf(byteString, 16);
						}else if(decRb.isSelected()){
							i = Integer.valueOf(byteString);
						}else if(binRb.isSelected()) {
							i = Integer.parseInt(byteString, 2);
						}
						bytes.add((byte) i);
					}
					//获取被选择的包类名字
					String name = getSelectedPackage().getClass().getSimpleName();
					//解析
					BasePackage p = PackageParser.parse(bytes, packageClasses, name.contains("Reply"));
					//更新对象并选择被修改的包类项
					for (int i = 0; i < packageClasses.size(); i++) {
						if(p.getClass().getSimpleName().equals(packageClasses.get(i).getSimpleName())) {
							packageObjects.set(i, p);
							if(packageTb.getSelectionModel().getSelectedIndex() != i) {
								packageTb.getSelectionModel().select(i);
							}
						}
					}
				} catch (NumberFormatException e) {
					error("字节集格式出错(" + e.getMessage()+")");
					e.printStackTrace();
				} catch (PackageParseException e) {
					error("反序列化包对象时出错：" + e.getClass().getSimpleName() + " : " +e.getMessage());
					e.printStackTrace();
				}
			}
		});
	}


	public void initSerialNoTfListener() {
		serialNoTf.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				//更新对应的包对象信息序列号
				getSelectedPackage().serialNo = Integer.valueOf(newValue, 16).shortValue();
				//序列化并更新字节集文本域
				serializePackageAndRefreshBytesTa();
			}
		});
	}
	
	
	public void initPackageSelectedListener() {
		//设置选择后的监听器
		packageTb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PackageInfoProperty>() {
	
			@Override
			public void changed(ObservableValue<? extends PackageInfoProperty> observable, PackageInfoProperty oldValue,
					PackageInfoProperty newValue) {
				String clsName = newValue.getName();
				for (BasePackage p : packageObjects) {
					if(p.getClass().getSimpleName().equals(clsName)) {
						//更新右侧字段表数据
						loadFieldTb(p);
						//更新信息序列号
						if(p.serialNo == null) {
							p.serialNo = 0;
						}
						String string = Integer.toHexString(p.serialNo).toUpperCase();
						if(string.length() >= 4 ) {
							string = string.substring(string.length() - 4);
						}else if(string.length() < 4){
							int length = string.length();
							for (int i = 0; i < 4 - length; i++) {
								string = "0" + string;
							}
						}
						serialNoTf.setText(string);
						serializePackageAndRefreshBytesTa();
					}
				}
			}
		});
	}


	public void onClickPathBt() {
		//初始化文件选择器
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("选择通讯包class文件所在目录");
		//尝试读取配置文件获取上次默认路径
		String classPath = new String();
		File classDir = null;
		try {
			classPath = properties.getProperty(CONFIG_KEY_CLASS_PATH);
			if(classPath != null && !classPath.equals("")) {
				File file = new File(classPath);
				if(file.getParentFile().exists()) {
					chooser.setInitialDirectory(file.getParentFile());
				}
			}
		} catch (NullPointerException e) {
			try {
				new File(CONFIG_FILE_NAME).createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				error("创建配置文件时出现IO错误");
			}
		}
		//选择文件
		classDir = chooser.showDialog(primaryStage);
		if(classDir != null) {
			//初始化Excel
			pathTf.setText(classDir.getAbsolutePath());
			//存配置
			properties.setProperty(CONFIG_KEY_CLASS_PATH, classDir.getAbsolutePath());
			//填充包表格
			loadPackageTb();
			try {
				properties.store(new FileOutputStream(new File(CONFIG_FILE_NAME)), null);
			} catch (IOException e) {
				e.printStackTrace();
				error("配置文件时出现IO错误");
			}
		}
	}


	public void onClickRandomBt() {
		int serialNo = Math.abs(new Random().nextInt() % 0x10000);
		String string = Integer.toHexString(serialNo).toUpperCase();
		if(string.length() >= 4 ) {
			string = string.substring(string.length() - 4);
		}else if(string.length() < 4){
			int length = string.length();
			for (int i = 0; i < 4 - length; i++) {
				string = "0" + string;
			}
		}
		serialNoTf.setText(string);
	}


	private void loadFieldTb(BasePackage p) {
		try {
			fieldInfoPropertiesList.clear();
			//获取所有带Parse注解的字段
			for (Field field : p.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				Parse parse = field.getAnnotation(Parse.class);
				if(parse != null) {
					FieldInfo info = new FieldInfo();
					info.setName(field.getName());
					info.setPosition(parse.value()[0]);
					String type = field.getType().getSimpleName();
					//获取值
					Object value = field.get(p);
					switch (type) {
					case "int":
						value = value == null ? 0 : value;
						info.setValue(Integer.toString((int) value));
						info.setType(parse.sign() ? "sign int" : "unsign int");
						info.setLength(parse.value()[1]);
						break;
					case "String":
						value = value == null ? "0" : value;
						info.setValue((String) value);
						info.setType(type);
						info.setLength(parse.value()[1]);
						break;
					case "Date":
						value = value == null ? new Date() : value;
						info.setValue(DateUtil.yyyyMMddHHmmss((Date)value));
						info.setType(type);
						info.setLength(parse.value()[1]);
						break;
					case "boolean":
						value = value == null ? false : value;
						info.setValue((boolean)value ? "1" : "0");
						info.setType(type);
						info.setLength(1);
						break;
					default:
						//获取枚举值
						Method method = field.getType().getMethod("values", new Class[] {});
						Object[] objects = (Object[]) method.invoke(null, new Object[] {});
						//匹配枚举值
						for (int i = 0; i < objects.length; i++) {
							if(objects[i].equals(value)){
								info.setValue(Integer.toString(i));
								break;
							}else {
								info.setValue(Integer.toString(0));
								value = objects[0];
							}
						}
						info.setType(type);
						info.setLength(parse.value()[1]);
						break;
					}
					//赋予值
					field.set(p, value);
					FieldInfoProperty property = new FieldInfoProperty(info);
					fieldInfoPropertiesList.add(property);
				}
			}
			info("加载"+ p.getClass().getSimpleName() +"属性成功");
		}catch (ReflectiveOperationException e) {
			e.printStackTrace();
			error("加载字段表格时出错：" + e.getMessage());
		}
	}


	private void serializePackageAndRefreshBytesTa() {
		try {
			StringBuffer sb = new StringBuffer();
			List<Byte> bytes = PackageParser.serialize(getSelectedPackage());
			for (Byte b : bytes) {
				if(hexRb.isSelected()) {
					String string = Integer.toHexString(b).toUpperCase();
					if(string.length() == 8) {
						string = string.substring(6);
					}else if(string.length() == 1){
						string = "0" + string;
					}
					sb.append(string);
				}else if(decRb.isSelected()){
					sb.append(Integer.toString(b));
				}else if(binRb.isSelected()) {
					String string = Integer.toBinaryString(b);
					if(string.length() == 32) {
						string = string.substring(24);
					}else if(string.length() == 4){
						string = "0000" + string;
					}
					sb.append(string);
				}
				sb.append(" ");
			}
			bytesTa.setText(sb.toString().trim());
		}catch (Exception e) {
			error("序列化包对象时出错：" + e.getMessage());
			e.printStackTrace();
		}
	}


	private void loadPackageTb() {
		try {
			List<Class> tempClasses = ClassScanner.searchClassInDir(properties.getProperty(CONFIG_KEY_CLASS_PATH));
			//创建列表
			packageClasses = new ArrayList<Class>();
			packageObjects = new ArrayList<BasePackage>();
			//剔除没有继承至BasePackage类的元素
			for (Class class1 : tempClasses) {
				Class superClass = class1.getSuperclass();
				if(superClass != null && superClass.getSimpleName().equals("BasePackage")) {
					//实例化类对象并加入列表
					packageClasses.add(class1);
					packageObjects.add((BasePackage)class1.newInstance());
				}
			}
			//判断是否是空的
			if(packageObjects.isEmpty()) {
				error("该目录下没有找到class（提示：如果您的class名为com.abc.Foo，路径为/xxx/yyy/com/abc/Foo.class，那么目录请选择/xxx/yyy/ ）");
				return;
			}
			//填充包类表格
			packageInfoPropertiesList.clear();
			for (BasePackage p : packageObjects) {
				PackageInfo info = new PackageInfo();
				//设置长度
				PackageParser.initPackageInfo(p);
				info.setLength(p.length);
				//设置名字
				info.setName(p.getClass().getSimpleName());
				//设置协议号
				byte protocol = ((Protocol)p.getClass().getAnnotation(Protocol.class)).value();
				String hex = Integer.toHexString(protocol);
				hex = hex.length() == 1 ? '0' + hex : hex;
				hex = hex.toUpperCase();
				hex = "0x" + hex;
				info.setProtocol(hex);
				//添加到列表
				PackageInfoProperty property = new PackageInfoProperty(info);
				packageInfoPropertiesList.add(property);
			}
			//选择第一项
			packageTb.getSelectionModel().select(0);
			info("加载包类表格成功");
		}catch (ReflectiveOperationException e) {
			error("加载包类表格出错：" + e.getMessage());
			e.printStackTrace();
		}
	}


	private void initTable() {
		//设置字段映射
		packageNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		packageProtocolCol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
		packageLengthCol.setCellValueFactory(new PropertyValueFactory<>("length"));
		fieldNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		fieldValueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
		fieldTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
		fieldPositionCol.setCellValueFactory(new PropertyValueFactory<>("position"));
		fieldLengthCol.setCellValueFactory(new PropertyValueFactory<>("length"));
		//绑定数据表
		packageInfoPropertiesList = FXCollections.observableArrayList();
		packageTb.setItems(packageInfoPropertiesList);
		fieldInfoPropertiesList = FXCollections.observableArrayList();
		fieldTb.setItems(fieldInfoPropertiesList);
		//设置包类表格项被选择时的监听器
		initPackageSelectedListener();
	}


	private void initViewValue() {
		String path = properties.getProperty(CONFIG_KEY_CLASS_PATH);
		pathTf.setText(path);
		loadPackageTb();
	}

	
	private void initConfigFile() {
		//读取上次文件路径和表名
		properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(CONFIG_FILE_NAME)));
		} catch (IOException e) {
			try {
				new File(CONFIG_FILE_NAME).createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				error("创建配置文件时出现IO错误");
			}
		}
	}


	private BasePackage getSelectedPackage() {
		int index = packageTb.getSelectionModel().getSelectedIndex();
		return packageObjects.get(index);
	}


	/**
	 * 显示正常状态
	 * @param message
	 */
	private void info(String message) {
		stateLb.setTextFill(Color.BLACK);
		stateLb.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
		stateLb.setText(DateUtil.HHmmss(new Date()) +" - "+ message);
	}
	
	
	/**
	 * 显示错误状态，并记录日志
	 * @param message
	 */
	private void error(String message) {
		stateLb.setTextFill(Color.WHITE);
		stateLb.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
		stateLb.setText(DateUtil.HHmmss(new Date()) +" - "+ message);
		logger.error(message);
	}


	public Stage getPrimaryStage() {
		return primaryStage;
	}


	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}


}
