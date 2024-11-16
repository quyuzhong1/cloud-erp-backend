package com.erp.server.bi.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.bi.entity.BiDataSourceCostDetailEntity;
import com.erp.model.bi.entity.BiDataSourceCostEntity;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.server.bi.enums.BiDataSourceCostEnum;
import com.erp.server.bi.service.BiDataSourceCostDetailService;
import com.erp.server.bi.service.BiDataSourceCostService;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/27 17:22
 */
public class BiDataSourceCostExcelListener extends AnalysisEventListener<Map<Integer,String>> {

    private BiDataSourceCostService biDataSourceCostService;

    private BiDataSourceCostDetailService biDataSourceCostDetailService;

    private List<BiShopInfoEntity> shopList;

    private List<FindUserDTO> userList;

    private List<BiDictEntity> dictList;

    private List<SysDepartmentDTO> deptList;

    private  List<Map<Integer,String>> list ;

    private Map<Integer,String> headMap;

    private List<String> headList;


    public BiDataSourceCostExcelListener(BiDataSourceCostService biDataSourceCostService, BiDataSourceCostDetailService biDataSourceCostDetailService,
                                         List<BiShopInfoEntity> shopList, List<FindUserDTO> userList, List<BiDictEntity> dictList, List<SysDepartmentDTO> deptList) {
        this.biDataSourceCostService = biDataSourceCostService;
        this.biDataSourceCostDetailService = biDataSourceCostDetailService;
        this.shopList = shopList;
        this.userList = userList;
        this.dictList = dictList;
        this.deptList = deptList;
        this.list = new ArrayList<>();
    }

    @Override
    public void invoke(Map<Integer,String> map, AnalysisContext analysisContext) {
        if (map.isEmpty()) {
            return;
        }
        List<String> errorMsgList = new ArrayList<>();
        //遍历map下的数据
        Iterator<Map.Entry<Integer, String>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
        BiDataSourceCostEntity entity = new BiDataSourceCostEntity();
        List<BiDataSourceCostDetailEntity> detailList = new ArrayList<>();
        //旧数据时记录
        if (iterator != null) {
            while (iterator.hasNext()) {
                Map.Entry<Integer, String> entry = iterator.next();
                if (ObjectUtils.isEmpty(entry.getKey())) {
                    continue;
                }

                String key = headMap.get(entry.getKey());
                String value = ObjectUtils.isEmpty(entry.getValue()) ? "" : entry.getValue();

                // 处理具体字段
                if (StringUtils.isNotBlank(key)) {
                    handleField(entity, detailList, errorMsgList, key, value);
                }
            }
        }


        if (fieldHandler(map, errorMsgList, entity)) return;
        //根据月份、店铺数据查询
        saveData(entity, detailList);
    }

    private boolean fieldHandler(Map<Integer, String> map, List<String> errorMsgList, BiDataSourceCostEntity entity) {
        validateDeptName(entity, errorMsgList);
        validatePlatformName(entity, errorMsgList);
        validateFieldIsNotBlank(entity.getSite(), "站点不能为空", errorMsgList);
        validateFieldIsNotBlank(entity.getShopName(), "店铺名称不能为空", errorMsgList);
        validateFieldIsNotBlank(entity.getChargeName(), "负责人不能为空", errorMsgList);
        validateShopInfo(entity, errorMsgList);

        return errorMsg(map, errorMsgList);
    }

    // 验证销售事业部
    private void validateDeptName(BiDataSourceCostEntity entity, List<String> errorMsgList) {
        if (StringUtils.isBlank(entity.getDeptName())) {
            errorMsgList.add("销售事业部不能为空");
        } else {
            String deptId = deptList.stream()
                    .filter(obj -> obj.getName().equals(entity.getDeptName()))
                    .map(SysDepartmentDTO::getId)
                    .findFirst()
                    .orElse("");
            if (StringUtils.isBlank(deptId)) {
                errorMsgList.add(CharSequenceUtil.format("部门{}不存在", entity.getDeptName()));
            } else {
                entity.setDeptId(deptId);
            }
        }
    }

    // 验证平台名称
    private void validatePlatformName(BiDataSourceCostEntity entity, List<String> errorMsgList) {
        if (StringUtils.isBlank(entity.getPlatformName())) {
            errorMsgList.add("平台名称不能为空");
        } else {
            PlatformDictEnum platformEnum = PlatformDictEnum.getByName(entity.getPlatformName());
            if (ObjectUtils.isEmpty(platformEnum)) {
                errorMsgList.add("系统中不存在此平台名称");
            }
        }
    }

    // 验证字段非空
    private void validateFieldIsNotBlank(String field, String errorMessage, List<String> errorMsgList) {
        if (StringUtils.isBlank(field)) {
            errorMsgList.add(errorMessage);
        }
    }

    // 验证店铺信息
    private void validateShopInfo(BiDataSourceCostEntity entity, List<String> errorMsgList) {
        BiShopInfoEntity biShopInfoEntity = shopList.stream()
                .filter(obj -> obj.getName().equals(entity.getShopName()) &&
                        obj.getSite().equals(entity.getSite()) &&
                        obj.getPlatformName().equals(entity.getPlatformName()))
                .findFirst()
                .orElse(null);
        if (biShopInfoEntity == null) {
            errorMsgList.add("在平台站点中未找到该店铺");
        } else {
            entity.setShopId(biShopInfoEntity.getId());
        }
    }

    private void handleField(BiDataSourceCostEntity entity,
                             List<BiDataSourceCostDetailEntity> detailList,
                             List<String> errorMsgList,
                             String key,
                             String value) {
        switch (BiDataSourceCostEnum.getEnum(key)) {
            case MONTH:
                handleMonthField(entity, value, errorMsgList);
                break;
            case DEPTNAME:
                entity.setDeptName(value);
                break;
            case PLATFORMNAME:
                entity.setPlatformName(value);
                break;
            case SITE:
                entity.setSite(value);
                break;
            case SHOPNAME:
                entity.setShopName(value);
                break;
            case CHARGENAME:
                handleChargeName(entity, value, errorMsgList);
                break;
            case COMBINATION:
                entity.setCombination(value);
                break;
            default:
                handleCostTypeField(key, value, detailList, errorMsgList);
                break;
        }
    }

    private void handleChargeName(BiDataSourceCostEntity entity, String value, List<String> errorMsgList) {
        if (CollectionUtils.isNotEmpty(userList)) {
            FindUserDTO findUserDTO = userList.stream().filter(obj -> obj.getUserName().equals(value)).findFirst().orElse(null);
            if (findUserDTO == null) {
                errorMsgList.add("系统中不存在该负责人");
            } else {
                entity.setChargeId(findUserDTO.getUserId());
            }
        }
        entity.setChargeName(value);
    }

    private void handleMonthField(BiDataSourceCostEntity entity, String value, List<String> errorMsgList) {
        try {
            Date parse = DateUtil.stringToDate(value);
            entity.setMonth(LocalDateUtil.date2LocalDateTime(parse));
        } catch (Exception e) {
            errorMsgList.add("月份格式错误");
        }
    }

    private void handleCostTypeField(String key, String value, List<BiDataSourceCostDetailEntity> detailList, List<String> errorMsgList) {
        if (CollectionUtils.isNotEmpty(dictList)) {
            String costType = dictList.stream().filter(obj -> obj.getName().equals(key)).map(BiDictEntity::getValue).findFirst().orElse(null);
            if (StringUtils.isBlank(costType)) {
                errorMsgList.add("未找到成本数据: " + key);
            }

            BiDataSourceCostDetailEntity detailEntity = new BiDataSourceCostDetailEntity();
            detailEntity.setCostType(costType);

            if (!StrUtils.isDigit(value) && !StrUtils.isPercentage(value)) {
                errorMsgList.add("成本必须是数值或百分比数据");
            } else {
                if (StrUtils.isDigit(value)) {
                    detailEntity.setCostValue(MathUtil.valueOf(value));
                    detailEntity.setValueType(MathUtil.ZERO);
                } else {
                    detailEntity.setValueType(MathUtil.ONE);
                    String costValue = value.replace("%", "");
                    detailEntity.setCostValue(MathUtil.divide(MathUtil.valueOf(costValue), new BigDecimal(100), 4));
                }
                detailList.add(detailEntity);
            }
        }
    }


    private void saveData(BiDataSourceCostEntity entity, List<BiDataSourceCostDetailEntity> detailList) {
        BiDataSourceCostEntity cost = biDataSourceCostService.getByCostParam(entity);
        if (ObjectUtils.isEmpty(cost)) {
            // 新增成本主表数据
            biDataSourceCostService.save(entity);
            if (CollUtil.isNotEmpty(detailList)) {
                detailList.forEach(obj -> obj.setCostId(entity.getId()));
                biDataSourceCostDetailService.saveBatch(detailList);
            }
        } else {
            entity.setId(cost.getId());
            // 更新成本主表数据
            biDataSourceCostService.updateById(entity);
            // 删除成本明细重新新增
            biDataSourceCostDetailService.removeByCostId(cost.getId());
            if (CollUtil.isNotEmpty(detailList)) {
                detailList.forEach(obj -> obj.setCostId(entity.getId()));
                biDataSourceCostDetailService.saveBatch(detailList);
            }
        }
    }

    private boolean errorMsg(Map<Integer, String> map, List<String> errorMsgList) {
        StringBuilder errStr = new StringBuilder();
        if (!errorMsgList.isEmpty()) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr.append(indexTemp).append("、").append(errorMsgList.get(i)).append("；");
            }
            map.put(map.size() , errStr.toString());
            list.add(map);
            return true;
        }
        return false;
    }

    @Override
    public void invokeHeadMap(Map<Integer,String> map, AnalysisContext analysisContext) {
        List<String> msglist = map.values().stream().collect(Collectors.toList());
        msglist.add("错误信息");
        this.headMap = map;
        this.headList = msglist;
    }

    public List<String> getHead(){
        return headList;
    }

    public List<Map<Integer,String>> getDateList(){
        return list;
    }

    /**
     * @description: 全部解析完回调此方法
     * @author Will
     * @date: 2022/12/16 10:26
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // document why this method is empty
    }
}
