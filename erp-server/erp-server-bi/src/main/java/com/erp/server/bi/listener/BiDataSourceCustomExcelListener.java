package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.erp.model.bi.entity.BiDataSourceCustomDetailEntity;
import com.erp.model.bi.entity.BiDataSourceCustomEntity;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.server.bi.enums.BiDataSourceCustomEnum;
import com.erp.server.bi.enums.BiDataSourceCustomTypeEnum;
import com.erp.server.bi.enums.DataTypeEnum;
import com.erp.server.bi.service.BiDataSourceCustomDetailService;
import com.erp.server.bi.service.BiDataSourceCustomService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/27 17:22
 */
public class BiDataSourceCustomExcelListener extends AnalysisEventListener<Map<Integer,String>> {

    private BiDataSourceCustomService biDataSourceCustomService;
    private BiDataSourceCustomDetailService biDataSourceCustomDetailService;
    List<Map<Integer,String>> list ;
    Map<Integer,String> headMap;
    List<String> headList;
    List<BiDictEntity> quarterList;
    List<BiDictEntity> monthList;
    Integer importType;

    public BiDataSourceCustomExcelListener(BiDataSourceCustomService biDataSourceCustomService, BiDataSourceCustomDetailService biDataSourceCustomDetailService,
                                          List<BiDictEntity> quarterList,List<BiDictEntity> monthList,Integer importType) {
        this.biDataSourceCustomService = biDataSourceCustomService;
        this.biDataSourceCustomDetailService = biDataSourceCustomDetailService;
        this.quarterList = quarterList;
        this.monthList = monthList;
        this.importType = importType;
        this.list = new ArrayList<>();
    }

    @Override
    public void invoke(Map<Integer,String> map, AnalysisContext analysisContext) {

        List<String> errorMsgList = new ArrayList<>();
        //遍历map下的数据
        Iterator<Map.Entry<Integer, String>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
        //主表数据
        BiDataSourceCustomEntity entity = new BiDataSourceCustomEntity();
        entity.setType(importType);
        //年导入时的主表新增数据
        List<BiDataSourceCustomEntity> addList = new ArrayList<>();
        //年导入时的主表修改数据
        List<BiDataSourceCustomEntity> updateList = new ArrayList<>();
        List<BiDataSourceCustomDetailEntity> detailList = new ArrayList<>();
        Integer yearDate = 0;
        //旧数据时记录
        if (ObjectUtils.isNotEmpty(iterator)) {
            while (iterator.hasNext()) {
                Map.Entry entry = (java.util.Map.Entry) iterator.next();
                if (ObjectUtils.isEmpty(entry.getKey())) {
                    continue;
                }
                Integer mapKey = Integer.valueOf(entry.getKey().toString());
                String value = ObjectUtils.isEmpty(entry.getValue()) ? "" : entry.getValue().toString();
                String key = headMap.get(mapKey);
                //明细数据
                BiDataSourceCustomDetailEntity detailEntity = new BiDataSourceCustomDetailEntity();
                if (BiDataSourceCustomEnum.YEAR.getName().equals(key)) {
                    String year = value.replace("年", "");
                    entity.setYear(Integer.valueOf(year));
                    yearDate = Integer.valueOf(year);
                    continue;
                }
                if (BiDataSourceCustomEnum.DATATYPE.getName().equals(key)) {
                    Integer code = DataTypeEnum.getCodeByName(value);
                    entity.setDataType(code);
                    continue;
                }
                if (BiDataSourceCustomEnum.TARGETTYPE.getName().equals(key)) {
                    entity.setTargetType(value);
                    continue;
                }
                if (BiDataSourceCustomEnum.TARGETNAME.getName().equals(key)) {
                    entity.setTargetName(value);
                    continue;
                }
                if (BiDataSourceCustomEnum.TARGEVALUE.getName().equals(key)) {
                    entity.setTargetValue(value);
                    continue;
                }
                //年导入
                if (BiDataSourceCustomTypeEnum.YEAR.getCode().equals(importType)) {
                    String year = key.replace("年", "");
                    detailEntity.setYear(Integer.valueOf(year));
                    detailEntity.setValue(value);
                    detailList.add(detailEntity);
                    //年导入时主表数据与明细数据一一对应
                    BiDataSourceCustomEntity main = new BiDataSourceCustomEntity();
                    BeanUtils.copyProperties(entity,main);
                    main.setYear(Integer.valueOf(year));
                    //根据类型、数据类型、年份、指标分类、指标名称查询
                    BiDataSourceCustomEntity custom = biDataSourceCustomService.getCustomByParam(main);
                    if (ObjectUtils.isEmpty(custom)) {
                        addList.add(main);
                    } else {
                        main.setId(custom.getId());
                        updateList.add(main);
                    }
                }
                //季度导入
                if (BiDataSourceCustomTypeEnum.QUARTER.getCode().equals(importType)) {
                    if (CollectionUtils.isNotEmpty(quarterList)) {
                        String quarter = quarterList.stream().filter(obj -> obj.getName().equals(key)).map(BiDictEntity::getValue).findFirst().orElse("");
                        detailEntity.setYear(yearDate);
                        detailEntity.setQuarter(Integer.valueOf(quarter));
                        detailEntity.setValue(value);
                        detailList.add(detailEntity);
                    }
                }
                //月导入
                if (BiDataSourceCustomTypeEnum.MONTH.getCode().equals(importType)) {
                    String month = monthList.stream().filter(obj -> obj.getName().equals(key)).map(BiDictEntity::getValue).findFirst().orElse("");
                    detailEntity.setYear(yearDate);
                    detailEntity.setMonth(Integer.valueOf(month));
                    detailEntity.setValue(value);
                    detailList.add(detailEntity);
                }
                //周导入
                if (BiDataSourceCustomTypeEnum.WEEK.getCode().equals(importType)) {
                    String week1 = key.split("-")[0];
                    String week2 = key.split("-")[1];
                    detailEntity.setYear(yearDate);
                    detailEntity.setWeekBegin(week1);
                    detailEntity.setWeekEnd(week2);
                    detailEntity.setValue(value);
                    detailList.add(detailEntity);
                }
                //日导入
                if (BiDataSourceCustomTypeEnum.DAY.getCode().equals(importType)) {
                    String month = key.split("月")[0];
                    String day = key.split("月")[1].split("日")[0];
                    detailEntity.setYear(yearDate);
                    detailEntity.setMonth(Integer.valueOf(month));
                    detailEntity.setDate(Integer.valueOf(day));
                    detailEntity.setValue(value);
                    detailList.add(detailEntity);
                }

            }
        }
        if (ObjectUtils.isEmpty(entity.getYear())) {
            errorMsgList.add("年份不能为空");
        }
        if (ObjectUtils.isEmpty(entity.getDataType())) {
            errorMsgList.add("数据类型输入有误");
        }
        if (ObjectUtils.isEmpty(entity.getTargetType())) {
            errorMsgList.add("指标分类不能为空");
        }
        if (ObjectUtils.isEmpty(entity.getTargetName())) {
            errorMsgList.add("指标名称不能为空");
        }
        String errStr = "";
        if (errorMsgList.size() > 0) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
            }
            map.put(map.size() ,errStr);
            list.add(map);
            return;
        }

        BiDataSourceCustomEntity custom = biDataSourceCustomService.getCustomByParam(entity);
        if (ObjectUtils.isEmpty(custom) && !BiDataSourceCustomTypeEnum.YEAR.getCode().equals(importType)) {
            addList.add(entity);
        } else {
            entity.setId(custom.getId());
            updateList.add(entity);
        }
        //新增数据
        if (CollectionUtils.isNotEmpty(addList)) {
            biDataSourceCustomService.saveBatch(addList);
            if (CollectionUtils.isNotEmpty(detailList)) {
                detailList.forEach(obj -> {
                    String id = addList.stream().filter(e -> e.getYear().equals(obj.getYear())).map(BiDataSourceCustomEntity::getId).findFirst().orElse("");
                    obj.setCustomId(id);
                });
                biDataSourceCustomDetailService.saveBatch(detailList);
            }
        }
        //修改数据
        if (CollectionUtils.isNotEmpty(updateList)) {
            biDataSourceCustomService.updateBatchById(updateList);
            //先删除原有明细再新增
            List<String> customIds = updateList.stream().map(BiDataSourceCustomEntity::getId).collect(Collectors.toList());
            biDataSourceCustomDetailService.removeByCustomIds(customIds);
            if (CollectionUtils.isNotEmpty(detailList)) {
                detailList.forEach(obj -> {
                    String id = updateList.stream().filter(e -> e.getYear().equals(obj.getYear())).map(BiDataSourceCustomEntity::getId).findFirst().orElse("");
                    obj.setCustomId(id);
                });
                biDataSourceCustomDetailService.saveBatch(detailList);
            }
        }

    }

    @Override
    public void invokeHeadMap(Map<Integer,String> map, AnalysisContext analysisContext) {
        List<String> headList = map.values().stream().collect(Collectors.toList());
        headList.add("错误信息");
        this.headMap = map;
        this.headList = headList;
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

    }
}
