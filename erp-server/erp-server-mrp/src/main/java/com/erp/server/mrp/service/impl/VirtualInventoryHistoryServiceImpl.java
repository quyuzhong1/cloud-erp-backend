package com.erp.server.mrp.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.VirtualInventoryHistoryDTO;
import com.erp.model.mrp.entity.VirtualInventoryHistoryEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.wms.feign.WmsVirtualWarehouseFeign;
import com.erp.server.mrp.mapper.VirtualInventoryHistoryMapper;
import com.erp.server.mrp.service.VirtualInventoryHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_VIRTUAL_INVENTORY;

/**
 * <p>
 * 虚拟库存表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-09-27
 */
@Service
public class VirtualInventoryHistoryServiceImpl extends SuperServiceImpl<VirtualInventoryHistoryMapper, VirtualInventoryHistoryEntity> implements VirtualInventoryHistoryService {


    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private WmsVirtualWarehouseFeign wmsVirtualWarehouseFeign;

    @Override
    public void saveTodayInventory(List<VirtualInventoryEntity> virtualInventory, LocalDate calculationDate) {
        List<VirtualInventoryHistoryEntity> entityList = list(Wrappers.<VirtualInventoryHistoryEntity>lambdaQuery().eq(VirtualInventoryHistoryEntity::getBillDate, calculationDate));
        List<VirtualInventoryHistoryEntity> entities = virtualInventory.parallelStream()
                .map(v -> {
                    VirtualInventoryHistoryEntity inventory = entityList.stream()
                            .filter(e -> v.getSkuId().equals(e.getSkuId()))
                            .filter(e -> v.getWarehouseId().equals(e.getWarehouseId()))
                            .filter(e -> v.getDictInventoryStatus().equals(e.getDictInventoryStatus()))
                            .filter(e -> v.getVirtualWarehouseId().equals(e.getVirtualWarehouseId()))
                            .findFirst()
                            .orElse(new VirtualInventoryHistoryEntity());
                    inventory.setWarehouseId(v.getWarehouseId());
                    inventory.setSkuId(v.getSkuId());
                    inventory.setSkuNo(v.getSkuNo());
                    inventory.setQty(v.getQty());
                    inventory.setDictInventoryStatus(v.getDictInventoryStatus());
                    inventory.setVirtualWarehouseId(v.getVirtualWarehouseId());
                    inventory.setBillDate(calculationDate);
                    return inventory;
                }).collect(Collectors.toList());
        ApplicationContextUtils.getBean(VirtualInventoryHistoryServiceImpl.class).saveOrUpdateBatch(entities);
    }

    @Override
    public PagingVO<VirtualInventoryHistoryDTO.ListDTO> paging(PagingDTO<VirtualInventoryHistoryDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<VirtualInventoryHistoryDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualInventoryHistoryDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams(), dto.getLastId());
        // 填充名称
        fillPageData(pageData.getRecords(), dto.getParams().getBillDate());
        return new PagingVO<>(pageData);
    }

    /**
     * 虚拟库存分页查询数据处理
     *
     * @param list
     * @param billDate
     * @author will
     * @date 2024/6/3 15:24
     */
    private void fillPageData(List<VirtualInventoryHistoryDTO.ListDTO> list, LocalDate billDate) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //产品信息
        List<String> skuIdList = list.stream().map(VirtualInventoryHistoryDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //虚拟仓库
        List<String> virtualWarehouseIdList = list.stream().map(VirtualInventoryHistoryDTO.ListDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseEntityList = wmsVirtualWarehouseFeign.listByIds(virtualWarehouseIdList);

        //虚拟库存数据
        List<VirtualInventoryHistoryDTO.ListInventoryDTO> virtualInventoryList = this.baseMapper.listVirtualWarehouseIdListAndSkuIdList(skuIdList, virtualWarehouseIdList, billDate);

        //实际仓库
        List<String> warehouseIdList = virtualInventoryList.stream().map(VirtualInventoryHistoryDTO.ListInventoryDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = FeignQuery.getByIds(WarehouseEntity.class, warehouseIdList);

        for (VirtualInventoryHistoryDTO.ListDTO listDTO : list) {
            //产品信息
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> obj.getId().equals(listDTO.getSkuId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "产品信息"));
            listDTO.setSkuNo(productDetailEntity.getSkuNo());
            listDTO.setProductName(productDetailEntity.getName());

            //虚拟仓库
            VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), listDTO.getVirtualWarehouseId())).findFirst().orElse(new VirtualWarehouseEntity());
            listDTO.setVirtualWarehouseCode(virtualWarehouseEntity.getCode());
            listDTO.setVirtualWarehouseName(virtualWarehouseEntity.getName());

            //明细信息(关联查询实物库存)
            List<VirtualInventoryHistoryDTO.ListInventoryDTO> virtualInventoryDetailList = virtualInventoryList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getSkuId(), listDTO.getSkuId())
                            && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), listDTO.getVirtualWarehouseId())
            ).collect(Collectors.toList());
            //无明细则直接返回
            if (CollectionUtils.isEmpty(virtualInventoryDetailList)) {
                continue;
            }
            //虚拟可用库存
            Integer virtualUsableQty = virtualInventoryDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode()))
                    .map(VirtualInventoryHistoryDTO.ListInventoryDTO::getVirtualQty).reduce(MathUtil.ZERO, Integer::sum);
            listDTO.setVirtualUsableQty(virtualUsableQty);
            //虚拟冻结库存
            Integer virtualFrozenQty = virtualInventoryDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.FROZEN.getCode()))
                    .map(VirtualInventoryHistoryDTO.ListInventoryDTO::getVirtualQty).reduce(MathUtil.ZERO, Integer::sum);
            listDTO.setVirtualFrozenQty(virtualFrozenQty);
            //虚拟库存总数
            listDTO.setVirtualQty(MathUtil.add(virtualUsableQty, virtualFrozenQty));

            List<VirtualInventoryHistoryDTO.ListDetailDTO> detailList = new ArrayList<>();
            //根据实物仓库分组
            Map<String, List<VirtualInventoryHistoryDTO.ListInventoryDTO>> map = virtualInventoryDetailList.stream().collect(Collectors.groupingBy(VirtualInventoryHistoryDTO.ListInventoryDTO::getWarehouseId));
            for (Map.Entry<String, List<VirtualInventoryHistoryDTO.ListInventoryDTO>> entry : map.entrySet()) {
                String warehouseId = entry.getKey();
                List<VirtualInventoryHistoryDTO.ListInventoryDTO> value = entry.getValue();
                //明细数据
                VirtualInventoryHistoryDTO.ListDetailDTO listDetailDTO = BeanMapperUtils.map(VirtualInventoryHistoryDTO.ListDetailDTO.class, listDTO);
                //实体仓库名称
                String warehouseName = warehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), warehouseId))
                        .map(WarehouseEntity::getName).findFirst().orElse("");

                String indexId = value.stream().map(VirtualInventoryHistoryDTO.ListInventoryDTO::getId).collect(Collectors.joining(","));
                listDetailDTO.setIndexId(CharSequenceUtil.format("{}_{}", indexId, warehouseId));
                listDetailDTO.setSkuNo(listDTO.getSkuNo());
                listDetailDTO.setWarehouseId(warehouseId);
                listDetailDTO.setWarehouseName(warehouseName);
                //可用库存
                Integer usableQty = value.stream().map(VirtualInventoryHistoryDTO.ListInventoryDTO::getUsableQty).findFirst().orElse(MathUtil.ZERO);
                listDetailDTO.setUsableQty(usableQty);
                //冻结库存
                Integer frozenQty = value.stream().map(VirtualInventoryHistoryDTO.ListInventoryDTO::getFrozenQty).findFirst().orElse(MathUtil.ZERO);
                listDetailDTO.setFrozenQty(frozenQty);
                //实际库存
                listDetailDTO.setRealQty(MathUtil.add(usableQty,frozenQty));

                //可用数据
                Integer detailVirtualUsableQty = value.stream().filter(obj -> CharSequenceUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode())).map(VirtualInventoryHistoryDTO.ListInventoryDTO::getVirtualQty).findFirst().orElse(MathUtil.ZERO);
                listDetailDTO.setVirtualUsableQty(detailVirtualUsableQty);
                //冻结数据
                Integer detailVirtualFrozenQty = value.stream().filter(obj -> CharSequenceUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.FROZEN.getCode())).map(VirtualInventoryHistoryDTO.ListInventoryDTO::getVirtualQty).findFirst().orElse(MathUtil.ZERO);
                listDetailDTO.setVirtualFrozenQty(detailVirtualFrozenQty);

                //实体仓分配数量
                Integer distributionQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), warehouseId)
                                && CharSequenceUtil.equals(obj.getSkuId(), listDTO.getSkuId()))
                        .map(VirtualInventoryHistoryDTO.ListInventoryDTO::getVirtualQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
                listDetailDTO.setDistributionQty(distributionQty);

                //虚拟仓数量
                listDetailDTO.setVirtualQty(MathUtil.add(listDetailDTO.getVirtualUsableQty(), listDetailDTO.getVirtualFrozenQty()));
                //未分配数量
                listDetailDTO.setUnDistributionQty(listDetailDTO.getRealQty() - distributionQty);

                detailList.add(listDetailDTO);
            }
            listDTO.setDetailList(detailList);
        }
    }


    @Override
    public void exportList(VirtualInventoryHistoryDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveExportTask("虚拟仓库历史库存信息", EXPORT_MRP_VIRTUAL_INVENTORY.getCode(), dto);
    }

    @Override
    public PagingVO<VirtualInventoryHistoryDTO.ListDTO> getVirtualInventory(PagingDTO<VirtualInventoryHistoryDTO.SearchParamDTO> dto) {
        return this.paging(dto);
    }
}
