package com.github.ruediste.laf.core.web.assetPipeline;

/**
 * Asset delegating to another. Can be used as base class if only one aspect of
 * an asset needs to be modified.
 */
public class DelegatingAsset implements Asset {

	private Asset delegate;

	public DelegatingAsset(Asset delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public AssetType getAssetType() {
		return delegate.getAssetType();
	}

	@Override
	public String getContentType() {
		return delegate.getContentType();
	}

	@Override
	public byte[] getData() {
		return delegate.getData();
	}
}