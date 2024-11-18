package com.erp.server.oms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.SoChangeDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoChangeTypeEnum;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.kingdee.SyncKingdeeSoChangeService;
import com.erp.server.oms.service.*;
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
 * @Classname SyncKingdeeSoServiceImpl
 * @Date 2023-05-30 11:46
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeSoChangeServiceImpl implements SyncKingdeeSoChangeService {


    @Resource
    private SysUserFeign sysUserFeign;


    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoChangeDetailService soChangeDetailService;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SoChangeService soChangeService;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;


    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private KingdeeFeign kingdeeFeign;
    
    @Resource
    private OmsPushMsgService omsPushMsgService;

    /**
     * 销售变更单同步金碟
     *
     * @param entity
     * @param operate
     * @return void
     * @author yl
     * @date 2023-05-30 11:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(SoChangeEntity entity, String operate) {
        //生成任务
       return saveTask(entity,operate,newSyncDataToKingdee(entity, operate));
    }


    /**
     * 填充数据
     *
     * @param entity
     * @return void
     * @author yl
     * @date 2023-06-07 14:08
     */
    private SoChangeEntity fillDb(SoChangeEntity entity, String soKingdeeId, String soCode) {
        //销售变更单详情
        List<SoChangeDetailEntity> details = soChangeDetailService.listDetailDbByMainId(entity.getId());
        //订单详情的ids
        List<String> soDetailIdList = details.stream().map(SoChangeDetailEntity::getSoDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = soDetailService.listByIdsSeq(soDetailIdList);
        List<String> soKingdeeDetailIds = soDetailList.stream().map(SoDetailEntity::getKingdeeDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        //表示是新增加审核
        if (StringUtils.isEmpty(entity.getSyncKingdeeId())) {
            Map<String, Object> map = new HashMap<>();
            map.put("SaleOrderBillId", soKingdeeId);
            map.put("SaleOrderBillNo", soCode);
            map.put("SOEntryIds", soKingdeeDetailIds);
            //自动生成变更单
            String result = dmpTaskFeign.createkingdeeSoChange(map);
            log.info("json======{}", result);
            JSONObject json = JSONUtil.parseObj(result);
            Boolean isSuccess = (Boolean) json.getOrDefault("IsSuccess", Boolean.FALSE);
            List<SoChangeDetailEntity> updateList = new ArrayList<>(10);
            //如果成功了
            if (isSuccess) {
                List<JSONObject> dataList = (List<JSONObject>) json.getOrDefault("Datas", new ArrayList<>());
                if (CollectionUtils.isNotEmpty(dataList)) {
                    JSONObject dataJson = dataList.get(0);
                    String syncKingdeeId = dataJson.get("FID").toString();
                    entity.setSyncKingdeeId(syncKingdeeId);
                    List<JSONObject> detailList = (List<JSONObject>) dataJson.getOrDefault("SaleOrderEntry", new ArrayList<>());
                    for (int i = 0; i < detailList.size(); i++) {
                        if (soDetailList.size() >= detailList.size()) {
                            JSONObject detailJson = detailList.get(i);
                            SoDetailEntity soDetail = soDetailList.get(i);
                            String soDetailId = soDetail.getId();
                        String KingdeeDetailId = String.valueOf(detailJson.getOrDefault("FEntryID", ""));
                            SoChangeDetailEntity soChangeDetail = details.stream().filter(d -> d.getSoDetailId().equals(soDetailId)).
                                    findFirst().orElse(null);
                            if (soChangeDetail != null) {
                        soChangeDetail.setKingdeeDetailId(KingdeeDetailId);
                        updateList.add(soChangeDetail);
                            }
                        }
                    }
                }
                soChangeService.updateById(entity);
            }
            if (updateList.size() > 0) {
                soChangeDetailService.updateBatchById(updateList);
            }
        }

        return entity;
    }


    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (SoChangeEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.SO_CHANGE.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.SO_CHANGE.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_SO_CHANGE_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            taskFeignDTO.setSyncOperate(operate);
           return dmpMqFeign.saveTask(taskFeignDTO);
        }
       
    	OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(entity.getId());
        omsPushMsgEntity.setSourceCode(entity.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.SO_CHANGE.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);
        
        return null;
    }


	@Override
	public Map<String, Object> newSyncDataToKingdee(SoChangeEntity entity, String operate) {
		String id = entity.getId();

        Map<String, Object> resultMap = new HashMap<>();
        //业务id
        resultMap.put("id", id);
        //编码
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        List<SoChangeDetailDTO.ViewDTO> detailList = soChangeDetailService.listDetailByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
           throw new ServiceException("未找到变更明细数据");
        }
        String soId = entity.getSoId();
        SoInfoDTO.CustomerDTO soInfo = soInfoService.getSoCustomer(soId);
        SoInfoEntity soInfoEntity = soInfoService.getById(soId);
        if (Objects.isNull(soInfo) || Objects.isNull(soInfoEntity)) {
           throw new ServiceException("未找到销售订单数据");
        }

        //填充数据
        fillDb(entity, soInfo.getSyncKingdeeId(), soInfo.getCode());

        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());

        List<String> orgIdList = new ArrayList<>(2);
        //库存组织
        String warehouseOrgId = soInfo.getWarehouseOrgId();
        //销售组织
        String salesOrgId = soInfo.getSalesOrgId();
        orgIdList.add(warehouseOrgId);
        orgIdList.add(salesOrgId);
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);
        //销售组织的金蝶code
        String salesOrgCode = orgList.stream().filter(o -> o.getId().equals(salesOrgId)).
                map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");

        //销售员
        String sellerId = soInfo.getSellerId();

        //客户id
        String customerId = soInfo.getCustomerId();
        //销售订单号
        resultMap.put("soCode", soInfo.getCode());
        resultMap.put("soId", soInfo.getId());
        resultMap.put("discountAmount", soInfoEntity.getDiscountAmount());
        //单据类型
        resultMap.put("orderType", "XSDDBGD01_SYS");
        //单据日期
        resultMap.put("billDate", LocalDateTimeUtil.format(soInfo.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        //客户
        if (StringUtils.isNotBlank(customerId)) {
            CustomerInfoEntity customerInfo = customerInfoService.getById(customerId);
            if (customerInfo != null) {
                resultMap.put("customerCode", customerInfo.getCode());
            }
        }
        //变更原因
        resultMap.put("remark", entity.getRemark());
        resultMap.put("seller", soInfo.getSellerName());
        //获取员工
        if (StringUtils.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgId(salesOrgId);
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerCode", kingSellerInfo.getUserPostCode());
                resultMap.put("deptCode", kingSellerInfo.getDeptCode());
            }
        }

        String warehouseId = soInfo.getWarehouseId();
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        String kingdeeWarehouseCode = "";
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            kingdeeWarehouseCode = warehouseList.get(0).getKingdeeWarehouseCode();
        }

        if (StringUtils.isNotBlank(salesOrgCode)) {
            resultMap.put("salesOrgCode", salesOrgCode);
        }
        String warehouseOrgCode = orgList.stream().filter(o -> o.getId().equals(warehouseOrgId)).
                map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");

        //要货日期
        LocalDate requireDate = soInfo.getRequireDate();
        List<JSONObject> list = new ArrayList<>(detailList.size());
        //表示删除
        String deleteCode = SoChangeTypeEnum.DELETE.getCode();
        List<SoChangeDetailDTO.ViewDTO> laterDetailList = soChangeDetailService.listDetailByMainId(id);

        for (SoChangeDetailDTO.ViewDTO item : laterDetailList) {
            JSONObject jsonObject = new JSONObject();
            String changeType = item.getChangeType().getCode();
            //是否是删除
            Boolean isDelete = deleteCode.equals(changeType);
            //金蝶详情id
            jsonObject.set("kingdeeDetailId", item.getKingdeeDetailId());
            jsonObject.set("skuNo", item.getSkuNo());
            jsonObject.set("changeType", item.getChangeType().getCode());
            jsonObject.set("soDetailId", item.getSoDetailId());
            jsonObject.set("requireDate", LocalDateTimeUtil.format(requireDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
            jsonObject.set("oldQty", item.getOldQty());
            if (isDelete) {
                jsonObject.set("qty", item.getOldQty());
                jsonObject.set("baseQty", item.getOldQty());
                jsonObject.set("stockBaseQty", item.getOldQty());
                jsonObject.set("currentInventoryQty", item.getOldQty());
                jsonObject.set("curInventoryQty", item.getOldQty());
                jsonObject.set("taxPrice", item.getOldPrice());
                jsonObject.set("taxRate", item.getOldTaxRate());

            } else {
                jsonObject.set("qty", item.getQty());
                jsonObject.set("baseQty", item.getQty());
                jsonObject.set("stockBaseQty", item.getQty());
                jsonObject.set("currentInventoryQty", item.getQty());
                jsonObject.set("curInventoryQty", item.getQty());
                jsonObject.set("taxPrice", item.getTaxPrice());
                jsonObject.set("taxRate", item.getTaxRate());
            }

            jsonObject.set("oldTaxPrice", item.getOldTaxPrice());
            jsonObject.set("oldPrice", item.getOldPrice());
            jsonObject.set("oldTaxRate", item.getOldTaxRate());
            jsonObject.set("isGift", item.getIsGift());
            jsonObject.set("amount", item.getAmount());

            //是否补发
            jsonObject.set("isReissue", item.getIsReissue());
            jsonObject.set("unit", "Pcs");
            jsonObject.set("remark", item.getRemark());
            jsonObject.set("warehouseOrgCode", warehouseOrgCode);
            jsonObject.set("kingdeeWarehouseCode", kingdeeWarehouseCode);
            list.add(jsonObject);
        }

        resultMap.put("detailList", list);
        return resultMap;
	}

}
