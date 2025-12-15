package cn.magicvector.common.basic.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class File {

    private String key;

    private String biz;

    private String extension;

    private String path;
}
