package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.ReportOrderSalesDTO;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;
import com.erp.model.wms.enums.inventory.InventoryOperationModeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.VirtualTransFlowMapper;
import com.erp.server.wms.service.VirtualTransFlowDetailService;
import com.erp.server.wms.service.VirtualTransFlowService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_TRANS_FLOW;

/**
 * <p>
 * 虚拟库存交易流水表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@Service
public class VirtualTransFlowServiceImpl extends SuperServiceImpl<VirtualTransFlowMapper, VirtualTransFlowEntity> implements VirtualTransFlowService {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private VirtualTransFlowDetailService virtualTransFlowDetailService;


    @Override
    public PagingVO<VirtualTransFlowDTO.ListDTO> paging(PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualTransFlowDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public VirtualTransFlowEntity add(VirtualTransFlowDTO.AddDTO addDTO, String virtualTansRuleId,InventoryModeEnum inventoryModeEnum) {
        // 记录交易流水
        VirtualTransFlowEntity virtualTransFlowEntity = new VirtualTransFlowEntity();
        BeanMapperUtils.copy(addDTO,virtualTransFlowEntity);

        LoginUser loginUser = UserContext.getDefaultLoginUser();
        virtualTransFlowEntity.setUserId(Objects.nonNull(loginUser) ? loginUser.getUid() : "");
        virtualTransFlowEntity.setTradeTime(ObjectUtil.isEmpty(virtualTransFlowEntity.getTradeTime()) ? LocalDateTime.now() : virtualTransFlowEntity.getTradeTime());
        virtualTransFlowEntity.setVirtualTransRuleId(StrUtils.null2EmptyWithTrim(virtualTansRuleId));
        Integer qty = addDTO.getQty();
        if(Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
            qty = qty * -1;
        }
        virtualTransFlowEntity.setQty(qty);
        boolean save = super.save(virtualTransFlowEntity);
        ValidatorUtil.isTrue(save, ()->new ServiceException("虚拟库存流水数据保存失败"));
        return virtualTransFlowEntity;
    }

    @Override
    public VirtualTransFlowEntity add(VirtualTransFlowEntity param, Integer afterInventoryQty) {
        // 记录交易流水
        LoginUser loginUser = UserContext.getDefaultLoginUser();

        // 复制所有参数
        VirtualTransFlowEntity virtualTransFlow = new VirtualTransFlowEntity();
        BeanMapper.copy(param,virtualTransFlow);
        // 更改指定的参数
        virtualTransFlow.setCurInventoryQty(afterInventoryQty);
        virtualTransFlow.setTradeTime(LocalDateTime.now());
        virtualTransFlow.setUserId(Objects.nonNull(loginUser) ? loginUser.getUid() : "0");
        virtualTransFlow.setUpdateUserId(loginUser.getUid());
        virtualTransFlow.setUpdateUserName(loginUser.getUserName());
        virtualTransFlow.setUpdateTime(LocalDateTime.now());

        // 个别参数设置空值
        virtualTransFlow.setId(null);
        boolean save = super.save(virtualTransFlow);
        ValidatorUtil.isTrue(save, ()->new ServiceException("虚拟库存数据保存失败"));
        return virtualTransFlow;
    }

    @Override
    public List<VirtualTransFlowEntity> getUnApprovedTxnFlows(String sourceType, String sourceId) {
        List<VirtualTransFlowEntity> txnFlows =  lambdaQuery()
                .eq(VirtualTransFlowEntity::getSourceType, sourceType)
                .eq(VirtualTransFlowEntity::getSourceId, sourceId)
                .eq(VirtualTransFlowEntity::getOperationMode, InventoryOperationModeEnum.APPROVE.getCode())
                .eq(VirtualTransFlowEntity::getIsUnapproved, Boolean.FALSE)
                .orderByDesc(VirtualTransFlowEntity::getId)
                .list();

        return txnFlows;
    }

    @Override
    public Boolean updateUnapprovedById(String id, Integer version) {
        LoginUser loginUser =  UserContext.getDefaultLoginUser();
        return baseMapper.updateUnapprovedById(id, version, LocalDateTime.now(), loginUser.getUid(), loginUser.getUserName());
    }

    @Override
    public PagingVO<VirtualTransFlowDTO.InventoryDetailDTO> detailPaging(PagingDTO<VirtualTransFlowDTO.InventoryDetailParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualTransFlowDTO.InventoryDetailDTO> pageData = this.baseMapper.detailPaging(query, dto.getParams());
        // 填充名称
        fillPageDetailData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(VirtualTransFlowDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("虚拟库存流水列表信息", EXPORT_WMS_VIRTUAL_TRANS_FLOW.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<VirtualTransFlowDTO.ListDTO> exportVirtualTransFlow(PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<VirtualTransFlowDTO.ListDTO> pageData = this.baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollUtil.isNotEmpty(pageData.getRecords())){
            //数据赋值处理
            fillPageData(pageData.getRecords());
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public List<VirtualTransFlowEntity> listHistoryFlow(List<String> sourceDetailIdList,String sourceType) {
        if (CollectionUtil.isEmpty(sourceDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery()
                .in(VirtualTransFlowEntity::getSourceDetailId,sourceDetailIdList)
                .eq(VirtualTransFlowEntity::getIsUnapproved,Boolean.FALSE)
                .eq(VirtualTransFlowEntity::getSourceType,sourceType)
                .list();
    }

    @Override
    public List<ReportOrderSalesDTO.LastVirtualQtyDTO> listLastVirtualQty(List<String> skuIdList, List<String> warehouseIdList, List<String> virtualWarehouseIdList, LocalDate localDate) {
        return baseMapper.listLastVirtualQty(skuIdList,warehouseIdList,virtualWarehouseIdList,localDate);
    }

    @Override
    public List<VirtualTransFlowEntity> listBySourceIdList(List<String> deliveryIdList) {
        if (CollUtil.isEmpty(deliveryIdList)) {
            throw new ServiceException("B2C发货单明细不能为空");
        }
        return  lambdaQuery().in(VirtualTransFlowEntity::getSourceId,deliveryIdList)
                .eq(VirtualTransFlowEntity::getDictBizType,VirtualInventoryBusinessTypeEnum.SO_OUT_STOCK.getCode())
                .list();
    }

    @Override
    public List<String> listVirtualInventoryId(String virtualInventoryId, String virtualWarehouseId, String warehouseId, String skuId, Boolean fromTable) {
        return baseMapper.listVirtualInventoryId(virtualInventoryId,virtualWarehouseId,warehouseId,skuId,fromTable);
    }

    @Override
    public void overrideVirtualTransFlow(LocalDate startDate, String virtualInvId) {
        log.info("###VirtualTransFlowServiceImpl:::overrideVirtualTransFlow 库存流水重算开始 virtualInvId={}, start_time={}", virtualInvId, LocalDateTime.now());
        List<VirtualTransFlowEntity> flowList = lambdaQuery()
                .eq(VirtualTransFlowEntity::getVirtualInventoryId, virtualInvId)
                .ge(VirtualTransFlowEntity::getBillDate, startDate)
                .last("for update")
                .list();
        if(CollUtil.isEmpty(flowList)) {
            log.warn("未找到需要重算的库存流水，库存id:{}, ", virtualInvId);
        }
        flowList = flowList.stream().sorted(Comparator.comparing(VirtualTransFlowEntity::getBillDate)
                        .thenComparing(VirtualTransFlowEntity::getTradeTime)
                        .thenComparing(VirtualTransFlowEntity::getId))
                .collect(Collectors.toList());
        Integer virtualQty = this.baseMapper.getVirtualQty(virtualInvId, startDate);
        // 重算库存流水
        overrideFlowByVirtualInventoryId(flowList, MathUtil.valueOfZero(virtualQty));
        log.info("###VirtualTransFlowServiceImpl:::overrideVirtualTransFlow 库存流水重算完成 virtualInvId={}, end_time={}",  virtualInvId, LocalDateTime.now());
    }

    @Override
    public List<VirtualTransFlowEntity> listHisVirtualTransFlow(VirtualTransFlowDetailDTO.HandleDTO dto) {
        return  lambdaQuery().in(CollUtil.isNotEmpty(dto.getIds()),VirtualTransFlowEntity::getId,dto.getIds())
                .ge(ObjUtil.isNotNull(dto.getStartDate()),VirtualTransFlowEntity::getBillDate,dto.getStartDate())
                .orderByAsc(VirtualTransFlowEntity::getBillDate)
                .orderByAsc(VirtualTransFlowEntity::getId)
                .list();
    }

    @Override
    public List<VirtualTransFlowEntity> listApproveByIds(List<String> oldVirtualTransFlowIdList) {
        if (CollUtil.isEmpty(oldVirtualTransFlowIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(VirtualTransFlowEntity::getId,oldVirtualTransFlowIdList)
                .lt(VirtualTransFlowEntity::getQty,MathUtil.ZERO)
                .eq(VirtualTransFlowEntity::getIsUnapproved,Boolean.FALSE)
                .orderByAsc(VirtualTransFlowEntity::getBillDate)
                .list();
    }

    @Override
    public List<VirtualTransFlowEntity> listApproveFlowDetail(VirtualTransFlowDetailDTO.HandleDTO dto) {
        List<VirtualTransFlowEntity> list = baseMapper.listApproveFlowDetail(dto);
        return list;
    }

    @Override
    public void handleAddDetail(VirtualTransFlowDetailDTO.HandleDTO dto) {
        log.warn("开始查询需要处理的数据，参数:{}", dto);
        List<VirtualTransFlowEntity> virtualTransFlowList = this.listApproveFlowDetail(dto);
        if (CollUtil.isEmpty(virtualTransFlowList)) {
            log.error("为查询到需要处理的虚拟库存流水数据，参数:{}", dto);
            return;
        }
        //清空数据
        if(dto.getIsClean()) {
            baseMapper.cleanALlData();
        }
        log.warn("清除数据完成，开始处理虚拟库存流水数据，参数:{}", dto);
        virtualTransFlowDetailService.handleAddTransFlowDetail(virtualTransFlowList);
    }

    @Override
    public void updateRemark(String id, String remark) {
        lambdaUpdate().eq(VirtualTransFlowEntity::getId,id).set(VirtualTransFlowEntity::getRemark,remark).update();
    }

    @Override
    public VirtualTransFlowEntity getUnApprovedTxnFlowBySource(VirtualTransFlowEntity entity) {
        return   lambdaQuery()
                .eq(VirtualTransFlowEntity::getSourceType, entity.getSourceType())
                .eq(VirtualTransFlowEntity::getSourceDetailId, entity.getSourceDetailId())
                .eq(VirtualTransFlowEntity::getSkuId, entity.getSkuId())
                .eq(VirtualTransFlowEntity::getOperationMode, InventoryOperationModeEnum.APPROVE.getCode())
                .le(VirtualTransFlowEntity::getTradeTime, entity.getTradeTime())
                .orderByDesc(VirtualTransFlowEntity::getTradeTime)
                .last("limit 1")
                .one();
    }

    /**
     * 重算库存流水
     * @author will
     * @date 2024/12/12 12:17
     * @param flowList
     */
    private void overrideFlowByVirtualInventoryId(List<VirtualTransFlowEntity> flowList,Integer virtualQty) {
        List<VirtualTransFlowEntity> updateList = new ArrayList<>();
        for (VirtualTransFlowEntity flowEntity : flowList) {
            Integer afterQty = virtualQty + flowEntity.getQty();
            updateList.add(new VirtualTransFlowEntity(flowEntity.getId(), afterQty));
            virtualQty = afterQty;
        }
        updateBatchById(updateList);
    }

    /**
     * 分页数据处理
     * @author will
     * @date 2024/6/3 17:10
     * @param list
     */
    private void fillPageData (List<VirtualTransFlowDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //产品信息
        List<String> skuIdList = list.stream().map(VirtualTransFlowDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);


        //实体仓库
        List<String> warehouseIdList = list.stream().map(VirtualTransFlowDTO.ListDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);

        //组织信息
        List<String> orgIdList = list.stream().map(VirtualTransFlowDTO.ListDTO::getOrgId).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);


        for (VirtualTransFlowDTO.ListDTO listDTO : list) {

            //产品信息
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> obj.getId().equals(listDTO.getSkuId())).findFirst().orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "产品信息"));
            listDTO.setProductName(productDetailEntity.getName());

            //来源类型名称
            listDTO.setSourceTypeName(SourceTypeEnum.getName(listDTO.getSourceType()));
            //实体仓库名称
            String warehouseName = warehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), listDTO.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
            listDTO.setWarehouseName(warehouseName);

            //组织名称
            String orgName = accountingCompanyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), listDTO.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
            listDTO.setOrgName(orgName);

            //操作状态名称
            listDTO.setOperationModeName(EnumMessage.getNameByCode(InventoryOperationModeEnum.class,listDTO.getOperationMode()));
            //库存状态名称
            listDTO.setDictInventoryStatusName(EnumMessage.getNameByCode(InventoryStatusEnum.class,listDTO.getDictInventoryStatus()));
        }
    }


    /**
     * 虚拟库存分页查询明细数据处理
     * @author will
     * @date 2024/6/3 16:58
     * @param list
     */
    private void fillPageDetailData (List<VirtualTransFlowDTO.InventoryDetailDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //实体仓库
        List<String> warehouseIdList = list.stream().map(VirtualTransFlowDTO.InventoryDetailDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
        for (VirtualTransFlowDTO.InventoryDetailDTO inventoryDetailDTO : list) {
            //来源类型名称
            inventoryDetailDTO.setSourceTypeName(SourceTypeEnum.getName(inventoryDetailDTO.getSourceType()));
            //实体仓库名称
            String warehouseName = warehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), inventoryDetailDTO.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
            inventoryDetailDTO.setWarehouseName(warehouseName);

        }
    }
}
