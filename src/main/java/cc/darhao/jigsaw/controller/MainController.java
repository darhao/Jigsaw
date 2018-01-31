package cc.darhao.jigsaw.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cc.darhao.dautils.api.BytesParser;
import cc.darhao.dautils.api.ClassScanner;
import cc.darhao.dautils.api.DateUtil;
import cc.darhao.dautils.api.StringUtil;
import cc.darhao.dautils.api.TextFileUtil;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
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
	
	private static final String[] randomStrings= {
			"Hello, Darhao!", "Are you ok?", "嘤嘤嘤( ′◔ ‸◔`)", "Je t'aime", "この生涯はよろしくお願いします", "晚安，世界"
	};
	
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
	@FXML
	private AnchorPane parentAp;
	@FXML
	private Button jigsawBt;
	@FXML
	private Button unJigsawBt;
	@FXML
	private TextField updateTf;
	@FXML
	private Button autoBt;
	@FXML
	private Button updateBt;
	@FXML
	private Label rightLb;
	@FXML
	private CheckBox packageInfoCb;
	@FXML
	private TextField startFlagsTf;
	@FXML
	private TextField endFlagsTf;
	@FXML
	private TextField endInvalidFlagsTf;
	
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
	
	/**
	 * 当前显示进制
	 */
	private int currentRadix = 16;
	
	
	private Stage primaryStage;

	
	public void initialize(URL location, ResourceBundle resources) {
		initConfigFile();
		initTable();
		initPackageSelectedListener();
		initViewValue();
		initSerialNoTfListener();
		initFormatRbsListener();
		initFieldSelectedListener();
		initUpdateTfListener();
		rightLb.setText("© 2017 - "+ (new Date().getYear() + 1900) +" 沫熊工作室  All rights reserved.");
	}

	
	public void initFieldSelectedListener() {
		fieldTb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FieldInfoProperty>() {

			@Override
			public void changed(ObservableValue<? extends FieldInfoProperty> observable, FieldInfoProperty oldValue,
					FieldInfoProperty newValue) {
				if(updateTf.isFocused()) {
					return;
				}
				if(newValue != null) {
					updateTf.setText(newValue.getValue());
					updateTf.setDisable(false);
					autoBt.setDisable(false);
					try {
						//判断选择的项是否为枚举类型
						if(newValue.getType().startsWith("枚举:")){
							//在下方状态栏枚举枚举值
							for(Field field : getSelectedPackage().getClass().getDeclaredFields()) {
								//匹配字段
								if(field.getType().getSimpleName().equals(newValue.getType().split(":")[1])) {
									Class enumClass = field.getType();
									//获取枚举值
									Method method = field.getType().getMethod("values", new Class[] {});
									Enum[] enums = (Enum[]) method.invoke(null, new Object[] {});
									//构建提示串
									StringBuffer stringBuffer = new StringBuffer();
									stringBuffer.append("枚举类型 " + enumClass.getSimpleName() + ": ");
									for (Enum enum1 : enums) {
										stringBuffer.append(enum1.name() + "("+ enum1.ordinal() +"), ");
									}
									String tip = stringBuffer.substring(0, stringBuffer.length() - 2);
									info(tip);
									break;
								}
							}
						}
					} catch (ReflectiveOperationException e) {
						e.printStackTrace();
					}
				}else {
					updateTf.setText("");
					updateTf.setDisable(true);
					autoBt.setDisable(true);
					updateBt.setDisable(true);

				}
			}
			
		});
	}


	public void initSerialNoTfListener() {
		serialNoTf.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				//更新对应的包对象信息序列号
				try {
					getSelectedPackage().serialNo = Integer.valueOf(newValue, 16).shortValue();
					info("更新信息序列号成功");
				} catch (NumberFormatException e) {
					e.printStackTrace();
					error("字节集格式出错(" + e.getMessage()+")");
				}
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
						updateObjectInfo(p);
					}
				}
			}
		});
	}


	public void initFormatRbsListener() {
		ChangeListener<Boolean> listener = new ChangeListener<Boolean>() {
	
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				List<Byte> bytes = BytesParser.parseXRadixStringToBytes(bytesTa.getText(), currentRadix);
				if(hexRb.isSelected()) {
					currentRadix = 16;
				}else if(decRb.isSelected()){
					currentRadix = 10;
				}else if(binRb.isSelected()) {
					currentRadix = 2;
				}
				bytesTa.setText(BytesParser.parseBytesToXRadixString(bytes, currentRadix));
			}
		};
		hexRb.selectedProperty().addListener(listener);
		decRb.selectedProperty().addListener(listener);
		binRb.selectedProperty().addListener(listener);
	}
	
	
	public void initUpdateTfListener() {
		updateTf.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				updateBt.setDisable(false);
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
		System.out.println(fieldInfoPropertiesList.get(0).getValue());
	}


	public void onClickRandomBt() {
		int serialNo = Math.abs(new Random().nextInt() % 0x10000);
		String string = Integer.toHexString(serialNo).toUpperCase();
		string = StringUtil.fixLength(string, 4);
		serialNoTf.setText(string);
	}

	
	public void onClickUnJigsawBt() {
		serializePackageAndRefreshBytesTa();
	}
	

	public void onClickJigsawBt() {
		parseBytesAndRefreshTables();
	}
	
	
	public void onClickAutoBt() {
		try {
			//获取当前选中行
			int index = fieldTb.getSelectionModel().getSelectedIndex();
			FieldInfoProperty property = fieldInfoPropertiesList.get(index);
			//匹配字段
			for (Field field : getSelectedPackage().getClass().getDeclaredFields()) {
				if(field.getName().equals(property.getName())) {
					field.setAccessible(true);
					String valueString = "";
					Random random = new Random(new Date().getTime());
					switch (property.getType()) {
					case "带符号整数":
						String s  = StringUtil.fixLength(Integer.toHexString(random.nextInt()), property.getLength() * 2);
						int i = Integer.valueOf(s, 16);
						field.set(getSelectedPackage(), i);
						valueString = i + "";
						break;
					case "无符号整数":
						String s1  = StringUtil.fixLength(Integer.toHexString(Math.abs(random.nextInt())), property.getLength() * 2);
						int i1 = Integer.valueOf(s1, 16);
						field.set(getSelectedPackage(), i1);
						valueString = i1 + "";
						break;
					case "哈希串":
						int len = property.getLength();
						byte[] bytes = new byte[len];
						random.nextBytes(bytes);
						valueString = BytesParser.parseBytesToHexString(BytesParser.cast(bytes));
						field.set(getSelectedPackage(), valueString);
						break;
					case "字符串":
						valueString = randomStrings[Math.abs(random.nextInt()) % 6];
						field.set(getSelectedPackage(), valueString);
						break;
					case "日期时间":;
						Date now = new Date();
						valueString = DateUtil.yyyyMMddHHmmss(new Date());
						field.set(getSelectedPackage(), now);
						break;
					case "布尔":
						valueString = random.nextBoolean() + "";
						field.set(getSelectedPackage(), (valueString.equals("1") || valueString.equals("true"))? true : false);
						break;
					default:
						//获取枚举值
						Method method = field.getType().getMethod("values", new Class[] {});
						Object[] objects = (Object[]) method.invoke(null, new Object[] {});
						valueString = (Math.abs(random.nextInt()) % objects.length) + "";
						field.set(getSelectedPackage(), objects[Integer.valueOf(valueString)]);
						break;
					}
					info("修改值成功");
					property.setValue(valueString);
					fieldInfoPropertiesList.set(index, property);
					break;
				}
			}
		} catch (ReflectiveOperationException e) {
			error("对象赋值时反射错误：" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	public void onClickUpdateBt() {
		if(updateTf.getText() != null && !updateTf.getText().equals("")) {
			try {
				//获取当前选中行
				int index = fieldTb.getSelectionModel().getSelectedIndex();
				FieldInfoProperty property = fieldInfoPropertiesList.get(index);
				//匹配字段
				for (Field field : getSelectedPackage().getClass().getDeclaredFields()) {
					if(field.getName().equals(property.getName())) {
						field.setAccessible(true);
						String valueString = updateTf.getText();
						if(valueString == null || valueString.equals("")) {
							error("属性值不能为空");
							return;
						}
						switch (property.getType()) {
						case "带符号整数":
						case "无符号整数":
							field.set(getSelectedPackage(), Integer.valueOf(valueString));
							break;
						case "哈希串":
						case "字符串":
							field.set(getSelectedPackage(), valueString);
							break;
						case "日期时间":
							field.set(getSelectedPackage(), DateUtil.yyyyMMddHHmmss(valueString));
							break;
						case "布尔":
							field.set(getSelectedPackage(), (valueString.equals("1") || valueString.equals("true"))? true : false);
							break;
						default:
							//获取枚举值
							Method method = field.getType().getMethod("values", new Class[] {});
							Object[] objects = (Object[]) method.invoke(null, new Object[] {});
							field.set(getSelectedPackage(), objects[Integer.valueOf(valueString)]);
							break;
						}
						info("修改值成功");
						property.setValue(valueString);
						fieldInfoPropertiesList.set(index, property);
						updateBt.setDisable(true);
						fieldTb.requestFocus();
						break;
					}
				}
			} catch (NumberFormatException e) {
				error("数字值不符合规范：" + e.getMessage());
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				error("对象赋值时反射错误：" + e.getMessage());
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				error("不合法的参数异常：" + e.getMessage());
				e.printStackTrace();
			} catch (ParseException e) {
				error("日期时间解析出错：" + e.getMessage());
				e.printStackTrace();
			}catch (ArrayIndexOutOfBoundsException e) {
				error("值超出范围，如果是枚举类型请仔细检查定义范围：" + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	
	public void onClickPackageInfoCb() {
		if(packageInfoCb.isSelected()) {
			startFlagsTf.setDisable(false);
			endFlagsTf.setDisable(false);
			endInvalidFlagsTf.setDisable(false);
		}else {
			startFlagsTf.setDisable(true);
			endFlagsTf.setDisable(true);
			endInvalidFlagsTf.setDisable(true);
		}
		onClickUnJigsawBt();
	}
	
	
	private void serializePackageAndRefreshBytesTa() {
		try {
			List<Byte> bytes = PackageParser.serialize(getSelectedPackage());
			//把字节集转换成文本
			if(packageInfoCb.isSelected()) {
				//获取标识值
				byte[] endFlags = new byte[2];
				byte[] endInvalidFlags = new byte[2];
				byte[] startFlags = new byte[2];
				String ef= StringUtil.fixLength(endFlagsTf.getText(), 4);
				String evf = StringUtil.fixLength(endInvalidFlagsTf.getText(), 4);
				String sf = StringUtil.fixLength(startFlagsTf.getText(), 4);
				endFlags[0] = (byte) Integer.parseInt(ef.substring(0, 2),16);
				endFlags[1] = (byte) Integer.parseInt(ef.substring(2, 4),16);
				endInvalidFlags[0] = (byte) Integer.parseInt(evf.substring(0, 2),16);
				endInvalidFlags[1] = (byte) Integer.parseInt(evf.substring(2, 4),16);
				startFlags[0] = (byte) Integer.parseInt(sf.substring(0, 2),16);
				startFlags[1] = (byte) Integer.parseInt(sf.substring(2, 4),16);
				//检测文中是否存在结束位，如果有则用去语义标识注释
				byte a1, a2 = 0;
				for (int i = 0; i < bytes.size(); i++) {
					a1 = a2;
					a2 = bytes.get(i);
					if(a1 == endFlags[0] && a2 == endFlags[1]) {
						bytes.add(i - 1, endInvalidFlags[0]);
						bytes.add(i, endInvalidFlags[1]);
						i += 2;
					}
				}
				//加入起止标识
				bytes.add(0, startFlags[0]);
				bytes.add(1, startFlags[1]);
				bytes.add(endFlags[0]);
				bytes.add(endFlags[1]);
			}
			bytesTa.setText(BytesParser.parseBytesToXRadixString(bytes, currentRadix));
			info("包分解为字节集成功");
		}catch (Exception e) {
			error("序列化包对象时出错：" + e.getMessage());
			e.printStackTrace();
		}
	}

	
	private void parseBytesAndRefreshTables() {
		try {
			//文本解析成字节集
			List<Byte> bytes = BytesParser.parseXRadixStringToBytes(bytesTa.getText(), currentRadix);
			//把字节集转换成文本
			if(packageInfoCb.isSelected()) {
				//获取标识值
				byte[] endFlags = new byte[2];
				byte[] endInvalidFlags = new byte[2];
				byte[] startFlags = new byte[2];
				String ef= StringUtil.fixLength(endFlagsTf.getText(), 4);
				String evf = StringUtil.fixLength(endInvalidFlagsTf.getText(), 4);
				String sf = StringUtil.fixLength(startFlagsTf.getText(), 4);
				endFlags[0] = (byte) Integer.parseInt(ef.substring(0, 2),16);
				endFlags[1] = (byte) Integer.parseInt(ef.substring(2, 4),16);
				endInvalidFlags[0] = (byte) Integer.parseInt(evf.substring(0, 2),16);
				endInvalidFlags[1] = (byte) Integer.parseInt(evf.substring(2, 4),16);
				startFlags[0] = (byte) Integer.parseInt(sf.substring(0, 2),16);
				startFlags[1] = (byte) Integer.parseInt(sf.substring(2, 4),16);
				//去掉所有停止标识去语义标识符
				byte a1, a2 = 0;
				for (int i = 0; i < bytes.size(); i++) {
					a1 = a2;
					a2 = bytes.get(i);
					if (a1 == endFlags[0] && a2 == endFlags[1]) {
						byte e1 = bytes.get(i - 3);
						byte e2 = bytes.get(i - 2);
						if(e1 ==  endInvalidFlags[0] && e2 == endInvalidFlags[1]) {
							//去掉去语义标识符
							bytes.remove(i - 3);
							bytes.remove(i - 3);
						}
					}
				}
				//去掉起止标识
				bytes.remove(0);
				bytes.remove(0);
				bytes.remove(bytes.size() - 1);
				bytes.remove(bytes.size() - 1);
			}
			BasePackage p;
			try {
				//尝试解析为正常包
				p = PackageParser.parse(bytes, packageClasses, false);
			} catch (PackageParseException e) {
				//尝试解析为回复包
				p = PackageParser.parse(bytes, packageClasses, true);
			}
			//更新对象并选择被修改的包类项
			for (int i = 0; i < packageClasses.size(); i++) {
				if(p.getClass().getSimpleName().equals(packageClasses.get(i).getSimpleName())) {
					packageObjects.set(i, p);
					if(packageTb.getSelectionModel().getSelectedIndex() == i) {
						updateObjectInfo(p);
					}else {
						packageTb.getSelectionModel().select(i);
					}
					packageTb.scrollTo(i);
				}
			}
			info("字节集拼凑为包成功");
		} catch (NumberFormatException e) {
			error("字节集格式出错(" + e.getMessage()+")");
			e.printStackTrace();
		} catch (PackageParseException e) {
			error("反序列化包对象时出错：" + e.getClass().getSimpleName() + " : " +e.getMessage());
			e.printStackTrace();
		}
	}


	private void updateObjectInfo(BasePackage p) {
		//更新右侧字段表数据
		loadFieldTb(p);
		//更新信息序列号
		if(p.serialNo == null) {
			p.serialNo = 0;
		}
		String string = Integer.toHexString(p.serialNo).toUpperCase();
		string = StringUtil.fixLength(string, 4);
		serialNoTf.setText(string);
		//更新包名
		nameLb.setText(p.getClass().getSimpleName());
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
						info.setType(parse.sign() ? "带符号整数" : "无符号整数");
						info.setLength(parse.value()[1]);
						break;
					case "String":
						int length = parse.value()[1];
						info.setLength(length);
						if(parse.utf8()) {
							value = value == null ? randomStrings[0] : value;
							info.setType("字符串");
						}else {
							String initValue = "";
							for (int i = 0; i < length; i++) {
								initValue += "00 ";
							}
							initValue = initValue.trim();
							value = value == null ? initValue : value;
							info.setType("哈希串");
						}
						info.setValue((String) value);
						break;
					case "Date":
						value = value == null ? new Date() : value;
						info.setValue(DateUtil.yyyyMMddHHmmss((Date)value));
						info.setType("日期时间");
						info.setLength(parse.value()[1]);
						break;
					case "boolean":
						value = value == null ? false : value;
						info.setValue((boolean)value ? "true" : "false");
						info.setType("布尔");
						info.setLength(1);
						break;
					default:
						info.setType("枚举:"+type);
						info.setLength(parse.value()[1]);
						//获取枚举值
						Method method = field.getType().getMethod("values", new Class[] {});
						Object[] objects = (Object[]) method.invoke(null, new Object[] {});
						//匹配枚举值
						if(value == null) {
							value = objects[0];
							info.setValue(Integer.toString(0));
						}else {
							for (int i = 0; i < objects.length; i++) {
								if(objects[i].equals(value)){
									info.setValue(Integer.toString(i));
									break;
								}
							}
						}
						break;
					}
					//赋予值
					field.set(p, value);
					FieldInfoProperty property = new FieldInfoProperty(info);
					fieldInfoPropertiesList.add(property);
				}
			}
		}catch (ReflectiveOperationException e) {
			e.printStackTrace();
			error("加载字段表格时出错：" + e.getMessage());
		}
	}


	private void loadPackageTb() {
		try {
			List<Class> tempClasses = ClassScanner.searchClassInDir(properties.getProperty(CONFIG_KEY_CLASS_PATH));
			//创建列表
			packageInfoPropertiesList.clear();
			fieldInfoPropertiesList.clear();
			packageClasses = new ArrayList<Class>();
			//剔除没有继承至BasePackage类的元素
			for (Class class1 : tempClasses) {
				Class superClass = class1.getSuperclass();
				if(superClass != null && superClass.getSimpleName().equals("BasePackage")) {
					//实例化类对象并加入列表
					packageClasses.add(class1);
				}
			}
			//优先从备份文件中读取包对象数据，如果没有，新建一个包对象
			packageObjects = new ArrayList<BasePackage>();
			JSONArray jsonArray = null;
			try {
				jsonArray = JSONArray.parseArray(TextFileUtil.readFromFile("pack.dat"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			for (int i = 0; i < packageClasses.size(); i++) {
				Class class1 = packageClasses.get(i);
				BasePackage bp = null;
				try {
					bp = (BasePackage) ((JSONObject)jsonArray.get(i)).toJavaObject(class1);
				} catch (Exception e) {
					bp = (BasePackage) class1.newInstance();
				}
				packageObjects.add(bp);
			}
			//判断是否是空的
			if(packageObjects.isEmpty()) {
				error("该目录下没有找到class（提示：如果class名为com.abc.Foo，路径为/xxx/yyy/com/abc/Foo.class，那目录请选择/xxx/yyy/ ）");
				return;
			}
			//填充包类表格
			for (BasePackage p : packageObjects) {
				PackageInfo info = new PackageInfo();
				//设置长度
				PackageParser.initPackageInfo(p);
				info.setLength(p.length);
				//设置名字
				info.setName(p.getClass().getSimpleName());
				//设置协议号
				byte protocol = ((Protocol)p.getClass().getAnnotation(Protocol.class)).value();
				String hex = Integer.toHexString(protocol).toUpperCase();
				hex = StringUtil.fixLength(hex, 2);
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
		//设置可编辑字段
		fieldValueCol.setCellFactory(TextFieldTableCell.forTableColumn());
		//绑定数据表
		packageInfoPropertiesList = FXCollections.observableArrayList();
		packageTb.setItems(packageInfoPropertiesList);
		fieldInfoPropertiesList = FXCollections.observableArrayList();
		fieldTb.setItems(fieldInfoPropertiesList);
	}


	private void initViewValue() {
		String path = properties.getProperty(CONFIG_KEY_CLASS_PATH);
		if(path == null || path.equals("")) {
			return;
		}
		pathTf.setText(path);
		loadPackageTb();
	}

	
	private void initConfigFile() {
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


	public List<BasePackage> getPackageObjects() {
		return packageObjects;
	}


	
}
