package com.erp.server.dmp.inout.handler.output.create;

import com.erp.model.dmp.enums.DmpOutputTaskTypeEnum;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * dmp输出创建历史任务处理器
 *
 */
@Service
public class DmpOutputHistoryCreateHandler extends DmpOutputDetailCreateHandler {
    @Override
    public DmpOutputTaskTypeEnum getDmpOutputTaskTypeEnum() {
        return DmpOutputTaskTypeEnum.HISTORY;
    }

    @Override
    public Set<DmpOutputTaskTypeEnum> getIngTaskType() {
        Set<DmpOutputTaskTypeEnum> set = new HashSet<>();
        set.add(DmpOutputTaskTypeEnum.NORMAL);
        set.add(DmpOutputTaskTypeEnum.HISTORY);
        return set;
    }
}
