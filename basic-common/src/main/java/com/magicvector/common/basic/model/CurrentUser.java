package com.magicvector.common.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class CurrentUser implements Serializable {

	private String userId;

	private String username;

	private String nickname;

	private String phone;

	/**
	 * 用户的额外信息（注意别和上下文的额外信息弄混，这里只放用户的额外信息，如出生年月等）
	 */
	private Map<String, String> ext;

	public CurrentUser(){
		ext = new HashMap<>();
	}

}
