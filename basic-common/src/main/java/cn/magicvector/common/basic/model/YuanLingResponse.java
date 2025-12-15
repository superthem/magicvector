package cn.magicvector.common.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class YuanLingResponse<T> implements Serializable {

    private Integer iStatus;

    private String sInfo;

    private String sType;

    private Object aError;

    private Integer mPrimary;

    private T mData;

    private T kData;

    private List<T> aLists;

    public Integer getiStatus() {
        return iStatus;
    }

    public void setiStatus(Integer iStatus) {
        this.iStatus = iStatus;
    }

    public T getmData() {
        return mData;
    }

    public void setmData(T mData) {
        this.mData = mData;
    }

    public T getkData() {
        return kData;
    }

    public void setkData(T kData) {
        this.kData = kData;
    }

    public List<T> getaLists() {
        return aLists;
    }

    public void setaLists(List<T> aLists) {
        this.aLists = aLists;
    }

    public String getsInfo() {
        return sInfo;
    }

    public void setsInfo(String sInfo) {
        this.sInfo = sInfo;
    }

    public String getsType() {
        return sType;
    }

    public void setsType(String sType) {
        this.sType = sType;
    }

    public Object getaError() {
        return aError;
    }

    public void setaError(Object aError) {
        this.aError = aError;
    }

    public Integer getmPrimary() {
        return mPrimary;
    }

    public void setmPrimary(Integer mPrimary) {
        this.mPrimary = mPrimary;
    }
}
