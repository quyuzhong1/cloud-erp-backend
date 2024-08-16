package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.enums.VwAllocationDirectionEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.mapper.VirtualInventoryMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @Resource
    private VirtualWarehouseAllocationService virtualWarehouseAllocationService;

    @Resource
    private VirtualInventoryService virtualInventoryService;


    
    @Override
    public PagingVO<VirtualInventoryDiffDTO.ListDTO> diffPaging(PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        //库存差异
        Object isDiff = dto.getParams().getAdvanceQueryDTOList().stream().filter(obj -> StrUtil.equals(obj.getField(), "isDiff") && ObjectUtil.isNotNull(obj.getValue())).map(AdvanceQueryDTO::getValue).findFirst().orElse(null);
        if (ObjectUtil.isNotNull(isDiff)) {
            dto.getParams().setIsDiff(Boolean.valueOf(isDiff.toString()));
        }
        //超出分配
        Object isExceed = dto.getParams().getAdvanceQueryDTOList().stream().filter(obj -> StrUtil.equals(obj.getField(), "isExceed") && ObjectUtil.isNotNull(obj.getValue())).map(AdvanceQueryDTO::getValue).findFirst().orElse(null);
        if (ObjectUtil.isNotNull(isExceed)) {
            dto.getParams().setIsExceed(Boolean.valueOf(isExceed.toString()));
        }
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
        //统计数据
        Object isDiff = dto.getAdvanceQueryDTOList().stream().filter(obj -> StrUtil.equals(obj.getField(), "isDiff") && ObjectUtil.isNotNull(obj.getValue())).map(AdvanceQueryDTO::getValue).findFirst().orElse(null);
        if (ObjectUtil.isNotNull(isDiff)) {
            dto.setIsDiff(Boolean.valueOf(isDiff.toString()));
        }
        List<VirtualInventoryDTO.WarehouseStatisticsExcelDTO> warehouseStatisticsList = baseMapper.listWarehouseStatistics(dto);

        //数据赋值处理
        fillExportData(list);

        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        //主表数据
        pairList.add(new Pair<>(MathUtil.ZERO, list));
        //明细数据
        pairList.add(new Pair<>(MathUtil.ONE, warehouseStatisticsList));

        String name = "库存差异列表信息";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/virtualInventoryDiff.xlsx";
        try {
            new ExcelPrintUtils().sheetPatchExport(pairList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("虚拟仓库库存信息导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Integer diffPagingCount(PermissionsDTO dto) {
        return baseMapper.diffPagingCount(dto);
    }

    @Override
    public List<VirtualInventoryDiffDTO.ListDetailQtyDTO> listDiffDetail(VirtualInventoryDiffDTO.SearchParamDetailDTO dto) {
        PagingDTO<VirtualInventoryDiffDTO.SearchParamDetailDTO> pagingParamDTO = new PagingDTO<>();
        pagingParamDTO.setParams(dto);
        pagingParamDTO.setPageSize(-1);
        PagingVO<VirtualInventoryDiffDTO.ListDetailQtyDTO> resultList = this.diffDetailPaging(pagingParamDTO);
        List<VirtualInventoryDiffDTO.ListDetailQtyDTO> list = (List<VirtualInventoryDiffDTO.ListDetailQtyDTO>) resultList.getList();
        return list;
    }

    @Override
    public void updateVirtualInventory(List<VirtualInventoryDiffDTO.UpdateVirtualInventoryDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        VirtualWarehouseAllocationDTO.AddDTO addDTO = new VirtualWarehouseAllocationDTO.AddDTO();
        //取消分货
        addDTO.setType(VirtualWarehouseAllocationTypeEnum.CANCEL.getCode());
        addDTO.setDirection(VwAllocationDirectionEnum.FORWARD.getCode());
        addDTO.setRemark("库存差异一键调整");
        addDTO.setStatus(VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode());
        addDTO.setDisabled(Boolean.FALSE);
        List<VirtualWarehouseAllocationDTO.DetailDto> detailList = new ArrayList<>();
        for (VirtualInventoryDiffDTO.UpdateVirtualInventoryDTO updateDTO : list) {
            VirtualWarehouseAllocationDTO.DetailDto detailDto = new VirtualWarehouseAllocationDTO.DetailDto();
            detailDto.setSkuId(updateDTO.getSkuId());
            detailDto.setWarehouseId(updateDTO.getWarehouseId());
            detailDto.setFromVirtualWarehouseId(updateDTO.getVirtualWarehouseId());
            //数量为0不加入分货单
            if (MathUtil.compareTo(updateDTO.getQty(),MathUtil.ZERO) == MathUtil.ZERO) {
                continue;
            }
            detailDto.setQty(updateDTO.getQty());
            detailList.add(detailDto);
        }
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        addDTO.setDetailList(detailList);
        BaseResultDTO.AddDTO add = virtualWarehouseAllocationService.add(addDTO);
        VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity = virtualWarehouseAllocationService.getById(add.getId());
        try {
            BatchResultDTO submit = virtualWarehouseAllocationService.submit(virtualWarehouseAllocationEntity);
            if (!submit.getSuccess()) {
                throw new ServiceException("分货单提交失败");
            }
        } catch (Exception e) {
            log.error("分货单提交失败，e = {}",e);
        }
    }

    @Override
    public List<VirtualInventoryDiffDTO.ListSuggestQtyDTO> listSuggestQty(List<VirtualInventoryDiffDTO.ListSuggestQtyParamDTO> list) {
        List<VirtualInventoryDiffDTO.ListSuggestQtyDTO> resultList = new ArrayList<>();
        //sku集合
        List<String> skuIdList = list.stream().map(VirtualInventoryDiffDTO.ListSuggestQtyParamDTO::getSkuId)
                .distinct().collect(Collectors.toList());
        //实体仓库集合
        List<String> warehouseIdList = list.stream().map(VirtualInventoryDiffDTO.ListSuggestQtyParamDTO::getWarehouseId)
                .distinct().collect(Collectors.toList());
        //虚拟仓库Id集合
        List<String> virtualWarehouseIdList = list.stream().map(VirtualInventoryDiffDTO.ListSuggestQtyParamDTO::getVirtualWarehouseId)
                .distinct().collect(Collectors.toList());

        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setSkuIdList(skuIdList);
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setVirtualWarehouseIdList(virtualWarehouseIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryQtyList = virtualInventoryService.listInventoryQty(paramDTO);

        //查询实际出库可用库存
        Integer usableInventoryTotalQty = inventoryService.getUsableInventoryTotal(warehouseIdList.get(0), skuIdList.get(0));

        //虚拟库存总和
        Integer inventoryTotalQty = virtualInventoryQtyList.stream()
                .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                .reduce(MathUtil.ZERO, Integer::sum);

        //虚拟可用库存总和
        Integer usableTotalQty = virtualInventoryQtyList.stream()
                .filter(obj -> StrUtil.equals(obj.getDictInventoryStatus(),InventoryStatusEnum.USABLE.getCode()))
                .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                .reduce(MathUtil.ZERO, Integer::sum);

        //超出数量绝对值
        Integer exceedQty =  Math.abs(usableInventoryTotalQty - inventoryTotalQty);

        for (VirtualInventoryDiffDTO.ListSuggestQtyParamDTO qtyParamDTO : list) {
            VirtualInventoryDiffDTO.ListSuggestQtyDTO listSuggestQtyDTO = BeanMapperUtils.map(VirtualInventoryDiffDTO.ListSuggestQtyDTO.class, qtyParamDTO);
            //可用数量
            Integer usableQty = virtualInventoryQtyList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), qtyParamDTO.getSkuId())
                            && StrUtil.equals(obj.getWarehouseId(), qtyParamDTO.getWarehouseId())
                            && StrUtil.equals(obj.getVirtualWarehouseId(), qtyParamDTO.getVirtualWarehouseId())
                            && StrUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode()))
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                    .findFirst().orElse(MathUtil.ZERO);
            /**
             * 建议调整数量 = 超出数量绝对值 * 明细行虚拟仓可用库存 / 虚拟仓可用库存总和，按比例分配，抹零取整
             */
            int suggestQty = MathUtil.compareTo(usableTotalQty,MathUtil.ZERO) == MathUtil.ZERO ? MathUtil.ZERO : exceedQty * usableQty / usableTotalQty;
            listSuggestQtyDTO.setQty(suggestQty);
            resultList.add(listSuggestQtyDTO);
        }
        return resultList;
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
        for (VirtualInventoryDiffDTO.ListDTO listDTO : list) {
            //实体仓实际库存数量
            listDTO.setRealQty(MathUtil.add(listDTO.getUsableQty(),listDTO.getFrozenQty()));
            //已分配数量
            listDTO.setDistributionQty(listDTO.getVirtualQty());
            //未分配数量
            listDTO.setUnDistributionQty(listDTO.getRealQty() - listDTO.getDistributionQty());
            //是否有差异
            boolean isDiff = listDTO.getVirtualQty() > listDTO.getRealQty();
            listDTO.setIsDiff(isDiff);
            //超出分配数量
            Integer exceedQty = listDTO.getUsableQty() - listDTO.getVirtualQty();
            listDTO.setExceedQty(exceedQty);
            //是否超出分配
            Boolean isExceed = exceedQty >= 0 ? Boolean.FALSE : Boolean.TRUE;
            listDTO.setIsExceed(isExceed);
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
        //虚拟仓库
        List<String> virtualWarehouseIdList = list.stream().map(VirtualInventoryDiffDTO.ListDiffExportDataDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseEntityList = virtualWarehouseService.listByIds(virtualWarehouseIdList);

        //标识,用于判断是否需要赋值（相同sku、仓库只需要第一条赋值）
        List<String> flagList = new ArrayList<>();

        for (VirtualInventoryDiffDTO.ListDiffExportDataDTO listDTO : list) {
            //虚拟仓库
            VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseEntityList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getVirtualWarehouseId()))
                    .findFirst().orElse(new VirtualWarehouseEntity());
            listDTO.setVirtualWarehouseCode(virtualWarehouseEntity.getCode());
            listDTO.setVirtualWarehouseName(virtualWarehouseEntity.getName());
            //标识
            String flag = StrUtil.format("{}_{}",listDTO.getSkuId(),listDTO.getWarehouseId());
            if (flagList.contains(flag)) {
                continue;
            }
            flagList.add(flag);
            //仓库实际数量
            listDTO.setRealQty(MathUtil.add(listDTO.getUsableQty(),listDTO.getFrozenQty()));
            listDTO.setDistributionQty(listDTO.getTotalVirtualQty());
            //未分配数量
            listDTO.setUnDistributionQty(listDTO.getRealQty() - listDTO.getDistributionQty());
        }
    }
}
