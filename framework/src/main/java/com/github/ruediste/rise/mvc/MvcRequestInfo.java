package com.github.ruediste.rise.mvc;

import com.github.ruediste.rise.core.persistence.TransactionControl;
import com.github.ruediste.rise.core.scopes.RequestScoped;

@RequestScoped
public class MvcRequestInfo {

	private boolean updating;

	private TransactionControl transactionControl;

	public MvcRequestInfo self() {
		return this;
	}

	public void setIsUpdating(boolean updating) {
		this.setUpdating(updating);
	}

	public boolean isUpdating() {
		return updating;
	}

	public void setUpdating(boolean updating) {
		this.updating = updating;
	}

	public void setTransactionControl(TransactionControl transactionControl) {
		this.transactionControl = transactionControl;

	}

	public TransactionControl getTransactionControl() {
		return transactionControl;
	}
}