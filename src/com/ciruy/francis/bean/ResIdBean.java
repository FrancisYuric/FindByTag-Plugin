package com.ciruy.francis.bean;

/**
 * Created by Ciruy on 2019/2/27.
 * Description:储存资源id和名字键值对
 */
public class ResIdBean {
    //simpleName
    private String name;
    private String canonicalName;
    //id
    private String id;
    //tag
    private String tag;

    public ResIdBean(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public ResIdBean(String name, String id, String tag) {
        this.name = name;
        this.id = id;
        this.tag = tag;
    }

    public ResIdBean(String name, String canonicalName, String id, String tag) {
        this.name = name;
        this.canonicalName = canonicalName;
        this.id = id;
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
