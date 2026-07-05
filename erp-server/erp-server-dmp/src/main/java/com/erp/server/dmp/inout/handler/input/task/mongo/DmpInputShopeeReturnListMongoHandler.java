package com.erp.server.dmp.inout.handler.input.task.mongo;

import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Shopee 售后退货列表 mongo 处理器。
 */
@Service
@Scope("prototype")
public class DmpInputShopeeReturnListMongoHandler extends DmpInputBaseMongoHandler {

    @Override
    protected List<String> getNextLevelIdList(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        return Collections.singletonList("");
    }
}
