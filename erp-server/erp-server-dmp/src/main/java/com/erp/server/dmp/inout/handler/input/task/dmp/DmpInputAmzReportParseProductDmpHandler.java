package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.sdk.oms.amz.spapi.dto.ReportListingMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonListingStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.fbainventory.InventoryDetails;
import com.erp.sdk.oms.amz.spapi.model.fbainventory.ResearchingQuantity;
import com.erp.sdk.oms.amz.spapi.model.fbainventory.ReservedQuantity;
import com.erp.sdk.oms.amz.spapi.model.fbainventory.UnfulfillableQuantity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportParseProductDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAmzReportParseProductDmpHandler afterConvertData 处理");
        super.afterConvertData(dmpInputDataDmpRelationMaps);
    }

}
