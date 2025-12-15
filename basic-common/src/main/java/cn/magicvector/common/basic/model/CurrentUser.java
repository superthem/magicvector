package cn.magicvector.common.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class CurrentUser implements Serializable {

	private String token;

	private Map<String, Object> userProps;

	private String loginUrl;

	public CurrentUser(){
		userProps = new HashMap<>();
	}

}
