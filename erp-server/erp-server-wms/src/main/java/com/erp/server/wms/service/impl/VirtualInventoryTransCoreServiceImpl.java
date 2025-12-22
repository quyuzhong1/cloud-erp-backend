package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidGroup;
import com.common.business.vo.LoginUser;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 虚拟库存交易核心处理类
 * @author will
 * @date 2024/6/5 17:44
 */
@Service
public class VirtualInventoryTransCoreServiceImpl implements VirtualInventoryTransCoreService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private CfgVirtualTransRulesService cfgVirtualTransRulesService;

    @Resource
    private VirtualInventoryTradingService virtualInventoryTradingService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private VirtualInventoryService virtualInventoryService;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    @Resource
    private VirtualTransFlowService virtualTransFlowService;



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(VirtualInventoryStockDTO.StockParamDTO dto) {
        ValidatorUtil.validateEntity(dto);
        //BOM拆分
        List<VirtualInventoryStockDTO.OutInStockDTO> outInStockList = splitBom(dto.getParamList(), dto.getIsSplitBom());
        if (CollUtil.isEmpty(outInStockList)) {
            outInStockList = dto.getParamList();
        }

        //查询规则
        List<VirtualTransRuleDTO.StockParamDTO> rules = dto.getRules();
        if (CollUtil.isEmpty(dto.getRules())) {
            rules = this.wrapTransactionRule(VirtualInventoryBusinessTypeEnum.getByCode(dto.getBusinessType()));
        }
        //校验业务参数
        this.checkInOutParam(outInStockList);
        //校验规则
        this.checkRule(rules);
        //解析库存交易数据
        List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionDtoList = this.parseTranactionFromInOut(dto.getBusinessType(),outInStockList,rules);
        //执行库存交易
        virtualInventoryTradingService.doTransactionList(transactionDtoList, InventoryTradingService.APPROVE);
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(VirtualInventoryStockDTO.TransferParamDTO dto) {
        //查询规则
        List<VirtualTransRuleDTO.StockParamDTO> rules = dto.getRules();
        if (CollUtil.isEmpty(dto.getRules())) {
            rules = this.wrapTransactionRule(VirtualInventoryBusinessTypeEnum.getByCode(dto.getBusinessType()));
        }
        //校验业务参数
        this.checkTransferParam(dto.getParamList());
        //校验规则
        this.checkRule(rules);
        //解析库存交易数据
        List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionDtoList = this.parseTransactionFromTransfer(dto.getBusinessType(),dto.getParamList(),rules);
        //执行库存交易
        virtualInventoryTradingService.doTransactionList(transactionDtoList, InventoryTradingService.APPROVE);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unApprove(InventoryUnApproveDTO dto) {
        ValidatorUtil.validateEntity(dto);
        List< VirtualTransFlowEntity> transactionFlowList = this.queryTransactionFlowList(dto.getSourceType(),dto.getBillId());
        List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionDtoList= this.parseTransactionForUnApprove(transactionFlowList);
        virtualInventoryTradingService.doTransactionList(transactionDtoList,InventoryTradingService.UNAPPROVE);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUnApprove(InventoryBatchUnApproveDTO dto) {
        ValidatorUtil.validateEntity(dto);
        dto.getBillIds().forEach(billId->{
            InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO();
            inventoryUnApproveDTO.setSourceType(dto.getSourceType());
            inventoryUnApproveDTO.setBillId(billId);
            this.unApprove(inventoryUnApproveDTO);
        });
    }

    /**
     * 查询交易流水
     * @param sourceType    业务类型
     * @param billId    单据id
     * @return  交易流水
     */
    private List<VirtualTransFlowEntity> queryTransactionFlowList(InventorySourceTypeEnum sourceType, String billId) {
        List<VirtualTransFlowEntity> result = Lists.newArrayList();
        QueryWrapper<VirtualTransFlowEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(VirtualTransFlowEntity::getSourceType, sourceType.getCode())
                .eq(VirtualTransFlowEntity::getSourceId, billId)
                .eq(VirtualTransFlowEntity::getIsUnapproved, false);
        result.addAll(virtualTransFlowService.list(wrapper));
        return result;
    }

    /**
     * 解析交易流水
     * @author will
     * @date 2025/11/25 10:57
     * @param transactionFlowList
     * @return List<InventoryTransactionDTO>
     */
    private List<VirtualInventoryStockDTO.InventoryTransactionDTO> parseTransactionForUnApprove(List<VirtualTransFlowEntity> transactionFlowList) {
        List<VirtualInventoryStockDTO.InventoryTransactionDTO> result = Lists.newArrayList();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<WarehouseEntity> warehouseEntityList = warehouseService.listWarehouseWithCaches();
        List<BaseIdDTO> orgList = sysUserFeign.listAccountingCompany();

        transactionFlowList.forEach(flow->{
            VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO = new VirtualInventoryStockDTO.InventoryTransactionDTO();
            // 交易头部信息
            transactionDTO.setId(flow.getId());
            transactionDTO.setTransactionNo(flow.getTransactionNo());
            transactionDTO.setVirtualTransRuleId(flow.getVirtualTransRuleId());

            // 交易明细信息
            transactionDTO.setVirtualInventoryId(flow.getVirtualInventoryId());
            transactionDTO.setSkuId(flow.getSkuId());
            transactionDTO.setSkuNo(flow.getSkuNo());
            transactionDTO.setOrgId(flow.getOrgId());
            transactionDTO.setWarehouseId(flow.getWarehouseId());
            transactionDTO.setInventoryStatus(flow.getDictInventoryStatus());

            transactionDTO.setOrgName(getOrgName(orgList,flow.getOrgId()));
            transactionDTO.setWarehouseName(getWarehouseInfo(warehouseEntityList,flow.getWarehouseId()).getName());
            transactionDTO.setInventoryStatusName(InventoryStatusEnum.getByCode(flow.getDictInventoryStatus()).getName());

            // 交易时间 & 单据类型
            transactionDTO.setBillDate(flow.getBillDate());
            transactionDTO.setDictBizType(flow.getDictBizType());
            transactionDTO.setSourceType(flow.getSourceType());
            transactionDTO.setSourceTypeName(InventorySourceTypeEnum.getByCode(flow.getSourceType()).getName());
            transactionDTO.setSourceId(flow.getSourceId());
            transactionDTO.setSourceCode(flow.getSourceCode());
            transactionDTO.setSourceDetailId(flow.getSourceDetailId());

            // 交易数量
            transactionDTO.setQty(flow.getQty()*-1);

            // 交易人员信息
            transactionDTO.setUserId(userInfo.getUid());
            transactionDTO.setUserName(userInfo.getUserName());

            result.add(transactionDTO);
        });

        // 更新交易数据 是否忽略交易|是否允许负库存
        this.fillTransactionIgnoreOptions(result);
        return result;
    }


    /**
     * 解析库存交易数据
     * @author will
     * @date 2025/11/25 10:45
     * @param businessType
     * @param transferList
     * @param rules
     * @return List<InventoryTransactionDTO>
     */
    private List<VirtualInventoryStockDTO.InventoryTransactionDTO> parseTransactionFromTransfer(String businessType, List<VirtualInventoryStockDTO.TransferStockDTO> transferList, List<VirtualTransRuleDTO.StockParamDTO> rules) {
        List<VirtualInventoryStockDTO.InventoryTransactionDTO> result = Lists.newArrayList();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<WarehouseEntity> warehouseEntityList = warehouseService.listWarehouseWithCaches();
        List<BaseIdDTO> orgList = sysUserFeign.listAccountingCompany();

        // 关联交易号
        String transactionNo = IdUtil.getSnowflake().nextIdStr();

        for(VirtualInventoryStockDTO.TransferStockDTO flow : transferList) {
            for(VirtualTransRuleDTO.StockParamDTO rule : rules) {
                VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO = new VirtualInventoryStockDTO.InventoryTransactionDTO();

                // 交易头部信息
                transactionDTO.setTransactionNo(transactionNo);
                transactionDTO.setVirtualTransRuleId(rule.getId());

                // 库存基础信息
                VirtualInventoryStockDTO.StockBaseDTO stockBaseDTO = new VirtualInventoryStockDTO.StockBaseDTO();
                stockBaseDTO.setSkuId(flow.getSkuId());
                stockBaseDTO.setSkuNo(flow.getSkuNo());
                stockBaseDTO.setInventoryStatus(rule.getInventoryStatus());
                if(rule.getWarehouseOption()==InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT){
                    stockBaseDTO.setWarehouseId(flow.getCurWarehouseId());
                    stockBaseDTO.setVirtualWarehouseId(flow.getVirtualCurWarehouseId());
                }else {
                    stockBaseDTO.setWarehouseId(flow.getTargetWarehouseId());
                    stockBaseDTO.setVirtualWarehouseId(flow.getVirtualTargetWarehouseId());
                }
                VirtualInventoryEntity virtualInventoryEntity = virtualInventoryService.getByTransaction(VirtualInventoryStockDTO.InventoryTransactionDTO.getInventoryTransactionDTO(stockBaseDTO));
                transactionDTO.setVirtualInventoryId(null == virtualInventoryEntity ? null : virtualInventoryEntity.getId());

                // 交易明细信息
                transactionDTO.setSkuId(stockBaseDTO.getSkuId());
                transactionDTO.setSkuNo(stockBaseDTO.getSkuNo());
                String orgId = getOrgIdFromWarehouse(warehouseEntityList, stockBaseDTO.getWarehouseId());
                transactionDTO.setOrgId(orgId);
                transactionDTO.setWarehouseId(stockBaseDTO.getWarehouseId());
                transactionDTO.setVirtualWarehouseId(stockBaseDTO.getVirtualWarehouseId());
                transactionDTO.setInventoryStatus(rule.getInventoryStatus().getCode());
                // 设置冗余信息部分
                transactionDTO.setOrgName(getOrgName(orgList,orgId));
                transactionDTO.setWarehouseName(getWarehouseInfo(warehouseEntityList,stockBaseDTO.getWarehouseId()).getName());
                transactionDTO.setInventoryStatusName(rule.getInventoryStatus().getName());

                // 交易时间 & 单据类型
                transactionDTO.setBillDate(flow.getBillDate());
                transactionDTO.setDictBizType(businessType);
                transactionDTO.setSourceType(flow.getSourceType().getCode());
                transactionDTO.setSourceTypeName(flow.getSourceType().getName());
                transactionDTO.setSourceId(flow.getSourceId());
                transactionDTO.setSourceCode(flow.getSourceCode());
                transactionDTO.setSourceDetailId(flow.getSourceDetailId());

                // 交易数量
                transactionDTO.setQty(flow.getQty() * rule.getTransactionMode().getCode());

                // 交易人员信息
                transactionDTO.setUserId(userInfo.getUid());
                transactionDTO.setUserName(userInfo.getUserName());

                result.add(transactionDTO);
            }
        }

        // 更新交易数据 是否忽略交易|是否允许负库存
        this.fillTransactionIgnoreOptions(result);
        return result;
    }

    /**
     * 解析库存交易数据
     * @author will
     * @date 2025/11/24 16:37
     * @param businessType
     * @param outInStockList
     * @param rules
     * @return List<InventoryTransactionDTO>
     */
    private List<VirtualInventoryStockDTO.InventoryTransactionDTO> parseTranactionFromInOut(String businessType, List<VirtualInventoryStockDTO.OutInStockDTO> outInStockList, List<VirtualTransRuleDTO.StockParamDTO> rules) {
        List<VirtualInventoryStockDTO.InventoryTransactionDTO> result = Lists.newArrayList();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //仓库
        List<WarehouseEntity> warehouseEntityList = warehouseService.listWarehouseWithCaches();
        //组织
        List<BaseIdDTO> orgList = sysUserFeign.listAccountingCompany();
        //虚拟仓
        List<String> virtualWarehouseIdList = outInStockList.stream().map(VirtualInventoryStockDTO.OutInStockDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(virtualWarehouseIdList);
        Map<String, String> virtualWarehouseMap = CollUtil.isEmpty(virtualWarehouseList) ? new HashMap<>() :
                virtualWarehouseList.stream().collect(Collectors.toMap(VirtualWarehouseEntity::getId, VirtualWarehouseEntity::getName));


        //同一个操作产生的交易流水使用同一个关联交易号
        String transactionNo =  docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XLS);

        for(VirtualInventoryStockDTO.OutInStockDTO outInStockDTO : outInStockList) {
            for(VirtualTransRuleDTO.StockParamDTO rule : rules) {
                VirtualInventoryStockDTO.InventoryTransactionDTO transactionDTO = new VirtualInventoryStockDTO.InventoryTransactionDTO();

                // 交易头部信息
                transactionDTO.setTransactionNo(transactionNo);
                transactionDTO.setVirtualTransRuleId(rule.getId());


                // 交易明细信息
                transactionDTO.setSkuId(outInStockDTO.getSkuId());
                transactionDTO.setSkuNo(outInStockDTO.getSkuNo());
                String orgId = getOrgIdFromWarehouse(warehouseEntityList, outInStockDTO.getWarehouseId());
                transactionDTO.setOrgId(orgId);
                transactionDTO.setWarehouseId(outInStockDTO.getWarehouseId());
                transactionDTO.setVirtualWarehouseId(outInStockDTO.getVirtualWarehouseId());
                transactionDTO.setInventoryStatus(rule.getInventoryStatus().getCode());

                // 库存基础信息
                VirtualInventoryEntity virtualInventoryEntity = virtualInventoryService.getByTransaction(transactionDTO);
                transactionDTO.setVirtualInventoryId(ObjectUtil.isEmpty(virtualInventoryEntity) ? null : virtualInventoryEntity.getId());

                // 设置冗余信息部分
                transactionDTO.setOrgName(getOrgName(orgList,orgId));
                transactionDTO.setWarehouseName(getWarehouseInfo(warehouseEntityList,outInStockDTO.getWarehouseId()).getName());
                transactionDTO.setInventoryStatusName(rule.getInventoryStatus().getName());
                transactionDTO.setVirtualWarehouseName(virtualWarehouseMap.get(outInStockDTO.getVirtualWarehouseId()));

                // 交易时间 & 单据类型
                transactionDTO.setBillDate(outInStockDTO.getBillDate());
                transactionDTO.setDictBizType(businessType);
                transactionDTO.setSourceType(outInStockDTO.getSourceType().getCode());
                transactionDTO.setSourceTypeName(outInStockDTO.getSourceType().getName());
                transactionDTO.setSourceId(outInStockDTO.getSourceId());
                transactionDTO.setSourceCode(outInStockDTO.getSourceCode());
                transactionDTO.setSourceDetailId(outInStockDTO.getSourceDetailId());

                // 交易数量
                transactionDTO.setQty(outInStockDTO.getQty() * rule.getTransactionMode().getCode());

                // 交易人员信息
                transactionDTO.setUserId(userInfo.getUid());
                transactionDTO.setUserName(userInfo.getUserName());

                // 是否允许负库存
                transactionDTO.setAllowNegativeInventory(Boolean.FALSE);

                result.add(transactionDTO);
            }
        }
        // 更新交易数据 是否忽略交易|是否允许负库存
        this.fillTransactionIgnoreOptions(result);
        return result;
    }

    /**
     * 更新交易数据：是否忽略交易|是否允许负库存
     * @param  paramList   交易数据
     */
    private void fillTransactionIgnoreOptions(List<VirtualInventoryStockDTO.InventoryTransactionDTO> paramList) {
        if(CollUtil.isEmpty(paramList)) {
            return;
        }
        // 查询忽略库存的sku
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }
        List<String> finalIgnoreInventorySkuIds = ignoreInventorySkuIds;

        // 更新交易数据
        paramList.forEach(transactionDTO-> {
            // 是否忽略交易
            transactionDTO.setIgnoreTransaction(finalIgnoreInventorySkuIds.contains(transactionDTO.getSkuId()));
        });
    }

    /**
     * 校验出入库信息
     * @author will
     * @date 2025/11/24 15:19
     * @param outInStockList
     * @return void
     */
    private void checkInOutParam (List<VirtualInventoryStockDTO.OutInStockDTO> outInStockList) {
        if(CollUtil.isEmpty(outInStockList)) {
            ServiceException.runError("入库出库参数不能为空");
        }

        List<String>  ignoreInventorySkuIds = this.getIgnoreSkuIds();

        for (VirtualInventoryStockDTO.OutInStockDTO outInStockDTO : outInStockList) {
            ValidatorUtil.validateEntity(outInStockDTO, ValidGroup.Update.class);
            // 非赠品类sku数量必须大于0
            if(0 >= outInStockDTO.getQty() && !ignoreInventorySkuIds.contains(outInStockDTO.getSkuId())) {
                throw new ServiceException("库存变更数量不能小于等于0");
            }
        }
    }


    /**
     * 校验调拨参数
     * @param transferParam 调拨参数
     */
    private void checkTransferParam(List<VirtualInventoryStockDTO.TransferStockDTO> transferParam) {
        if(CollUtil.isEmpty(transferParam)) {
            ServiceException.runError("调拨参数不能为空");
        }
        List<String>  ignoreInventorySkuIds = this.getIgnoreSkuIds();

        for (VirtualInventoryStockDTO.TransferStockDTO transferDTO : transferParam) {
            ValidatorUtil.validateEntity(transferDTO);
            // 非赠品类sku数量必须大于0
            if(0 >= transferDTO.getQty() && !ignoreInventorySkuIds.contains(transferDTO.getSkuId())) {
                throw new ServiceException("库存变更数量不能小于等于0");
            }
        }
    }

   /**
    * 校验交易规则
    * @author will
    * @date 2025/11/24 15:23
    * @param rules
    * @return void
    */
    private void checkRule(List<VirtualTransRuleDTO.StockParamDTO> rules) {
        if(CollUtil.isEmpty(rules)) {
            ServiceException.runError("交易规则不能为空");
        }
        for (VirtualTransRuleDTO.StockParamDTO rule : rules) {
            ValidatorUtil.validateEntity(rule);
        }
    }

    /**
     * 获取货主公司id
     * @param warehouseId 仓库id
     * @return  核算公司id
     */
    private String getOrgIdFromWarehouse(List<WarehouseEntity> warehouseList,String warehouseId) {
        if(CollectionUtils.isEmpty(warehouseList)) {
            ServiceException.runError("查询仓库列表为空");
        }
        WarehouseEntity warehouseEntity = warehouseList.stream().filter(w->w.getId().equals(warehouseId)).findFirst().orElse(null);
        if(null == warehouseEntity) {
            ServiceException.runError("仓库信息(ID={})不存在", warehouseId);
        }
        return warehouseEntity.getOrgId();
    }

    /**
     * 获取核算公司名称
     * @param orgId 核算公司id
     * @return  核算公司名称
     */
    private String getOrgName(List<BaseIdDTO> orgList, String orgId) {
        if(CollUtil.isEmpty(orgList)) {
            ServiceException.runError("查询核算公司列表为空");
        }
        BaseIdDTO org = orgList.stream().filter(o->o.getId().equals(orgId)).findFirst().orElse(null);
        if(null == org) {
            ServiceException.runError("核算公司(ID={})不存在", orgId);
        }
        return org.getName();
    }

    /**
     * 过滤并获取仓库信息
     * @param warehouseList 仓库列表
     * @param warehouseId   仓库id
     * @return  仓库名称
     */
    private WarehouseEntity getWarehouseInfo(List<WarehouseEntity> warehouseList, String warehouseId) {
        if(CollectionUtils.isEmpty(warehouseList)) {
            ServiceException.runError("查询仓库列表为空");
        }
        WarehouseEntity warehouseEntity = warehouseList.stream().filter(w->w.getId().equals(warehouseId)).findFirst().orElse(null);
        if(null == warehouseEntity) {
            ServiceException.runError("仓库信息(ID={})不存在", warehouseId);
        }

        return warehouseEntity;
    }



    /**
     * 获取忽略库存计算的sku
     * @return  返回忽略的SKU ID列表
     */
    protected List<String> getIgnoreSkuIds() {
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }
        return ignoreInventorySkuIds;
    }

    /**
     * 拆分BOM
     * @author will
     * @date 2024/7/21 16:02
     * @param paramList
     * @return List<OutInStockDTO>
     */
    private List<VirtualInventoryStockDTO.OutInStockDTO> splitBom (List<VirtualInventoryStockDTO.OutInStockDTO> paramList,Boolean isSplitBom) {
        List<VirtualInventoryStockDTO.OutInStockDTO> resultList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(isSplitBom) && !isSplitBom) {
            return resultList;
        }
        //是否拆分bom
        List<DictBasicDTO.ListDTO> list = dictBasicService.getByKey(DictBasicEnum.VIRTUAL_SPLIT_BOM.getKey());
        if (CollUtil.isEmpty(list) || !Boolean.valueOf(list.get(0).getValue())) {
            return resultList;
        }

        List<String> skuIdList = paramList.stream().map(VirtualInventoryStockDTO.OutInStockDTO::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);
        if (CollUtil.isEmpty(bomChildrenSkuList)) {
            return resultList;
        }
        List<BomChildrenSkuDTO> bomList = bomChildrenSkuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
        if (CollUtil.isEmpty(bomList)) {
            return resultList;
        }
        for (VirtualInventoryStockDTO.OutInStockDTO outInStockDTO : paramList) {
            //bom信息
            List<BomChildrenSkuDTO> childList = bomList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentSkuId(), outInStockDTO.getSkuId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(childList)) {
                resultList.add(outInStockDTO);
                continue;
            }
            //优先取录入的bom版本，没有则取最新bom版本
            if (CharSequenceUtil.isNotBlank(outInStockDTO.getBomVersion())) {
                childList = childList.stream().filter(obj -> CharSequenceUtil.equals(obj.getBomVersion(),outInStockDTO.getBomVersion())).collect(Collectors.toList());
            } else {
                String bomVersion = childList.stream().max(Comparator.comparing(BomChildrenSkuDTO::getBomVersion)).map(BomChildrenSkuDTO::getBomVersion).get();
                childList = childList.stream().filter(obj -> CharSequenceUtil.equals(obj.getBomVersion(),bomVersion)).collect(Collectors.toList());
            }
            if (CollUtil.isEmpty(childList)) {
                throw new ServiceException(CharSequenceUtil.format("SKU【】未找到版本为【{}】的BOM",outInStockDTO.getSkuId(),outInStockDTO.getBomVersion()));
            }
            for (BomChildrenSkuDTO bomChildrenSkuDTO: childList) {
                VirtualInventoryStockDTO.OutInStockDTO newOutInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
                BeanMapperUtils.copy(outInStockDTO,newOutInStockDTO);
                newOutInStockDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
                newOutInStockDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                newOutInStockDTO.setQty(bomChildrenSkuDTO.getQuantity() * outInStockDTO.getQty());
                resultList.add(newOutInStockDTO);
            }
        }
        return resultList;
    }


    /**
     * 查询配置的交易规则
     * @author will
     * @date 2024/6/4 11:24
     * @param businessType  业务类型
     * @return List<StockParamDTO> 交易规则
     */
    protected List<VirtualTransRuleDTO.StockParamDTO> wrapTransactionRule(VirtualInventoryBusinessTypeEnum businessType) {
        // 查询配置的交易规则
        List<CfgVirtualTransRulesEntity> list = cfgVirtualTransRulesService.findByDictBizType(businessType.getCode());
        if(CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<VirtualTransRuleDTO.StockParamDTO> ruleList = Lists.newArrayListWithExpectedSize(list.size());
        list.forEach(r->{
            VirtualTransRuleDTO.StockParamDTO stockParamDTO = new VirtualTransRuleDTO.StockParamDTO();
            stockParamDTO.setId(r.getId());
            stockParamDTO.setDictBizType(VirtualInventoryBusinessTypeEnum.getByCode(r.getDictBizType()));
            stockParamDTO.setWarehouseOption(InventoryWarehouseOptionEnum.getByCode(r.getWarehouseOption()));
            stockParamDTO.setInventoryStatus(InventoryStatusEnum.getByCode(r.getInventoryStatus()));
            stockParamDTO.setTransactionMode(InventoryModeEnum.getByCode(r.getTransactionMode()));
            ruleList.add(stockParamDTO);
        });
        return ruleList;
    }
}