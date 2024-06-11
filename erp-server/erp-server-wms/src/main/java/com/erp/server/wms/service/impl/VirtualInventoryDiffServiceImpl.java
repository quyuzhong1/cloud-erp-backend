package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.mapper.VirtualInventoryMapper;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.VirtualInventoryDiffService;
import com.erp.server.wms.service.VirtualWarehouseService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 库存差异 服务实现类
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@Service
public class VirtualInventoryDiffServiceImpl extends SuperServiceImpl<VirtualInventoryMapper, VirtualInventoryEntity> implements VirtualInventoryDiffService {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    @Resource
    private InventoryService inventoryService;
    
    @Override
    public PagingVO<VirtualInventoryDiffDTO.ListDTO> diffPaging(PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualInventoryDiffDTO.ListDTO> pageData = this.baseMapper.diffPaging(query, dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public PagingVO<VirtualInventoryDiffDTO.ListDetailQtyDTO> diffDetailPaging(PagingDTO<VirtualInventoryDiffDTO.SearchParamDetailDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualInventoryDiffDTO.ListDetailQtyDTO> pageData = this.baseMapper.diffDetailPaging(query, dto.getParams());
        // 填充名称
        fillDetailPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(VirtualInventoryDiffDTO.SearchParamDTO dto, HttpServletResponse response) {

        List<VirtualInventoryDiffDTO.ListDiffExportDataDTO> list = baseMapper.listDiffExportData(dto);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //数据赋值处理
        fillExportData(list);
        String name = "库存差异列表信息";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/virtualInventoryDiff.xlsx";
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("库存差异列表信息导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Integer diffPagingCount(PermissionsDTO dto) {
        return baseMapper.diffPagingCount(dto);
    }

    /**
     * 虚拟库存分页查询数据处理
     * @author will
     * @date 2024/6/3 15:24
     * @param list
     */
    private void fillPageData (List<VirtualInventoryDiffDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //产品信息
        List<String> skuIdList = list.stream().map(VirtualInventoryDiffDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //实际仓库
        List<String> warehouseIdList = list.stream().map(VirtualInventoryDiffDTO.ListDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIdList);

        for (VirtualInventoryDiffDTO.ListDTO listDTO : list) {
            //产品信息
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> obj.getId().equals(listDTO.getSkuId()))
                    .findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "产品信息"));
            listDTO.setSkuNo(productDetailEntity.getSkuNo());
            listDTO.setProductName(productDetailEntity.getName());
            //实体仓库名称
            String warehouseName = warehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getWarehouseId()))
                    .map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse("");
            listDTO.setWarehouseName(warehouseName);
            //已分配数量
            listDTO.setDistributionQty(listDTO.getVirtualQty());
            //未分配数量
            listDTO.setUnDistributionQty(listDTO.getUsableQty() - listDTO.getDistributionQty());
        }
    }

    /**
     * 虚拟库存差异数据处理
     * @author will
     * @date 2024/6/11 10:53
     * @param list
     */
    private void fillDetailPageData (List<VirtualInventoryDiffDTO.ListDetailQtyDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //虚拟仓库
        List<String> virtualWarehouseIdList = list.stream().map(VirtualInventoryDiffDTO.ListDetailQtyDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseEntityList = virtualWarehouseService.listByIds(virtualWarehouseIdList);
        for (VirtualInventoryDiffDTO.ListDetailQtyDTO listDetailQtyDTO : list) {
            //虚拟仓库
            VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDetailQtyDTO.getVirtualWarehouseId()))
                    .findFirst().orElse(new VirtualWarehouseEntity());
            listDetailQtyDTO.setVirtualWarehouseCode(virtualWarehouseEntity.getCode());
            listDetailQtyDTO.setVirtualWarehouseName(virtualWarehouseEntity.getName());
        }
    }

    /**
     * 导出数据处理
     * @author will
     * @date 2024/6/11 15:43
     * @param list
     */
    private void fillExportData (List<VirtualInventoryDiffDTO.ListDiffExportDataDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //sku
        List<String> skuIdList = list.stream().map(VirtualInventoryDiffDTO.ListDiffExportDataDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //实体仓库
        List<String> warehouseIdList = list.stream().map(VirtualInventoryDiffDTO.ListDiffExportDataDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIdList);

        //虚拟仓库
        List<String> virtualWarehouseIdList = list.stream().map(VirtualInventoryDiffDTO.ListDiffExportDataDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseEntityList = virtualWarehouseService.listByIds(virtualWarehouseIdList);

        //实体仓库存查看
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setSkuIdList(skuIdList);
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);

        //标识
        List<String> flagList = new ArrayList<>();

        for (VirtualInventoryDiffDTO.ListDiffExportDataDTO listDTO : list) {

            //虚拟仓库
            VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getVirtualWarehouseId()))
                    .findFirst().orElse(new VirtualWarehouseEntity());
            listDTO.setVirtualWarehouseCode(virtualWarehouseEntity.getCode());
            listDTO.setVirtualWarehouseName(virtualWarehouseEntity.getName());

            String flag = StrUtil.format("{}_{}",listDTO.getSkuId(),listDTO.getWarehouseId());

            //实体库存
            Integer curInventoryQty = skuInventoryList.stream().filter(r -> Objects.equals(r.getSkuId(), listDTO.getSkuId())
                    && Objects.equals(r.getWarehouseId(), listDTO.getWarehouseId())
            ).map(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).reduce(MathUtil.ZERO,Integer::sum);
            listDTO.setUsableQty(curInventoryQty);

            //产品信息
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> obj.getId().equals(listDTO.getSkuId()))
                    .findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "产品信息"));
            listDTO.setSkuNo(productDetailEntity.getSkuNo());
            listDTO.setProductName(productDetailEntity.getName());
            //实体仓库名称
            String warehouseName = warehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getWarehouseId()))
                    .map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse("");
            listDTO.setWarehouseName(warehouseName);
            //已分配数量
            listDTO.setDistributionQty(listDTO.getVirtualQty());
            //未分配数量
            listDTO.setUnDistributionQty(listDTO.getUsableQty() - listDTO.getDistributionQty());

        }
    }
}
