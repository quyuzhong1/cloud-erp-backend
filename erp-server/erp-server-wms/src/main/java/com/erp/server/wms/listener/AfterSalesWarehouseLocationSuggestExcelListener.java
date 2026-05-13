package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.AfterSalesWarehouseLocationSuggestExcelDto;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * 仓位售后推荐导入监听
 *
 * @author liuchao
 * @date 2026-05-06
 */
@Getter
public class AfterSalesWarehouseLocationSuggestExcelListener extends AnalysisEventListener<LinkedHashMap<Integer, String>> {
    /**
     * 导入正确数据
     */
    @Getter
    private List<AfterSalesWarehouseLocationSuggestExcelDto> successList = new ArrayList<>();

    /**
     * 导入数据，用于判断导入是否为空
     */
    @Getter
    private List<AfterSalesWarehouseLocationSuggestExcelDto> allList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    @Getter
    private List<AfterSalesWarehouseLocationSuggestExcelDto> errorList = new ArrayList<>();

    private PlmTaskFeign plmTaskFeign = SpringUtil.getBean(PlmTaskFeign.class);
    private WarehouseLocationService warehouseLocationService = SpringUtil.getBean(WarehouseLocationService.class);
    private WarehouseService warehouseService = SpringUtil.getBean(WarehouseService.class);


    @Override
    public void invoke(LinkedHashMap<Integer, String> data, AnalysisContext context) {
//        Integer rowIndex = context.readRowHolder().getRowIndex();
        AfterSalesWarehouseLocationSuggestExcelDto dto = new AfterSalesWarehouseLocationSuggestExcelDto();
        dto.setSkuNo(data.get(0));
        dto.setProductName(data.get(1));
        dto.setEanCode(data.get(2));
        dto.setWarehouseName(data.get(3));
        dto.setWarehouseAreaName(data.get(4));
        dto.setWarehouseLocationCode(data.get(5));
        dto.setSort(data.get(6));
        dto.setStatus(data.get(7));
        verifyField(dto);
        if (CharSequenceUtil.isNotBlank(dto.getErrorMsg())) {
            errorList.add(dto);
        }
        allList.add(dto);
    }

    private void verifyField(AfterSalesWarehouseLocationSuggestExcelDto dto) {
        //sku编码校验
        if (CharSequenceUtil.isBlank(dto.getSkuNo())) {
            dto.setErrorMsg("SKU编码不能为空，");
            return;
        }
        if (dto.getSkuNo().length() > 255) {
            dto.setErrorMsg("SKU编码过长");
            return;
        }
        //仓库名称校验
        if (CharSequenceUtil.isBlank(dto.getWarehouseName())) {
            dto.setErrorMsg("仓库名称不能为空，");
            return;
        }
        if (dto.getWarehouseName().length() > 255) {
            dto.setErrorMsg("仓库名称过长");
            return;
        }
        //EAN码校验
        if (dto.getEanCode() != null && dto.getEanCode().length() > 255) {
            dto.setErrorMsg("EAN码过长");
            return;
        }
        //库区名称校验
        if (CharSequenceUtil.isBlank(dto.getWarehouseAreaName())) {
            dto.setErrorMsg("库区名称不能为空");
            return;
        }
        if (dto.getWarehouseAreaName().length() > 19) {
            dto.setErrorMsg("库区名称过长");
            return;
        }
        //推荐仓位校验
        if (CharSequenceUtil.isBlank(dto.getWarehouseLocationCode())) {
            dto.setErrorMsg("推荐仓位编码不能为空");
            return;
        }
        if (dto.getWarehouseLocationCode().length() > 32) {
            dto.setErrorMsg("推荐仓位编码过长");
            return;
        }
        //优先级校验
        if (CharSequenceUtil.isBlank(dto.getSort())) {
            dto.setErrorMsg("优先级不能为空");
            return;
        }
        if (!StrUtils.isInteger(dto.getSort())) {
            dto.setErrorMsg("优先级请填写数字");
            return;
        }
        if (Integer.parseInt(dto.getSort()) > 9) {
            dto.setErrorMsg("优先级的值不能大于9");
            return;
        }
        //状态校验
        if (dto.getStatus().length() > 10) {
            dto.setErrorMsg("状态过长");
            return;
        }
        Set<String> strings = new HashSet<>();
        strings.add("启用");
        strings.add("禁用");
        if (!strings.contains(dto.getStatus())) {
            dto.setErrorMsg("无法识别状态选择，仅可填“启用/禁用”");
            return;
        } else {
            switch (dto.getStatus()) {
                case "启用":
                    dto.setDisabled(Boolean.FALSE);
                    break;
                case "禁用":
                    dto.setDisabled(Boolean.TRUE);
                    break;
            }
        }

    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (CollectionUtils.isEmpty(allList)) {
            return;
        }
        // 生成业务唯一 Key 的函数
        Function<AfterSalesWarehouseLocationSuggestExcelDto, String> businessKeyFunc =
                dto -> dto.getSkuNo() + "|" + dto.getWarehouseName() + "|" + dto.getWarehouseAreaName() + "|" + dto.getWarehouseLocationCode();

        // 统计每个 Key 出现的次数
        Map<String, Long> countMap = allList.stream().collect(Collectors.groupingBy(businessKeyFunc, Collectors.counting()));

        // 标记重复项
        allList.forEach(dto -> {
            // 只有原本没有错误的信息才参与重复校验
            if (CharSequenceUtil.isBlank(dto.getErrorMsg())) {
                if (countMap.get(businessKeyFunc.apply(dto)) > 1) {
                    dto.setErrorMsg("存在重复项");
                    // 因为是 doAfterAllAnalysed，发现重复直接加进 errorList
                    errorList.add(dto);
                }
            }
        });
        //获取解析正常数据的skuNo
        List<String> skuNoList = allList.stream().filter(e -> CharSequenceUtil.isBlank(e.getErrorMsg())).map(AfterSalesWarehouseLocationSuggestExcelDto::getSkuNo).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        //获取解析正常的数据的仓库名称
        List<String> warehouseNameList = allList.stream().filter(e -> CharSequenceUtil.isBlank(e.getErrorMsg())).map(AfterSalesWarehouseLocationSuggestExcelDto::getWarehouseName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        //提前查询出所有相关的sku信息
        List<ProductDetailEntity> skuVOS = CollUtil.isNotEmpty(skuNoList) ? plmTaskFeign.listBySkuNos(skuNoList) : Collections.emptyList();
        //提前查询出所有的相关的仓库信息
        List<WarehouseDTO.ListDTO> warehouseVOS = CollUtil.isNotEmpty(warehouseNameList) ? warehouseService.listByNames(warehouseNameList) : Collections.emptyList();

        Map<String, WarehouseDTO.ListDTO> warehouseNameAndIdMap = warehouseVOS.stream().collect(Collectors.toMap(WarehouseDTO.ListDTO::getName, item -> item));

        List<String> warehouseIdList = warehouseNameAndIdMap.values().stream().map(WarehouseDTO.ListDTO::getId).collect(Collectors.toList());

        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIds(warehouseIdList);

        Map<String, List<WarehouseLocationEntity>> warehouseLocationMap = warehouseLocationList.stream().collect(Collectors.groupingBy(WarehouseLocationEntity::getWarehouseId));
        //
        for (AfterSalesWarehouseLocationSuggestExcelDto excelDTO : allList) {
            if (CharSequenceUtil.isNotBlank(excelDTO.getErrorMsg())) {
                continue;
            }
            List<String> errorMsgList = new ArrayList<>();
            //校验sku/ena码是否指向已存在的商品信息
            ProductDetailEntity productDetail = skuVOS.stream().filter(e -> Objects.equals(e.getSkuNo(), excelDTO.getSkuNo())).findFirst().orElse(null);

            if (Objects.isNull(productDetail)) {
                errorMsgList.add("无法识别sku，请确定sku编码是否正确");
            } else {
                excelDTO.setSkuId(productDetail.getId());
//                excelDTO.setProductName(productDetail.getName());
            }

            //校验仓库/库区/仓位名称是否指向已存在的仓库信息
            WarehouseDTO.ListDTO warehouseDto = warehouseNameAndIdMap.get(excelDTO.getWarehouseName());

            if (warehouseDto == null || warehouseDto.getDisabled()) {
                errorMsgList.add("未能找到仓库，请确定仓库是否正确/已启用");
            } else {
                //仓库存在,校验库区存在
                String warehouseId = warehouseDto.getId();
                excelDTO.setWarehouseId(warehouseId);

                List<WarehouseLocationEntity> locationEntities = warehouseLocationMap.get(warehouseId);
                Map<String, WarehouseLocationEntity> locationEntityMap = locationEntities.stream().collect(Collectors.toMap(WarehouseLocationEntity::getId, item -> item));
                locationEntities = locationEntities.stream().filter(i -> !i.getDisabled()).collect(Collectors.toList());

                if (CollUtil.isEmpty(locationEntities)) {
                    errorMsgList.add("未能找到仓库库区，请确定库区是否正确");
                } else {
                    //库区存在,检验这个库区下有没有启用的仓位
                    Map<String, List<WarehouseLocationEntity>> convertToMap = convertToMap(locationEntities);
                    String warehouseAreaName = excelDTO.getWarehouseAreaName();
                    List<WarehouseLocationEntity> locationList = convertToMap.get(warehouseAreaName) == null ? new ArrayList<>() : convertToMap.get(warehouseAreaName);

                    locationList = locationList.stream()
                            .filter(i -> i != null && !i.getDisabled())
                            .collect(Collectors.toList());

                    if (CollUtil.isEmpty(locationList)) {
                        errorMsgList.add("未能找到仓库仓位，请确定仓位编码是否正确");
                    } else {
                        List<WarehouseLocationEntity> enableLocationList = locationList.stream().filter(i -> Objects.equals(i.getCode(), excelDTO.getWarehouseLocationCode())).filter(i -> !i.getDisabled()).collect(Collectors.toList());
                        if (CollUtil.isEmpty(enableLocationList)) {
                            errorMsgList.add("该仓位不存在");
                        } else if (enableLocationList.size() > 1) {
                            errorMsgList.add("该库区下有多个仓位编码相同的仓位");
                        } else {
                            excelDTO.setWarehouseAreaId(enableLocationList.get(0).getParentId());
                            WarehouseLocationEntity warehouseAreaEntity = locationEntityMap.get(excelDTO.getWarehouseAreaId());
                            excelDTO.setWarehouseAreaCode(warehouseAreaEntity.getCode());
                            excelDTO.setWarehouseLocationId(enableLocationList.get(0).getId());
                            excelDTO.setWarehouseLocationCode(enableLocationList.get(0).getCode());
                        }
                    }
                }
            }
            if (!errorMsgList.isEmpty()) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            successList.add(excelDTO);
        }
    }

    /**
     * 将 WarehouseLocation 集合转换成库区信息为key，仓位信息为value 的map集合
     *
     * @param allList WarehouseLocation集合
     * @return Map<String, List<WarehouseLocationEntity>> key:库区名称 value:库区下的仓位信息
     */
    public Map<String, List<WarehouseLocationEntity>> convertToMap(List<WarehouseLocationEntity> allList) {
        // 1. 过滤并提取所有父项（Area），转为 Map<id, WarehouseLocationEntity>
        Map<String, WarehouseLocationEntity> areaMap = allList.stream().filter(e -> WarehouseLocationTypeEnum.AREA.getCode().equals(e.getType())).collect(Collectors.toMap(WarehouseLocationEntity::getId, e -> e, (k1, k2) -> k1));

        // 2. 过滤并提取所有子项（Location），按 parentId 进行分组 Map<parentId, List<Location>>
        Map<String, List<WarehouseLocationEntity>> locationGroupedByParentId = allList.stream().filter(e -> WarehouseLocationTypeEnum.LOCATION.getCode().equals(e.getType())).collect(Collectors.groupingBy(WarehouseLocationEntity::getParentId));

        // 3. 将结果转化为 Map<父项名称, 子项列表>
        return areaMap.entrySet().stream().collect(Collectors.toMap(entry -> entry.getValue().getName(), // Key: 父项名称
                entry -> locationGroupedByParentId.getOrDefault(entry.getKey(), new ArrayList<>()) // Value: 子项列表
        ));
    }
}
