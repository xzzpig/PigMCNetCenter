package com.github.xzzpig.pigmcnetcenter;

import static com.github.xzzpig.pigmcnetcenter.Config.config;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import com.github.xzzpig.pigmcnetcenter.command.Command;
import com.github.xzzpig.pigmcnetcenter.command.SystemCommander;
import com.github.xzzpig.pigutils.pack.FileTransferPackage;
import com.github.xzzpig.pigutils.pack.socket.eventdrive.EDPackageSocketServer;
import com.github.xzzpig.pigutils.pack.socket.eventdrive.PackageSocketCloseEvent;
import com.github.xzzpig.pigutils.pack.socket.eventdrive.PackageSocketErrorEvent;
import com.github.xzzpig.pigutils.pack.socket.eventdrive.PackageSocketOpenEvent;;

public class Main {
	public static EDPackageSocketServer server;

	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	public static final SystemCommander SYSTEM_COMMANDER = new SystemCommander();

	private static final File configfile = new File("./config.json");

	public static void main(String... args) throws IOException {

		try {
			Config.load(configfile);
		} catch (Exception e) {
			LOGGER.error("Config读取失败(可能文件不存在)");
		}
		if (config == null) {
			config = new Config();
			Config.save(configfile);
		}
		if (config.debug) {
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			Configuration conf = ctx.getConfiguration();
			conf.getLoggerConfig(LOGGER.getName()).setLevel(Level.ALL);
			ctx.updateLoggers(conf);
		}
		server = new EDPackageSocketServer(config.port);// new
		// EventDriveWebSocketServer(new
		// InetSocketAddress(config.port));
		server.regRunner((PackageSocketOpenEvent e) -> LOGGER
				.info(e.getPackageSocket().getSocket().getInetAddress().getHostName() + " connected"));
		server.regRunner((PackageSocketErrorEvent e) -> LOGGER.catching(e.getError()));
		server.regRunner((PackageSocketCloseEvent e) -> {
			try {
				LOGGER.info(e.getPackageSocket().getSocket().getInetAddress().getHostName() + " disconnected");
			} catch (Exception ex) {
			}
		});
		server.regListener(new PackageListener());
		server.start();
		FileTransferPackage.addSupport(server);
		FileTransferPackage.MaxSpeed = config.maxSpeed;
		LOGGER.info("Server 已启动于端口" + config.port);
		if (config.maxSpeed > 0)
			LOGGER.info("已限制最大上传速度:" + (config.maxSpeed / 1024f) + "KBps");
		LOGGER.info("输入/stop可停止服务器");
		LOGGER.info("输入/help可查看帮助");
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			String str = scanner.nextLine();
			if (str.equalsIgnoreCase("/stop")) {
				break;
			}
			if (str.startsWith("/")) {
				String cmd = str.split(" ")[0];
				String[] args2 = null;
				if (str.contains(" ")) {
					args2 = str.replace(cmd + " ", "").split(" ");
				}
				Command command = new Command(cmd.replaceFirst("/", ""), args2);
				SYSTEM_COMMANDER.run(command);
			}
		}
		server.stop();
		try {
			Config.save(configfile);
		} catch (IOException e) {
			LOGGER.catching(e);
			LOGGER.error("Config保存失败");
		}
		scanner.close();
		LOGGER.info("Server 已停止");
		System.exit(0);
	}
}
