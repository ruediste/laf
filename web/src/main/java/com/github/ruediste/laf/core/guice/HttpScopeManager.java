package com.github.ruediste.laf.core.guice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manager for {@link RequestScoped @RequestScoped} and {@link SessionScoped
 * @SessionScoped}
 */
public interface HttpScopeManager {

	/**
	 * Enter the scopes
	 */
	public abstract void enter(HttpServletRequest request,
			HttpServletResponse response);

	/**
	 * Leave the scope. Always call from a finally block, to make sure scopes
	 * are correctly cleaned up
	 */
	public abstract void exit();

}