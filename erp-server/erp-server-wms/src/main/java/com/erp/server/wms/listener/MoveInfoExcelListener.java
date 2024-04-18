//
//package com.erp.server.wms.listener;
//
//import cn.hutool.core.util.ObjectUtil;
//import cn.hutool.core.util.StrUtil;
//import com.alibaba.excel.context.AnalysisContext;
//import com.alibaba.excel.event.AnalysisEventListener;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
//import com.common.business.dto.FindUserDTO;
//import com.common.business.dto.base.BaseIdDTO;
//import com.common.business.enums.ApproveStatusEnum;
//import com.common.business.enums.PlatformDictEnum;
//import com.common.core.enums.ApiError;
//import com.common.core.utils.FieldValidUtil;
//import com.common.core.utils.StrUtils;
//import com.erp.model.plm.dto.CleanSkuDto;
//import com.erp.model.plm.dto.ProductDetailDTO;
//import com.erp.model.wms.dto.WarehouseDTO;
//import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
//import com.erp.model.wms.dto.WarehouseLocationMoveInfoDTO;
//import com.erp.model.wms.dto.excel.MoveInfoExcelDTO;
//import com.erp.model.wms.dto.excel.MoveInfoExcelDTO;
//import com.erp.model.wms.entity.*;
//import com.erp.rpc.plm.feign.PlmTaskFeign;
//import com.erp.server.wms.service.WarehouseLocationMoveInfoService;
//import com.erp.server.wms.service.WarehouseMappingService;
//import com.erp.server.wms.service.WarehouseService;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.json.JsonObject;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * @author Lambda
// * @Classname WarehouseExcelListener
// * @Date 2023-03-22 17:22
// * @Created by yl
// */
//public class MoveInfoExcelListener extends AnalysisEventListener<MoveInfoExcelDTO> {
//
//    private WarehouseService warehouseService;
//
//    private WarehouseLocationMoveInfoService warehouseLocationMoveInfoService;
//
//    private PlmTaskFeign plmTaskFeign;
//
//
//    /**
//     * 错误信息
//     */
//    private List<MoveInfoExcelDTO> errorList = new ArrayList<>();
//
//
//    public MoveInfoExcelListener(WarehouseService warehouseService, WarehouseLocationMoveInfoService warehouseLocationMoveInfoService, PlmTaskFeign plmTaskFeign) {
//        this.warehouseService = warehouseService;
//        this.warehouseLocationMoveInfoService = warehouseLocationMoveInfoService;
//        this.plmTaskFeign = plmTaskFeign;
//    }
//
//    /**
//     * 每解析一行数据回调一遍
//     *
//     * @param moveInfoExcelDTO
//     * @param analysisContext
//     * @return void
//     * @author hyj
//     * @date 2024/4/18 9:09
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void invoke(MoveInfoExcelDTO moveInfoExcelDTO, AnalysisContext analysisContext) {
//        List<String> errorMsgList = new ArrayList<>();
//        WarehouseLocationMoveInfoDTO.AddDTO addDTO = new WarehouseLocationMoveInfoDTO.AddDTO();
//        List<WarehouseLocationMoveDetailDTO.AddDTO> detailList = new ArrayList<>();
//        //基础验证
//        List<String> msgList = FieldValidUtil.fieldValid(moveInfoExcelDTO);
//        if (CollectionUtils.isNotEmpty(msgList)) {
//            errorMsgList.addAll(msgList);
//        }
//
//        //存在错误数据则直接返回
//        if (errorMsgList.size() > 0) {
//            moveInfoExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
//            errorList.add(moveInfoExcelDTO);
//            return;
//        }
//        if (StringUtils.isBlank(moveInfoExcelDTO.getSkuNo())) {
//            errorMsgList.add("SKU不能为空");
//        }
//        //查看sku是否存在
//        if (StringUtils.isNotBlank(moveInfoExcelDTO.getSkuNo())) {
//            if (!StrUtils.isLetterDigit(moveInfoExcelDTO.getSkuNo())) {
//                errorMsgList.add("SKU只能包含字母和数字");
//            }
//            //根据sku编号查询sku
//            Map<String, String> skuParams = new HashMap<>();
//            skuParams.put("skuNo", moveInfoExcelDTO.getSkuNo());
//            ProductDetailDTO productDetailDTO = plmTaskFeign.getSkuByParam(skuParams);
//            if (ObjectUtils.isEmpty(productDetailDTO)) {
//                errorMsgList.add("系统中不存在此sku编号");
//            }
//        }
//
//        if (StringUtils.isBlank(moveInfoExcelDTO.getQty()) || !StrUtils.isDigit(moveInfoExcelDTO.getQty())) {
//            errorMsgList.add("移动数量只能是数字");
//        }
//        if (StringUtils.isBlank(moveInfoExcelDTO.getWarehouseName())) {
//            errorMsgList.add("仓库名称不能为空");
//        }
////        if (StringUtils.isBlank(moveInfoExcelDTO.getOutWarehouseLocationName())) {
////            errorMsgList.add("取货仓位不能为空");
////        }
////        if (StringUtils.isBlank(moveInfoExcelDTO.getInWarehouseLocationName())) {
////            errorMsgList.add("上架仓位不能为空");
////        }
//
//        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.getByNames(Arrays.asList(moveInfoExcelDTO.getWarehouseName(),
//                moveInfoExcelDTO.getOutWarehouseLocationName(),
//                moveInfoExcelDTO.getInWarehouseLocationName()));
//        if (CollectionUtils.isEmpty(warehouseList)) {
//            errorMsgList.add("仓库名称不存在");
//        }
//        Map<String, List<WarehouseDTO.ListDTO>> nameMap = warehouseList.stream().collect(Collectors.groupingBy(WarehouseDTO.ListDTO::getName));
//        if (ObjectUtil.isEmpty(nameMap.get(moveInfoExcelDTO.getWarehouseName()))
//                || StringUtils.isBlank(nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0).getId())) {
//            errorMsgList.add("仓库名称不存在");
//        }
//        if (StringUtils.isNotBlank(moveInfoExcelDTO.getOutWarehouseLocationName())
//                && (ObjectUtil.isEmpty(nameMap.get(moveInfoExcelDTO.getOutWarehouseLocationName()))
//                || StringUtils.isBlank(nameMap.get(moveInfoExcelDTO.getOutWarehouseLocationName()).get(0).getId()))) {
//            errorMsgList.add("取货仓位不存在");
//        }
//        if (StringUtils.isNotBlank(moveInfoExcelDTO.getOutWarehouseLocationName())
//                && (ObjectUtil.isEmpty(nameMap.get(moveInfoExcelDTO.getInWarehouseLocationName()))
//                || StringUtils.isBlank(nameMap.get(moveInfoExcelDTO.getInWarehouseLocationName()).get(0).getId()))) {
//            errorMsgList.add("上架仓位不存在");
//        }
//
//        addDTO.setWarehouseId(nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0).getId());
//        WarehouseLocationMoveDetailEntity locationMoveDetailEntity = new WarehouseLocationMoveDetailEntity();
//        locationMoveDetailEntity.setOutWarehouseLocation(nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0).getId());
//        locationMoveDetailEntity.setInWarehouseLocation(StringUtils.isNotBlank(moveInfoExcelDTO.getOutWarehouseLocationName()) ? "" : nameMap.get(moveInfoExcelDTO.getInWarehouseLocationName()).get(0).getId());
////        JSONObject jsonObject = new JSONObject();
////        jsonObject.put("")
////        addDTO.set(nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0).getId());
////        addDTO.setWarehouseId(nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0).getId());
////        new
////        addDTO.set(nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0).getId());
////        addDTO.setWarehouseId(nameMap.get(moveInfoExcelDTO.getWarehouseName()).get(0).getId());
////
////        addDTO.setKingdeeWarehouseCode(kingdeeWarehouseCode);
////        addDTO.setTypeId(typeId);
////        //组织
////        String orgName = moveInfoExcelDTO.getOrgName();
////        String orgId = orgList.stream().filter(d -> d.getName().equals(orgName)).findFirst().
////                flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
////        if (StringUtils.isBlank(orgId)) {
////            errorMsgList.add("仓库组织不存在");
////        }
////        addDTO.setOrgId(orgId);
////        //是否虚拟仓
////        String isVirtual = moveInfoExcelDTO.getIsVirtual();
////        List virtualList = Arrays.asList("是", "否");
////        //不为 是否
////        if (!virtualList.contains(isVirtual)) {
////            errorMsgList.add("是否虚拟仓有误");
////        }
////        addDTO.setIsVirtual(isVirtual.equals("是"));
////        //仓库负责人
////        String chargeName = moveInfoExcelDTO.getChargeName();
////        if (StringUtils.isNotBlank(chargeName)) {
////            String chargeId = userList.stream().filter(d -> d.getUserName().equals(chargeName)).findFirst().
////                    flatMap(obj -> Optional.ofNullable(obj.getUserId())).orElse("");
////            if (StringUtils.isBlank(chargeId)) {
////                errorMsgList.add("仓库负责人有误");
////            }
////            addDTO.setChargeId(chargeId);
////        }
//        //存在错误数据则直接返回
//        if (errorMsgList.size() > 0) {
//            moveInfoExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
//            errorList.add(moveInfoExcelDTO);
//            return;
//        }
////        if (ObjectUtil.isNotEmpty(warehouseEntity)) {
////            addDTO.setOnwayWarehouseId(warehouseEntity.getId());
////            addDTO.setOnwayWarehouseName(warehouseEntity.getName());
////        }
//
//        //保存的数据
//        warehouseLocationMoveInfoService.add(addDTO);
//    }
//
//    @Override
//    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
//
//    }
//
//
////    /**
////     * 数据全部解析完后执行
////     *
////     * @param analysisContext
////     * @return void
////     * @author yl
////     * @date 2023-03-22 17:59
////     */
////    @Override
////    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
////        if (CollectionUtils.isNotEmpty(addWarehouseList)) {
////            warehouseService.saveBatch(addWarehouseList);
////        }
////    }
//
//    public List<MoveInfoExcelDTO> getErrorList() {
//        return errorList;
//    }
//}
