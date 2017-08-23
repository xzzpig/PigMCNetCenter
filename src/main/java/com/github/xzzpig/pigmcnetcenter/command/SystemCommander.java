package com.github.xzzpig.pigmcnetcenter.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.xzzpig.pigutils.core.MD5;
import com.github.xzzpig.pigutils.file.ExtendFile;
import com.github.xzzpig.pigutils.json.JSONException;
import com.github.xzzpig.pigutils.json.JSONObject;

public class SystemCommander implements CommandRunner {

	private static final Logger LOGGER = LogManager.getLogger(SystemCommander.class);

	public SystemCommander() {
	}

	private static final String helpStr = new StringBuffer("命令帮助:\n").append("/stop\t停止服务器\n")
			.append("/updateclientindex\t更新客户端文件列表\n").toString();

	@Override
	public void run(Command cmd) {
		if (cmd.getCmd().equalsIgnoreCase("help")) {
			LOGGER.info(helpStr);
			return;
		} else if (cmd.getCmd().equalsIgnoreCase("updateclientindex")) {
			updateClientIndex();
		}
	}

	public static void updateClientIndex() {
		LOGGER.info("开始更新客户端文件列表...");
		JSONObject json = getDirMD5Json(new File("client"));
		LOGGER.debug("clientindex.json:\n" + json.toString(4));
		if (!new ExtendFile("./clientindex.json").writeObject(json.toString())) {
			LOGGER.error("客户端文件列表更新失败");
		} else
			LOGGER.info("客户端文件列表更新完成");
		System.gc();
		// try {
		// json.saveToFile(new File("./clientindex.json"));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	private static JSONObject getDirMD5Json(File dir) {
		JSONObject json = new JSONObject();
		File ignoreFile = new File(dir, ".ignore");
		List<String> ignores = new ArrayList<>();
		if (ignoreFile.exists()) {
			try {
				Scanner scanner = new Scanner(ignoreFile);
				while (scanner.hasNextLine()) {
					ignores.add(scanner.nextLine());
				}
				scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		for (File file : dir.listFiles(file -> !ignores.contains(file.getName()))) {
			if (file.isDirectory()) {
				json.put(file.getName(), getDirMD5Json(file));
			} else {
				try {
					String md5 = MD5.GetMD5Code(file);
					LOGGER.info(file.getPath() + ":" + md5);
					if (ignores.contains(md5))
						continue;
					json.put(file.getName(), md5);
				} catch (JSONException | FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return json;
	}

}
