package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.model.wms.enums.ReturnReasonEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoReturnService;
import com.erp.server.wms.service.SoReturnInstockDetailService;
import com.erp.server.wms.service.SoReturnReceiveDetailService;
import com.erp.server.wms.service.SoReturnReceiveService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.WmsPushMsgService;

import io.seata.spring.annotation.GlobalTransactional;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 同步销售退货单到金蝶
 * @Author Luo_WG
 * @Date 2023/5/25 10:53
 **/
@Slf4j
@Service
public class SyncKingdeeSoReturnServiceImpl implements SyncKingdeeSoReturnService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private KingdeeFeign kingdeeFeign;


    @Resource
    private SoReturnReceiveService soReturnReceiveService;

    @Resource
    private SoReturnReceiveDetailService soReturnReceiveDetailService;

    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(SoReturnInstockEntity entity, String operate) {
        //生成任务
        return saveTask(entity,operate,this.newSyncDataToKingdee(entity, operate));
    }

    private String getSoDetailid(SoReturnInstockEntity entity,  List<SoReturnDetailEntity> returnDetailEntitys, List<SoReturnDetailEntity> returnDetailList, List<SoDetailEntity> soDetailList, SoReturnInstockDetailEntity detailEntity, String soDetailId) {
        if (SourceTypeEnum.SO_RETURN_RECEIVE.getCode().equals(entity.getSourceType())) {
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntitys.stream().filter(req -> req.getId().equals(detailEntity.getSoReturnDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soReturnDetailEntity)) {
                soDetailId = soReturnDetailEntity.getSourceDetailId();
            }
        } else if (SourceTypeEnum.SO_RETURN.getCode().equals(entity.getSourceType())) {
            SoReturnDetailEntity soReturnDetailEntity = returnDetailList.stream().filter(req -> req.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soReturnDetailEntity)) {
                soDetailId = soReturnDetailEntity.getSourceDetailId();
            }
        } else if (SourceTypeEnum.SO_INFO.getCode().equals(entity.getSourceType())) {
            SoDetailEntity soDetailEntity = soDetailList.stream().filter(req -> req.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soDetailEntity)) {
                soDetailId = soDetailEntity.getId();
            }
        }
        return soDetailId;
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (SoReturnInstockEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.SO_RETURN_INSTOCK.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
          DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
          dmpSyncTaskDTO.setSourceId(entity.getId());
          dmpSyncTaskDTO.setSourceCode(entity.getCode());
          dmpSyncTaskDTO.setSourceType(SourceTypeEnum.SO_RETURN_INSTOCK.getCode());
          dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
          dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_SO_RETURN_TAG.getName());
          dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
          dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
          dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
          dmpSyncTaskDTO.setSyncOperate(operate);
          dmpSyncTaskDTO.setParentId(entity.getSoId());
          return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }

    	WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.SO_RETURN_INSTOCK.getCode());
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        wmsPushMsgEntity.setParentId(entity.getSoId());

        wmsPushMsgService.save(wmsPushMsgEntity);

        return null;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(SoReturnInstockEntity entity, String operate) {
		Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //客户编号
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }

        List<SoReturnInstockDetailEntity> returnInstockDetailEntities = soReturnInstockDetailService.listDetailByMainId(entity.getId());

        //客户信息
        List<CustomerInfoEntity> customerInfoEntitieList = customerFeign.listCustomerByIds(Arrays.asList(entity.getCustomerId()));
        //退货单
        SoReturnEntity soReturnEntity = StringUtils.isNotBlank(entity.getSourceId())?soReturnFeign.getSoReturnById(entity.getSourceId()):null;



        String soId = "";
        if (SourceTypeEnum.SO_RETURN_RECEIVE.getCode().equals(entity.getSourceType())) {
            List<SoReturnReceiveEntity> soReturnReceiveEntities = soReturnReceiveService.listByIds(Arrays.asList(entity.getSourceId()));
            if (CollectionUtils.isNotEmpty(soReturnReceiveEntities)) {
                soId = soReturnReceiveEntities.get(0).getSoId();
            }
        } else if (SourceTypeEnum.SO_RETURN.getCode().equals(entity.getSourceType())) {
            soId = entity.getSoId();
        } else if (SourceTypeEnum.SO_INFO.getCode().equals(entity.getSourceType())) {
            soId = entity.getSoId();
        }

        //销售单
        SoInfoEntity soInfoEntity = new SoInfoEntity();
        if (StringUtils.isNotBlank(soId)) {
            soInfoEntity = soInfoFeign.getSoInfoById(soId);
        }


        //销售单明细
        List<SoDetailEntity> soDetailEntitieList = new ArrayList<>();
        if (StringUtils.isNotBlank(soId)) {
            soDetailEntitieList = soInfoFeign.listSoDetailByMainIds(Arrays.asList(soId));
        }
        List<String> warehouseIds = returnInstockDetailEntities.stream().map(SoReturnInstockDetailEntity::getWarehouseId).collect(Collectors.toList());
        //仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);
        List<String> orgIdList = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
        orgIdList.add(entity.getSalesOrgId());
        orgIdList.add(entity.getInventoryOrgId());
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);

        //单据日期
        resultMap.put("billDate", LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        //组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSalesOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            //销售组织
            resultMap.put("salesOrgCode", salesOrgCode);
            String inventoryOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInventoryOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            //库存组织
            resultMap.put("inventoryOrgCode", inventoryOrgCode);
        }
        //销售员
        String sellerId = entity.getSellerId();

        //获取业务员信息
        if (StringUtils.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgId(entity.getSalesOrgId());
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerUserCode", kingSellerInfo.getUserPostCode());
                resultMap.put("sellerDeptCode", kingSellerInfo.getDeptCode());
            }
        }

        if (StringUtils.isNotBlank(entity.getWarehouseKeeperId())) {
            //仓管员编码
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("warehouseKeeperCode",findUserDTO.getCode());
            }
        }

        //客户
        if (CollectionUtils.isNotEmpty(customerInfoEntitieList)) {
            CustomerInfoEntity customerInfoEntity = customerInfoEntitieList.stream().filter(obj -> obj.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            resultMap.put("customerCode", customerInfoEntity.getCode());
            //收款条件
            List<DictBasicDTO.ViewDTO> collectionTermsList = customerFeign.getDictBasicByKey("collectionTerms");
            DictBasicDTO.ViewDTO viewDTO = collectionTermsList.stream().filter(req -> req.getValue().equals(customerInfoEntity.getCode())).findFirst().orElse(new DictBasicDTO.ViewDTO());
            resultMap.put("collectionTerms", viewDTO.getRemark());

            //获取币别信息
            List<CurrencyDTO.ViewDTO> currencyListt = sysUserFeign.listByCurrency(Arrays.asList(customerInfoEntity.getCurrency()));
            CurrencyDTO.ViewDTO currencyDTO = currencyListt.stream().filter(req -> req.getId().equals(customerInfoEntity.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
            resultMap.put("currencyCode", currencyDTO.getKingdeeCode());
        }

        //结算组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            BaseIdDTO.CodeDTO codeDTO = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSalesOrgId())).findFirst().orElse(new BaseIdDTO.CodeDTO());
            resultMap.put("salesOrgCode", codeDTO.getCode());
        }
        List<String> soKingdeeDetailIdList = soDetailEntitieList.stream().map(req -> req.getKingdeeDetailId()).collect(Collectors.toList());

        resultMap.put("soKingdeeDetailIds", String.join(",", soKingdeeDetailIdList));
        //金蝶 FEntity:物料信息
        List<Map<String,Object>> list = new ArrayList<>();

        List<String> sourceDetailIds = returnInstockDetailEntities.stream().map(req -> req.getSourceDetailId()).distinct().collect(Collectors.toList());


        //根据相应的来源id查询上有的数据
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailList = new ArrayList<>();
        List<SoReturnDetailEntity> returnDetailEntitys = new ArrayList<>();
        List<SoReturnDetailEntity> returnDetailList = new ArrayList<>();
        List<SoDetailEntity> soDetailList = new ArrayList<>();
        if (SourceTypeEnum.SO_RETURN_RECEIVE.getCode().equals(entity.getSourceType())) {
            soReturnReceiveDetailList = soReturnReceiveDetailService.listDetailByIds(sourceDetailIds);
            List<String> soReturnDetailIds = soReturnReceiveDetailList.stream().map(req -> req.getSourceDetailId()).distinct().collect(Collectors.toList());
            returnDetailEntitys = soReturnFeign.listDetailByIds(soReturnDetailIds);
        } else if (SourceTypeEnum.SO_RETURN.getCode().equals(entity.getSourceType())) {
            returnDetailList = soReturnFeign.listDetailByIds(sourceDetailIds);
        } else if (SourceTypeEnum.SO_INFO.getCode().equals(entity.getSourceType())) {
            soDetailList = soInfoFeign.listSoDetailByIds(sourceDetailIds);
        }

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(warehouseIds);

        for (SoReturnInstockDetailEntity detailEntity : returnInstockDetailEntities) {
            //根据来源id获取销售订单详情id
            String soDetailId = "";
            soDetailId = getSoDetailid(entity, returnDetailEntitys, returnDetailList, soDetailList, detailEntity, soDetailId);

            String finalSoDetailId = soDetailId;
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(finalSoDetailId)).findFirst().orElse(new SoDetailEntity());
            Map<String,Object> map = new HashMap<>();
            //退货原因
            if (StringUtils.isNotBlank(detailEntity.getReturnReasonDict())) {
                resultMap.put("returnReason", ReturnReasonEnum.getEnum(detailEntity.getReturnReasonDict()).getKingdeeCode());
            }
            //销售订单金蝶id
            map.put("soSyncKingdeeId", entity.getSyncKingdeeId());
            //物料编码
            map.put("skuNo", detailEntity.getSkuNo());
            //退货数量
            map.put("returnQty", detailEntity.getRealQty());
            map.put("salesQty", soDetailEntity.getQty());
            //单价
            map.put("price", soDetailEntity.getPrice());
            //含税单价
            BigDecimal flagTaxRate = MathUtil.divide(soDetailEntity.getTaxRate(), MathUtil.BigDecimal_100);
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(soDetailEntity.getPrice(), multiplyTax);
            //含税单价
            map.put("taxPrice", taxPrice);
            //是否赠品
            map.put("isGift", soDetailEntity.getIsGift());
            //金额
            map.put("amount", soDetailEntity.getAmount());
//            //税率
//            map.put("isGift", flagTaxRate);
            //退货类型
            map.put("returnType", detailEntity.getReturnTypeDict());
            //货主
            map.put("salesOrgCode", resultMap.get("salesOrgCode"));
            //仓库
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String warehouseId = detailEntity.getWarehouseId();
                String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(warehouseId)).map(WarehouseDTO.UpdateDTO::getKingdeeWarehouseCode).findFirst().orElse("");
                //仓库
                map.put("warehouseCode", warehouseCode);
            }
            //是否下推仓位
            Boolean isPush = pushKingdeeList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), detailEntity.getWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPush) {
                //仓位
                map.put("warehouseLocation", detailEntity.getWarehouseLocation());
            }

            //退货日期
            map.put("billDate", LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            String inventoryOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInventoryOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            //库存组织
            map.put("inventoryOrgCode", inventoryOrgCode);
            //备注
            map.put("remark", detailEntity.getRemark());
            if (ObjectUtils.isNotEmpty(soReturnEntity)) {
                if (soReturnEntity.getSourceType().equals(SourceTypeEnum.SO_INFO.getCode())) {
                    //原单类型
                    map.put("FSrcBillTypeID", "SAL_SaleOrder");
                    //原单编号
                    map.put("FSrcBillNo", soReturnEntity.getSourceCode());
                }
            }




            if (StringUtil.isNotBlank(soDetailEntity.getKingdeeDetailId()) && StringUtil.isNotBlank(soInfoEntity.getSyncKingdeeId())) {
                List<Map<String, Object>> mapList = new ArrayList<>();
                Map<String, Object> mapPush = new HashMap<>();
                mapPush.put("soKingdeeDetailId", soDetailEntity.getKingdeeDetailId());
                mapPush.put("soSyncKingdeeId", soInfoEntity.getSyncKingdeeId());
                mapPush.put("FEntity_Link_FSTableName", "T_SAL_ORDERENTRY");
                mapPush.put("FEntity_Link_FRuleId", "SaleOrder-SalReturnStock");
                mapList.add(mapPush);
                //销售单金蝶明细id
                map.put("FEntity_Link", mapList);
            }
            list.add(map);
        }
        resultMap.put("FEntityList", list);
        return resultMap;
	}
}
