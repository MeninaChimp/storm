package org.apache.storm.daemon.supervisor.cgroup.command;

/**
 * Created by zhenghao on 2018/9/30.
 */
public enum CgroupCommandType {

    CREATE("cgcreate"),
    SET("cgset"),
    DELETE("cgdelete"),
    EXEC("cgexec");

    private String command;

    CgroupCommandType(String command) {
        this.command = command;
    }

    public String command(){
        return this.command;
    }
}
