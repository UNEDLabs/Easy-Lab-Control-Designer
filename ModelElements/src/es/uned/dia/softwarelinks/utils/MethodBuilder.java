package es.uned.dia.softwarelinks.utils;

import java.util.HashMap;
import java.util.Map;

public class MethodBuilder {
	private Map<String, String> info = new HashMap<>();
	
	public MethodBuilder(String name, String returns) {
		setName(name);
		setReturnType(returns);
		setParams("");
		setBody("");
	}

	public MethodBuilder(String name, String returns, String params) {
		setName(name);
		setReturnType(returns);
		setParams(params);
		setBody("");
	}

	public MethodBuilder(String name, String returns, String params, String body) {
		setName(name);
		setReturnType(returns);
		setParams(params);
		setBody(body);
	}
	
	public void setName(String name) {
		info.put("name", name);
	}

	public void setReturnType(String returns) {
		info.put("returns", returns);
	}

	public void setParams(String returns) {
		info.put("params", returns);
	}

	public void setBody(String body) {
		info.put("body", body);
	}

	public String toString() {
		String code = "public %s %s(%s) {%s}";
		return String.format(code,
			info.get("returns"),
			info.get("name"), 
			info.get("params"), 
			info.get("body")
		);
	}
}