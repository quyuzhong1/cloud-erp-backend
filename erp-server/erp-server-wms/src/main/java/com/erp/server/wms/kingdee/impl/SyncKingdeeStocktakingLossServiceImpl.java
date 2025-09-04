package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeStocktakingLossService;
import com.erp.server.wms.service.StocktakingProfitLossDetailService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.WmsPushMsgService;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname SyncKingdeeStocktakingLossServiceImpl
 * @Description TODO
 * @Date 2023-08-14 15:13
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeStocktakingLossServiceImpl implements SyncKingdeeStocktakingLossService {
    @Resource
    private StocktakingProfitLossDetailService stocktakingProfitLossDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    
    @Resource
    private WmsPushMsgService wmsPushMsgService;

    /**
     * 同步金蝶
     *
     * @param entity
     * @param operate
     * @return void
     * @author yl
     * @date 2023-08-14 15:14
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(StocktakingProfitLossEntity entity, String operate) {
        //生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
    	}
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (StocktakingProfitLossEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.STOCKTAKING_LOSS.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.STOCKTAKING_PROFIT_LOSS.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_STOCKTAKING_LOSS_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.STOCKTAKING_LOSS.getCode());
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
        wmsPushMsgService.save(wmsPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(StocktakingProfitLossEntity entity, String operate) {
		Map<String, Object> resultMap = new HashMap<>();
        if (Objects.isNull(entity)) {
            throw new ServiceException("未找到盘亏单");
        }
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //业务id
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }

        resultMap.put("sourceCode", entity.getSourceCode());
        //单据类型
        resultMap.put("billType", entity.getBillType().getCode());
        LocalDate billDate = entity.getBillDate();
        if (Objects.isNull(billDate)) {
            billDate = LocalDate.now();
        }
        String billDateStr = billDate.format(DateTimeFormatter.ISO_DATE);
        //单据日期
        resultMap.put("billDate", billDateStr);
        List<StocktakingProfitLossDetailDTO.ViewDTO> detailDbList = stocktakingProfitLossDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isEmpty(detailDbList)) {
            throw new ServiceException("盘盈盘亏单明细不能为空");
        }

        String warehouseOrgCode = "";
        List<String> warehouseIdList = detailDbList.stream().map(StocktakingProfitLossDetailDTO.ViewDTO::getWarehouseId).collect(Collectors.toList());
        //仓库
        List<WarehouseEntity> warehouseList = CollectionUtils.isNotEmpty(warehouseIdList) ? warehouseService.listByIds(warehouseIdList) : Collections.emptyList();
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            String orgId=warehouseList.get(0).getOrgId();
            //组织信息
            List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(orgId));
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                warehouseOrgCode = accountingCompanyList.get(0).getCode();
            }
        }

        //仓库组织 货主
        resultMap.put("warehouseOrgCode", warehouseOrgCode);

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(warehouseIdList);

        List<JSONObject> list = new ArrayList<>(detailDbList.size());
        for (StocktakingProfitLossDetailDTO.ViewDTO item : detailDbList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("skuNo", item.getSkuNo());
            String unit = item.getUnit();
            jsonObject.set("unit", CharSequenceUtil.isNotBlank(unit) ? unit : "Pcs");
            jsonObject.set("qty", item.getQty());
            String kingdeeWarehouseCode =warehouseList.stream().filter(w->w.getId().equals(item.getWarehouseId())).
                    map(WarehouseEntity::getKingdeeWarehouseCode).findFirst().orElse("");
            jsonObject.set("kingdeeWarehouseCode", kingdeeWarehouseCode);
            Integer inventoryQty = item.getFrozenQty() + item.getUsableQty();
            jsonObject.set("inventoryQty", inventoryQty);
            //是否下推仓位
            Boolean isPush = pushKingdeeList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), item.getWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPush) {
                //仓位
                jsonObject.set("warehouseLocation", item.getWarehouseLocation());
            }
            jsonObject.set("warehouseOrgCode", warehouseOrgCode);
            jsonObject.set("diffQty", Math.abs(item.getDiffQty()));
            list.add(jsonObject);
        }
        resultMap.put("detailList", list);
        return resultMap;
	}

}
