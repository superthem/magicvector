package com.magicvector.common.basic.model;

import lombok.Data;

@Data
public class SignPdf {

    private int pageNum;
    private String imageUrl;
    private float imageWidth;
    private float imageHeight;
    private float left;
    private float bottom;
}
