package com.github.ruediste.laf.core.web.resource;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.github.ruediste.laf.core.web.resource.PathGroup;

public class PathGroupTest {

	@Test
	public void testInsertMin() throws Exception {
		PathGroup group = new PathGroup(null, (List<String>) null);
		assertEquals("foo.min.bar", group.insertMin("foo.bar"));
		assertEquals("bar", group.insertMin("bar"));
	}

}