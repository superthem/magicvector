package com.magicvector.common.basic.model;

import lombok.Data;

import java.util.List;

@Data
public class ImagePdf {

    private String pdfUrl;
    private String fileName;
    private List<SignPdf> signPdfs;


}
