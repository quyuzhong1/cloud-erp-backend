
package com.erp.server.wms.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.dto.CleanSkuDto;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.dto.excel.PurchaseApplicationImportExcelDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveInfoDTO;
import com.erp.model.wms.dto.excel.MoveInfoExcelDTO;
import com.erp.model.wms.dto.excel.MoveInfoExcelDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.WarehouseLocationMoveInfoService;
import com.erp.server.wms.service.WarehouseMappingService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.json.JsonObject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname WarehouseExcelListener
 * @Date 2023-03-22 17:22
 * @Created by yl
 */
public class MoveInfoExcelListener extends AnalysisEventListener<MoveInfoExcelDTO> {

    private WarehouseService warehouseService;
    /**
     * 导入正确数据
     */
    private List<WarehouseLocationMoveInfoDTO.PcAddDTO> successList = new ArrayList<>();



    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<MoveInfoExcelDTO> allList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    private List<MoveInfoExcelDTO> errorList = new ArrayList<>();

    private WarehouseLocationMoveInfoService warehouseLocationMoveInfoService;

    private PlmTaskFeign plmTaskFeign;


    public MoveInfoExcelListener(WarehouseLocationMoveInfoService warehouseLocationMoveInfoService, WarehouseService warehouseService, PlmTaskFeign plmTaskFeign) {
        this.warehouseLocationMoveInfoService = warehouseLocationMoveInfoService;
        this.warehouseService = warehouseService;
        this.plmTaskFeign = plmTaskFeign;
    }

    /**
     * 每解析一行数据回调一遍
     *
     * @param moveInfoExcelDTO
     * @param analysisContext
     * @return void
     * @author hyj
     * @date 2024/4/18 9:09
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(MoveInfoExcelDTO moveInfoExcelDTO, AnalysisContext analysisContext) {
        //添加数据用于判断是否为空
        allList.add(moveInfoExcelDTO);

        List<String> errorMsgList = new ArrayList<>();
        WarehouseLocationMoveInfoDTO.PcAddDTO pcAddDTO = new WarehouseLocationMoveInfoDTO.PcAddDTO();
        List<WarehouseLocationMoveDetailDTO.AddDTO> detailList = new ArrayList<>();
        if (StringUtils.isBlank(moveInfoExcelDTO.getSkuNo())) {
            errorMsgList.add("SKU不能为空");
        }
        WarehouseLocationMoveDetailEntity locationMoveDetailEntity = new WarehouseLocationMoveDetailEntity();

        //查看sku是否存在
        if (StringUtils.isNotBlank(moveInfoExcelDTO.getSkuNo())) {
            if (!StrUtils.isLetterDigit(moveInfoExcelDTO.getSkuNo())) {
                errorMsgList.add("SKU只能包含字母和数字");
            }
            //根据sku编号查询sku
            Map<String, String> skuParams = new HashMap<>();
            skuParams.put("skuNo", moveInfoExcelDTO.getSkuNo());
            ProductDetailDTO productDetailDTO = plmTaskFeign.getSkuByParam(skuParams);
            if (ObjectUtils.isEmpty(productDetailDTO)) {
                errorMsgList.add("系统中不存在此sku编号");
            }
            locationMoveDetailEntity.setSkuId(productDetailDTO.getId());
        }

        if (!StrUtils.isDigit(String.valueOf(moveInfoExcelDTO.getQty())) || ObjectUtil.isEmpty(moveInfoExcelDTO.getQty())) {
            errorMsgList.add("移动数量只能是数字");
        }
        if (StringUtils.isBlank(moveInfoExcelDTO.getWarehouseName())) {
            errorMsgList.add("仓库名称不能为空");
        }
//        if (StringUtils.isBlank(moveInfoExcelDTO.getOutWarehouseLocationName())) {
//            errorMsgList.add("取货仓位不能为空");
//        }
//        if (StringUtils.isBlank(moveInfoExcelDTO.getInWarehouseLocationName())) {
//            errorMsgList.add("上架仓位不能为空");
//        }

        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.getByNames(Arrays.asList(moveInfoExcelDTO.getWarehouseName(),
                moveInfoExcelDTO.getOutWarehouseLocationName(),
                moveInfoExcelDTO.getInWarehouseLocationName()));
        if (CollectionUtils.isEmpty(warehouseList)) {
            errorMsgList.add("仓库名称不存在");
        }
        Map<String, List<WarehouseDTO.ListDTO>> nameMap = warehouseList.stream().collect(Collectors.groupingBy(WarehouseDTO.ListDTO::getName));
        if (ObjectUtil.isEmpty(nameMap.get(moveInfoExcelDTO.getWarehouseName()))
                || StringUtils.isBlank(nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0).getId())) {
            errorMsgList.add("仓库名称不存在");
        }
        if (StringUtils.isNotBlank(moveInfoExcelDTO.getOutWarehouseLocationName())
                && (ObjectUtil.isEmpty(nameMap.get(moveInfoExcelDTO.getOutWarehouseLocationName()))
                || StringUtils.isBlank(nameMap.get(moveInfoExcelDTO.getOutWarehouseLocationName()).get(0).getId()))) {
            errorMsgList.add("取货仓位不存在");
        }
        if (StringUtils.isNotBlank(moveInfoExcelDTO.getOutWarehouseLocationName())
                && (ObjectUtil.isEmpty(nameMap.get(moveInfoExcelDTO.getInWarehouseLocationName()))
                || StringUtils.isBlank(nameMap.get(moveInfoExcelDTO.getInWarehouseLocationName()).get(0).getId()))) {
            errorMsgList.add("上架仓位不存在");
        }

        pcAddDTO.setWarehouseId((StringUtils.isNotBlank(moveInfoExcelDTO.getWarehouseName())
                && Objects.nonNull(nameMap.get(moveInfoExcelDTO.getWarehouseName())) && Objects.nonNull(nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0))
                && StringUtils.isNotBlank(nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0).getId())) ?
                nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0).getId() : "");
        locationMoveDetailEntity.setOutWarehouseLocation(StringUtils.isNotBlank(moveInfoExcelDTO.getInWarehouseLocationName()) ? "" : nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0).getId());
        locationMoveDetailEntity.setInWarehouseLocation(StringUtils.isNotBlank(moveInfoExcelDTO.getOutWarehouseLocationName()) ? "" : nameMap.get(moveInfoExcelDTO.getInWarehouseLocationName()).get(0).getId());
        locationMoveDetailEntity.setSkuNo(moveInfoExcelDTO.getSkuNo());
        locationMoveDetailEntity.setQty(moveInfoExcelDTO.getQty());
        List<WarehouseLocationMoveDetailDTO.AddDTO> detailList1 = pcAddDTO.getDetailList();
        WarehouseLocationMoveDetailDTO.AddDTO addDTO = new WarehouseLocationMoveDetailDTO.AddDTO();
        BeanMapperUtils.copy(locationMoveDetailEntity, addDTO);
        pcAddDTO.setDetailList(Arrays.asList(addDTO));
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            moveInfoExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(moveInfoExcelDTO);
            return;
        }
        successList.add(pcAddDTO);

        //保存的数据
        warehouseLocationMoveInfoService.pcAdd(pcAddDTO);
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

    public List<MoveInfoExcelDTO> getErrorList() {
        return errorList;
    }
    public List<WarehouseLocationMoveInfoDTO.PcAddDTO> getSuccessList() {
        return successList;
    }

    public List<MoveInfoExcelDTO> getAllList() {
        return allList;
    }
}
