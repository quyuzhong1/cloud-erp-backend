package com.erp.server.wms.wdt.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import com.erp.server.wms.wdt.SyncWdtOtherOutStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 将erp其他出库单同步至旺店通
 *
 * @author tanmujin
 * @date 2024-05-16
 */
@Slf4j
@Service
public class SyncWdtOtherOutStockServiceImpl implements SyncWdtOtherOutStockService {

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Override
    public DmpPushTaskEntity saveTask(List<CreateOtherStockoutRequest.GoodsList> goodsList, String operateCode, String sourceCode, String detailId, String outerCode, String thirdWarehouseCode, boolean checkOuterCode) {
        CreateOtherStockoutRequest request = new CreateOtherStockoutRequest();
        request.setOuterNo(outerCode);

        //查询推送任务表，如果有了相同的来源单据号，则序号累加
        if(checkOuterCode){
            DmpSyncTaskDTO.ListCodeDTO param = new DmpSyncTaskDTO.ListCodeDTO(Collections.singletonList(outerCode), PlatformEnum.WANGDIAN.getDesc(), PlatformEnum.ERP.getDesc());
            List<DmpPushTaskEntity> taskList = dmpMqFeign.listByCodeParam(param);
            Optional<CreateOtherStockoutRequest> optional = taskList.stream()
                    .filter(task -> task.getSyncOperate().equalsIgnoreCase(operateCode))
                    .map(task -> JSON.parseObject(task.getMqData(), CreateOtherStockoutRequest.class))
                    .max((o1, o2) -> ObjectUtil.compare(o1.getOuterNo(), o2.getOuterNo()));
            if(optional.isPresent()){
                String maxOuterNo = optional.get().getOuterNo();
                if(maxOuterNo.contains("_")){
                    String[] split = maxOuterNo.split("_");
                    Integer seq = Integer.parseInt(split[1]) + 1;
                    request.setOuterNo(split[0] + "_" + String.format("%03d", seq));
                }else {
                    request.setOuterNo(outerCode + "_001");
                }
            }
        }

        //临时转换仓位
        /*for (CreateOtherStockoutRequest.GoodsList goods : goodsList) {
            if(goods.getPositionNo().equals("TC-JHZC") || goods.getPositionNo().equals("B2B-JHZC")){
                goods.setPositionNo("发货暂存");
            }
        }*/

        request.setWarehouseNo(thirdWarehouseCode);
        request.setisCheck(Boolean.TRUE);
        request.setGoodsList(goodsList);
        request.setSourceId(outerCode);
        request.setOperateCode(operateCode);
        request.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        request.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        request.setCreateTime(LocalDateTime.now());
        request.setRemark("原始单据号：" + sourceCode);

        //添加推送任务
        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(detailId);
        dmpSyncTaskDTO.setSourceCode(sourceCode);
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.OTHER_OUTSTOCK.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.WDT_OTHER_OUT_STOCK_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(request));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operateCode);
        return dmpMqFeign.saveTask(dmpSyncTaskDTO);
    }

    public DmpPushTaskFeignDTO generateTask(List<CreateOtherStockoutRequest.GoodsList> goodsList, String operateCode, String sourceCode, String detailId, String outerCode, String thirdWarehouseCode, boolean checkOuterCode) {
        CreateOtherStockoutRequest request = new CreateOtherStockoutRequest();
        request.setOuterNo(outerCode);

        //查询推送任务表，如果有了相同的来源单据号，则序号累加
        if(checkOuterCode){
            DmpSyncTaskDTO.ListCodeDTO param = new DmpSyncTaskDTO.ListCodeDTO(Collections.singletonList(outerCode), PlatformEnum.WANGDIAN.getDesc(), PlatformEnum.ERP.getDesc());
            List<DmpPushTaskEntity> taskList = dmpMqFeign.listByCodeParam(param);
            Optional<CreateOtherStockoutRequest> optional = taskList.stream()
                    .filter(task -> task.getSyncOperate().equalsIgnoreCase(operateCode))
                    .map(task -> JSON.parseObject(task.getMqData(), CreateOtherStockoutRequest.class))
                    .max((o1, o2) -> ObjectUtil.compare(o1.getOuterNo(), o2.getOuterNo()));
            if(optional.isPresent()){
                String maxOuterNo = optional.get().getOuterNo();
                if(maxOuterNo.contains("_")){
                    String[] split = maxOuterNo.split("_");
                    Integer seq = Integer.parseInt(split[1]) + 1;
                    request.setOuterNo(split[0] + "_" + String.format("%03d", seq));
                }else {
                    request.setOuterNo(outerCode + "_001");
                }
            }
        }

        //临时转换仓位
        /*for (CreateOtherStockoutRequest.GoodsList goods : goodsList) {
            if(goods.getPositionNo().equals("TC-JHZC") || goods.getPositionNo().equals("B2B-JHZC")){
                goods.setPositionNo("发货暂存");
            }
        }*/

        request.setWarehouseNo(thirdWarehouseCode);
        request.setisCheck(Boolean.TRUE);
        request.setGoodsList(goodsList);
        request.setSourceId(outerCode);
        request.setOperateCode(operateCode);
        request.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        request.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        request.setCreateTime(LocalDateTime.now());
        request.setRemark("原始单据号：" + sourceCode);

        //添加推送任务
        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(detailId);
        dmpSyncTaskDTO.setSourceCode(sourceCode);
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.OTHER_OUTSTOCK.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.WDT_OTHER_OUT_STOCK_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(request));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operateCode);
        return dmpSyncTaskDTO;
    }
}
