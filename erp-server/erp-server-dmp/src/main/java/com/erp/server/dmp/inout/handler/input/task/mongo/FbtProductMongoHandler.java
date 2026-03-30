package com.erp.server.dmp.inout.handler.input.task.mongo;

import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * FBT产品MongoDB处理Handler
 * 处理FBT商品数据存储到MongoDB
 *
 * @author System
 * @since 2026-02-10
 */
@Service
@Scope("prototype")
public class FbtProductMongoHandler extends DmpInputBaseMongoHandler {
    
    @Override
    protected List<String> getNextLevelIdList(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        return Collections.singletonList("");
    }
}
