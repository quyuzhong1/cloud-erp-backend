package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ValidatorUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.mapper.VirtualInventoryMapper;
import com.erp.server.wms.service.VirtualInventoryService;
import com.erp.server.wms.service.VirtualWarehouseService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private VirtualWarehouseService virtualWarehouseService;

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
    public Boolean exportExcel(VirtualInventoryDTO.SearchParamDTO dto, HttpServletResponse response) {
        PagingDTO<VirtualInventoryDTO.SearchParamDTO> pagingParamDTO = new PagingDTO<>();
        pagingParamDTO.setParams(dto);
        pagingParamDTO.setPageSize(-1);
        PagingVO<VirtualInventoryDTO.ListDTO> resultList = this.paging(pagingParamDTO);
        List<VirtualInventoryDTO.ListDTO> list = (List<VirtualInventoryDTO.ListDTO>)resultList.getList();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //数据赋值处理
        fillPageData(list);

        //明细数据
        List<VirtualInventoryDTO.ListDTO> detailList = list.stream().flatMap(obj -> Stream.of(obj.getDetailList().stream().toArray(VirtualInventoryDTO.ListDTO[]::new))).collect(Collectors.toList());
        List<Pair<Integer,List<?>>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(MathUtil.ZERO,list));
        pairList.add(new Pair<>(MathUtil.ONE,detailList));

        String name = "虚拟仓库库存信息";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/virtualInventory.xlsx";
        try {
            new ExcelPrintUtils().sheetPatchExport(pairList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("虚拟仓库库存信息导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public VirtualInventoryEntity findVirtualInventoryStock(String virtualWarehouseId,String warehouseId , String skuId, String inventoryStatus) {
        VirtualInventoryEntity entity = lambdaQuery()
                .eq(VirtualInventoryEntity::getVirtualWarehouseId, virtualWarehouseId)
                .eq(VirtualInventoryEntity::getWarehouseId, warehouseId)
                .eq(VirtualInventoryEntity::getSkuId, skuId)
                .eq(VirtualInventoryEntity::getDictInventoryStatus, inventoryStatus)
                .last("limit 1")
                .one();
        return entity;
    }

    @Override
    public VirtualInventoryEntity addOrUpdate(String virtualWarehouseId,String warehouseId, String skuId, String skuNo, String inventoryStatus, Integer qty) {
        //根据虚拟仓库id、skuId、状态编码查询
        VirtualInventoryEntity found = findVirtualInventoryStock(virtualWarehouseId,warehouseId, skuId, inventoryStatus);
        if (ObjectUtil.isEmpty(found)) {
            log.info("库存状态：【{}】，虚拟仓库【{}】，实体仓库【{}】，SKU：【{}】，SKU编号：【{}】在库存实时表中不存在数据，新增数据", inventoryStatus, virtualWarehouseId,warehouseId, skuId, skuNo);
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
            log.info("库存状态：【{}】，虚拟仓库【{}】，实体仓库【{}】，SKU：【{}】，SKU编号：【{}】在库存实时表中存在数据，修改数据", inventoryStatus, virtualWarehouseId,warehouseId, skuId, skuNo);
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
     * @param list
     * @author hyj
     * @date 2024/6/6
     */
    @Override
    public List<VirtualInventoryDTO.ViewQtyDTO> getInventoryQty(List<VirtualInventoryDTO.QtySearchDTO> list) {
        return null;
    }

    /**
     * 虚拟库存分页查询数据处理
     * @author will
     * @date 2024/6/3 15:24
     * @param list
     */
    private void fillPageData (List<VirtualInventoryDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //产品信息
        List<String> skuIdList = list.stream().map(VirtualInventoryDTO.ListDTO::getSkuNo).distinct().collect(Collectors.toList());
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
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> obj.getId().equals(listDTO.getSkuId())).findFirst().orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "产品信息"));
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
                    .map(VirtualInventoryDTO.ListInventoryDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);
            listDTO.setVirtualUsableQty(virtualUsableQty);
            //虚拟冻结库存
            Integer virtualFrozenQty = virtualInventoryDetailList.stream().filter(obj -> StrUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.FROZEN.getCode()))
                    .map(VirtualInventoryDTO.ListInventoryDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);
            listDTO.setVirtualFrozenQty(virtualFrozenQty);

            List<VirtualInventoryDTO.ListDetailDTO> detailList = new ArrayList<>();
            //根据实物仓库分组
            Map<String, List<VirtualInventoryDTO.ListInventoryDTO>> map = virtualInventoryDetailList.stream().collect(Collectors.groupingBy(VirtualInventoryDTO.ListInventoryDTO::getWarehouseId));
            for (Map.Entry<String, List<VirtualInventoryDTO.ListInventoryDTO>> entry : map.entrySet()) {
                String warehouseId = entry.getKey();
                List<VirtualInventoryDTO.ListInventoryDTO> value = entry.getValue();
                //明细数据
                VirtualInventoryDTO.ListDetailDTO listDetailDTO = BeanMapperUtils.map(VirtualInventoryDTO.ListDetailDTO.class, value.get(0));
                //实体仓库名称
                String warehouseName = warehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), warehouseId))
                        .map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse("");
                listDetailDTO.setWarehouseName(warehouseName);
                //可用数据
                VirtualInventoryDTO.ListInventoryDTO usableInventory = value.stream().filter(obj -> StrUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode())).findFirst().orElse(null);
                listDetailDTO.setUsableQty(usableInventory.getQty());
                listDetailDTO.setVirtualUsableQty(usableInventory.getVirtualQty());
                //冻结数据
                VirtualInventoryDTO.ListInventoryDTO frozenInventory = value.stream().filter(obj -> StrUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.FROZEN.getCode())).findFirst().orElse(null);
                listDetailDTO.setVirtualFrozenQty(frozenInventory.getVirtualQty());

                //实体仓分配数量
                Integer distributionQty = MathUtil.add(usableInventory.getQty(), usableInventory.getVirtualQty());
                listDetailDTO.setDistributionQty(distributionQty);
                listDetailDTO.setVirtualQty(distributionQty);
                //未分配数量
                listDetailDTO.setUnDistributionQty(listDetailDTO.getUsableQty() - distributionQty);

                detailList.add(listDetailDTO);
            }
            listDTO.setDetailList(detailList);
        }
    }


}
