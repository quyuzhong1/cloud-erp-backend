package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.erp.model.bi.entity.BiDataSourceCustomDetailEntity;
import com.erp.model.bi.entity.BiDataSourceCustomEntity;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.server.bi.enums.BiDataSourceCustomEnum;
import com.erp.server.bi.enums.BiDataSourceCustomTypeEnum;
import com.erp.server.bi.service.BiDataSourceCustomDetailService;
import com.erp.server.bi.service.BiDataSourceCustomService;
import org.apache.commons.collections.CollectionUtils;

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

    private List<Map<Integer,String>> list ;

    private Map<Integer,String> headMap;

    private List<String> headList;

    private List<BiDictEntity> quarterList;

    private List<BiDictEntity> monthList;

    private Integer importType;

    private Integer dataType;

    public BiDataSourceCustomExcelListener(BiDataSourceCustomService biDataSourceCustomService, BiDataSourceCustomDetailService biDataSourceCustomDetailService,
                                          List<BiDictEntity> quarterList,List<BiDictEntity> monthList,Integer importType,Integer dataType) {
        this.biDataSourceCustomService = biDataSourceCustomService;
        this.biDataSourceCustomDetailService = biDataSourceCustomDetailService;
        this.quarterList = quarterList;
        this.monthList = monthList;
        this.importType = importType;
        this.dataType = dataType;
        this.list = new ArrayList<>();
    }

    @Override
    public void invoke(Map<Integer,String> map, AnalysisContext analysisContext) {
        //表头信息
        List<String> head = getHead();
        List<String> errorMsgList = new ArrayList<>();
        //遍历map下的数据
        Iterator<Map.Entry<Integer, String>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
        //主表数据
        BiDataSourceCustomEntity entity = new BiDataSourceCustomEntity();
        entity.setType(importType);
        entity.setDataType(dataType);
        //年导入时的主表新增数据
        List<BiDataSourceCustomEntity> addList = new ArrayList<>();
        //年导入时的主表修改数据
        List<BiDataSourceCustomEntity> updateList = new ArrayList<>();
        List<BiDataSourceCustomDetailEntity> detailList = new ArrayList<>();
        Integer yearDate = 0;
        //旧数据时记录
        if (ObjectUtils.isNotEmpty(iterator)) {
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next();
                if (ObjectUtils.isEmpty(entry.getKey())) {
                    continue;
                }
                Integer mapKey = Integer.valueOf(entry.getKey().toString());
                String value = ObjectUtils.isEmpty(entry.getValue()) ? "" : entry.getValue().toString();
                String key = headMap.get(mapKey);
                //明细数据
                BiDataSourceCustomDetailEntity detailEntity = new BiDataSourceCustomDetailEntity();
                if (BiDataSourceCustomEnum.YEAR.getDesc().equals(key)) {
                    String year = "";
                    try {
                        year = value.replace("年", "");
                        entity.setYear(Integer.valueOf(year));
                    } catch (Exception e){
                        errorMsgList.add("年份格式有误");
                        continue;
                    }
                    yearDate = Integer.valueOf(year);
                    continue;
                }
                if (BiDataSourceCustomEnum.TARGETTYPE.getDesc().equals(key)) {
                    entity.setTargetType(value);
                    continue;
                }
                if (BiDataSourceCustomEnum.TARGETNAME.getDesc().equals(key)) {
                    entity.setTargetName(value);
                    continue;
                }
                if (BiDataSourceCustomEnum.TARGEVALUE.getDesc().equals(key)) {
                    entity.setTargetValue(value);
                    continue;
                }
                //年导入
                if (BiDataSourceCustomTypeEnum.YEAR.getCode().equals(importType)) {
                    if ("实际值".equals(key)){
                        detailEntity.setYear(yearDate);
                        detailEntity.setValue(value);
                        detailList.add(detailEntity);
                    }
                }
                //季度导入
                if (BiDataSourceCustomTypeEnum.QUARTER.getCode().equals(importType)) {
                    if (CollectionUtils.isNotEmpty(quarterList)) {
                        try {
                            String quarter = quarterList.stream().filter(obj -> obj.getName().equals(key)).map(BiDictEntity::getValue).findFirst().orElse("");
                            detailEntity.setYear(yearDate);
                            detailEntity.setQuarter(Integer.valueOf(quarter));
                            detailEntity.setValue(value);
                            detailList.add(detailEntity);
                        } catch (Exception e){
                            errorMsgList.add("季度格式有误，例如：Q1");
                        }
                    }
                }
                //月导入
                if (BiDataSourceCustomTypeEnum.MONTH.getCode().equals(importType)) {
                    try {
                        String month = monthList.stream().filter(obj -> obj.getName().equals(key)).map(BiDictEntity::getValue).findFirst().orElse("");
                        detailEntity.setYear(yearDate);
                        detailEntity.setMonth(Integer.valueOf(month));
                        detailEntity.setValue(value);
                        detailList.add(detailEntity);
                    } catch (Exception e){
                        errorMsgList.add("月份格式有误，例如：1月");
                    }
                }
                //周导入
                if (BiDataSourceCustomTypeEnum.WEEK.getCode().equals(importType)) {
                    try {
                        String week1 = key.split("-")[0];
                        String week2 = key.split("-")[1];
                        detailEntity.setYear(yearDate);
                        detailEntity.setWeekBegin(week1);
                        detailEntity.setWeekEnd(week2);
                        detailEntity.setValue(value);
                        detailList.add(detailEntity);
                    } catch (Exception e){
                        errorMsgList.add("周期格式有误，例如：1月1日-1月7日");
                    }
                }
                //日导入
                if (BiDataSourceCustomTypeEnum.DAY.getCode().equals(importType)) {
                    try {
                        String month = key.split("月")[0];
                        String day = key.split("月")[1].split("日")[0];
                        detailEntity.setYear(yearDate);
                        detailEntity.setMonth(Integer.valueOf(month));
                        detailEntity.setDate(Integer.valueOf(day));
                        detailEntity.setValue(value);
                        detailList.add(detailEntity);
                    } catch (Exception e){
                        errorMsgList.add("日期格式有误，例如：1月1日");
                    }
                }

            }
        }
        if (ObjectUtils.isEmpty(entity.getYear())) {
            errorMsgList.add("年份不能为空");
        }
        if (ObjectUtils.isEmpty(entity.getDataType())) {
            errorMsgList.add("数据类型输入有误");
        }
        if (ObjectUtils.isEmpty(entity.getTargetName())) {
            errorMsgList.add("指标名称不能为空");
        }
        if (ObjectUtils.isEmpty(entity.getTargetValue())) {
            errorMsgList.add("目标值不能为空");
        }
        String errStr = "";
        if (errorMsgList.size() > 0) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
            }
            map.put(head.size() - 1 ,errStr);
            list.add(map);
            return;
        }

        BiDataSourceCustomEntity custom = biDataSourceCustomService.getCustomByParam(entity);
        if (ObjectUtils.isEmpty(custom)) {
            addList.add(entity);
        } else if (ObjectUtils.isNotEmpty(custom)){
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
