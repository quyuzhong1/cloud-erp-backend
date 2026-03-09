package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeSubcontractBOMConsumerService;
import com.erp.server.dmp.push.service.business.KingdeeSubcontractOrderConsumerService;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author: wtr
 * @Date: 2026/1/19 10:18
 * @Param:
 * @Return:
 * @Description:
 **/
@Service
@Slf4j
public class KingdeeSubcontractBOMConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private KingdeeSubcontractBOMConsumerService kingdeeSubcontractBOMConsumerService;

    @Resource
    private DmpPushTaskService dmpPushTaskService;


    public static void main(String[] args) {
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SUBCONTRACT_BOM.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FBillNo = '%s'", "SUBBOM00002303"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FBillNo";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 1);
        System.out.println(queryList);

    }

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpPushTaskService.updateStatus(paramDTO);
    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {

    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpPushTaskService.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        Map<String, Object> map = JSONUtil.parseObj(ext);
        kingdeeSubcontractBOMConsumerService.executeConsumer(map);
        return ApiResult.success();
    }

}
