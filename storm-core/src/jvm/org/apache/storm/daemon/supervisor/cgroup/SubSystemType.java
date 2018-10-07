package org.apache.storm.daemon.supervisor.cgroup;

/**
 * Created by zhenghao on 2018/9/29.
 */
public enum SubSystemType {

    CPU("cpu"),
    MEMORY("memory"),
    DISK("blkio");

    private String name;

    SubSystemType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
