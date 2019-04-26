package edu.wgu.links;

public class CleanURL {
	private String url;
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLandingPage() {
		return landingPage;
	}

	public void setLandingPage(String landingPage) {
		this.landingPage = landingPage;
	}

	public String getMobilePlatform() {
		return mobilePlatform;
	}

	public void setMobilePlatform(String mobilePlatform) {
		this.mobilePlatform = mobilePlatform;
	}

	public boolean isCompatible() {
		return compatible;
	}

	public void setCompatible(boolean compatible) {
		this.compatible = compatible;
	}

	private String landingPage;
	private String mobilePlatform;
	private boolean compatible;

	@Override
	public String toString() {
		return
				url + ',' +
				landingPage + ',' +
				mobilePlatform + ',' + compatible;
	}

	public CleanURL(String url, String landingPage, String mobilePlatform, boolean compatible) {
		this.url = url;
		this.landingPage = landingPage;
		this.mobilePlatform = mobilePlatform;
		this.compatible = compatible;
	}
}
