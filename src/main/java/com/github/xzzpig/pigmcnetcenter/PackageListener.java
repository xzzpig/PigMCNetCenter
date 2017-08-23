package com.github.xzzpig.pigmcnetcenter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.xzzpig.pigmcnetcenter.command.SystemCommander;
import com.github.xzzpig.pigutils.core.MD5;
import com.github.xzzpig.pigutils.event.EventHandler;
import com.github.xzzpig.pigutils.event.Listener;
import com.github.xzzpig.pigutils.file.ExtendFile;
import com.github.xzzpig.pigutils.json.JSONArray;
import com.github.xzzpig.pigutils.json.JSONObject;
import com.github.xzzpig.pigutils.pack.FileTransferPackage;
import com.github.xzzpig.pigutils.pack.Package;
import com.github.xzzpig.pigutils.pack.socket.PackageSocket;
import com.github.xzzpig.pigutils.pack.socket.eventdrive.PackageSocketCloseEvent;
import com.github.xzzpig.pigutils.pack.socket.eventdrive.PackageSocketPackageEvent;

public class PackageListener implements Listener {
	private static final Logger LOGGER = LogManager.getLogger(PackageListener.class);

	//
	// @EventHandler
	// public void onDisConnected(WebSocketCloseEvent event) {
	// websocketdata.remove(event.getWebSocket());
	// }
	//
	// @EventHandler
	// public void onMessage(WebSocketMessageEvent event) {
	// LOGGER.debug(event.getWebSocket().getRemoteSocketAddress().getHostString()
	// + ":\n" + event.getMessage());
	// }
	//
	// private JSONObject msg;
	//
	// @EventHandler(mainLevel = EventRunLevel.Highest)
	// public void onFilltoJson(WebSocketMessageEvent event) {
	// try {
	// msg = new JSONObject(event.getMessage());
	// } catch (Exception e) {
	// msg = null;
	// }
	// }
	//
	// @EventHandler
	// public void onGetClientInfo(WebSocketMessageEvent event) {
	// if (msg == null)
	// return;
	// String cmd = msg.optString("command", "");
	// if (!cmd.equalsIgnoreCase("clientinfo"))
	// return;
	// msg.remove(cmd);
	// JSONObject data = getData(event.getWebSocket());
	// for (String key : msg.keySet()) {
	// if (key == null)
	// continue;
	// Object value = msg.opt(key);
	// if (value == null)
	// continue;
	// data.put(key, value);
	// }
	// LOGGER.debug(data.toString());
	// msg = null;
	// }
	//
	// @EventHandler
	// public void onGetCilentIndexMD5(PackageSocketPackageEvent event) {
	// if (!event.getPackage().getType().equals("GetNoticePackage"))
	// return;
	// // if (msg == null)
	// // return;
	// // String cmd = msg.optString("command", "");
	// // if (!cmd.equalsIgnoreCase("clientindexmd5"))
	// // return;
	// // msg.remove(cmd);
	// // msg = null;
	// JSONObject json = new JSONObject();
	// File index = new File("clientindex.json");
	// if (!index.exists()) {
	// SystemCommander.updateClientIndex();
	// }
	// try {
	// json.put("clientindexmd5", MD5.GetMD5Code(index));
	// } catch (JSONException | FileNotFoundException e) {
	// e.printStackTrace();
	// }
	// event.getWebSocket().send(json.toString());
	// LOGGER.info(event.getWebSocket().getRemoteSocketAddress().getHostName() +
	// " get md5 of clientindex.json");
	// }
	// @EventHandler
	public void onPrintPackage(PackageSocketPackageEvent event) {
		LOGGER.info(event.getPackage() + event.getPackage().getStringData());
	}

	@EventHandler
	public void onGetNoticePackage(PackageSocketPackageEvent event) {
		if (!event.getPackage().getType().equals("GetNoticePackage"))
			return;
		ExtendFile file = new ExtendFile("Notice.txt");
		JSONArray notices = new JSONArray();
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				LOGGER.catching(e);
				event.getPackageSocket().send(new Package("GetNoticeResultPackage", notices.toString().getBytes()));
				return;
			}
		file.eachLine(notices::put);
		event.getPackageSocket().send(new Package("GetNoticeResultPackage", notices.toString().getBytes()));
		LOGGER.info(event.getPackageSocket().getSocket().getInetAddress().getHostName() + " get notices");
	}

	@EventHandler
	public void onGetFileListMD5Package(PackageSocketPackageEvent event) {
		if (!event.getPackage().getType().equals("GetFileListMD5Package"))
			return;
		File index = new File("clientindex.json");
		if (!index.exists()) {
			SystemCommander.updateClientIndex();
		}
		try {
			event.getPackageSocket().send(new Package("GetFileListMD5ResultPackage", MD5.GetMD5Code(index).getBytes()));
		} catch (FileNotFoundException e) {
			LOGGER.catching(e);
		}
		LOGGER.info(
				event.getPackageSocket().getSocket().getInetAddress().getHostName() + " get md5 of clientindex.json");
	}

	@EventHandler
	public void onPullFileListPackage(PackageSocketPackageEvent event) {
		if (!event.getPackage().getType().equals("PullFileListPackage"))
			return;
		File index = new File("clientindex.json");
		if (!index.exists()) {
			SystemCommander.updateClientIndex();
		}
		FileTransferPackage.sendFile(event.getPackageSocket(), index, new File("."), false);
		LOGGER.info(event.getPackageSocket().getSocket().getInetAddress().getHostName() + " download clientindex.json");
	}

	@EventHandler
	public void onDownloadClientPackage(PackageSocketPackageEvent event) {
		if (!event.getPackage().getType().equals("DownloadClientPackage"))
			return;
		File index = new File(event.getPackage().getStringData().replaceFirst(".", "./client"));
		if (!index.exists()) {
			return;
		}
		FileTransferPackage.sendFile(event.getPackageSocket(), index, new File("./client/"), false);
		LOGGER.info(event.getPackageSocket().getSocket().getInetAddress().getHostName() + " download file:" + index);
	}

	static Map<PackageSocket, Integer> chatClientidMap = new HashMap<>();

	@EventHandler
	public void onMCChatClientAuthPackage(PackageSocketPackageEvent event) {
		if (!event.getPackage().getType().equals("MCChatClientAuthPackage"))
			return;
		JSONObject json = new JSONObject(event.getPackage().getStringData());
		if (Config.config.chatAuthKey.equals(json.optString("authkey", ""))) {
			LOGGER.info(event.getPackageSocket().getSocket().getInetAddress() + "聊天客户端验证成功");
			chatClientidMap.put(event.getPackageSocket(), json.optInt("id", 1));
		} else {
			LOGGER.warn(event.getPackageSocket().getSocket().getInetAddress() + "聊天客户端验证失败,断开连接");
			LOGGER.warn(Config.config.chatAuthKey + "|" + event.getPackage().getStringData());
			try {
				event.getPackageSocket().getSocket().close();
			} catch (IOException e) {
				LOGGER.warn("断开连接失败");
				LOGGER.catching(e);
			}
		}
	}

	@EventHandler
	public void onClientClose(PackageSocketCloseEvent event) {
		chatClientidMap.remove(event.getPackageSocket());
	}

	@EventHandler
	public void onServerChatPackage(PackageSocketPackageEvent event) {
		if (!event.getPackage().getType().equals("ServerChatPackage"))
			return;
		for (PackageSocket socket : chatClientidMap.keySet()) {
			socket.send(event.getPackage());
		}
	}
}
