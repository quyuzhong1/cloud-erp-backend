package com.erp.server.scm.kingdee.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SubcontractTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.scm.entity.*;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchaseOrderService;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeePurchaseOrderServiceImpl implements SyncKingdeePurchaseOrderService {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private SubcontractChangeService subcontractChangeService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void syncDataToKingdee(PurchaseOrderEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //如果上游单据未发送成功则无需发送
        SubcontractOrderEntity subcontractOrderEntity = null;
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = null;
        if (StringUtils.isNotBlank(entity.getSubcontractType())) {
            //委外订单
            subcontractOrderEntity = subcontractOrderService.getById(entity.getSourceId());
            if (ObjectUtils.isEmpty(subcontractOrderEntity)) {
                throw new ServiceException(ApiError.ERROR_98073);
            }
            //委外订单明细
            subcontractOrderDetailList = subcontractOrderDetailService.listByMainId(subcontractOrderEntity.getId());
        }

        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getCode());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getSyncKingdeeId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            sendMqAndSaveTask(entity,operate,resultMap);
            return;
        }
        //采购日期
        resultMap.put("purchaseDate", LocalDateTimeUtil.format(entity.getPurchaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //单据类型
        resultMap.put("type",entity.getType());

        //查询采购供应商
        PurchaseOrderSupplierEntity purchaseOrderSupplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(entity.getId());
        if (ObjectUtils.isEmpty(purchaseOrderSupplierEntity)) {
            return;
        }
        SupplierEntity supplierEntity = supplierService.getById(purchaseOrderSupplierEntity.getSupplierId());
        if (ObjectUtils.isEmpty(supplierEntity)) {
            return;
        }

        //供应商编码
        resultMap.put("supplierCode",supplierEntity.getCode());

        //采购部门
        resultMap.put("purchaseDeptName",entity.getPurchaseDeptName());

        //获取用户部门id
        if (StringUtils.isNotBlank(entity.getPurchaseDeptId())) {
            SysDepartmentDTO departmentDTO = sysUserFeign.getUserDeptById(entity.getPurchaseDeptId());
            //采购部门
            if (ObjectUtil.isNotEmpty(departmentDTO)) {
                resultMap.put("purchaseDeptCode", departmentDTO.getCode());
            }
        }

        //采购员编码
        if (StringUtils.isNotBlank(entity.getPurchaseUserId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getPurchaseUserId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("purchaseUserCode", findUserDTO.getCode());
            }
        }
        //供应商联系人
        resultMap.put("contactName",purchaseOrderSupplierEntity.getContactName());
        //是否是新品首批
        resultMap.put("isFirstMassProduct",entity.getIsFirstMassProduct());

        if (ObjectUtils.isNotEmpty(purchaseOrderSupplierEntity.getPaymentCondition())) {
            //付款条件
            resultMap.put("paymentCondition",purchaseOrderSupplierEntity.getPaymentCondition());
        }

        //采购明细
        List<PurchaseOrderDetailEntity> details = purchaseOrderDetailService.listByPurchaseOrderId(entity.getId());
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        //组织机构编码
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getPurchaseOrgId(),entity.getReceiveOrgId()));
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            //采购组织编码
            String purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
            resultMap.put("purchaseOrgCode", purchaseOrgCode);
        }

        //部门信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(entity.getDeliveryWarehouseId()));

        List<JSONObject> list = new ArrayList<>();
        for (PurchaseOrderDetailEntity detailEntity : details) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("detailId",detailEntity.getId());
            jsonObject.set("skuNo",detailEntity.getSkuNo());
            jsonObject.set("purchaseQty",detailEntity.getPurchaseQty());
            jsonObject.set("planDeliveryDate",LocalDateTimeUtil.format(detailEntity.getPlanDeliveryDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
            jsonObject.set("price", MathUtil.divide(detailEntity.getTaxPrice(),MathUtil.add(MathUtil.BigDecimal_1,detailEntity.getTaxRate())) );
            jsonObject.set("taxPrice",detailEntity.getTaxPrice());
            //部门编码
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String kingdeeWarehouseCode = warehouseList.get(0).getKingdeeWarehouseCode();
                jsonObject.set("kingdeeWarehouseCode",kingdeeWarehouseCode);
            }
            jsonObject.set("taxRate",MathUtil.multiply(detailEntity.getTaxRate(),MathUtil.BigDecimal_100));
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                //收料组织编码
                String receiveOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
                jsonObject.set("receiveOrgCode", receiveOrgCode);

                //采购组织编码
                String purchaseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getCode())).orElse(null);
                jsonObject.set("purchaseOrgCode", purchaseOrgCode);
            }
            jsonObject.set("isGift",detailEntity.getIsGift());
            jsonObject.set("detailRemark",detailEntity.getRemark());
            //来源单据类型类型(只有委外父级SKU生成的采购订单需要设置委外采购)
            if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(entity.getSourceType()) && SubcontractTypeEnum.ENUM_PARENT.getCode().equals(entity.getSubcontractType())) {
                jsonObject.set("detailSourceType", KingdeePushModuleEnum.SUB_SUBREQORDER.getCode());
                if (ObjectUtils.isNotEmpty(subcontractOrderEntity)) {
                    //委外单号
                    jsonObject.set("refCode", subcontractOrderEntity.getCode());
                }
                //委外订单关联关系
                List<Map<String,Object>> refList = new ArrayList<>();
                JSONObject refJsonObject = new JSONObject();
                if (ObjectUtils.isNotEmpty(subcontractOrderEntity)) {
                    refJsonObject.set("refKingdeeId",subcontractOrderEntity.getSyncKingdeeId());
                    if (CollectionUtils.isNotEmpty(subcontractOrderDetailList)) {
                        String subDetailKingdeeId = subcontractOrderDetailList.stream()
                                .filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeDetailId()))
                                .orElse("");
                        refJsonObject.set("refDetailKingdeeId",subDetailKingdeeId);
                    }
                    refList.add(refJsonObject);
                    jsonObject.set("refList",refList);
                }
            }
            list.add(jsonObject);
        }
        resultMap.put("list",list);

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
    private void sendMqAndSaveTask (PurchaseOrderEntity entity, String operate, Map<String, Object> resultMap) {
        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(entity.getId());
        taskFeignDTO.setSourceCode(entity.getCode());
        taskFeignDTO.setSourceType(SourceTypeEnum.PURCHASE_ORDER.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_PURCHASE_ORDER_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
        taskFeignDTO.setSyncOperate(operate);
        if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(entity.getSourceType()) && SubcontractTypeEnum.ENUM_PARENT.getCode().equals(entity.getSubcontractType())) {
            taskFeignDTO.setParentId(entity.getSourceId());
        }
        dmpMqFeign.sendMqAndSaveTask(taskFeignDTO);
    }
}
