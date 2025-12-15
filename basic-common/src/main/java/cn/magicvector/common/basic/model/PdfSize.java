package cn.magicvector.common.basic.model;

import lombok.Data;

@Data
public class PdfSize {

    private String key;
    private String biz;
    private String extension;
    private String pdfUrl;
    private int pageNum;
    private float width;
    private float height;
}
