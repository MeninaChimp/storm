package org.apache.storm.daemon.supervisor.cgroup.command;

import org.apache.storm.daemon.supervisor.cgroup.subsystem.SubSystem;

import java.util.List;

/**
 * Created by zhenghao on 2018/9/29.
 */
public class CgroupCommand {

    private List<SubSystem> subSystems;

    private String group;

    public CgroupCommand(List<SubSystem> subSystems, String group) {
        this.subSystems = subSystems;
        this.group = group;
    }

    public List<SubSystem> getSubSystems() {
        return subSystems;
    }

    public String getGroup() {
        return group;
    }

}
