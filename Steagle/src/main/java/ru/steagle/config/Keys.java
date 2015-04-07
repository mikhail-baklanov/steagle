package ru.steagle.config;

public enum Keys {

	USE_MOBILE_INET("useMobileInet"), AUTO_LOAD("autoLoad"), SOUND_NOTIFY("soundNotify"),
    VIBRO_NOTIFY("vibroNotify"), AUTO_LOGIN("autoLogin"), LOGIN("login"), PASSWORD("password"),
    HISTORY_LIMIT("historyLimit");

	private String prefKey;

	public String getPrefKey() {
		return prefKey;
	}

	private Keys(String prefKey) {
		this.prefKey = prefKey;
	}
}
