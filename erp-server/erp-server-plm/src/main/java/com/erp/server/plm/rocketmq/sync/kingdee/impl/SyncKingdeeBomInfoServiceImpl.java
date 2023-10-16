package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourcePlatformEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeBomInfoService;
import com.erp.server.plm.service.BomSkuService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/8 18:13
 */
@Service
public class SyncKingdeeBomInfoServiceImpl implements SyncKingdeeBomInfoService {

    @Resource
    private BomSkuService bomSkuService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void syncDataToKingdee(BomInfoEntity entity,String operate) {

        Map<String, Object> resultMap = new HashMap<>();

        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            sendMqAndSaveTask(entity,operate,resultMap);
            return;
        }
        List<BomSkuDTO> bomList = bomSkuService.getByBomId(entity.getId());
        if (CollectionUtils.isEmpty(bomList)) {
            return;
        }
        //父级物料
        BomSkuDTO parent = bomList.get(0);
        //父级sku编码
        resultMap.put("id",entity.getId());
        //父级sku编码
        resultMap.put("parentSkuNo",parent.getSkuNo());
        //版本
        resultMap.put("version",parent.getSkuNo().concat("_").concat(entity.getVersion().toString()));

        //子级物料
        List<BomChildrenSkuDTO> childrenList = parent.getChildren();
        if (CollectionUtils.isEmpty(childrenList)) {
            return;
        }
        List<Map<String, Object>> mapList = new ArrayList<>();

        for (BomChildrenSkuDTO bomChildrenSkuDTO: childrenList) {
            Map<String, Object> detailMap = new HashMap<>(MathUtil.THREE);
            detailMap.put("skuNo",bomChildrenSkuDTO.getSkuNo());
            detailMap.put("date", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            detailMap.put("quantity",bomChildrenSkuDTO.getQuantity().toString());
            mapList.add(detailMap);
        }
        resultMap.put("list",mapList);
        //生成任务
        sendMqAndSaveTask(entity,operate,resultMap);
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private void sendMqAndSaveTask (BomInfoEntity entity,String operate,Map<String, Object> resultMap) {
        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(entity.getId());
        taskFeignDTO.setSourceCode(entity.getSerialNumber());
        taskFeignDTO.setSourceType(SourceTypeEnum.PRODUCT_BOM_INFO.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_BOM_INFO_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        taskFeignDTO.setSourcePlatformName(SourcePlatformEnum.ERP_PLM.getCode());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
        taskFeignDTO.setSyncOperate(operate);
        dmpMqFeign.sendMqAndSaveTask(taskFeignDTO);
    }
}
