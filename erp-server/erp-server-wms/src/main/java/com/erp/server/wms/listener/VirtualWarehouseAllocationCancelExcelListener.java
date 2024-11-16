
package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.VwAllocationAllocationCancelExcelDTO;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.VirtualInventoryService;
import com.erp.server.wms.service.VirtualWarehouseRelationService;
import com.erp.server.wms.service.VirtualWarehouseService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 分货单校验
 *
 * @author Lambda
 * @Classname VirtualWarehouseAllocationExcelListener
 */
public class VirtualWarehouseAllocationCancelExcelListener extends AnalysisEventListener<VwAllocationAllocationCancelExcelDTO> {
    private VirtualWarehouseRelationService virtualWarehouseRelationService;

    private WarehouseService warehouseService;
    private VirtualWarehouseService virtualWarehouseService;
    private VirtualInventoryService virtualInventoryService;
    /**
     * 导入正确数据
     */
    private List<VirtualWarehouseAllocationDTO.DetailDto> successList = new ArrayList<>();


    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<VwAllocationAllocationCancelExcelDTO> allList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    private List<VwAllocationAllocationCancelExcelDTO> errorList = new ArrayList<>();


    private PlmTaskFeign plmTaskFeign;


    public VirtualWarehouseAllocationCancelExcelListener(VirtualWarehouseRelationService virtualWarehouseRelationService,
                                                         WarehouseService warehouseService, VirtualWarehouseService virtualWarehouseService,
                                                         PlmTaskFeign plmTaskFeign, VirtualInventoryService virtualInventoryService) {
        this.warehouseService = warehouseService;
        this.virtualWarehouseRelationService = virtualWarehouseRelationService;
        this.virtualWarehouseService = virtualWarehouseService;
        this.plmTaskFeign = plmTaskFeign;
        this.virtualInventoryService = virtualInventoryService;
    }

    /**
     * 每解析一行数据回调一遍
     *
     * @param vwAllocationAllocationExcelDTO
     * @param analysisContext
     * @return void
     * @author hyj
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(VwAllocationAllocationCancelExcelDTO vwAllocationAllocationExcelDTO, AnalysisContext analysisContext) {
        //添加数据用于判断是否为空
        allList.add(vwAllocationAllocationExcelDTO);
        List<String> msgList = FieldValidUtil.fieldValid(vwAllocationAllocationExcelDTO);
        List<String> errorMsgList = new ArrayList<>();
        errorMsgList.addAll(msgList);
        if (CollectionUtils.isNotEmpty(msgList)) {
            vwAllocationAllocationExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(vwAllocationAllocationExcelDTO);
            return;
        }
        if (CharSequenceUtil.isBlank(vwAllocationAllocationExcelDTO.getSkuNo())) {
            errorMsgList.add("SKU不能为空");
        }
        VirtualWarehouseAllocationDTO.DetailDto detailDto = new VirtualWarehouseAllocationDTO.DetailDto();

        //查看sku是否存在
        if (CharSequenceUtil.isNotBlank(vwAllocationAllocationExcelDTO.getSkuNo())) {
            //根据sku编号查询sku
            Map<String, String> skuParams = new HashMap<>();
            skuParams.put("skuNo", vwAllocationAllocationExcelDTO.getSkuNo());
            ProductDetailDTO productDetailDTO = plmTaskFeign.getSkuByParam(skuParams);
            if (ObjectUtils.isEmpty(productDetailDTO)) {
                errorMsgList.add("系统中不存在此sku编号");
            } else {
                detailDto.setSkuId(productDetailDTO.getId());
                detailDto.setSkuNo(productDetailDTO.getSkuNo());
                detailDto.setProductName(productDetailDTO.getName());
                detailDto.setImageUrl(productDetailDTO.getImagesUrl());
            }
        }
        if (!StrUtils.isDigit(String.valueOf(vwAllocationAllocationExcelDTO.getQty())) || ObjectUtil.isEmpty(vwAllocationAllocationExcelDTO.getQty())) {
            errorMsgList.add("移动数量只能是数字");
        } else {
            detailDto.setQty(Integer.valueOf(vwAllocationAllocationExcelDTO.getQty()));
        }
        if (CharSequenceUtil.isBlank(vwAllocationAllocationExcelDTO.getWarehouseName())) {
            errorMsgList.add("实体仓不能为空");
        }
        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.getByNames(Collections.singletonList(vwAllocationAllocationExcelDTO.getWarehouseName()));
        List<VirtualWarehouseDTO.VwDTO> vwDtoList = virtualWarehouseService.getByNames(Collections.singletonList(vwAllocationAllocationExcelDTO.getFromVirtualWarehouseName()));
        if (CollectionUtils.isEmpty(vwDtoList) || Objects.isNull(vwDtoList.get(0))) {
            errorMsgList.add("调出虚拟仓不存在");
        } else {
            if (Boolean.TRUE.equals(vwDtoList.get(0).getDisabled())) {
                errorMsgList.add("调出虚拟仓非启用状态");
            } else {
                detailDto.setFromVirtualWarehouseId(vwDtoList.get(0).getId());
            }
        }

        if (CollectionUtils.isEmpty(warehouseList) || Objects.isNull(warehouseList.get(0))) {
            errorMsgList.add("实体仓不存在");
        } else {
            if (Boolean.TRUE.equals(warehouseList.get(0).getDisabled())) {
                errorMsgList.add("实体仓非启用状态");
            } else {
                //根据实体仓获取虚拟仓
                List<VirtualWarehouseRelationEntity> vwRelationList = virtualWarehouseRelationService.getByWarehouseId(Collections.singletonList(warehouseList.get(0).getId()));
                if (CollUtil.isEmpty(vwRelationList) || Objects.isNull(vwRelationList.get(0))) {
                    errorMsgList.add("当前实体仓没有关联虚拟仓");
                } else {
                    VirtualWarehouseRelationEntity fromVmRelation = vwRelationList.stream().filter(item ->
                            Objects.equals(item.getVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId())).findFirst().orElse(null);
                    if (Objects.isNull(fromVmRelation)) {
                        errorMsgList.add("实体仓没有关联此调出虚拟仓");
                    } else {
                        BeanUtils.copyProperties(vwAllocationAllocationExcelDTO, detailDto);
                        detailDto.setWarehouseId(warehouseList.get(0).getId());
                        //获取数量
                        VirtualInventoryDTO.QtyTypeDTO qtyTypeDTO = new VirtualInventoryDTO.QtyTypeDTO();
                        qtyTypeDTO.setType(VirtualWarehouseAllocationTypeEnum.CANCEL.getCode());
                        VirtualInventoryDTO.QtySearchDTO qtySearchDTO = new VirtualInventoryDTO.QtySearchDTO();
                        BeanUtils.copyProperties(detailDto, qtySearchDTO);
                        List<VirtualInventoryDTO.QtySearchDTO> qtySearchList = new ArrayList<>();
                        qtySearchList.add(qtySearchDTO);
                        qtyTypeDTO.setQtySearchList(qtySearchList);
                        List<VirtualInventoryDTO.ViewQtyDTO> qtyDTOList = virtualInventoryService.getQty(qtyTypeDTO);
                        qtyDTOList.forEach(qtyDTO -> {
                            if (Objects.equals(qtyDTO.getWarehouseId(), detailDto.getWarehouseId()) && Objects.equals(qtyDTO.getSkuId(), detailDto.getSkuId())) {
                                detailDto.setWarehouseUsableQty(qtyDTO.getWarehouseAllocationQty());
                                if (Objects.equals(qtyDTO.getFromVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId())) {
                                    detailDto.setFromVirtualWarehouseUsableQty(qtyDTO.getFromVirtualWarehouseUsableQty());
                                }
                            }
                        });
                    }
                }
            }
        }
        //存在错误数据则直接返回
        if (CollectionUtils.isNotEmpty(errorMsgList)) {
            vwAllocationAllocationExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(vwAllocationAllocationExcelDTO);
            return;
        }
        successList.add(detailDto);
    }


    /**
     * 数据全部解析完后执行
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-22 17:59
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<VwAllocationAllocationCancelExcelDTO> getErrorList() {
        return errorList;
    }

    public List<VirtualWarehouseAllocationDTO.DetailDto> getSuccessList() {
        return successList;
    }

    public List<VwAllocationAllocationCancelExcelDTO> getAllList() {
        return allList;
    }
}
