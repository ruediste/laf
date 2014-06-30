package laf.component.reqestProcessing;

import java.util.Map;

import laf.base.ViewTechnology;
import laf.base.configuration.ConfigurationParameter;
import laf.requestProcessing.RequestProcessor;

public interface InvokeReloadReqestProcessorsCP
		extends
		ConfigurationParameter<Map<Class<? extends ViewTechnology>, RequestProcessor>> {

}
