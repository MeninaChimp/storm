package org.apache.storm.daemon.supervisor.cgroup;

import org.apache.storm.Config;
import org.apache.storm.daemon.supervisor.BasicContainer;
import org.apache.storm.daemon.supervisor.ExitCodeCallback;
import org.apache.storm.daemon.supervisor.cgroup.command.CgroupCommand;
import org.apache.storm.daemon.supervisor.cgroup.command.CgroupCommandManager;
import org.apache.storm.daemon.supervisor.cgroup.command.CgroupCommandType;
import org.apache.storm.generated.LocalAssignment;
import org.apache.storm.generated.WorkerResources;
import org.apache.storm.utils.ConfigUtils;
import org.apache.storm.utils.LocalState;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhenghao on 2018/9/29.
 */
public class CgroupContainer extends BasicContainer {

    private static final Logger LOG = LoggerFactory.getLogger(CgroupContainer.class);
    private CgroupCommandManager cgroupCommandManager = CgroupCommandManager.instance();

    /**
     * Create a new CgroupContainer
     *
     * @param type         the type of container being made.
     * @param conf         the supervisor config
     * @param supervisorId the ID of the supervisor this is a part of.
     * @param port         the port the container is on.  Should be <= 0 if only a partial recovery
     * @param assignment   the assignment for this container. Should be null if only a partial recovery.
     * @param localState   the local state of the supervisor.  May be null if partial recovery
     * @param workerId     the id of the worker to use.  Must not be null if doing a partial recovery.
     */
    public CgroupContainer(ContainerType type, Map<String, Object> conf, String supervisorId, int port, LocalAssignment assignment, LocalState localState, String workerId) throws IOException {
        super(type, conf, supervisorId, port, assignment, localState, workerId);
    }

    protected class CgroupProcessExitCallback implements ExitCodeCallback {
        private final String _logPrefix;
        private String workerId;
        private Map<String, Object> conf;

        public CgroupProcessExitCallback(String logPrefix, Map<String, Object> conf, String workerId) {
            this._logPrefix = logPrefix;
            this.workerId = workerId;
            this.conf = conf;
        }

        @Override
        public void call(int exitCode) {
            CgroupCommand command = cgroupCommandManager.create(this.conf, this.workerId);
            String shell = cgroupCommandManager.buildSheel(command, CgroupCommandType.DELETE);
            LOG.info("{} exited with code: {}, release group {}", _logPrefix, exitCode, command.getGroup());
            cgroupCommandManager.execCgroupShell(shell);
            _exitedEarly = true;
        }
    }

    @Override
    public void launch() throws IOException {
        _type.assertFull();
        LOG.info("Launching worker with assignment {} for this supervisor {} on port {} with id {}", _assignment,
                _supervisorId, _port, _workerId);
        String logPrefix = "Worker Process " + _workerId;
        CgroupProcessExitCallback cgroupProcessExitCallback = new CgroupProcessExitCallback(logPrefix, _conf, _workerId);
        _exitedEarly = false;

        final WorkerResources resources = _assignment.get_resources();
        final int memOnheap = getMemOnHeap(resources);
        final String stormRoot = ConfigUtils.supervisorStormDistRoot(_conf, _topologyId);
        final String jlp = javaLibraryPath(stormRoot, _conf);

        CgroupCommand command = cgroupCommandManager.create(_conf, _workerId);
        String cgroupShell = cgroupCommandManager.buildSheel(command, CgroupCommandType.CREATE);
        LOG.info("ready to create resource control for worker: {}", _workerId);
        boolean success = cgroupCommandManager.execCgroupShell(cgroupShell);
        if (success) {
            cgroupShell = cgroupCommandManager.buildSheel(command, CgroupCommandType.SET);
            LOG.info("ready to apply resource control for worker: {}", _workerId);
            success &= cgroupCommandManager.execCgroupShell(cgroupShell);
        }

        List<String> commandList = mkLaunchCommand(memOnheap, stormRoot, jlp, command, success);
        Map<String, String> topEnvironment = new HashMap<String, String>();
        @SuppressWarnings("unchecked")
        Map<String, String> environment = (Map<String, String>) _topoConf.get(Config.TOPOLOGY_ENVIRONMENT);
        if (environment != null) {
            topEnvironment.putAll(environment);
        }
        topEnvironment.put("LD_LIBRARY_PATH", jlp);

        LOG.info("Launching worker with command: {}. ", Utils.shellCmd(commandList));

        String workerDir = ConfigUtils.workerRoot(_conf, _workerId);
        launchWorkerProcess(commandList, topEnvironment, logPrefix, cgroupProcessExitCallback, new File(workerDir));
    }

    @Override
    protected void kill(long pid) throws IOException {
        super.kill(pid);
        CgroupCommand command = cgroupCommandManager.create(_conf, _workerId);
        String shell = cgroupCommandManager.buildSheel(command, CgroupCommandType.DELETE);
        cgroupCommandManager.execCgroupShell(shell);
    }

    /**
     * Create the command to launch the worker process
     *
     * @param memOnheap the on heap memory for the worker
     * @param stormRoot the root dist dir for the topology
     * @param jlp       java library path for the topology
     * @return the command to run
     * @throws IOException on any error.
     */
    protected List<String> mkLaunchCommand(final int memOnheap, final String stormRoot,
                                           final String jlp, CgroupCommand command, final boolean allowCgroup) throws IOException {
        final String javaCmd = javaCmd("java");
        final String stormOptions = ConfigUtils.concatIfNotNull(System.getProperty("storm.options"));
        final String stormConfFile = ConfigUtils.concatIfNotNull(System.getProperty("storm.conf.file"));
        final String workerTmpDir = ConfigUtils.workerTmpRoot(_conf, _workerId);

        List<String> classPathParams = getClassPathParams(stormRoot);
        List<String> commonParams = getCommonParams();

        List<String> commandList = new ArrayList<>();
        //Log Writer Command...
        commandList.add(javaCmd);
        commandList.addAll(classPathParams);
        commandList.addAll(substituteChildopts(_topoConf.get(Config.TOPOLOGY_WORKER_LOGWRITER_CHILDOPTS)));
        commandList.addAll(commonParams);
        commandList.add("org.apache.storm.LogWriter");

        if (allowCgroup) {
            String execShell = cgroupCommandManager.buildSheel(command, CgroupCommandType.EXEC);
            if (execShell != null) {
                String[] cmdArr = execShell.split(CgroupCommandManager.BLANK);
                Collections.addAll(commandList, cmdArr);
                LOG.info("worker: {} run with cgroup :{}", _workerId, command.toString());
            }
        }

        //Worker Command...
        commandList.add(javaCmd);
        commandList.add("-server");
        commandList.addAll(commonParams);
        commandList.addAll(substituteChildopts(_conf.get(Config.WORKER_CHILDOPTS), memOnheap));
        commandList.addAll(substituteChildopts(_topoConf.get(Config.TOPOLOGY_WORKER_CHILDOPTS), memOnheap));
        commandList.addAll(substituteChildopts(OR(
                _topoConf.get(Config.TOPOLOGY_WORKER_GC_CHILDOPTS),
                _conf.get(Config.WORKER_GC_CHILDOPTS)), memOnheap));
        commandList.addAll(getWorkerProfilerChildOpts(memOnheap));
        commandList.add("-Djava.library.path=" + jlp);
        commandList.add("-Dstorm.conf.file=" + stormConfFile);
        commandList.add("-Dstorm.options=" + stormOptions);
        commandList.add("-Djava.io.tmpdir=" + workerTmpDir);
        commandList.addAll(classPathParams);
        commandList.add("org.apache.storm.daemon.worker");
        commandList.add(_topologyId);
        commandList.add(_supervisorId);
        commandList.add(String.valueOf(_port));
        commandList.add(_workerId);

        return commandList;
    }
}
