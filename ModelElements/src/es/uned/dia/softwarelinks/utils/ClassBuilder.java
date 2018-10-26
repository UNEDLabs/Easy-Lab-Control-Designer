package es.uned.dia.softwarelinks.utils;

import java.util.HashMap;
import java.util.Map;

public class ClassBuilder {
	private String parent = "";
	private String name = "";
	private Map<String, MethodBuilder> methods = new HashMap<>();

	public ClassBuilder() {}

	public ClassBuilder(String name) {
		this.name = name;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public void addMethod(String method, MethodBuilder builder) {
		methods.put(method, builder);
	}
	
	public void addMethod(String method, String returns, String params, String body) {
		methods.put(method, new MethodBuilder(method, returns, params, body));
	}

	public String getCode(String method) {
		return methods.get(method).toString();
	}

	public String getBody() {
		StringBuilder body = new StringBuilder();
		for(String method : methods.keySet()) {
			body.append(getCode(method));
		}
		return body.toString();
	}
	
	public String toString() {
		if(name != "") {
			if(parent != "") {
				String code = "public class %s extends %s {%s}";
				return String.format(code, name, parent, getBody());
			} else {
				String code = "public class %s {%s}";
				return String.format(code, name, getBody());
			}
		} else {
			String code = "{%s}";
			return String.format(code, getBody());
		}
	}
}