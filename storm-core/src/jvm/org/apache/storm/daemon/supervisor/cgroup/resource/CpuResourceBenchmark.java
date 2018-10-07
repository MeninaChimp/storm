package org.apache.storm.daemon.supervisor.cgroup.resource;

/**
 * Created by zhenghao on 2018/9/30.
 */
public enum CpuResourceBenchmark implements ResourceBenchmark{

    CPU_SHARE(1024L),
    CPU_CORE_BENCHMARK(100*1000L),
    CPU_CORE_LIMIT(100*1000L);

    private Long benchmark;

    CpuResourceBenchmark(Long benchmark) {
        this.benchmark = benchmark;
    }

    @Override
    public Long value(){
        return benchmark;
    }
}
