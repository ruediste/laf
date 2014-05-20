package laf.base;

import javax.inject.Singleton;

import laf.attachedProperties.AttachedPropertiesModule;
import laf.configuration.ConfigurationModule;

import org.jabsaw.Module;

@Module(description = "Base classes of the LAF Framework", exported = {
		AttachedPropertiesModule.class, ConfigurationModule.class }, hideFromDependencyGraphOutput = true)
@Singleton
public class BaseModule {

	public enum ProjectStage {
		DEVELOPMENT, PRODUCTION,
	}

	private ProjectStage projectStage;

	public ProjectStage getProjectStage() {
		return projectStage;
	}

	public void setProjectStage(ProjectStage projectStage) {
		this.projectStage = projectStage;
	}
}