package laf.module.model;

import java.util.*;

public class ClassModel {

	private final ProjectModel projectModel;
	private final String qualifiedName;

	public ClassModel(ProjectModel projectModel, String qualifiedName) {
		this.projectModel = projectModel;
		this.qualifiedName = qualifiedName;
		projectModel.addClass(this);
	}

	ModuleModel module;

	final Set<ClassModel> usesClasses = new HashSet<>();
	final Set<String> usesClassNames = new HashSet<>();

	public String getQualifiedName() {
		return qualifiedName;
	}

	public ProjectModel getProjectModel() {
		return projectModel;
	}

	public Set<ClassModel> getUsesClasses() {
		return Collections.unmodifiableSet(usesClasses);
	}

	public void addUsesClass(ClassModel clazz) {
		usesClasses.add(clazz);
	}

	public Set<String> getUsesClassNames() {
		return Collections.unmodifiableSet(usesClassNames);
	}

	public void addUsesClassName(String name) {
		usesClassNames.add(name);
	}

	public ModuleModel getModule() {
		return module;
	}

	public void setModule(ModuleModel module) {
		if (this.module != null) {
			this.module.classes.remove(this);
		}
		this.module = module;
		if (module != null) {
			module.addClass(this);
		}
	}

	public void resolveDependencies() {
		// resolve usage dependencies
		for (String usesClassName : usesClassNames) {
			ClassModel classModel = projectModel.getClassModel(usesClassName);
			if (classModel != null) {
				addUsesClass(classModel);
			}
		}

		// resolve module
		Set<ModuleModel> matchingModules = projectModel
				.getMatchingModules(this);
		if (matchingModules.size() > 1) {
			throw new RuntimeException("Multiple Modules found for class "
					+ qualifiedName + ": " + matchingModules);
		}

		if (matchingModules.size() == 1) {
			setModule(matchingModules.iterator().next());
		}
	}

	public void checkDependencies(ArrayList<String> errors) {
		if (module == null) {
			return;
		}

		for (ClassModel clazz : usesClasses) {
			if (!module.isAccessible(clazz)) {
				errors.add("Class " + this + " references class " + clazz
						+ ", which is not accessible for classes in module "
						+ module);
			}
		}
	}

	@Override
	public String toString() {
		return qualifiedName;
	}

	public String details() {
		StringBuilder sb = new StringBuilder();
		sb.append("Class " + qualifiedName + "\n");
		sb.append("module: " + module + "\n");
		sb.append("usesClasses: " + usesClasses + "\n");
		return sb.toString();
	}
}
