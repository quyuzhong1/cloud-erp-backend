package com.erp.server.dmp.inout.handler.output.create;

import com.erp.model.dmp.enums.DmpOutputTaskTypeEnum;
import org.springframework.stereotype.Service;

/**
 * dmp输出创建正常任务处理器
 *
 */
@Service
public class DmpOutputNormalCreateHandler extends DmpOutputDetailCreateHandler {
    @Override
    public DmpOutputTaskTypeEnum getDmpOutputTaskTypeEnum() {
        return DmpOutputTaskTypeEnum.NORMAL;
    }

}
