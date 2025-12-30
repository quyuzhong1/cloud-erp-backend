package com.erp.server.fms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.fms.entity.AssetAcceptDetailEntity;
import com.erp.model.fms.entity.AssetAcceptEntity;
import com.erp.model.fms.entity.FmsPushMsgEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.AssetPurchaseOrderDetailEntity;
import com.erp.model.scm.entity.AssetPurchaseOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.fms.kingdee.SyncKingdeeAssetAcceptService;
import com.erp.server.fms.service.AssetAcceptDetailService;
import com.erp.server.fms.service.FmsPushMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 同步客户到金蝶
 *
 * @Author Luo_WG
 * @Date 2023/5/25 10:53
 **/
@Slf4j
@Service
public class SyncKingdeeAssetAcceptServiceImpl implements SyncKingdeeAssetAcceptService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private FmsPushMsgService fmsPushMsgService;

    @Resource
    private AssetAcceptDetailService assetAcceptDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(AssetAcceptEntity entity, String operate) {
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
    private DmpPushTaskEntity saveTask (AssetAcceptEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.ASSET_ACCEPTANCE.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.ASSET_ACCEPTANCE.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_ASSET_ACCEPT_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }
    	
    	FmsPushMsgEntity fmsPushMsgEntity = new FmsPushMsgEntity();
        fmsPushMsgEntity.setSourceId(entity.getId());
        fmsPushMsgEntity.setSourceCode(entity.getCode());
        fmsPushMsgEntity.setSourceType(SourceTypeEnum.ASSET_ACCEPTANCE.getCode());
        fmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        fmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        fmsPushMsgEntity.setSyncOperate(operate);
        fmsPushMsgService.save(fmsPushMsgEntity);
        return null;
    }


	@Override
	public Map<String, Object> newSyncDataToKingdee(AssetAcceptEntity entity, String operate) {
		Map<String, Object> resultMap = new HashMap<>();

        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //资产验收单编号
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        //明细
        List<AssetAcceptDetailEntity> detailList =  assetAcceptDetailService.listByMainIdList(Collections.singletonList(entity.getId()));
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_98025.ERROR_98025);
        }

        //资金采购订单id
        AssetPurchaseOrderEntity assetPoEntity = FeignQuery.getById(AssetPurchaseOrderEntity.class, entity.getSourceId());
        if (ObjUtil.isEmpty(assetPoEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        //资金采购订单明细id
        List<String> sourceDetailIdList = detailList.stream().map(AssetAcceptDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<AssetPurchaseOrderDetailEntity> podEntityList = FeignQuery.getByIds(AssetPurchaseOrderDetailEntity.class, sourceDetailIdList);
        if (CollUtil.isEmpty(podEntityList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        Map<String, AssetPurchaseOrderDetailEntity> podMap = podEntityList.stream().collect(Collectors.toMap(AssetPurchaseOrderDetailEntity::getId, obj -> obj));

        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(entity.getAcceptOrgId()));
        if (CollUtil.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORG_NOT_FOUND);
        }
        //组织编码
        resultMap.put("orgCode", accountingCompanyList.get(0).getCode());

        //验收日期
        resultMap.put("acceptDate", LocalDateTimeUtil.format(entity.getAcceptDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        //部门
        if  (CharSequenceUtil.isNotBlank(entity.getAcceptDeptId())) {
            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
            dto.setDeptId(entity.getAcceptDeptId());
            dto.setOrgId(entity.getAcceptOrgId());
            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
            if (ObjectUtils.isNotEmpty(deptKingdee)) {
                resultMap.put("deptCode", deptKingdee.getKingdeeDeptCode());
            }
        }

        //员工岗位
        List<KingdeePostDTO.UserKingdeePostInfoDTO> userKingdeePostInfoList = sysUserFeign.listUserKingdeePostByUserIds(Collections.singletonList(entity.getAcceptUserId()));

        if (CollectionUtils.isNotEmpty(userKingdeePostInfoList)) {
            //收料人
            String acceptUserCode = userKingdeePostInfoList.stream().filter(obj -> obj.getUserId().equals(entity.getAcceptUserId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeePostCode())).orElse(null);
            resultMap.put("acceptUserCode", acceptUserCode);
        }
        //供应商编码
        SupplierEntity supplierEntity = FeignQuery.getById(SupplierEntity.class, entity.getSupplierId());
        if (ObjUtil.isEmpty(supplierEntity)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        resultMap.put("supplierCode", supplierEntity.getCode());



        List<String> skuIdList = detailList.stream().map(AssetAcceptDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);
        if (CollUtil.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        Map<String, ProductDetailEntity> skuMap = productDetailList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, obj -> obj));

        List<JSONObject> jsonDetailList = new ArrayList<>();
        for (AssetAcceptDetailEntity detail: detailList) {
            JSONObject jsonObject = new JSONObject();
            //sku编码
            jsonObject.set("skuNo", detail.getSkuNo());
            //验收数量
            jsonObject.set("acceptQty", detail.getAcceptQty());
            ProductDetailEntity productDetailEntity = skuMap.get(detail.getSkuId());
            if (ObjUtil.isNotEmpty(productDetailEntity)) {
                //单位
                jsonObject.set("unit",productDetailEntity.getUnitName());
            }

            //资金采购明细
            AssetPurchaseOrderDetailEntity assetPodEntity = podMap.get(detail.getSourceDetailId());
            if (ObjUtil.isEmpty(assetPodEntity)) {
                throw new ServiceException(ApiError.ERROR_98026);
            }

            List<Map<String, Object>> mapList = new ArrayList<>();
            Map<String, Object> entityMap = new HashMap<>();
            entityMap.put("poKingdeeDetailId", assetPodEntity.getKingdeeDetailId());
            entityMap.put("poSyncKingdeeId", assetPoEntity.getSyncKingdeeId());
            mapList.add(entityMap);
            //销售单金蝶明细id
            jsonObject.set("FPURMRBENTRY_Link", mapList);
            jsonDetailList.add(jsonObject);
       }
        resultMap.put("list", jsonDetailList);
        return resultMap;
	}
}
