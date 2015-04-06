package com.github.ruediste.laf.core.web.resource;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.ruediste.laf.core.web.resource.Processors;
import com.github.ruediste.laf.core.web.resource.Resource;

public class ProcessorsTest {

	@Test
	public void testDataEqualityTracker() {
		Resource r = new TestResourceImpl("foo", "bar");
		Processors processors = new Processors();

		Object identifier = new Object();
		Resource r1 = processors.wrapProcessor(null, identifier).apply(r);
		Resource r2 = processors.wrapProcessor(null, identifier).apply(r);

		assertTrue(r1.containsSameDataAs(r2));
		assertFalse(r1.containsSameDataAs(r));
	}
}