package org.apache.storm.daemon.supervisor.cgroup.subsystem;

import org.apache.storm.daemon.supervisor.cgroup.SubSystemType;

import java.util.Map;

/**
 * Created by zhenghao on 2018/9/30.
 */
public interface SubSystem {

    SubSystemType getType();

    String rootHierarchy();

    Map<String, Long> paramters();

}
