package laf.urlMapping;

import java.util.List;

import laf.controllerInfo.ControllerInfo;

import com.google.common.base.*;
import com.google.common.collect.Lists;

/**
 * Generates a controller identifier form a controller class name by removing
 * the basePackage prefix, removing an eventual Controller suffix of the class
 * name, and removing an eventual controller subpackage. The packages of the
 * identifier are separated with a forward slash.
 */
public class DefaultControllerIdentifierStrategy implements
		ControllerIdentifierStrategy {

	private String basePackage;

	public String getControllerIdentifier(String controllerClassName) {
		List<String> parts = getControllerIdentifierParts(controllerClassName);

		// lowercamelize the controller name, which is last in the parts
		parts.set(
				parts.size() - 1,
				CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
						parts.get(parts.size() - 1)));

		// join the parts
		return "/" + Joiner.on("/").join(parts);

	}

	public List<String> getControllerIdentifierParts(String controllerClassName) {
		String name = controllerClassName;

		if (!Strings.isNullOrEmpty(basePackage)
				&& name.startsWith(basePackage + ".")) {
			// remove package
			name = controllerClassName.substring((basePackage + ".").length());

		}

		// remove Controller suffix
		if (name.endsWith("Controller")) {
			name = name.substring(0, name.length() - "Controller".length());
		}

		// split the name
		List<String> parts = Lists.newArrayList(Splitter.on(".").split(name));

		// remove a controller subpackage
		if (parts.size() >= 2) {
			if (parts.get(parts.size() - 2).equals("controller")) {
				parts.remove(parts.size() - 2);
			}
		}
		return parts;
	}

	@Override
	public String generateIdentifier(ControllerInfo info) {
		return null;
	}

	public String getBasePackage() {
		return basePackage;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

}
