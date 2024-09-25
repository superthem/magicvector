package com.magicvector.common.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class YuanLingRequest<T> implements Serializable {

    private String sCallback;

    private List<T> aParameters;

}
