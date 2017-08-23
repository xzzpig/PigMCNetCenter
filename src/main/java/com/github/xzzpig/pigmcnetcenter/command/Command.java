package com.github.xzzpig.pigmcnetcenter.command;

public class Command {

	private String cmd;
	private String[] args;

	public Command(String cmd, String[] args) {
		this.cmd = cmd;
		this.args = args;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}
}
