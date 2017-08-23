package com.github.xzzpig.pigmcnetcenter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.github.xzzpig.pigutils.JsonUtils;
import com.github.xzzpig.pigutils.json.JSONObject;
import com.github.xzzpig.pigutils.json.JSONTokener;


public class Config {

	public static Config config;

	int port = 10727;
	
	boolean debug = false;
	
	int maxSpeed = -1;
	
	String chatAuthKey = "setbyyourself";

	public Config() {
		config = this;
	}

	public static Config load(File file) throws IOException {
		if (!file.exists())
			file.createNewFile();
		FileInputStream fin = new FileInputStream(file);
		JSONTokener tokener = new JSONTokener(fin);
		JSONObject json = new JSONObject(tokener);
		return JsonUtils.json2Bean(json, Config.class);
	}

	public static void save(File file) throws IOException {
		FileInputStream fout = new FileInputStream(file);
		JSONObject json = JsonUtils.bean2Json(config);
		json.saveToFile(file);
		fout.close();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public String getChatAuthKey() {
		return chatAuthKey;
	}

	public void setChatAuthKey(String chatAuthKey) {
		this.chatAuthKey = chatAuthKey;
	}

}
