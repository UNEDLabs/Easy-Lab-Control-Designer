package es.uned.dia.softwarelinks.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodGetBuilder extends MethodBuilder {
	List<Map<String, String>> links = new ArrayList<>();

	public MethodGetBuilder() {
		super("get", "void");
	}

	public void addLink(String matlab, String ejs, String type) {
		Map<String, String> link = new HashMap<>();
		link.put("ejs", ejs);
		link.put("matlab", matlab);
		link.put("type", type);
		links.add(link);
	}
	
	@Override
	public String toString() {
		StringBuilder code = new StringBuilder();
		StringBuilder variables = new StringBuilder();
		int i=0;
		for(Map<String, String> link : links) {
			String type = link.get("type"), ejs = link.get("ejs"), matlab = link.get("matlab");
			variables.append((i==0) ? String.format("\"%s\"", matlab) : String.format(",\"%s\"", matlab));
			switch(type) {
			case "Integer":
				code.append(String.format("%s = ((Double[])result[%d])[0].intValue();\n", ejs, i));
				break;
			case "Double":
				code.append(String.format("%s = ((Double[])result[%d])[0].doubleValue();\n", ejs, i));
				break;
			default:
				code.append(String.format("%s = ((%s)result[%d]);\n", ejs, type, i));
			}
			i++;
		}
		String body = "String[] variables = new String[]{%s};\nObject[] result = get(variables);\n%s";
		setBody(String.format(body, variables.toString(), code.toString()));
		return super.toString();
	}
}