package cc.darhao.jigsaw.app;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.alibaba.fastjson.JSONArray;

import cc.darhao.dautils.api.ResourcesUtil;
import cc.darhao.dautils.api.TextFileUtil;
import cc.darhao.jigsaw.controller.MainController;
import cc.darhao.jiminal.core.BasePackage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * <br>
 * <b>2018年1月15日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class Main extends Application{

	private MainController mainController;
	
	private static final String VESION = "V1.3 Beta";
	
	private static final String NAME = "Jigsaw";

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(ResourcesUtil.getResourceURL("fxml/app.fxml"));
		Parent root = loader.load();
		//把Stage存入MainController
		mainController = loader.getController();
        mainController.setPrimaryStage(primaryStage);
        //显示
        primaryStage.setTitle(NAME +" "+ VESION);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
	}
	
	
	
	/**
	 * 程序入口
	 * @param args
	 */
	public static void main(String[] args) {
		
		ConfigurationSource source;
		try {
			source = new ConfigurationSource(ResourcesUtil.getResourceAsStream("log4j/log4j.xml"));
			Configurator.initialize(null, source);   
		} catch (IOException e) {
			e.printStackTrace();
		}
		launch(args);
	}
	
	
	@Override
	public void stop() throws Exception {
		List<BasePackage> packages = mainController.getPackageObjects();
		String data = JSONArray.toJSONString(packages);
		TextFileUtil.writeToFile("pack.dat", data);
	}
	
}
