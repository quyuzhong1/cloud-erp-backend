package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.CfgTransactionRulesEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryWarehouseOptionEnum;
import com.erp.server.wms.mapper.CfgTransactionRulesMapper;
import com.erp.server.wms.service.CfgTransactionRulesService;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Classname: TransactionRuleServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-26  10:20
 * @Author: zhangchunlin
 */
@Service
public class CfgTransactionRulesServiceImpl extends SuperServiceImpl<CfgTransactionRulesMapper, CfgTransactionRulesEntity> implements CfgTransactionRulesService {

    @Override
    public List<CfgTransactionRulesEntity> findByDictBizType(String dictBizType) {
        return lambdaQuery().eq(CfgTransactionRulesEntity::getDictBizType,dictBizType).list();
    }

    @Override
    public void initRules() {
        List<CfgTransactionRulesEntity> rules = Lists.newArrayList();
        // 21-入库预报
        // 在途增加
        CfgTransactionRulesEntity cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        InventoryBusinessTypeEnum businessType = InventoryBusinessTypeEnum.INSTOCK_FORCAST;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        InventoryStatusEnum inventoryStatus = InventoryStatusEnum.IN_TRANSIT;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        InventoryModeEnum inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 01-采购签收
        // 在途减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.PO_RECEIVE;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.IN_TRANSIT;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);
        // 待检增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.PO_RECEIVE;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.WAIT_QC;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 02-采购入库（有收货单）
        // 1.待检减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.PO_INSTOCK_REC;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.WAIT_QC;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);
        // 2.可用增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.PO_INSTOCK_REC;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 03-采购入库（无收货单）
        // 1.在途减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.PO_INSTOCK_UNREC;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.IN_TRANSIT;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);
        // 2.可用增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.PO_INSTOCK_UNREC;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 04-采购退货（退货来源：库存退货，退货方式：退货补货）
        // 1.在途增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.PO_RETURN_REP;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.IN_TRANSIT;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(),  inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 2.可用减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.PO_RETURN_REP;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 05-采购退货（退货来源：库存退货，退货方式：退货退款）
        // 可用减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.PO_RETURN_REF;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 06-调拨申请单
        // 1.可用减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.TRANSFER_APPLY;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);
        // 2.冻结增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.TRANSFER_APPLY;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.FROZEN;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(),  inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 10-销售发货通知单
        // 1.可用减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.SO_DELIVERY_NOTICE;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);
        // 2.冻结增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.SO_DELIVERY_NOTICE;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.FROZEN;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 11-销售出库
        // 冻结减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.SO_OUTSTOCK;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.FROZEN;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 07-直接调拨单（新增生成）
        // 当前仓可用减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.DIRECT_ALLOCATE;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);
        // 目的仓可用增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.DIRECT_ALLOCATE;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_TARGET.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_TARGET.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 08-分布式调拨调出
        // 当前仓冻结减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.TRANSFER_OUT;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.FROZEN;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);
        // 目的仓在途增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.TRANSFER_OUT;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_TARGET.getCode());
        inventoryStatus = InventoryStatusEnum.IN_TRANSIT;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), InventoryWarehouseOptionEnum.WAREHOUSE_TARGET.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 09-分布式调拨调入
        // 目的仓在途减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.TRANSFER_IN;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_TARGET.getCode());
        inventoryStatus = InventoryStatusEnum.IN_TRANSIT;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), InventoryWarehouseOptionEnum.WAREHOUSE_TARGET.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);
        // 目的仓可用增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.TRANSFER_IN;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_TARGET.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), InventoryWarehouseOptionEnum.WAREHOUSE_TARGET.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 19-其他入库单
        // 当前仓可用增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.OTHER_IN;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 20-其他出库单
        // 当前仓可用减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.OTHER_OUT;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT;
        cfgTransactionRulesEntity.setWarehouseOption(inventoryWarehouseOptionEnum.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), inventoryWarehouseOptionEnum.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 12-销售退货
        // 当前仓可用增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.SO_RETURN_INSTOCK;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        inventoryWarehouseOptionEnum = InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT;
        cfgTransactionRulesEntity.setWarehouseOption(inventoryWarehouseOptionEnum.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), inventoryWarehouseOptionEnum.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 17-加工单组装（父SKU增加）
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.ASSEMBLE_IN_PARENT;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        inventoryWarehouseOptionEnum = InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT;
        cfgTransactionRulesEntity.setWarehouseOption(inventoryWarehouseOptionEnum.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), inventoryWarehouseOptionEnum.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 18-加工单拆卸（父SKU减少）
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.DISASSEMBLE_IN_PARENT;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        inventoryWarehouseOptionEnum = InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT;
        cfgTransactionRulesEntity.setWarehouseOption(inventoryWarehouseOptionEnum.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), inventoryWarehouseOptionEnum.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 23-加工单组装（子SKU减少）
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.ASSEMBLE_IN_CHILDD;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        inventoryWarehouseOptionEnum = InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT;
        cfgTransactionRulesEntity.setWarehouseOption(inventoryWarehouseOptionEnum.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), inventoryWarehouseOptionEnum.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 24-加工单拆卸（子SKU增加）
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.DISASSEMBLE_IN_CHILD;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        inventoryWarehouseOptionEnum = InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT;
        cfgTransactionRulesEntity.setWarehouseOption(inventoryWarehouseOptionEnum.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), inventoryWarehouseOptionEnum.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 25-采购订单结束交货
        // 在途减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.PURCHASE_ORDER_FINISH;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        inventoryWarehouseOptionEnum = InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT;
        cfgTransactionRulesEntity.setWarehouseOption(inventoryWarehouseOptionEnum.getCode());
        inventoryStatus = InventoryStatusEnum.IN_TRANSIT;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(), inventoryWarehouseOptionEnum.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 26-直接调拨单（调拨申请单下推）
        // 当前仓冻结减少
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.DIRECT_ALLOCATE_APPLY;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.FROZEN;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.OUT_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);
        // 目的仓可用增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.DIRECT_ALLOCATE_APPLY;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_TARGET.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_TARGET.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 00-期初库存
        // 可用增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.INVENTORY_INIT;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.USABLE;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(), inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        // 27-采购退货（退货来源：质检退货，退货方式：退货补货）
        // 1.在途增加
        cfgTransactionRulesEntity = new CfgTransactionRulesEntity();
        businessType = InventoryBusinessTypeEnum.PO_RETURN_QC;
        cfgTransactionRulesEntity.setDictBizType(businessType.getCode());
        cfgTransactionRulesEntity.setWarehouseOption(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getCode());
        inventoryStatus = InventoryStatusEnum.IN_TRANSIT;
        cfgTransactionRulesEntity.setInventoryStatus(inventoryStatus.getCode());
        inventoryMode = InventoryModeEnum.IN_STOCK;
        cfgTransactionRulesEntity.setTransactionMode(inventoryMode.getCode());
        cfgTransactionRulesEntity.setRemark(StrUtil.format("{}，{}{}{}",businessType.getName(),  InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT.getName(),  inventoryStatus.getName(), inventoryMode.getName()));
        rules.add(cfgTransactionRulesEntity);

        super.saveBatch(rules);
    }

}