package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.ReportOrderDemandDTO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.ReportOrderDemandEntity;
import com.erp.model.wms.entity.ReportOrderSalesEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.model.wms.enums.VirtualReportTypeEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.enums.VwAllocationDirectionEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.ReportOrderDemandMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REPORT_ORDER_DEMAND;

/**
 * <p>+
 *
 *
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
@Slf4j
@Service
public class ReportOrderDemandServiceImpl extends SuperServiceImpl<ReportOrderDemandMapper, ReportOrderDemandEntity> implements ReportOrderDemandService {
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private VirtualInventoryService virtualInventoryService;

    @Resource
    private VirtualWarehouseRelationService virtualWarehouseRelationService;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    @Resource
    private VirtualWarehouseAllocationService virtualWarehouseAllocationService;

    @Resource
    private ReportOrderSalesService reportOrderSalesService;


    @Override
    public Boolean batchAddOrUpdate(List<ReportOrderDemandDTO.AddDTO> addOrUpdateList) {
        List<ReportOrderDemandEntity> list =  BeanMapperUtils.copyList(ReportOrderDemandEntity.class, addOrUpdateList);
        //删除原数据
        deleteAll();
        log.info("开始新增");
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<ReportOrderDemandDTO.ListDTO> paging(PagingDTO<ReportOrderDemandDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ReportOrderDemandDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public Boolean exportExcel(ReportOrderDemandDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("缺货统计", EXPORT_WMS_REPORT_ORDER_DEMAND.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ReportOrderDemandDTO.ListDTO> listReportOrderDemand(PagingDTO<ReportOrderDemandDTO.PagingParamDTO> pagingParamDTO) {
        PagingVO<ReportOrderDemandDTO.ListDTO> resultList = this.paging(pagingParamDTO);
        return resultList;
    }

    @Override
    public ReportOrderDemandDTO.ViewVirtualAllocationDTO viewAllocation(ReportOrderDemandDTO.ViewVirtualAllocationParamDTO dto) {
        ReportOrderDemandDTO.ViewVirtualAllocationDTO resultDTO = new ReportOrderDemandDTO.ViewVirtualAllocationDTO();
        //查询唯一数据
        ReportOrderDemandDTO.ViewDTO oldDTO = getByUnique(dto.getType(),dto.getSkuId(), dto.getWarehouseId(), dto.getVirtualWarehouseId());
        BeanMapperUtils.copy(oldDTO,resultDTO);

        //实体仓实际库存
        Integer realTotal = inventoryService.getRealInventoryTotal(resultDTO.getWarehouseId(), resultDTO.getSkuId());
        //虚拟仓实际库存
        Integer virtualRealTotal = virtualInventoryService.getInventoryQtyByWarehouseId(resultDTO.getWarehouseId(), resultDTO.getSkuId());

        //新增分货数据
        ReportOrderDemandDTO.AddAllocationViewDTO addAllocationViewDTO = new ReportOrderDemandDTO.AddAllocationViewDTO();
        addAllocationViewDTO.setWarehouseId(resultDTO.getWarehouseId());
        addAllocationViewDTO.setWarehouseName(resultDTO.getWarehouseName());
        Integer unDistributionQty = MathUtil.valueOfZero(realTotal) - MathUtil.valueOfZero(virtualRealTotal);
        addAllocationViewDTO.setUnDistributionQty(unDistributionQty);
        //可分配库存大于0则添加数据
        if (MathUtil.compareTo(unDistributionQty,MathUtil.ZERO) > MathUtil.ZERO) {
            resultDTO.setAddAllocationViewDTO(addAllocationViewDTO);
        }

        //虚拟仓调拨
        List<VirtualWarehouseRelationEntity> list = virtualWarehouseRelationService.getByWarehouseId(Collections.singletonList(resultDTO.getWarehouseId()));
        if (CollUtil.isEmpty(list)) {
            return resultDTO;
        }
        //虚拟仓
        List<String> virtualWarehouseIdList = list.stream().map(VirtualWarehouseRelationEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(virtualWarehouseIdList);

        //可用库存
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setSkuIdList(Collections.singletonList(resultDTO.getSkuId()));
        paramDTO.setWarehouseIdList(Collections.singletonList(resultDTO.getWarehouseId()));
        paramDTO.setVirtualWarehouseIdList(virtualWarehouseIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList = virtualInventoryService.listInventoryQty(paramDTO);

        List<ReportOrderDemandDTO.VirtualTransferViewDTO> virtualTransferViewList = new ArrayList<>();
        for (VirtualWarehouseRelationEntity relationEntity : list) {

            //数据虚拟仓和调入虚拟仓一致则跳过
            if (CharSequenceUtil.equals(dto.getVirtualWarehouseId(),relationEntity.getVirtualWarehouseId())) {
                continue;
            }
            ReportOrderDemandDTO.VirtualTransferViewDTO transferViewDTO = new ReportOrderDemandDTO.VirtualTransferViewDTO();
            transferViewDTO.setVirtualWarehouseId(relationEntity.getVirtualWarehouseId());
            //虚拟仓名称
            String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), relationEntity.getVirtualWarehouseId()))
                    .map(VirtualWarehouseEntity::getName).findFirst().orElse("");
            transferViewDTO.setVirtualWarehouseName(virtualWarehouseName);

            //虚拟仓可用
            Integer virtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(InventoryStatusEnum.USABLE.getCode(), obj.getDictInventoryStatus())
                            && CharSequenceUtil.equals(obj.getVirtualWarehouseId(),transferViewDTO.getVirtualWarehouseId()))
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty).reduce(MathUtil.ZERO, Integer::sum);
            transferViewDTO.setVirtualUsableQty(virtualUsableQty);
            //小于等于0则跳过
            if (MathUtil.compareTo(virtualUsableQty,MathUtil.ZERO) <= MathUtil.ZERO) {
                continue;
            }
            virtualTransferViewList.add(transferViewDTO);
        }
        resultDTO.setVirtualTransferViewList(virtualTransferViewList);
        return resultDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAllocation(ReportOrderDemandDTO.AddVirtualAllocationDTO dto) {
        ReportOrderDemandDTO.AddAllocationDTO addAllocationDTO = dto.getAddAllocationDTO();

        if (ObjectUtil.isEmpty(addAllocationDTO) && CollUtil.isEmpty(dto.getVirtualTransferList())) {
            throw new ServiceException("新增分货和虚拟仓调拨未填写数据不支持分货");
        }
        //新增分货
        if (ObjectUtil.isNotEmpty(addAllocationDTO)) {
            addVirtualAllocation(addAllocationDTO,dto.getSkuId());
        }

        //虚拟仓调拨
        if (CollUtil.isNotEmpty(dto.getVirtualTransferList())) {
            addVirtualTransfer(dto.getVirtualTransferList(),dto.getSkuId());
        }
        return Boolean.TRUE;
    }



    @Override
    public List<ReportOrderDemandDTO.BatchViewVirtualAllocationDTO> batchViewAllocation(ValidList<ReportOrderDemandDTO.ViewVirtualAllocationParamDTO> list) {
        if (CollUtil.isEmpty(list) || CollUtil.isEmpty(list.getList())) {
            throw new ServiceException("选择数据不能为空");
        }
        List<ReportOrderDemandDTO.ViewVirtualAllocationParamDTO> paramList = list.getList();
        //返回结果
        List<ReportOrderDemandDTO.BatchViewVirtualAllocationDTO> resultList = new ArrayList<>();
        //SKU
        List<String> skuIdList = paramList.stream().map(ReportOrderDemandDTO.ViewVirtualAllocationParamDTO::getSkuId).distinct().collect(Collectors.toList());
        //仓库
        List<String> warehouseIdList = paramList.stream().map(ReportOrderDemandDTO.ViewVirtualAllocationParamDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //虚拟仓库
        List<String> virtualWarehouseIdList = paramList.stream().map(ReportOrderDemandDTO.ViewVirtualAllocationParamDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        //唯一数据
        List<ReportOrderDemandDTO.ViewDTO> virtualAllocationList = listByUnique(list.get(0).getType(),skuIdList, warehouseIdList, virtualWarehouseIdList);

        //实体仓实际库存
        InventoryQtyDTO.SkuInventoryStatusParamDTO inventoryParamDTO = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        inventoryParamDTO.setWarehouseIdList(warehouseIdList);
        inventoryParamDTO.setSkuIdList(skuIdList);
        inventoryParamDTO.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(),InventoryStatusEnum.FROZEN.getCode()));
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> skuInventoryStatusList = inventoryService.listSkuInventory(inventoryParamDTO);

        //虚拟仓关联关系
        List<VirtualWarehouseRelationEntity> relationList = virtualWarehouseRelationService.getByWarehouseId(warehouseIdList);
        List<String> newVirtualWarehouseIdList = relationList.stream().map(VirtualWarehouseRelationEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());

        if (CollUtil.isEmpty(newVirtualWarehouseIdList)) {
            return Collections.emptyList();
        }
        //虚拟仓
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(newVirtualWarehouseIdList);

        //虚拟仓实际库存
        VirtualInventoryDTO.VirtualInventoryParamDTO virtualInventoryParamDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        virtualInventoryParamDTO.setSkuIdList(skuIdList);
        virtualInventoryParamDTO.setWarehouseIdList(warehouseIdList);
        virtualInventoryParamDTO.setVirtualWarehouseIdList(newVirtualWarehouseIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList = virtualInventoryService.listInventoryQty(virtualInventoryParamDTO);

        for (ReportOrderDemandDTO.ViewVirtualAllocationParamDTO paramDTO : paramList) {

            ReportOrderDemandDTO.ViewDTO oldDTO = virtualAllocationList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), paramDTO.getSkuId())
                            && CharSequenceUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId())
                            && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), paramDTO.getVirtualWarehouseId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isEmpty(oldDTO)) {
                continue;
            }
            //新增分货数据
            batchViewVirtualAllocation(oldDTO,skuInventoryStatusList,virtualInventoryList,resultList);

            List<VirtualWarehouseRelationEntity> newRelationList = relationList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(),paramDTO.getWarehouseId()) && !CharSequenceUtil.equals(obj.getVirtualWarehouseId(), paramDTO.getVirtualWarehouseId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(newRelationList)) {
                //虚拟仓调拨数据
                batchViewVirtualTransfer(oldDTO,newRelationList,virtualInventoryList,resultList,virtualWarehouseList);
            }
        }
        return resultList;
    }

    /**
     * 虚拟仓调拨数据
     * @author will
     * @date 2024/9/26 9:38
     * @param oldDTO
     * @param relationList
     * @param virtualInventoryList
     * @param resultList
     */
    private void batchViewVirtualTransfer (ReportOrderDemandDTO.ViewDTO oldDTO,List<VirtualWarehouseRelationEntity> relationList,
                                             List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList,List<ReportOrderDemandDTO.BatchViewVirtualAllocationDTO> resultList,
                                           List<VirtualWarehouseEntity> virtualWarehouseList) {
        if (CollUtil.isEmpty(relationList)) {
            return;
        }
        for (VirtualWarehouseRelationEntity relationEntity : relationList) {

            ReportOrderDemandDTO.BatchViewVirtualAllocationDTO viewVirtualAllocationDTO = new ReportOrderDemandDTO.BatchViewVirtualAllocationDTO();
            //虚拟仓调拨
            BeanMapperUtils.copy(oldDTO,viewVirtualAllocationDTO);
            viewVirtualAllocationDTO.setType(VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode());
            viewVirtualAllocationDTO.setTypeName(VirtualWarehouseAllocationTypeEnum.TRANSFER.getName());
            viewVirtualAllocationDTO.setOutWarehouseId(relationEntity.getVirtualWarehouseId());
            viewVirtualAllocationDTO.setVirtualScarceTotalQty(oldDTO.getVirtualScarceQty());
            viewVirtualAllocationDTO.setVirtualUsableTotalQty(oldDTO.getVirtualUsableQty());
            //名称
            String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), relationEntity.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
            viewVirtualAllocationDTO.setOutWarehouseName(virtualWarehouseName);
            //虚拟仓实际数量
            Integer realVirtualTotalQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), viewVirtualAllocationDTO.getSkuId())
                            && CharSequenceUtil.equals(obj.getWarehouseId(), viewVirtualAllocationDTO.getWarehouseId())
                            && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), relationEntity.getVirtualWarehouseId())
                            && CharSequenceUtil.equals(obj.getDictInventoryStatus(),InventoryStatusEnum.USABLE.getCode()))
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                    .reduce(MathUtil.ZERO, Integer::sum);
            viewVirtualAllocationDTO.setUnDistributionQty(realVirtualTotalQty);
            if (MathUtil.compareTo(realVirtualTotalQty,MathUtil.ZERO) <= MathUtil.ZERO) {
                continue;
            }
            resultList.add(viewVirtualAllocationDTO);
        }
    }

    /**
     * 批量分货查询处理分货
     * @author will
     * @date 2024/9/26 9:22
     * @param oldDTO
     * @param skuInventoryStatusList
     * @param virtualInventoryList
     * @param resultList
     */
    private void batchViewVirtualAllocation (ReportOrderDemandDTO.ViewDTO oldDTO,List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> skuInventoryStatusList,
                                             List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList,List<ReportOrderDemandDTO.BatchViewVirtualAllocationDTO> resultList) {
        ReportOrderDemandDTO.BatchViewVirtualAllocationDTO viewVirtualAllocationDTO = new ReportOrderDemandDTO.BatchViewVirtualAllocationDTO();
        //添加分货
        BeanMapperUtils.copy(oldDTO,viewVirtualAllocationDTO);
        viewVirtualAllocationDTO.setType(VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode());
        viewVirtualAllocationDTO.setTypeName(VirtualWarehouseAllocationTypeEnum.ALLOCATION.getName());
        viewVirtualAllocationDTO.setOutWarehouseId(oldDTO.getWarehouseId());
        viewVirtualAllocationDTO.setOutWarehouseName(oldDTO.getWarehouseName());
        viewVirtualAllocationDTO.setVirtualUsableTotalQty(oldDTO.getVirtualUsableQty());
        viewVirtualAllocationDTO.setVirtualScarceTotalQty(oldDTO.getVirtualScarceQty());
        //实体仓实际库存
        Integer realTotalQty = skuInventoryStatusList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), viewVirtualAllocationDTO.getSkuId())
                        && CharSequenceUtil.equals(obj.getWarehouseId(), viewVirtualAllocationDTO.getOutWarehouseId()))
                .map(InventoryQtyDTO.SkuInventoryStatusTotalDTO::getInventoryTotal)
                .reduce(MathUtil.ZERO, Integer::sum);
        //虚拟仓实际库存
        Integer virtualRealTotalQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), viewVirtualAllocationDTO.getSkuId())
                        && CharSequenceUtil.equals(obj.getWarehouseId(), viewVirtualAllocationDTO.getOutWarehouseId()))
                .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                .reduce(MathUtil.ZERO, Integer::sum);
        viewVirtualAllocationDTO.setUnDistributionQty(realTotalQty - virtualRealTotalQty);

        if (MathUtil.compareTo(viewVirtualAllocationDTO.getUnDistributionQty(),MathUtil.ZERO) <= MathUtil.ZERO) {
           return;
        }
        resultList.add(viewVirtualAllocationDTO);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAddAllocation(ValidList<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> list) {
        if (CollUtil.isEmpty(list) || CollUtil.isEmpty(list.getList())) {
            throw new ServiceException("选择数据不能为空");
        }
        List<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> addVirtualAllocationList = list.getList();

        //新增分货
        List<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> virtualAllocationList = addVirtualAllocationList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode())).collect(Collectors.toList());
        if (ObjectUtil.isNotEmpty(virtualAllocationList)) {
            batchAddVirtualAllocation(virtualAllocationList);
        }
        //调拨分货
        List<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> virtualTransferList = addVirtualAllocationList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(virtualTransferList)) {
            batchAddVirtualTransfer(virtualTransferList);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<ReportOrderDemandEntity> listByParam(List<String> skuIdList, List<String> warehouseIdList, List<String> virtualWarehouseIdList) {
        if (CollUtil.isEmpty(skuIdList) || CollUtil.isEmpty(warehouseIdList) || CollUtil.isEmpty(virtualWarehouseIdList)) {
            return Collections.emptyList();
        }
        return this.baseMapper.listByParam(skuIdList,warehouseIdList,virtualWarehouseIdList);
    }

    /**
     * 批量分货新增分货
     * @author will
     * @date 2024/9/26 10:32
     * @param virtualAllocationList
     */
    private void batchAddVirtualAllocation (List<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> virtualAllocationList) {
        //新增分货
        VirtualWarehouseAllocationDTO.AddDTO addDTO = new VirtualWarehouseAllocationDTO.AddDTO();
        addDTO.setType(VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode());
        addDTO.setDirection(VwAllocationDirectionEnum.FORWARD.getCode());
        addDTO.setStatus(VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode());
        addDTO.setDisabled(Boolean.FALSE);
        addDTO.setIsStatistics(Boolean.TRUE);
        List<VirtualWarehouseAllocationDTO.DetailDto> detailList = new ArrayList<>();
        for (ReportOrderDemandDTO.BatchAddVirtualAllocationDTO allocationDTO : virtualAllocationList) {
            VirtualWarehouseAllocationDTO.DetailDto detailDto = new VirtualWarehouseAllocationDTO.DetailDto();
            detailDto.setSkuId(allocationDTO.getSkuId());
            detailDto.setWarehouseId(allocationDTO.getWarehouseId());
            detailDto.setToVirtualWarehouseId(allocationDTO.getVirtualWarehouseId());
            detailDto.setQty(allocationDTO.getQty());
            detailList.add(detailDto);
        }
        addDTO.setDetailList(detailList);
        virtualWarehouseAllocationService.add(addDTO);
    }

    /**
     * 批量分货新增分货
     * @author will
     * @date 2024/9/26 10:32
     * @param addVirtualTransferList
     */
    private void batchAddVirtualTransfer (List<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> addVirtualTransferList) {
        //调拨分货
        VirtualWarehouseAllocationDTO.AddDTO addDTO = new VirtualWarehouseAllocationDTO.AddDTO();
        addDTO.setType(VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode());
        addDTO.setDirection(VwAllocationDirectionEnum.FORWARD.getCode());
        addDTO.setStatus(VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode());
        addDTO.setDisabled(Boolean.FALSE);
        addDTO.setIsStatistics(Boolean.FALSE);
        List<VirtualWarehouseAllocationDTO.DetailDto> detailList = new ArrayList<>();
        for (ReportOrderDemandDTO.BatchAddVirtualAllocationDTO addVirtualAllocationDTO : addVirtualTransferList) {
            VirtualWarehouseAllocationDTO.DetailDto detailDto = new VirtualWarehouseAllocationDTO.DetailDto();
            detailDto.setSkuId(addVirtualAllocationDTO.getSkuId());
            detailDto.setWarehouseId(addVirtualAllocationDTO.getWarehouseId());
            detailDto.setToVirtualWarehouseId(addVirtualAllocationDTO.getVirtualWarehouseId());
            detailDto.setFromVirtualWarehouseId(addVirtualAllocationDTO.getOutWarehouseId());
            detailDto.setQty(addVirtualAllocationDTO.getQty());
            detailList.add(detailDto);
        }
        addDTO.setDetailList(detailList);
        virtualWarehouseAllocationService.add(addDTO);
    }

    /**
     * 删除所有数据
     * @author will
     * @date 2024/9/27 10:43
     */
    private void deleteAll() {
        baseMapper.deleteAll();
    }

    /**
     * 根据sku、实体仓、虚拟仓查询唯一数据
     * @author will
     * @date 2024/9/25 18:36
     * @param type
     * @param skuId
     * @param warehouseId
     * @param virtualWarehouseId
     * @return ReportOrderDemandEntity
     */
    private ReportOrderDemandDTO.ViewDTO getByUnique(String type,String skuId,String warehouseId,String virtualWarehouseId) {
        ReportOrderDemandDTO.ViewDTO dto = new ReportOrderDemandDTO.ViewDTO();
        if (VirtualReportTypeEnum.SALES_DASHBOARD.getCode().equals(type)) {
            ReportOrderSalesEntity entity = reportOrderSalesService.getByUnique(skuId, warehouseId, virtualWarehouseId);
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException("未找到销售看板数据");
            }
            BeanMapperUtils.copy(entity,dto);
        } else {
            ReportOrderDemandEntity entity = lambdaQuery().eq(ReportOrderDemandEntity::getSkuId, skuId)
                    .eq(ReportOrderDemandEntity::getWarehouseId, warehouseId)
                    .eq(ReportOrderDemandEntity::getVirtualWarehouseId, virtualWarehouseId)
                    .last("limit 1")
                    .one();
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException("未找到缺货统计数据");
            }
            BeanMapperUtils.copy(entity,dto);
        }
        return dto;
    }

    /**
     * 根据唯一键查询
     * @author will
     * @date 2024/9/26 9:07
     * @param skuIdList
     * @param warehouseIdList
     * @param virtualWarehouseIdList
     * @return List<ReportOrderDemandEntity>
     */
    private List<ReportOrderDemandDTO.ViewDTO> listByUnique(String type,List<String> skuIdList,List<String> warehouseIdList,List<String> virtualWarehouseIdList) {
        List<ReportOrderDemandDTO.ViewDTO> resultList = new ArrayList<>();
        if (VirtualReportTypeEnum.SALES_DASHBOARD.getCode().equals(type)) {
            List<ReportOrderSalesEntity> list = reportOrderSalesService.listByUnique(skuIdList, warehouseIdList, virtualWarehouseIdList);
            if (CollUtil.isEmpty(list)) {
                throw new ServiceException("未找到销售看板数据");
            }
            resultList = BeanMapperUtils.copyList(ReportOrderDemandDTO.ViewDTO.class, list);
        } else {
            List<ReportOrderDemandEntity> list = lambdaQuery().in(ReportOrderDemandEntity::getSkuId, skuIdList)
                    .in(ReportOrderDemandEntity::getWarehouseId, warehouseIdList)
                    .in(ReportOrderDemandEntity::getVirtualWarehouseId, virtualWarehouseIdList)
                    .list();
            if (CollUtil.isEmpty(list)) {
                throw new ServiceException("未找到缺货统计数据");
            }
            resultList = BeanMapperUtils.copyList(ReportOrderDemandDTO.ViewDTO.class, list);
        }
        return resultList;
    }


    /**
     * 单个添加分货
     * @author will
     * @date 2024/9/25 19:18
     * @param addAllocationDTO
     */
    private void addVirtualAllocation (ReportOrderDemandDTO.AddAllocationDTO addAllocationDTO,String skuId) {
        VirtualWarehouseAllocationDTO.AddDTO addDTO = new VirtualWarehouseAllocationDTO.AddDTO();
        addDTO.setType(VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode());
        addDTO.setDirection(VwAllocationDirectionEnum.FORWARD.getCode());
        addDTO.setStatus(VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode());
        addDTO.setDisabled(Boolean.FALSE);
        addDTO.setIsStatistics(Boolean.TRUE);
        VirtualWarehouseAllocationDTO.DetailDto detailDto = new VirtualWarehouseAllocationDTO.DetailDto();
        detailDto.setSkuId(skuId);
        detailDto.setWarehouseId(addAllocationDTO.getWarehouseId());
        detailDto.setToVirtualWarehouseId(addAllocationDTO.getVirtualWarehouseId());
        detailDto.setQty(addAllocationDTO.getQty());
        addDTO.setDetailList(Collections.singletonList(detailDto));
        virtualWarehouseAllocationService.add(addDTO);
    }

    /**
     * 单个添加虚拟仓调拨
     * @author will
     * @date 2024/9/25 19:20
     * @param virtualTransferList
     */
    private void addVirtualTransfer (List<ReportOrderDemandDTO.VirtualTransferDTO> virtualTransferList,String skuId) {
        VirtualWarehouseAllocationDTO.AddDTO addDTO = new VirtualWarehouseAllocationDTO.AddDTO();
        addDTO.setType(VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode());
        addDTO.setDirection(VwAllocationDirectionEnum.FORWARD.getCode());
        addDTO.setStatus(VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode());
        addDTO.setDisabled(Boolean.FALSE);
        addDTO.setIsStatistics(Boolean.FALSE);
        List<VirtualWarehouseAllocationDTO.DetailDto> detailList = new ArrayList<>();
        for (ReportOrderDemandDTO.VirtualTransferDTO virtualTransferDTO : virtualTransferList) {
            VirtualWarehouseAllocationDTO.DetailDto detailDto = new VirtualWarehouseAllocationDTO.DetailDto();
            detailDto.setSkuId(skuId);
            detailDto.setWarehouseId(virtualTransferDTO.getWarehouseId());
            detailDto.setToVirtualWarehouseId(virtualTransferDTO.getToVirtualWarehouseId());
            detailDto.setFromVirtualWarehouseId(virtualTransferDTO.getFromVirtualWarehouseId());
            detailDto.setQty(virtualTransferDTO.getQty());
            detailList.add(detailDto);
        }
        addDTO.setDetailList(detailList);
        virtualWarehouseAllocationService.add(addDTO);
    }


    /**
     * 分页数据处理
     * @author will
     * @date 2024/9/25 14:31
     * @param list
     */
    private void fillPageData (List<ReportOrderDemandDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (ReportOrderDemandDTO.ListDTO listDTO : list) {
            //订单类型
            listDTO.setIsVirtualScarceName(listDTO.getIsVirtualScarce() ? "是" : "否");
        }
    }
}
