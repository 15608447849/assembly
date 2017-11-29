package com.m.backup.slice;

/**
 * Created by user on 2017/11/21.
    分片信息
 */
public class SliceInfo extends Slice{
    private String adler32Hex;
    private String md5Hex;

    public String getAdler32Hex() {
        return adler32Hex;
    }

    public void setAdler32Hex(String adler32Hex) {
        this.adler32Hex = adler32Hex;
    }

    public String getMd5Hex() {
        return md5Hex;
    }

    public void setMd5Hex(String md5Hex) {
        this.md5Hex = md5Hex;
    }

}
