package org.apache.storm.daemon.supervisor.cgroup;

import org.apache.storm.daemon.supervisor.Container;
import org.apache.storm.daemon.supervisor.ContainerLauncher;
import org.apache.storm.daemon.supervisor.Killable;
import org.apache.storm.generated.LocalAssignment;
import org.apache.storm.utils.LocalState;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zhenghao on 2018/9/29.
 */
public class CgroupContainerLauncher extends ContainerLauncher {

    private final Map<String, Object> _conf;
    private final String _supervisorId;

    public CgroupContainerLauncher(Map<String, Object> conf, String supervisorId) throws IOException {
        _conf = conf;
        _supervisorId = supervisorId;
    }

    @Override
    public Container launchContainer(int port, LocalAssignment assignment, LocalState state) throws IOException {
        Container container = new CgroupContainer(Container.ContainerType.LAUNCH, _conf, _supervisorId, port, assignment,
                state, null);
        container.setup();
        container.launch();
        return container;
    }

    @Override
    public Container recoverContainer(int port, LocalAssignment assignment, LocalState state) throws IOException {
        return new CgroupContainer(Container.ContainerType.RECOVER_FULL, _conf, _supervisorId, port, assignment,
                state, null);
    }

    @Override
    public Killable recoverContainer(String workerId, LocalState localState) throws IOException {
        return new CgroupContainer(Container.ContainerType.RECOVER_PARTIAL, _conf, _supervisorId, -1, null,
                localState, workerId);
    }
}
