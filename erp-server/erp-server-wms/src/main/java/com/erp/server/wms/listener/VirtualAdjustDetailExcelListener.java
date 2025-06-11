
package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.excel.VirtualAdjustExcelDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.WarehouseLocationMoveService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname WarehouseExcelListener
 * @Date 2023-03-22 17:22
 * @Created by yl
 */
public class VirtualAdjustDetailExcelListener extends AnalysisEventListener<VirtualAdjustExcelDTO> {
    private WarehouseLocationService warehouseLocationService;

    private InventoryService inventoryService;
    private WarehouseService warehouseService;
    /**
     * 导入正确数据
     */
    private List<WarehouseLocationMoveDTO.DetailViewDTO> successList = new ArrayList<>();


    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<VirtualAdjustExcelDTO> allList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    private List<VirtualAdjustExcelDTO> errorList = new ArrayList<>();

    private WarehouseLocationMoveService warehouseLocationMoveService;

    private PlmTaskFeign plmTaskFeign;


    public VirtualAdjustDetailExcelListener(WarehouseLocationMoveService warehouseLocationMoveService,
                                            WarehouseService warehouseService, WarehouseLocationService warehouseLocationService,
                                            PlmTaskFeign plmTaskFeign, InventoryService inventoryService) {
        this.warehouseLocationMoveService = warehouseLocationMoveService;
        this.warehouseService = warehouseService;
        this.warehouseLocationService = warehouseLocationService;
        this.plmTaskFeign = plmTaskFeign;
        this.inventoryService = inventoryService;
    }

    /**
     * 每解析一行数据回调一遍
     *
     * @param VirtualAdjustExcelDTO
     * @param analysisContext
     * @return void
     * @author hyj
     * @date 2024/4/18 9:09
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(VirtualAdjustExcelDTO VirtualAdjustExcelDTO, AnalysisContext analysisContext) {
        //添加数据用于判断是否为空
        allList.add(VirtualAdjustExcelDTO);
        List<String> msgList = FieldValidUtil.fieldValid(VirtualAdjustExcelDTO);
        List<String> errorMsgList = new ArrayList<>();
        errorMsgList.addAll(msgList);
        if (CollectionUtils.isNotEmpty(msgList)){
            VirtualAdjustExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(VirtualAdjustExcelDTO);
            return;
        }
        WarehouseLocationMoveDTO.PcAddDTO pcAddDTO = new WarehouseLocationMoveDTO.PcAddDTO();
        if (CharSequenceUtil.isBlank(VirtualAdjustExcelDTO.getSkuNo())) {
            errorMsgList.add("SKU不能为空");
        }
        WarehouseLocationMoveDTO.DetailViewDTO pcViewDTO = new WarehouseLocationMoveDTO.DetailViewDTO();

        //查看sku是否存在
        if (CharSequenceUtil.isNotBlank(VirtualAdjustExcelDTO.getSkuNo())) {
            //根据sku编号查询sku
            Map<String, String> skuParams = new HashMap<>();
            skuParams.put("skuNo", VirtualAdjustExcelDTO.getSkuNo());
            ProductDetailDTO productDetailDTO = plmTaskFeign.getSkuByParam(skuParams);
            if (ObjectUtils.isEmpty(productDetailDTO)) {
                errorMsgList.add("系统中不存在此sku编号");
            } else {
                pcViewDTO.setSkuId(productDetailDTO.getId());
                pcViewDTO.setSkuNo(productDetailDTO.getSkuNo());
                pcViewDTO.setProductName(productDetailDTO.getName());
            }
        }

        if (!StrUtils.isDigit(String.valueOf(VirtualAdjustExcelDTO.getQty())) || ObjectUtil.isEmpty(VirtualAdjustExcelDTO.getQty())) {
            errorMsgList.add("移动数量只能是数字");
        }else  if(VirtualAdjustExcelDTO.getQty() < 1 || VirtualAdjustExcelDTO.getQty() >999999999){
            errorMsgList.add("移动数量范围1-999999999");
        }
        if (CharSequenceUtil.isBlank(VirtualAdjustExcelDTO.getWarehouseName())) {
            errorMsgList.add("仓库名称不能为空");
        }

        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listByNames(Collections.singletonList(VirtualAdjustExcelDTO.getWarehouseName()));
        if (CollectionUtils.isEmpty(warehouseList)) {
            errorMsgList.add(ApiError.WAREHOUSE_NOT_EXIST_NO_PERMISSION.msg);
        }else {
            //根据仓库获取仓位
            List<WarehouseLocationDTO.LocationListDTO> warehouseLocationList = warehouseLocationService.select(warehouseList.get(0).getId());
            if (CollUtil.isEmpty(warehouseLocationList)) {
                errorMsgList.add("当前仓库没有仓位");
            }
            Map<String, List<WarehouseLocationDTO.LocationListDTO>> locationMap = warehouseLocationList.stream().collect(Collectors.groupingBy(WarehouseLocationDTO.LocationListDTO::getName));
            //设置空仓位
            if (StringUtils.isEmpty(VirtualAdjustExcelDTO.getOutWarehouseLocationName())) {
                VirtualAdjustExcelDTO.setOutWarehouseLocationName("空仓位");
            }
            if (StringUtils.isEmpty(VirtualAdjustExcelDTO.getInWarehouseLocationName())) {
                VirtualAdjustExcelDTO.setInWarehouseLocationName("空仓位");
            }

            if (Objects.isNull(locationMap.get(VirtualAdjustExcelDTO.getOutWarehouseLocationName()))) {
                errorMsgList.add("取货仓位不存在");
            }
            if (Objects.isNull(locationMap.get(VirtualAdjustExcelDTO.getInWarehouseLocationName()))) {
                errorMsgList.add("上架仓位不存在");
            }
            pcViewDTO.setOutWarehouseLocation(CharSequenceUtil.isBlank(VirtualAdjustExcelDTO.getOutWarehouseLocationName()) ? ""
                    : (Objects.nonNull(locationMap) && Objects.nonNull(locationMap.get(VirtualAdjustExcelDTO.getOutWarehouseLocationName()))
                    && Objects.nonNull(locationMap.get(VirtualAdjustExcelDTO.getOutWarehouseLocationName()).get(0))
                    && Objects.nonNull(locationMap.get(VirtualAdjustExcelDTO.getOutWarehouseLocationName()).get(0).getId())
                    ? locationMap.get(VirtualAdjustExcelDTO.getOutWarehouseLocationName()).get(0).getCode() : ""));
            pcViewDTO.setInWarehouseLocation(CharSequenceUtil.isBlank(VirtualAdjustExcelDTO.getInWarehouseLocationName()) ? ""
                    : (Objects.nonNull(locationMap) && Objects.nonNull(locationMap.get(VirtualAdjustExcelDTO.getInWarehouseLocationName()))
                    && Objects.nonNull(locationMap.get(VirtualAdjustExcelDTO.getInWarehouseLocationName()).get(0))
                    && Objects.nonNull(locationMap.get(VirtualAdjustExcelDTO.getInWarehouseLocationName()).get(0).getId())
                    ? locationMap.get(VirtualAdjustExcelDTO.getInWarehouseLocationName()).get(0).getCode() : ""));
        }

        pcAddDTO.setWarehouseId((CollectionUtils.isEmpty(warehouseList) || Objects.isNull(warehouseList.get(0))) ? "" : warehouseList.get(0).getId());
        pcViewDTO.setOutWarehouseLocationName(VirtualAdjustExcelDTO.getOutWarehouseLocationName());

        pcViewDTO.setInWarehouseLocationName(VirtualAdjustExcelDTO.getInWarehouseLocationName());
        pcViewDTO.setSkuNo(VirtualAdjustExcelDTO.getSkuNo());
        pcViewDTO.setQty(VirtualAdjustExcelDTO.getQty());
        pcViewDTO.setRemark(VirtualAdjustExcelDTO.getRemark());
        pcViewDTO.setWarehouseId((CollectionUtils.isEmpty(warehouseList) || Objects.isNull(warehouseList.get(0))) ? "" : warehouseList.get(0).getId());
        pcViewDTO.setWarehouseName((CollectionUtils.isEmpty(warehouseList) || Objects.isNull(warehouseList.get(0))) ? "" : warehouseList.get(0).getName());
        //设置库存
        if (CharSequenceUtil.isNotBlank(VirtualAdjustExcelDTO.getSkuNo()) && CharSequenceUtil.isNotBlank(pcViewDTO.getSkuId())
                && !(CollectionUtils.isEmpty(warehouseList) || Objects.isNull(warehouseList.get(0)))) {
            InventoryDTO.InventoryBySkuIdAndWarehouseDTO inventoryBySkuIdAndWarehouseDTO = new InventoryDTO.InventoryBySkuIdAndWarehouseDTO();
            inventoryBySkuIdAndWarehouseDTO.setWarehouseId(warehouseList.get(0).getId());
            inventoryBySkuIdAndWarehouseDTO.setSkuId(pcViewDTO.getSkuId());
            inventoryBySkuIdAndWarehouseDTO.setWarehouseLocation(pcViewDTO.getOutWarehouseLocation());
            List<InventoryDTO.InventoryViewQtyDTO> inventoryQtys = inventoryService.getInventoryQty(Collections.singletonList(inventoryBySkuIdAndWarehouseDTO));
            inventoryQtys.stream().forEach(inventoryQtyDTO -> {
                pcViewDTO.setUsableQty(inventoryQtyDTO.getUsableQty());
                pcViewDTO.setFrozenQty(inventoryQtyDTO.getFrozenQty());
                pcViewDTO.setRealQty(inventoryQtyDTO.getRealQty());
            });
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            VirtualAdjustExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(VirtualAdjustExcelDTO);
            return;
        }
        successList.add(pcViewDTO);
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

    public List<VirtualAdjustExcelDTO> getErrorList() {
        return errorList;
    }

    public List<WarehouseLocationMoveDTO.DetailViewDTO> getSuccessList() {
        return successList;
    }

    public List<VirtualAdjustExcelDTO> getAllList() {
        return allList;
    }
}
