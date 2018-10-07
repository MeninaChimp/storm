package org.apache.storm.daemon.supervisor.cgroup.subsystem;

import org.apache.storm.daemon.supervisor.cgroup.SubSystemType;
import org.apache.storm.daemon.supervisor.cgroup.resource.CpuResourceBenchmark;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhenghao on 2018/9/30.
 */
public class SubSystemCpu implements SubSystem{

    public static final String CPU_SHARE = "cpu.shares";
    public static final String CPU_CORE_LIMIT= "cpu.cfs_quota_us";
    public static final String CPU_CORE_BENCHMARK = "cpu.cfs_period_us";

    private Long shares;
    private Long core;

    public Long getShares() {
        return shares;
    }

    public Long getCore() {
        return core;
    }

    private SubSystemCpu(Long shares, Long core) {
        this.shares = shares;
        this.core = core;
    }

    @Override
    public SubSystemType getType() {
        return SubSystemType.CPU;
    }

    @Override
    public String rootHierarchy() {
        return this.getType().getName();
    }

    @Override
    public Map<String, Long> paramters() {
        Map<String, Long> map = new HashMap<>();
        map.put(CPU_CORE_BENCHMARK, CpuResourceBenchmark.CPU_CORE_BENCHMARK.value());

        if(core != null){
            map.put(CPU_CORE_LIMIT, this.core);
        }

        if(shares != null){
            map.put(CPU_SHARE, this.shares);
        }

        return map;
    }

    public static SubSystemCpuBuilder builder(){
        return new SubSystemCpuBuilder();
    }

    public static class SubSystemCpuBuilder{
        private Long shares;
        private Long core;

        public SubSystemCpuBuilder shares(Long num, CpuResourceBenchmark benchmark) {
            this.shares = benchmark.value() * num;
            return this;
        }

        public SubSystemCpuBuilder core(Long num, CpuResourceBenchmark benchmark) {
            this.core = benchmark.value() * num;
            return this;
        }

        public SubSystemCpu build(){
            return new SubSystemCpu(shares, core);
        }
    }
}
