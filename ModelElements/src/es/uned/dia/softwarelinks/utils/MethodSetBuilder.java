package es.uned.dia.softwarelinks.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodSetBuilder extends MethodBuilder {
	List<Map<String, String>> links = new ArrayList<>();

	public MethodSetBuilder() {
		super("set", "void");
	}

	public void addLink(String matlab, String ejs) {
		Map<String, String> link = new HashMap<>();
		link.put("ejs", ejs);
		link.put("matlab", matlab);
		links.add(link);
	}
	
	@Override
	public String toString() {
		StringBuilder variables = new StringBuilder(), values = new StringBuilder();
		boolean firstTime = true;
		for(Map<String, String> link : links) {
			String ejs = link.get("ejs"), matlab = link.get("matlab");
			if(firstTime) {
				variables.append("\"" + matlab + "\"");
				values.append(ejs);
				firstTime = false;
			} else {
				variables.append(", \"" + matlab + "\"");
				values.append("," + ejs);
			}			
		}
		String body = "set(new String[]{%s}, new Object[]{%s});\n";
		setBody(String.format(body, variables.toString(), values.toString()));
		return super.toString();
	}
}