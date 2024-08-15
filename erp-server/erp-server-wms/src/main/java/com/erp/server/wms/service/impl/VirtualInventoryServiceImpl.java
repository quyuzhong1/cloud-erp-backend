package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.VirtualInventoryMapper;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.VirtualInventoryService;
import com.erp.server.wms.service.VirtualWarehouseService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_INVENTORY;

/**
 * <p>
 * 虚拟库存表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@Service
public class VirtualInventoryServiceImpl extends SuperServiceImpl<VirtualInventoryMapper, VirtualInventoryEntity> implements VirtualInventoryService {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public PagingVO<VirtualInventoryDTO.ListDTO> paging(PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualInventoryDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }


    @Override
    public Boolean exportExcel(VirtualInventoryDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("虚拟仓库库存信息", EXPORT_WMS_VIRTUAL_INVENTORY.getCode(), dto);
        return Boolean.TRUE;
    }


    @Override
    public VirtualInventoryEntity findVirtualInventoryStock(String virtualWarehouseId, String warehouseId, String skuId, String inventoryStatus) {
        return lambdaQuery()
                .eq(VirtualInventoryEntity::getVirtualWarehouseId, virtualWarehouseId)
                .eq(VirtualInventoryEntity::getWarehouseId, warehouseId)
                .eq(VirtualInventoryEntity::getSkuId, skuId)
                .eq(VirtualInventoryEntity::getDictInventoryStatus, inventoryStatus)
                .last("limit 1")
                .one();
    }

    @Override
    public VirtualInventoryEntity addOrUpdate(String virtualWarehouseId, String warehouseId, String skuId, String skuNo, String inventoryStatus, Integer qty) {
        //根据虚拟仓库id、skuId、状态编码查询
        VirtualInventoryEntity found = findVirtualInventoryStock(virtualWarehouseId, warehouseId, skuId, inventoryStatus);
        if (ObjectUtil.isEmpty(found)) {
            log.info("库存状态：【{}】，虚拟仓库【{}】，实体仓库【{}】，SKU：【{}】，SKU编号：【{}】在库存实时表中不存在数据，新增数据", inventoryStatus, virtualWarehouseId, warehouseId, skuId, skuNo);
            found = new VirtualInventoryEntity();
            found.setVirtualWarehouseId(virtualWarehouseId);
            found.setWarehouseId(warehouseId);
            found.setSkuId(skuId);
            found.setSkuNo(skuNo);
            found.setDictInventoryStatus(inventoryStatus);
            found.setQty(qty);
            found.setAfterQty(qty);
            boolean save = super.save(found);
            ValidatorUtil.isTrue(save, () -> new ServiceException("虚拟库存数据保存失败"));
        } else {
            found.setAfterQty(found.getQty() + qty);
            log.info("库存状态：【{}】，虚拟仓库【{}】，实体仓库【{}】，SKU：【{}】，SKU编号：【{}】在库存实时表中存在数据，修改数据", inventoryStatus, virtualWarehouseId, warehouseId, skuId, skuNo);
            // 更新实时库存表数量
            boolean updateFlag = this.updateQtyById(found.getId(), qty);
            if (!updateFlag) {
                throw new ServiceException(ApiError.ERROR_1027);
            }
        }
        return found;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateQtyById(String id, Integer qty) {
        boolean flag = lambdaUpdate()
                .setSql(StrUtil.format("{}={}+{}", "qty", "qty", qty))
                .eq(VirtualInventoryEntity::getId, id)
                .update(new VirtualInventoryEntity());
        if (!flag) {
            throw new ServiceException(ApiError.ERROR_1027);
        }
        return flag;
    }

    /**
     * 根据条件查询库存信息
     *
     * @param qtyTypeDTO
     * @author hyj
     * @date 2024/6/6
     */
    @Override
    public List<VirtualInventoryDTO.ViewQtyDTO> getQty(VirtualInventoryDTO.QtyTypeDTO qtyTypeDTO) {
        String type = qtyTypeDTO.getType();
        List<VirtualInventoryDTO.QtySearchDTO> qtySearchList = qtyTypeDTO.getQtySearchList();
        if (CollectionUtils.isEmpty(qtySearchList)) {
            return BeanMapperUtils.copyList(VirtualInventoryDTO.ViewQtyDTO.class, qtySearchList);
        }
        List<VirtualInventoryDTO.QtySearchDTO> qtySearchDTOS = qtySearchList.stream().filter(qtySearchDTO ->
                StringUtils.isNotBlank(qtySearchDTO.getWarehouseId()) && StringUtils.isNotBlank(qtySearchDTO.getSkuId())).collect(Collectors.toList());
        List<VirtualInventoryDTO.ViewQtyDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(qtySearchDTOS)) {
            return BeanMapperUtils.copyList(VirtualInventoryDTO.ViewQtyDTO.class, qtySearchList);
        }
        List<String> skuIds = qtySearchDTOS.stream().map(VirtualInventoryDTO.QtySearchDTO::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> warehouseIds = qtySearchDTOS.stream().map(VirtualInventoryDTO.QtySearchDTO::getWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        VirtualInventoryDTO.ParamDTO vmParamDto = new VirtualInventoryDTO.ParamDTO();
        vmParamDto.setSkuIdList(skuIds);
        vmParamDto.setWarehouseIdList(warehouseIds);
        switch (VirtualWarehouseAllocationTypeEnum.getEnum(type)) {
            case ALLOCATION:
                getAllocationInfo(qtySearchDTOS, skuIds, warehouseIds, vmParamDto, qtySearchList, resultList);
                break;
            case TRANSFER:
                getTransferInfo(qtySearchDTOS, vmParamDto, qtySearchList, resultList);
                break;
            case CANCEL:
                getCancelInfo(qtySearchDTOS, skuIds, warehouseIds, vmParamDto, qtySearchList, resultList);
                break;
            default:
                resultList.addAll(qtySearchList.stream().map(item -> {
                    VirtualInventoryDTO.ViewQtyDTO viewQtyDTO = new VirtualInventoryDTO.ViewQtyDTO();
                    BeanUtils.copyProperties(item, viewQtyDTO);
                    return viewQtyDTO;
                }).collect(Collectors.toList()));
                break;
        }
        return resultList;
    }

    private void getCancelInfo(List<VirtualInventoryDTO.QtySearchDTO> qtySearchDTOS, List<String> skuIds, List<String> warehouseIds, VirtualInventoryDTO.ParamDTO vmParamDto, List<VirtualInventoryDTO.QtySearchDTO> qtySearchList, List<VirtualInventoryDTO.ViewQtyDTO> resultList) {
        //获取实体仓可用库存
        InventoryDTO.ParamDTO paramDTO = new InventoryDTO.ParamDTO();
        paramDTO.setSkuIdList(skuIds);
        paramDTO.setWarehouseIdList(warehouseIds);
        List<InventoryDTO.InventoryViewQtyDTO> inventoryUsableQtyList = inventoryService.getUsableQtyBySkuIdsAndWarehouseIds(paramDTO);
        //获取实体仓对应的虚拟仓所有（可用+冻结）库存数量
        List<VirtualInventoryDTO.ViewQtyDTO> vmRealQtyList = baseMapper.getRealQty(vmParamDto);
        //获取调出虚拟仓可用数量
        List<String> fromVmIds = qtySearchDTOS.stream().map(VirtualInventoryDTO.QtySearchDTO::getFromVirtualWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        vmParamDto.setVirtualWarehouseIdList(fromVmIds);
        List<VirtualInventoryDTO.ViewQtyDTO> vwUsableQtyList = getVmUsableQty(vmParamDto, fromVmIds);
        for (VirtualInventoryDTO.QtySearchDTO qtySearchDTO : qtySearchList) {
            //获取实体仓可用库存
            VirtualInventoryDTO.ViewQtyDTO viewQtyDTO = new VirtualInventoryDTO.ViewQtyDTO();
            BeanUtils.copyProperties(qtySearchDTO, viewQtyDTO);
            List<InventoryDTO.InventoryViewQtyDTO> inventoryViewQtyDTOS = inventoryUsableQtyList.stream()
                    .filter(inventoryViewQtyDTO -> Objects.equals(inventoryViewQtyDTO.getSkuId(), qtySearchDTO.getSkuId())
                            && Objects.equals(inventoryViewQtyDTO.getWarehouseId(), qtySearchDTO.getWarehouseId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(inventoryViewQtyDTOS)) {
                Integer warehouseAllocationQty = inventoryViewQtyDTOS.get(0).getUsableQty();
                viewQtyDTO.setWarehouseAllocationQty(warehouseAllocationQty);
                viewQtyDTO.setWarehouseUsableQty(warehouseAllocationQty);
            }

            //获取调出仓可用库存
            VirtualInventoryDTO.ViewQtyDTO qtyDTO = vwUsableQtyList.stream().filter(item -> Objects.equals(item.getSkuId(), qtySearchDTO.getSkuId())
                            && Objects.equals(item.getWarehouseId(), qtySearchDTO.getWarehouseId())
                            && Objects.equals(item.getToVirtualWarehouseId(), qtySearchDTO.getFromVirtualWarehouseId()))
                    .findFirst().orElse(null);
            if (Objects.nonNull(qtyDTO)) {
                viewQtyDTO.setFromVirtualWarehouseUsableQty(qtyDTO.getToVirtualWarehouseUsableQty());
            }
            resultList.add(viewQtyDTO);
        }
    }

    private void getTransferInfo(List<VirtualInventoryDTO.QtySearchDTO> qtySearchDTOS, VirtualInventoryDTO.ParamDTO vmParamDto, List<VirtualInventoryDTO.QtySearchDTO> qtySearchList, List<VirtualInventoryDTO.ViewQtyDTO> resultList) {
        List<String> vmIds = qtySearchDTOS.stream().map(VirtualInventoryDTO.QtySearchDTO::getToVirtualWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        //获取调入、调出虚拟仓可用数量
        List<String> fromVmIds = qtySearchDTOS.stream().map(VirtualInventoryDTO.QtySearchDTO::getFromVirtualWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        vmIds.addAll(fromVmIds);
        List<VirtualInventoryDTO.ViewQtyDTO> vwUsableQtyList = getVmUsableQty(vmParamDto, vmIds);
        for (VirtualInventoryDTO.QtySearchDTO qtySearchDTO : qtySearchList) {//获取调入仓、调出仓可用库存
            VirtualInventoryDTO.ViewQtyDTO viewQtyDTO = new VirtualInventoryDTO.ViewQtyDTO();
            BeanUtils.copyProperties(qtySearchDTO, viewQtyDTO);
            //获取调出仓可用库存
            VirtualInventoryDTO.ViewQtyDTO fromQtyDto = vwUsableQtyList.stream().filter(item -> Objects.equals(item.getWarehouseId(), qtySearchDTO.getWarehouseId())
                            && Objects.equals(item.getSkuId(), qtySearchDTO.getSkuId()) && Objects.equals(item.getToVirtualWarehouseId(), qtySearchDTO.getFromVirtualWarehouseId()))
                    .findFirst().orElse(null);
            VirtualInventoryDTO.ViewQtyDTO toQtyDto = vwUsableQtyList.stream().filter(item -> Objects.equals(item.getWarehouseId(), qtySearchDTO.getWarehouseId())
                            && Objects.equals(item.getSkuId(), qtySearchDTO.getSkuId()) && Objects.equals(item.getToVirtualWarehouseId(), qtySearchDTO.getToVirtualWarehouseId()))
                    .findFirst().orElse(null);
            viewQtyDTO.setToVirtualWarehouseUsableQty(Objects.isNull(toQtyDto) ? 0 : toQtyDto.getToVirtualWarehouseUsableQty());
            viewQtyDTO.setFromVirtualWarehouseUsableQty(Objects.isNull(fromQtyDto) ? 0 : fromQtyDto.getToVirtualWarehouseUsableQty());
            resultList.add(viewQtyDTO);
        }
    }

    private void getAllocationInfo(List<VirtualInventoryDTO.QtySearchDTO> qtySearchDTOS, List<String> skuIds, List<String> warehouseIds, VirtualInventoryDTO.ParamDTO vmParamDto, List<VirtualInventoryDTO.QtySearchDTO> qtySearchList, List<VirtualInventoryDTO.ViewQtyDTO> resultList) {
        //获取实体仓可用库存
        InventoryDTO.ParamDTO paramDTO = new InventoryDTO.ParamDTO();
        paramDTO.setSkuIdList(skuIds);
        paramDTO.setWarehouseIdList(warehouseIds);
        List<InventoryDTO.InventoryViewQtyDTO> inventoryUsableQtyList = inventoryService.getUsableQtyBySkuIdsAndWarehouseIds(paramDTO);
        //获取实体仓对应的虚拟仓所有（可用+冻结）库存数量
        List<VirtualInventoryDTO.ViewQtyDTO> vmRealQtyList = baseMapper.getRealQty(vmParamDto);
        //获取调入虚拟仓可用数量
        List<String> toVmIds = qtySearchDTOS.stream().map(VirtualInventoryDTO.QtySearchDTO::getToVirtualWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<VirtualInventoryDTO.ViewQtyDTO> vmUsableQty = getVmUsableQty(vmParamDto, toVmIds);
        //拼接返回数据
        for (VirtualInventoryDTO.QtySearchDTO qtySearchDTO : qtySearchList) {
            //获取实体仓可分配库存：实体仓可用库存-虚拟仓实际库存（可用+冻结）
            VirtualInventoryDTO.ViewQtyDTO viewQtyDTO = new VirtualInventoryDTO.ViewQtyDTO();
            BeanUtils.copyProperties(qtySearchDTO, viewQtyDTO);
            List<InventoryDTO.InventoryViewQtyDTO> inventoryViewQtyDTOS = inventoryUsableQtyList.stream()
                    .filter(inventoryViewQtyDTO -> Objects.equals(inventoryViewQtyDTO.getSkuId(), qtySearchDTO.getSkuId())
                            && Objects.equals(inventoryViewQtyDTO.getWarehouseId(), qtySearchDTO.getWarehouseId()))
                    .collect(Collectors.toList());
            List<VirtualInventoryDTO.ViewQtyDTO> vmRealDTOS = vmRealQtyList.stream().filter(inventoryViewQtyDTO ->
                            Objects.equals(inventoryViewQtyDTO.getSkuId(), qtySearchDTO.getSkuId()) && Objects.equals(inventoryViewQtyDTO.getWarehouseId(), qtySearchDTO.getWarehouseId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(inventoryViewQtyDTOS)) {
                Integer warehouseAllocationQty = inventoryViewQtyDTOS.get(0).getUsableQty();
                if (CollectionUtils.isNotEmpty(vmRealDTOS)) {
                    warehouseAllocationQty -= vmRealDTOS.get(0).getToVirtualWarehouseRealQty();
                }
                viewQtyDTO.setWarehouseAllocationQty(warehouseAllocationQty);
            }
            VirtualInventoryDTO.ViewQtyDTO vmUsableQtyDto = vmUsableQty.stream().filter(inventoryViewQtyDTO -> Objects.equals(inventoryViewQtyDTO.getSkuId(), qtySearchDTO.getSkuId())
                    && Objects.equals(inventoryViewQtyDTO.getWarehouseId(), qtySearchDTO.getWarehouseId())
                    && Objects.equals(inventoryViewQtyDTO.getToVirtualWarehouseId(), qtySearchDTO.getToVirtualWarehouseId())).findFirst().orElse(null);
            if (Objects.nonNull(vmUsableQtyDto)) {
                viewQtyDTO.setToVirtualWarehouseUsableQty(vmUsableQtyDto.getToVirtualWarehouseUsableQty());
            }
            resultList.add(viewQtyDTO);
        }
    }

    private List<VirtualInventoryDTO.ViewQtyDTO> getVmUsableQty(VirtualInventoryDTO.ParamDTO vmParamDto, List<String> toVmIds) {
        List<VirtualInventoryDTO.ViewQtyDTO> vmUsableQty = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(toVmIds)) {
            vmParamDto.setVirtualWarehouseIdList(toVmIds);
            vmUsableQty = baseMapper.getUsableQty(vmParamDto);
        }
        return vmUsableQty;
    }

    /**
     * 根据skuIds warehouseIds vmIds获取虚拟仓可用库存
     *
     * @param vmParamDto
     * @return
     */
    @Override
    public List<VirtualInventoryDTO.ViewQtyDTO> getVmUsableQtyBySkuIdsAndWIdsAndVmIds(VirtualInventoryDTO.ParamDTO vmParamDto) {
        return baseMapper.getUsableQty(vmParamDto);
    }

    @Override
    public List<VirtualInventoryDTO.VirtualInventoryQtyDTO> listInventoryQty(VirtualInventoryDTO.VirtualInventoryParamDTO params) {
        return baseMapper.listInventoryQty(params);
    }


    /**
     * 获取可用数量
     */
    @Override
    public Integer findUsableQtyByQtyDto(VirtualInventoryDTO.VirtualInventoryQtyDTO virtualInventoryQtyDTO) {
        return baseMapper.findUsableQtyByQtyDto(virtualInventoryQtyDTO);
    }

    @Override
    public Integer getInventoryQtyByWarehouseId(String warehouseId, String skuId) {
        return baseMapper.getInventoryQtyByWarehouseId(warehouseId, skuId);
    }

    /**
     * 根据sku和虚拟仓id获取是否存在关联关系
     *
     * @param skuId
     * @param virtualWarehouseId
     * @return
     */
    @Override
    public List<VirtualInventoryDTO.CommonDTO> getBySkuIdAndVwId(String skuId, String virtualWarehouseId) {
        return baseMapper.getBySkuIdAndVwId(skuId, virtualWarehouseId);
    }

    @Override
    public PagingVO<VirtualInventoryDTO.ListDTO> getVirtualInventory(PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto) {
        PagingVO<VirtualInventoryDTO.ListDTO> resultList = this.paging(dto);
        List<VirtualInventoryDTO.ListDTO> list = (List<VirtualInventoryDTO.ListDTO>) resultList.getList();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //数据赋值处理
        fillPageData(list);
        return new PagingVO<>(list, resultList.getTotalCount(), dto.getPageSize(), dto.getCurrPage());
    }

    /**
     * 虚拟库存分页查询数据处理
     *
     * @param list
     * @author will
     * @date 2024/6/3 15:24
     */
    private void fillPageData(List<VirtualInventoryDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //产品信息
        List<String> skuIdList = list.stream().map(VirtualInventoryDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //虚拟仓库
        List<String> virtualWarehouseIdList = list.stream().map(VirtualInventoryDTO.ListDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseEntityList = virtualWarehouseService.listByIds(virtualWarehouseIdList);

        //虚拟库存数据
        List<VirtualInventoryDTO.ListInventoryDTO> virtualInventoryList = this.baseMapper.listVirtualWarehouseIdListAndSkuIdList(skuIdList, virtualWarehouseIdList);

        //实际仓库
        List<String> warehouseIdList = virtualInventoryList.stream().map(VirtualInventoryDTO.ListInventoryDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIdList);

        for (VirtualInventoryDTO.ListDTO listDTO : list) {
            //产品信息
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> obj.getId().equals(listDTO.getSkuId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "产品信息"));
            listDTO.setSkuNo(productDetailEntity.getSkuNo());
            listDTO.setProductName(productDetailEntity.getName());

            //虚拟仓库
            VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getVirtualWarehouseId())).findFirst().orElse(new VirtualWarehouseEntity());
            listDTO.setVirtualWarehouseCode(virtualWarehouseEntity.getCode());
            listDTO.setVirtualWarehouseName(virtualWarehouseEntity.getName());

            //明细信息(关联查询实物库存)
            List<VirtualInventoryDTO.ListInventoryDTO> virtualInventoryDetailList = virtualInventoryList.stream().filter(obj ->
                    StrUtil.equals(obj.getSkuId(), listDTO.getSkuId())
                            && StrUtil.equals(obj.getVirtualWarehouseId(), listDTO.getVirtualWarehouseId())
            ).collect(Collectors.toList());
            //无明细则直接返回
            if (CollectionUtil.isEmpty(virtualInventoryDetailList)) {
                continue;
            }
            //虚拟可用库存
            Integer virtualUsableQty = virtualInventoryDetailList.stream().filter(obj -> StrUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode()))
                    .map(VirtualInventoryDTO.ListInventoryDTO::getVirtualQty).reduce(MathUtil.ZERO, Integer::sum);
            listDTO.setVirtualUsableQty(virtualUsableQty);
            //虚拟冻结库存
            Integer virtualFrozenQty = virtualInventoryDetailList.stream().filter(obj -> StrUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.FROZEN.getCode()))
                    .map(VirtualInventoryDTO.ListInventoryDTO::getVirtualQty).reduce(MathUtil.ZERO, Integer::sum);
            listDTO.setVirtualFrozenQty(virtualFrozenQty);
            //虚拟库存总数
            listDTO.setVirtualQty(MathUtil.add(virtualUsableQty, virtualFrozenQty));

            List<VirtualInventoryDTO.ListDetailDTO> detailList = new ArrayList<>();
            //根据实物仓库分组
            Map<String, List<VirtualInventoryDTO.ListInventoryDTO>> map = virtualInventoryDetailList.stream().collect(Collectors.groupingBy(VirtualInventoryDTO.ListInventoryDTO::getWarehouseId));
            for (Map.Entry<String, List<VirtualInventoryDTO.ListInventoryDTO>> entry : map.entrySet()) {
                String warehouseId = entry.getKey();
                List<VirtualInventoryDTO.ListInventoryDTO> value = entry.getValue();
                //明细数据
                VirtualInventoryDTO.ListDetailDTO listDetailDTO = BeanMapperUtils.map(VirtualInventoryDTO.ListDetailDTO.class, listDTO);
                //实体仓库名称
                String warehouseName = warehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), warehouseId))
                        .map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse("");

                String indexId = value.stream().map(VirtualInventoryDTO.ListInventoryDTO::getId).collect(Collectors.joining(","));
                listDetailDTO.setIndexId(StrUtil.format("{}_{}", indexId, warehouseId));
                listDetailDTO.setSkuNo(listDTO.getSkuNo());
                listDetailDTO.setWarehouseId(warehouseId);
                listDetailDTO.setWarehouseName(warehouseName);
                //可用数据
                VirtualInventoryDTO.ListInventoryDTO usableInventory = value.stream().filter(obj -> StrUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode())).findFirst().orElse(null);
                listDetailDTO.setUsableQty(ObjectUtil.isEmpty(usableInventory) ? MathUtil.ZERO : usableInventory.getQty());
                listDetailDTO.setVirtualUsableQty(ObjectUtil.isEmpty(usableInventory) ? MathUtil.ZERO : usableInventory.getVirtualQty());
                //冻结数据
                VirtualInventoryDTO.ListInventoryDTO frozenInventory = value.stream().filter(obj -> StrUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.FROZEN.getCode())).findFirst().orElse(null);
                listDetailDTO.setVirtualFrozenQty(ObjectUtil.isEmpty(frozenInventory) ? MathUtil.ZERO : frozenInventory.getVirtualQty());

                //实体仓分配数量
                Integer distributionQty = virtualInventoryList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), warehouseId)
                                && StrUtil.equals(obj.getSkuId(), listDTO.getSkuId()))
                        .map(VirtualInventoryDTO.ListInventoryDTO::getVirtualQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
                listDetailDTO.setDistributionQty(distributionQty);

                //虚拟仓数量
                listDetailDTO.setVirtualQty(MathUtil.add(listDetailDTO.getVirtualUsableQty(), listDetailDTO.getVirtualFrozenQty()));
                //未分配数量
                listDetailDTO.setUnDistributionQty(listDetailDTO.getUsableQty() - distributionQty);

                detailList.add(listDetailDTO);
            }
            listDTO.setDetailList(detailList);
        }
    }


}
