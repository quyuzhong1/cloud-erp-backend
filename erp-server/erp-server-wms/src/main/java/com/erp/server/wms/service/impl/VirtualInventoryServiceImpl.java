package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.server.wms.mapper.VirtualInventoryMapper;
import com.erp.server.wms.service.VirtualInventoryService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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
    public PagingVO<VirtualInventoryDTO.ListDetailDTO> detailPaging(PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualInventoryDTO.ListDetailDTO> pageData = this.baseMapper.detailPaging(query, dto.getParams());
        // 填充名称
        fillPageDetailData(pageData.getRecords());
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
        String name = "虚拟仓库库存信息";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/virtualInventory.xlsx";
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("虚拟仓库库存信息导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
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
        List<String> skuIdList = list.stream().map(VirtualInventoryDTO.ListDTO::getSkuNo).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //虚拟仓库
        List<String> virtualWarehouseIdList = list.stream().map(VirtualInventoryDTO.ListDTO::getVirtualWarehouseId).collect(Collectors.toList());
        List<VirtualInventoryEntity> virtualInventoryList = this.baseMapper.listVirtualWarehouseIdListAndSkuIdList(skuIdList, virtualWarehouseIdList);

        //实际仓库
        List<String> warehouseIdList = virtualInventoryList.stream().map(VirtualInventoryEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIdList);

        for (VirtualInventoryDTO.ListDTO listDTO : list) {
            //产品信息
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> obj.getId().equals(listDTO.getSkuId())).findFirst().orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "产品信息"));
            listDTO.setProductName(productDetailEntity.getName());

            //明细信息
            List<VirtualInventoryEntity> virtualInventoryDetailList = virtualInventoryList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), listDTO.getSkuId())
                            && StrUtil.equals(obj.getVirtualWarehouseId(), listDTO.getVirtualWarehouseId()))
                    .collect(Collectors.toList());
            //无明细则直接返回
            if (CollectionUtil.isEmpty(virtualInventoryDetailList)) {
                continue;
            }
            List<VirtualInventoryDTO.ListDetailDTO> detailList = new ArrayList<>();
            for (VirtualInventoryEntity detailEntity : virtualInventoryDetailList) {
                //明细
                VirtualInventoryDTO.ListDetailDTO listDetailDTO = BeanMapperUtils.map(VirtualInventoryDTO.ListDetailDTO.class, detailEntity);
                String warehouseName = warehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getWarehouseId()))
                        .map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse("");
                listDetailDTO.setWarehouseName(warehouseName);
            }

        }
    }

    /**
     * 虚拟库存分页查询明细数据处理
     * @author will
     * @date 2024/6/3 16:58
     * @param list
     */
    private void fillPageDetailData (List<VirtualInventoryDTO.ListDetailDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
    }
}
