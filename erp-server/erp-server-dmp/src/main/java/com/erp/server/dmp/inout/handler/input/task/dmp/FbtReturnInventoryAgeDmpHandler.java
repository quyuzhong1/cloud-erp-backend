package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.erp.server.dmp.inout.handler.input.task.dmp.eccang.EccangReturnInventoryAgeDmpHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * FBT inventory-age DMP handler.
 */
@Slf4j
@Service
@Scope("prototype")
public class FbtReturnInventoryAgeDmpHandler extends EccangReturnInventoryAgeDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("FbtReturnInventoryAgeDmpHandler afterConvertData");
        super.afterConvertData(dmpInputDataDmpRelationMaps);
    }
}
