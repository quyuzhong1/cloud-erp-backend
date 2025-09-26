package com.erp.server.dmp.push.consumer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.sdk.third.kingdee.utils.K3CloudApiThreadLocal;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeSoOutstockConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.kingdee.impl.KingdeeCommonServiceImpl;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.kingdee.bos.webapi.sdk.K3CloudApi;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 对接金蝶销售出库
 *
 * @Author Luo_WG
 * @Date 2023/6/1 14:45
 **/
@Service
@Slf4j
//@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC,
//        selectorExpression = "kingdee_so_outstock_tag",
//        consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SO_OUTSTOCK,
//        consumeMode = ConsumeMode.CONCURRENTLY,
//        consumeThreadNumber = 5)
public class KingdeeSoOutstockConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private KingdeeSoOutstockConsumerService kingdeeSoOutstockConsumerService;

    @Resource
    private DmpPushTaskService dmpPushTaskService;


    public static void main(String[] args) throws Exception{
        //模块类型
        Integer type = ApiModuleTypeEnum.SO_OUTSTOCK.getCode();
        KingdeeCommonService kingdeeCommonService = new KingdeeCommonServiceImpl();
        Map<String, Object> map = new LinkedHashMap<>();
        //读取配置，初始化SDK
        
        JSONObject jsonObject = new JSONObject();
        
        jsonObject.set("number", "XSCK250214000003");
        //创建组织
        jsonObject.set("CreateOrgId", 1);

        ExecutorService excutor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 200; i++) {
        	int j = i;
        	excutor.execute(() -> {
        		log.info(j + "view方法数据查询,viewJson = {}", JSONUtil.toJsonStr(jsonObject));
        		K3CloudApiThreadLocal.set();
                KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_OUTSTOCK.getCode());
    			JSONObject model = kingdeeApiUtils.getViewJson(JSONUtil.toJsonStr(jsonObject));
                log.info(model.getStr("Id"));
                K3CloudApiThreadLocal.remove();
        	});
		}
        System.out.println();
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
        kingdeeSoOutstockConsumerService.executeConsumer(map);
        return ApiResult.success();
    }

}
