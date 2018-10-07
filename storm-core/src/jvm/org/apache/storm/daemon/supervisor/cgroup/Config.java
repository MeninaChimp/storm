package org.apache.storm.daemon.supervisor.cgroup;

/**
 * Created by zhenghao on 2018/09/30.
 */
public class Config {

    /**
     * switch
     */
    public static final String CGROUP_ENABLE = "supervisor.cgroup.enable";
    public static final String CGROUP_CPU_ENABLE = "cgroup.cpu.enable";

    /**
     * group
     */
    public static final String CGROUP_BASE_GROUP = "cgroup.base.group";
    public static final String DEFAULT_CGROUP_BASE_GROUP = "/storm/";

    /**
     * cpu
     */
    public static final String CPU_CORE_LIMIT = "cpu.core.limit";
    public static final String CPU_SHARE = "cpu.share";
    public static final Long DEAFULT_CPU_CORE_LIMIT = 3L;
    public static final Long MAX_CPU_CORE_LIMIT = 4L;
    public static final Long DEFAULT_CPU_SHARE = 1L;
}
