package com.erp.server.srm.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.entity.SrmPushMsgEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PoInstockEntity;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.srm.kingdee.SyncKingdeePoReconciliationService;
import com.erp.server.srm.service.SrmPushMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description: 同步直接调拨单
 * @author Will
 * @date: 2023/5/24 18:56
 */
@Slf4j
@Service
public class SyncKingdeePoReconciliationServiceImpl implements SyncKingdeePoReconciliationService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;


    @Resource
    private SrmPushMsgService srmPushMsgService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(PoReconciliationEntity entity, List<PoReconciliationDetailEntity> detailList, String operate) {
        //生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncDataToKingdee(entity , detailList , operate));
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
    private DmpPushTaskEntity saveTask (PoReconciliationEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.PO_RECONCILIATION.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.PO_RECONCILIATION.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_PO_RECONCILIATION_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }
        
        SrmPushMsgEntity srmPushMsgEntity = new SrmPushMsgEntity();
        srmPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        srmPushMsgEntity.setSourceType(SourceTypeEnum.PO_RECONCILIATION.getCode());
        srmPushMsgEntity.setSourceId(entity.getId());
        srmPushMsgEntity.setSourceCode(entity.getCode());
        srmPushMsgEntity.setSyncOperate(operate);
        srmPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        
        srmPushMsgService.save(srmPushMsgEntity);
        
        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(PoReconciliationEntity entity, List<PoReconciliationDetailEntity> detailList, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //调拨单号
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }

        //对账结束日期
        resultMap.put("endDate", entity.getEndDate());

        //供应商名称
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(entity.getSupplierId());
        resultMap.put("supplierCode", supplierEntity.getCode());

        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(entity.getSettleOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }

        //组织机构编码
        String orgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSettleOrgId()))
                .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
        resultMap.put("orgCode", orgCode);
        //日期
        resultMap.put("date", LocalDate.now());

        //抬头备注
        resultMap.put("remark", entity.getRemark());

        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }

        //获取sku的id集合
        List<String> skuIdList = detailList.stream().map(PoReconciliationDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //退货id集合
        Map<String,String> returnMap = new HashMap<>();
        Map<String,String> returnDetailMap = new HashMap<>();
        List<String> returnIdList = detailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.PO_RETURN.getCode())).map(PoReconciliationDetailEntity::getSourceId).distinct().collect(Collectors.toList());
        List<String> returnDetailIdList = detailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.PO_RETURN.getCode())).map(PoReconciliationDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(returnIdList)) {
            List<PoReturnEntity> returnList = FeignQuery.getByIds(PoReturnEntity.class, returnIdList);
            returnMap = returnList.stream().collect(Collectors.toMap(PoReturnEntity::getId, PoReturnEntity::getSyncKingdeeId));

            List<PoReturnDetailEntity> returnDetailList = FeignQuery.getByIds(PoReturnDetailEntity.class, returnDetailIdList);
            returnDetailMap = returnDetailList.stream().collect(Collectors.toMap(PoReturnDetailEntity::getId, PoReturnDetailEntity::getKingdeeDetailId));
        }

        //入库id集合
        Map<String,String> instockMap = new HashMap<>();
        Map<String,String> instockDetailMap = new HashMap<>();
        List<String> instockIdList = detailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.PO_INSTOCK.getCode())).map(PoReconciliationDetailEntity::getSourceId).distinct().collect(Collectors.toList());
        List<String> instockDetailIdList = detailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.PO_INSTOCK.getCode())).map(PoReconciliationDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(instockIdList)) {
            List<PoInstockEntity> instockList = FeignQuery.getByIds(PoInstockEntity.class, instockIdList);
            instockMap = instockList.stream().collect(Collectors.toMap(PoInstockEntity::getId, PoInstockEntity::getSyncKingdeeId));

            List<PoInstockDetailEntity> instockDetailList = FeignQuery.getByIds(PoInstockDetailEntity.class, instockDetailIdList);
            instockDetailMap = instockDetailList.stream().collect(Collectors.toMap(PoInstockDetailEntity::getId, PoInstockDetailEntity::getKingdeeDetailId));
        }

        //采购详情id
        Map<String,PurchaseOrderDetailEntity> poDetailMap = new HashMap<>();
        List<String> poDetailIdList = detailList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getPoDetailId())).map(PoReconciliationDetailEntity::getPoDetailId).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(poDetailIdList)) {
            List<PurchaseOrderDetailEntity> poDetailList = FeignQuery.getByIds(PurchaseOrderDetailEntity.class, poDetailIdList);
            poDetailMap = poDetailList.stream().collect(Collectors.toMap(PurchaseOrderDetailEntity::getId, Function.identity()));
        }

        //获取币别信息
        List<String> currencyCodeList = detailList.stream().map(PoReconciliationDetailEntity::getCurrency).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyCodeList);

        List<JSONObject> list = new ArrayList<>();
        for (PoReconciliationDetailEntity detail : detailList) {
            JSONObject jsonObject = new JSONObject();
            //SKU
            jsonObject.set("skuNo", detail.getSkuNo());
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(detail.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            //产品名称
            jsonObject.set("productName", productDetailEntity.getName());
            //数量
            jsonObject.set("qty", detail.getQty());
            //含税单价
            jsonObject.set("taxPrice", detail.getTaxPrice());
            //税率
            jsonObject.set("taxRate",  MathUtil.multiplyWithFour(detail.getTaxRate(),MathUtil.BigDecimal_100));
            //折扣率
            jsonObject.set("discountRate", MathUtil.multiplyWithFour(detail.getDiscountRate(),MathUtil.BigDecimal_100));
            //折扣额
            jsonObject.set("discountAmount", MathUtil.multiplyWithTwo(detail.getDiscountRate(),detail.getTaxAmount()));
            //税额
            jsonObject.set("taxAmount", detail.getTaxAmount());
            //价税合计
            jsonObject.set("discountTaxAmount", detail.getDiscountTaxAmount());

            //结算币别
            CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(detail.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
            jsonObject.set("currencyCode", viewDTO.getKingdeeCode());


            //采购订单号
            jsonObject.set("poCode", detail.getPoCode());
            //来源编码
            jsonObject.set("sourceCode", detail.getSourceCode());
            //来源类型
            jsonObject.set("sourceType", detail.getSourceType());
            //明细id
            jsonObject.set("detailId", detail.getId());

            //采购明细金蝶id
            if (CharSequenceUtil.isNotBlank(detail.getPoDetailId())) {
                PurchaseOrderDetailEntity poDetailEntity = ObjectUtil.isEmpty( poDetailMap.get(detail.getPoDetailId())) ? new PurchaseOrderDetailEntity() :  poDetailMap.get(detail.getPoDetailId());
                jsonObject.set("poDetailKingdeeId", poDetailEntity.getKingdeeDetailId());
                jsonObject.set("isGift", poDetailEntity.getIsGift());
            }

            //备注
            jsonObject.set("remark", detail.getRemark());
            List<Map<String,Object>> refList = new ArrayList<>();
            JSONObject refJsonObject = new JSONObject();
            String refKingdeeId = "";
            String refDetailKingdeeId = "";
            if (SourceTypeEnum.PO_INSTOCK.getCode().equals(detail.getSourceType())) {
                refKingdeeId =  instockMap.get(detail.getSourceId());
                refDetailKingdeeId =  instockDetailMap.get(detail.getSourceDetailId());
            } else {
                refKingdeeId =  returnMap.get(detail.getSourceId());
                refDetailKingdeeId =  returnDetailMap.get(detail.getSourceDetailId());
            }
            refJsonObject.set("refKingdeeId",refKingdeeId);
            refJsonObject.set("refDetailKingdeeId",refDetailKingdeeId);
            refJsonObject.set("sourceType", detail.getSourceType());
            refList.add(refJsonObject);
            jsonObject.set("refList",refList);
            list.add(jsonObject);
        }
        resultMap.put("list", list);
        return resultMap;
	}
}
