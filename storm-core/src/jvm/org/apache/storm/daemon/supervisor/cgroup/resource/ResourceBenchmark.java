package org.apache.storm.daemon.supervisor.cgroup.resource;

/**
 * Created by zhenghao on 2018/9/30.
 */
public interface ResourceBenchmark {

    /**
     * 基准值固化
     * @return
     */
    Long value();
}
