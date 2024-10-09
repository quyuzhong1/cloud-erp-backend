package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.enums.VwAllocationDirectionEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.ReportOrderDemandMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private VirtualInventoryService virtualInventoryService;

    @Autowired
    private VirtualWarehouseRelationService virtualWarehouseRelationService;

    @Autowired
    private VirtualWarehouseService virtualWarehouseService;

    @Autowired
    private VirtualWarehouseAllocationService virtualWarehouseAllocationService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
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
        ReportOrderDemandEntity entity = getByUnique(dto.getSkuId(), dto.getWarehouseId(), dto.getVirtualWarehouseId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("该产品未发现缺货统计数据");
        }
        BeanMapperUtils.copy(entity,resultDTO);

        //实体仓实际库存
        Integer realTotal = inventoryService.getRealInventoryTotal(entity.getWarehouseId(), entity.getSkuId());
        //虚拟仓实际库存
        Integer virtualRealTotal = virtualInventoryService.getInventoryQtyByWarehouseId(entity.getWarehouseId(), entity.getSkuId());

        //新增分货数据
        ReportOrderDemandDTO.AddAllocationViewDTO addAllocationViewDTO = new ReportOrderDemandDTO.AddAllocationViewDTO();
        addAllocationViewDTO.setWarehouseId(entity.getWarehouseId());
        addAllocationViewDTO.setWarehouseName(entity.getWarehouseName());
        Integer unDistributionQty = MathUtil.valueOfZero(realTotal) - MathUtil.valueOfZero(virtualRealTotal);
        addAllocationViewDTO.setUnDistributionQty(unDistributionQty);
        //可分配库存大于0则添加数据
        if (MathUtil.compareTo(unDistributionQty,MathUtil.ZERO) > MathUtil.ZERO) {
            resultDTO.setAddAllocationViewDTO(addAllocationViewDTO);
        }

        //虚拟仓调拨
        List<VirtualWarehouseRelationEntity> list = virtualWarehouseRelationService.getByWarehouseId(Arrays.asList(entity.getWarehouseId()));
        if (CollectionUtil.isEmpty(list)) {
            return resultDTO;
        }
        //虚拟仓
        List<String> virtualWarehouseIdList = list.stream().map(VirtualWarehouseRelationEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(virtualWarehouseIdList);

        //可用库存
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setSkuIdList(Arrays.asList(entity.getSkuId()));
        paramDTO.setWarehouseIdList(Arrays.asList(entity.getWarehouseId()));
        paramDTO.setVirtualWarehouseIdList(virtualWarehouseIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList = virtualInventoryService.listInventoryQty(paramDTO);

        List<ReportOrderDemandDTO.VirtualTransferViewDTO> virtualTransferViewList = new ArrayList<>();
        for (VirtualWarehouseRelationEntity relationEntity : list) {

            //数据虚拟仓和调入虚拟仓一致则跳过
            if (StrUtil.equals(dto.getVirtualWarehouseId(),relationEntity.getVirtualWarehouseId())) {
                continue;
            }
            ReportOrderDemandDTO.VirtualTransferViewDTO transferViewDTO = new ReportOrderDemandDTO.VirtualTransferViewDTO();
            transferViewDTO.setVirtualWarehouseId(relationEntity.getVirtualWarehouseId());
            //虚拟仓名称
            String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), relationEntity.getVirtualWarehouseId()))
                    .map(VirtualWarehouseEntity::getName).findFirst().orElse("");
            transferViewDTO.setVirtualWarehouseName(virtualWarehouseName);

            //虚拟仓可用
            Integer virtualUsableQty = virtualInventoryList.stream().filter(obj -> StrUtil.equals(InventoryStatusEnum.USABLE.getCode(), obj.getDictInventoryStatus())
                            && StrUtil.equals(obj.getVirtualWarehouseId(),transferViewDTO.getVirtualWarehouseId()))
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean addAllocation(ReportOrderDemandDTO.AddVirtualAllocationDTO dto) {
        ReportOrderDemandDTO.AddAllocationDTO addAllocationDTO = dto.getAddAllocationDTO();

        if (ObjectUtil.isEmpty(addAllocationDTO) && CollectionUtil.isNotEmpty(dto.getVirtualTransferList())) {
            throw new ServiceException("新增分货和虚拟仓调拨未填写数据不支持分货");
        }
        //新增分货
        if (ObjectUtil.isNotEmpty(addAllocationDTO)) {
            addVirtualAllocation(addAllocationDTO,dto.getSkuId());
        }

        //虚拟仓调拨
        if (CollectionUtil.isNotEmpty(dto.getVirtualTransferList())) {
            addVirtualTransfer(dto.getVirtualTransferList(),dto.getSkuId());
        }
        return Boolean.TRUE;
    }



    @Override
    public List<ReportOrderDemandDTO.BatchViewVirtualAllocationDTO> batchViewAllocation(ValidList<ReportOrderDemandDTO.ViewVirtualAllocationParamDTO> list) {
        if (CollectionUtil.isEmpty(list) || CollectionUtil.isEmpty(list.getList())) {
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
        //缺货统计数据
        List<ReportOrderDemandEntity> reportOrderDemandList = listByUnique(skuIdList, warehouseIdList, virtualWarehouseIdList);

        //实体仓实际库存
        InventoryQtyDTO.SkuInventoryStatusParamDTO inventoryParamDTO = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        inventoryParamDTO.setWarehouseIdList(warehouseIdList);
        inventoryParamDTO.setSkuIdList(skuIdList);
        inventoryParamDTO.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(),InventoryStatusEnum.FROZEN.getCode()));
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> skuInventoryStatusList = inventoryService.listSkuInventory(inventoryParamDTO);

        //虚拟仓关联关系
        List<VirtualWarehouseRelationEntity> relationList = virtualWarehouseRelationService.getByWarehouseId(warehouseIdList);
        List<String> newVirtualWarehouseIdList = relationList.stream().map(VirtualWarehouseRelationEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        //虚拟仓
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(newVirtualWarehouseIdList);

        //虚拟仓实际库存
        VirtualInventoryDTO.VirtualInventoryParamDTO virtualInventoryParamDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        virtualInventoryParamDTO.setSkuIdList(skuIdList);
        virtualInventoryParamDTO.setWarehouseIdList(warehouseIdList);
        virtualInventoryParamDTO.setVirtualWarehouseIdList(newVirtualWarehouseIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList = virtualInventoryService.listInventoryQty(virtualInventoryParamDTO);

        for (ReportOrderDemandDTO.ViewVirtualAllocationParamDTO paramDTO : paramList) {

            ReportOrderDemandEntity reportOrderDemandEntity = reportOrderDemandList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), paramDTO.getSkuId())
                            && StrUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId())
                            && StrUtil.equals(obj.getVirtualWarehouseId(), paramDTO.getVirtualWarehouseId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isEmpty(reportOrderDemandEntity)) {
                continue;
            }
            //新增分货数据
            batchViewVirtualAllocation(reportOrderDemandEntity,skuInventoryStatusList,virtualInventoryList,resultList);

            //虚拟仓调拨数据
            batchViewVirtualTransfer(reportOrderDemandEntity,relationList,virtualInventoryList,resultList,virtualWarehouseList);
        }
        return resultList;
    }

    /**
     * 虚拟仓调拨数据
     * @author will
     * @date 2024/9/26 9:38
     * @param reportOrderDemandEntity
     * @param relationList
     * @param virtualInventoryList
     * @param resultList
     */
    private void batchViewVirtualTransfer (ReportOrderDemandEntity reportOrderDemandEntity,List<VirtualWarehouseRelationEntity> relationList,
                                             List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList,List<ReportOrderDemandDTO.BatchViewVirtualAllocationDTO> resultList,
                                           List<VirtualWarehouseEntity> virtualWarehouseList) {
        if (CollectionUtil.isEmpty(relationList)) {
            return;
        }
        for (VirtualWarehouseRelationEntity relationEntity : relationList) {
            ReportOrderDemandDTO.BatchViewVirtualAllocationDTO viewVirtualAllocationDTO = new ReportOrderDemandDTO.BatchViewVirtualAllocationDTO();
            //虚拟仓调拨
            BeanMapperUtils.copy(reportOrderDemandEntity,viewVirtualAllocationDTO);
            viewVirtualAllocationDTO.setType(VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode());
            viewVirtualAllocationDTO.setTypeName(VirtualWarehouseAllocationTypeEnum.TRANSFER.getName());
            viewVirtualAllocationDTO.setOutWarehouseId(relationEntity.getVirtualWarehouseId());
            //名称
            String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), relationEntity.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
            viewVirtualAllocationDTO.setOutWarehouseName(virtualWarehouseName);
            //虚拟仓实际数量
            Integer realVirtualTotalQty = virtualInventoryList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), viewVirtualAllocationDTO.getSkuId())
                            && StrUtil.equals(obj.getWarehouseId(), reportOrderDemandEntity.getWarehouseId())
                            && StrUtil.equals(obj.getVirtualWarehouseId(), relationEntity.getVirtualWarehouseId())
                            && StrUtil.equals(obj.getDictInventoryStatus(),InventoryStatusEnum.USABLE.getCode()))
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                    .reduce(MathUtil.ZERO, Integer::sum);
            viewVirtualAllocationDTO.setUnDistributionQty(realVirtualTotalQty);
            if (MathUtil.compareTo(realVirtualTotalQty,MathUtil.ZERO) <= MathUtil.ZERO) {
                return;
            }
            resultList.add(viewVirtualAllocationDTO);
        }
    }

    /**
     * 批量分货查询处理分货
     * @author will
     * @date 2024/9/26 9:22
     * @param reportOrderDemandEntity
     * @param skuInventoryStatusList
     * @param virtualInventoryList
     * @param resultList
     */
    private void batchViewVirtualAllocation (ReportOrderDemandEntity reportOrderDemandEntity,List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> skuInventoryStatusList,
                                             List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList,List<ReportOrderDemandDTO.BatchViewVirtualAllocationDTO> resultList) {
        ReportOrderDemandDTO.BatchViewVirtualAllocationDTO viewVirtualAllocationDTO = new ReportOrderDemandDTO.BatchViewVirtualAllocationDTO();
        //添加分货
        BeanMapperUtils.copy(reportOrderDemandEntity,viewVirtualAllocationDTO);
        viewVirtualAllocationDTO.setType(VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode());
        viewVirtualAllocationDTO.setTypeName(VirtualWarehouseAllocationTypeEnum.ALLOCATION.getName());
        viewVirtualAllocationDTO.setOutWarehouseId(reportOrderDemandEntity.getWarehouseId());
        viewVirtualAllocationDTO.setOutWarehouseName(reportOrderDemandEntity.getWarehouseName());
        //实体仓实际库存
        Integer realTotalQty = skuInventoryStatusList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), viewVirtualAllocationDTO.getSkuId())
                        && StrUtil.equals(obj.getWarehouseId(), viewVirtualAllocationDTO.getOutWarehouseId()))
                .map(InventoryQtyDTO.SkuInventoryStatusTotalDTO::getInventoryTotal)
                .reduce(MathUtil.ZERO, Integer::sum);
        //虚拟仓实际库存
        Integer virtualRealTotalQty = virtualInventoryList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), viewVirtualAllocationDTO.getSkuId())
                        && StrUtil.equals(obj.getWarehouseId(), viewVirtualAllocationDTO.getOutWarehouseId())
                        && StrUtil.equals(obj.getVirtualWarehouseId(), viewVirtualAllocationDTO.getVirtualWarehouseId()))
                .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                .reduce(MathUtil.ZERO, Integer::sum);
        viewVirtualAllocationDTO.setUnDistributionQty(realTotalQty - virtualRealTotalQty);

        if (MathUtil.compareTo(viewVirtualAllocationDTO.getUnDistributionQty(),MathUtil.ZERO) <= MathUtil.ZERO) {
           return;
        }
        resultList.add(viewVirtualAllocationDTO);
    }


    @Override
    public Boolean batchAddAllocation(ValidList<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> list) {
        if (CollectionUtil.isEmpty(list) || CollectionUtil.isEmpty(list.getList())) {
            throw new ServiceException("选择数据不能为空");
        }
        List<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> addVirtualAllocationList = list.getList();
        Map<String, List<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO>> map = addVirtualAllocationList.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getWarehouseId()).concat(obj.getVirtualWarehouseId())));

        for (Map.Entry<String, List<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO>> entry : map.entrySet()) {
            List<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> value = entry.getValue();

            //新增分货
            ReportOrderDemandDTO.BatchAddVirtualAllocationDTO addVirtualAllocationDTO = value.stream().filter(obj -> StrUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(addVirtualAllocationDTO)) {
                batchAddVirtualAllocation(addVirtualAllocationDTO);
            }

            //调拨分货
            List<ReportOrderDemandDTO.BatchAddVirtualAllocationDTO> addVirtualTransferList = value.stream().filter(obj -> StrUtil.equals(obj.getType(), VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode())).collect(Collectors.toList());
            if (StrUtil.equals(addVirtualAllocationDTO.getType(),VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode())) {
                batchAddVirtualTransfer(addVirtualTransferList);
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 批量分货新增分货
     * @author will
     * @date 2024/9/26 10:32
     * @param addVirtualAllocationDTO
     */
    private void batchAddVirtualAllocation (ReportOrderDemandDTO.BatchAddVirtualAllocationDTO addVirtualAllocationDTO) {
        //新增分货
        VirtualWarehouseAllocationDTO.AddDTO addDTO = new VirtualWarehouseAllocationDTO.AddDTO();
        addDTO.setType(VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode());
        addDTO.setDirection(VwAllocationDirectionEnum.FORWARD.getCode());
        addDTO.setStatus(VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode());
        addDTO.setDisabled(Boolean.FALSE);
        VirtualWarehouseAllocationDTO.DetailDto detailDto = new VirtualWarehouseAllocationDTO.DetailDto();
        detailDto.setSkuId(addVirtualAllocationDTO.getSkuId());
        detailDto.setWarehouseId(addVirtualAllocationDTO.getWarehouseId());
        detailDto.setToVirtualWarehouseId(addVirtualAllocationDTO.getVirtualWarehouseId());
        detailDto.setQty(addVirtualAllocationDTO.getQty());
        addDTO.setDetailList(Arrays.asList(detailDto));
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
        List<VirtualWarehouseAllocationDTO.DetailDto> detailList = new ArrayList<>();
        for (ReportOrderDemandDTO.BatchAddVirtualAllocationDTO addVirtualAllocationDTO : addVirtualTransferList) {
            VirtualWarehouseAllocationDTO.DetailDto detailDto = new VirtualWarehouseAllocationDTO.DetailDto();
            detailDto.setSkuId(addVirtualAllocationDTO.getSkuId());
            detailDto.setWarehouseId(addVirtualAllocationDTO.getWarehouseId());
            detailDto.setToVirtualWarehouseId(addVirtualAllocationDTO.getVirtualWarehouseId());
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
     * @param skuId
     * @param warehouseId
     * @param virtualWarehouseId
     * @return ReportOrderDemandEntity
     */
    private ReportOrderDemandEntity getByUnique(String skuId,String warehouseId,String virtualWarehouseId) {
       return lambdaQuery().eq(ReportOrderDemandEntity::getSkuId,skuId)
                .eq(ReportOrderDemandEntity::getWarehouseId,warehouseId)
                .eq(ReportOrderDemandEntity::getVirtualWarehouseId,virtualWarehouseId)
                .last("limit 1")
                .one();
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
    private List<ReportOrderDemandEntity> listByUnique(List<String> skuIdList,List<String> warehouseIdList,List<String> virtualWarehouseIdList) {
        return lambdaQuery().in(ReportOrderDemandEntity::getSkuId,skuIdList)
                .in(ReportOrderDemandEntity::getWarehouseId,warehouseIdList)
                .in(ReportOrderDemandEntity::getVirtualWarehouseId,virtualWarehouseIdList)
                .list();
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
        VirtualWarehouseAllocationDTO.DetailDto detailDto = new VirtualWarehouseAllocationDTO.DetailDto();
        detailDto.setSkuId(skuId);
        detailDto.setWarehouseId(addAllocationDTO.getWarehouseId());
        detailDto.setToVirtualWarehouseId(addAllocationDTO.getVirtualWarehouseId());
        detailDto.setQty(addAllocationDTO.getQty());
        addDTO.setDetailList(Arrays.asList(detailDto));
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
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        for (ReportOrderDemandDTO.ListDTO listDTO : list) {
            //订单类型
            listDTO.setIsVirtualScarceName(listDTO.getIsVirtualScarce() ? "是" : "否");
        }
    }
}
