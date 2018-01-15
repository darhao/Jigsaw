package cc.darhao.jigsaw.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cc.darhao.dautils.api.ClassScanner;
import cc.darhao.dautils.api.DateUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
	
	private List<Class> packageClasses;
	
	private Stage primaryStage;

	
	public void initialize(URL location, ResourceBundle resources) {
		initConfigFile();
		initViewValue();
	}


	private void initViewValue() {
		String path = properties.getProperty(CONFIG_KEY_CLASS_PATH);
		pathTf.setText(path);
		packageClasses = ClassScanner.searchClassInDir(properties.getProperty(CONFIG_KEY_CLASS_PATH));
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
			//TODO:填充包表格
			
			//存配置
			properties.setProperty(CONFIG_KEY_CLASS_PATH, classDir.getAbsolutePath());
			try {
				properties.store(new FileOutputStream(new File(CONFIG_FILE_NAME)), null);
			} catch (IOException e) {
				e.printStackTrace();
				error("配置文件时出现IO错误");
			}
		}
	}
	
	
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
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


}
