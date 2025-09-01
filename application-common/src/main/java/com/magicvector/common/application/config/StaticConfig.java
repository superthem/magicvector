package com.magicvector.common.application.config;

import com.github.tbwork.anole.loader.Anole;

public class StaticConfig {
	
	public static final int ONE_YEAR = 365*24*3600;

	public static final String SESSION_CACHE_GROUP_NAME = Anole.getProperty("user.session.cache.key.prefix");
}
