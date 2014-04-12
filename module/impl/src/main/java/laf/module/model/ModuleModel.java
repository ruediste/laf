package laf.module.model;

import java.util.*;

import laf.module.pattern.ClassPattern;

public class ModuleModel {
	private final ProjectModel projectModel;
	private final String qualifiedNameOfRepresentingClass;
	final Set<ClassModel> classes = new HashSet<ClassModel>();
	final Set<ClassPattern> inclusionPatterns = new HashSet<>();
	final Set<ClassPattern> exclusionPatterns = new HashSet<>();

	/**
	 * Qualified names of the representing classes of the modules directly
	 * exported by this module.
	 */
	final Set<String> exportedModuleNames = new HashSet<>();

	/**
	 * Qualified names of the representing classes of the modules directly
	 * imported by this module. Only classes from these modules are accessible
	 * by this module. The modules in {@link #exportedModuleNames} are not
	 * accessible unless included in this set.
	 */
	final Set<String> importedModuleNames = new HashSet<>();

	/**
	 * Modules directly exported by this module.
	 */
	final Set<ModuleModel> exportedModules = new HashSet<>();

	/**
	 * Modules directly imported by this module. Only classes from these modules
	 * are accessible by this module. The modules in {@link #exportedModules}
	 * are not accessible unless included in this set.
	 */
	final Set<ModuleModel> importedModules = new HashSet<>();

	/**
	 * All Modules accessible by this module. Typically includes this module
	 * itself.
	 */
	final Set<ModuleModel> allAccessibleModules = new HashSet<>();

	/**
	 * All Modules exported by this module. Typically includes this module
	 * itself.
	 */
	final Set<ModuleModel> allExportedModules = new HashSet<>();

	public ModuleModel(ProjectModel projectModel,
			String qualifiedNameOfRepresentingClass) {
		this.projectModel = projectModel;
		this.qualifiedNameOfRepresentingClass = qualifiedNameOfRepresentingClass;
		projectModel.addModule(this);
	}

	public ProjectModel getProjectModel() {
		return projectModel;
	}

	public String getQualifiedNameOfRepresentingClass() {
		return qualifiedNameOfRepresentingClass;
	}

	public Set<ClassModel> getClasses() {
		return Collections.unmodifiableSet(classes);
	}

	public void addClass(ClassModel clazz) {
		if (clazz.module != null) {
			clazz.module.classes.remove(clazz);
		}
		classes.add(clazz);
		clazz.module = this;
	}

	public void resolveDependencies() {
		// resolve imports
		for (String name : importedModuleNames) {
			ModuleModel module = projectModel.getModule(name);
			if (module == null) {
				throw new RuntimeException("Module "
						+ qualifiedNameOfRepresentingClass
						+ ": could not find imported module " + name);
			}
			importedModules.add(module);
		}

		// resolve exports
		for (String name : exportedModuleNames) {
			ModuleModel module = projectModel.getModule(name);
			if (module == null) {
				throw new RuntimeException("Module "
						+ qualifiedNameOfRepresentingClass
						+ ": could not find exported module " + name);
			}
			exportedModules.add(module);
		}

	}

	public void addInclusionPattern(ClassPattern pattern) {
		inclusionPatterns.add(pattern);
	}

	public void addExclusionPattern(ClassPattern pattern) {
		exclusionPatterns.add(pattern);
	}

	public void addImportedModuleName(String qualifiedNameOfRepresentingClass) {
		importedModuleNames.add(qualifiedNameOfRepresentingClass);
	}

	public void addExportedModuleName(String qualifiedNameOfRepresentingClass) {
		exportedModuleNames.add(qualifiedNameOfRepresentingClass);
	}

	public boolean isIncluded(ClassModel model) {
		return isIncluded(model.getQualifiedName());
	}

	private boolean isIncluded(String qualifiedName) {
		ClassPattern bestInclusionMatch = ClassPattern.getBestMatch(
				inclusionPatterns, qualifiedName);
		ClassPattern bestExclusionMatch = ClassPattern.getBestMatch(
				exclusionPatterns, qualifiedName);
		if (bestInclusionMatch == null) {
			return false;
		}

		// there is an inclusion match

		if (bestExclusionMatch == null) {
			return true;
		}

		// there are two matches

		if (bestExclusionMatch.getScore() == bestInclusionMatch.getScore()) {
			throw new RuntimeException("Module "
					+ qualifiedNameOfRepresentingClass
					+ ": contradicting patterns found for class "
					+ qualifiedName + ": inclusion pattern "
					+ bestInclusionMatch + "; exclusion pattern "
					+ bestExclusionMatch);
		}

		return bestInclusionMatch.getScore() > bestExclusionMatch.getScore();
	}

	@Override
	public String toString() {
		return qualifiedNameOfRepresentingClass;
	}

	public boolean isAccessible(ClassModel clazz) {
		for (ModuleModel module : allAccessibleModules) {
			if (module.classes.contains(clazz)) {
				return true;
			}
		}
		return false;
	}

	public String details() {
		StringBuilder sb = new StringBuilder();
		sb.append("Module " + qualifiedNameOfRepresentingClass + "\n");
		sb.append("imported: " + importedModules + "\n");
		sb.append("exported: " + exportedModules + "\n");
		sb.append("all accessible: " + allAccessibleModules + "\n");
		sb.append("all exported: " + allExportedModules + "\n");
		sb.append("included: " + inclusionPatterns + "\n");
		sb.append("excluded: " + exclusionPatterns + "\n");
		sb.append("classes: " + classes + "\n");
		return sb.toString();
	}

	public String getPackage() {
		String[] parts = qualifiedNameOfRepresentingClass.split("\\.");
		if (parts.length == 1) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length - 1; i++) {
			if (i > 0) {
				sb.append(".");
			}
			sb.append(parts[i]);
		}
		return sb.toString();
	}
}
