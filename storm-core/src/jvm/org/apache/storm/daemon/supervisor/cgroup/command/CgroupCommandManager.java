package org.apache.storm.daemon.supervisor.cgroup.command;

import org.apache.commons.collections.map.HashedMap;
import org.apache.storm.daemon.supervisor.cgroup.Config;
import org.apache.storm.daemon.supervisor.cgroup.resource.CpuResourceBenchmark;
import org.apache.storm.daemon.supervisor.cgroup.subsystem.SubSystem;
import org.apache.storm.daemon.supervisor.cgroup.subsystem.SubSystemCpu;
import org.apache.storm.utils.ShellUtils;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhenghao on 2018/9/30.
 */
public class CgroupCommandManager {

    private static final Logger LOG = LoggerFactory.getLogger(CgroupCommandManager.class);
    private static final Object SYNC = new Object();
    private static CgroupCommandManager instance;
    private static String FILESEPARATOR = System.getProperty("file.separator");
    public static final String BLANK = " ";

    private CgroupCommandManager() {
    }

    /**
     * 实例
     *
     * @return
     */
    public static CgroupCommandManager instance() {
        if (instance == null) {
            synchronized (SYNC) {
                if (instance == null) {
                    instance = new CgroupCommandManager();
                }
            }
        }

        return instance;
    }

    /**
     * 创建Cgroup命令
     *
     * @param conf
     * @param group
     * @return
     */
    public CgroupCommand create(Map<String, Object> conf, String group) {
        if (conf == null || group == null) {
            LOG.warn("need conf && group");
            return null;
        }

        List<SubSystem> subSystems = new ArrayList<>();
        if (Utils.getBoolean(conf.get(Config.CGROUP_CPU_ENABLE), false)) {
            Long core = Utils.getLong(conf.get(Config.CPU_CORE_LIMIT), Config.DEAFULT_CPU_CORE_LIMIT);
            Long share = Utils.getLong(conf.get(Config.CPU_SHARE), Config.DEFAULT_CPU_SHARE);
            core = core > Config.MAX_CPU_CORE_LIMIT ? Config.MAX_CPU_CORE_LIMIT : core;
            SubSystem cpu = SubSystemCpu.builder()
                    .core(core, CpuResourceBenchmark.CPU_CORE_LIMIT)
                    .shares(share, CpuResourceBenchmark.CPU_SHARE)
                    .build();

            subSystems.add(cpu);
        }

        if (subSystems.size() == 0) {
            LOG.warn("no enable subsystem");
        }

        Object baseGroup = conf.get(Config.CGROUP_BASE_GROUP);
        if (baseGroup == null) {
            baseGroup = Config.DEFAULT_CGROUP_BASE_GROUP;
        } else {
            if (!baseGroup.toString().startsWith(FILESEPARATOR)) {
                baseGroup = FILESEPARATOR + baseGroup;
            }
            if (!baseGroup.toString().endsWith(FILESEPARATOR)) {
                baseGroup = baseGroup + FILESEPARATOR;
            }
        }

        return new CgroupCommand(subSystems, baseGroup + group);
    }

    /**
     * Cgroup命令生成器
     *
     * @param command
     * @param commandType
     * @return
     */
    public String buildSheel(CgroupCommand command, CgroupCommandType commandType) {
        if (command == null) {
            return BLANK;
        }

        switch (commandType) {
            case SET:
                return this.buildSetShell(command, CgroupCommandType.SET);
            default:
                return this.buildCommonShell(command, commandType);
        }
    }

    public boolean execCgroupShell(String shell) {
        String[] setShellCmd = {"bash", "-c", shell};
        LOG.info("ready for exec shell: {}", shell);
        try{
            ShellUtils.ShellCommandExecutor executor = new ShellUtils.ShellCommandExecutor(setShellCmd);
            executor.execute();
            int exitcode = executor.getExitCode();
            if (exitcode != 0) {
                LOG.error("Error exec shell: {}", shell);
                return false;
            }
        }catch (Exception e){
            LOG.error("Swallow exec exception: " + e.getMessage(), e);
            return false;
        }

        return true;
    }

    private String buildCommonShell(CgroupCommand command, CgroupCommandType type) {
        StringBuilder builder = new StringBuilder();
        builder.append(type.command());
        builder.append(BLANK);
        for (SubSystem subSystem : command.getSubSystems()) {
            builder.append("-g");
            builder.append(BLANK);
            builder.append(subSystem.rootHierarchy());
            builder.append(":");
            builder.append(command.getGroup());
            builder.append(BLANK);
        }

        return builder.toString().trim();
    }

    private String buildSetShell(CgroupCommand command, CgroupCommandType type) {
        StringBuilder builder = new StringBuilder();
        builder.append(type.command());
        builder.append(BLANK);
        for (SubSystem subSystem : command.getSubSystems()) {
            Map<String, Long> paramter = subSystem.paramters();
            for (String key : paramter.keySet()) {
                builder.append("-r");
                builder.append(BLANK);
                builder.append(key);
                builder.append("=");
                builder.append(paramter.get(key));
                builder.append(BLANK);
            }
        }

        builder.append(command.getGroup());
        return builder.toString().trim();
    }

    public static void main(String[] args) throws IOException {
        Map<String, Object> conf = new HashedMap();
        conf.put(Config.CPU_CORE_LIMIT, 1L);
        conf.put(Config.CPU_SHARE, 1L);
        conf.put(Config.CGROUP_CPU_ENABLE, true);
        conf.put(Config.CGROUP_BASE_GROUP, "storm");
        CgroupCommandManager cgroupCommandManager = CgroupCommandManager.instance();
        CgroupCommand command = cgroupCommandManager.create(conf, "foo");
        assert command != null;
        assert command.getSubSystems() != null;
        String create = cgroupCommandManager.buildSheel(command, CgroupCommandType.CREATE);
        assert create != null;
        assert create.equals("cgcreate -g cpu:/storm/foo");
        String delete = cgroupCommandManager.buildSheel(command, CgroupCommandType.DELETE);
        assert delete != null;
        assert delete.equals("cgdelete -g cpu:/storm/foo");
        String exec = cgroupCommandManager.buildSheel(command, CgroupCommandType.EXEC);
        assert exec != null;
        assert exec.equals("cgexec -g cpu:/storm/foo");
        String set = cgroupCommandManager.buildSheel(command, CgroupCommandType.SET);
        assert set != null;
        assert set.equals("cgset -r cpu.cfs_period_us=100000 -r cpu.cfs_quota_us=100000 -r cpu.shares=1024 /storm/foo");
        cgroupCommandManager.execCgroupShell(create);
    }
}
