package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.ExcelUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCustomSearchDTO;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.bi.entity.BiDataSourceCustomDetailEntity;
import com.erp.model.bi.entity.BiDataSourceCustomEntity;
import com.erp.server.bi.enums.BiDataSourceCustomEnum;
import com.erp.server.bi.enums.BiDataSourceCustomTypeEnum;
import com.erp.server.bi.enums.DataTypeEnum;
import com.erp.server.bi.enums.DictEnum;
import com.erp.server.bi.mapper.BiDataSourceCustomMapper;
import com.erp.server.bi.service.BiDataSourceCustomDetailService;
import com.erp.server.bi.service.BiDataSourceCustomService;
import com.erp.server.bi.service.BiDictService;
import com.erp.server.bi.service.DmpOrderInfoService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:48
 */
@Service
public class BiDataSourceCustomServiceImpl extends ServiceImpl<BiDataSourceCustomMapper, BiDataSourceCustomEntity>
        implements BiDataSourceCustomService {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private BiDictService biDictService;

    @Resource
    private BiDataSourceCustomDetailService biDataSourceCustomDetailService;



    @Override
    public PagingVO<LinkedHashMap<String,Object>> paging(PagingDTO<BiDataSourceCustomSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BiDataSourceCustomSearchDTO params = dto.getParams();
        IPage<LinkedHashMap<String,Object>> pageData = baseMapper.paging(query, params);
        LinkedHashMap<String,Object> resultMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> headMap = new LinkedHashMap<>();
        renewBiDataSourceCustom(pageData.getRecords(),headMap,dto.getParams().getType());
        headMap.remove("dataType");
        resultMap.put("head",headMap);
        resultMap.put("data",pageData.getRecords());
        pageData.setRecords(Arrays.asList(resultMap));
        return new PagingVO(pageData);
    }

    @Override
    public void exportExcel(BiDataSourceCustomSearchDTO dto, HttpServletResponse response) {

        Integer type = dto.getType();
        //查询所有数据
        List<LinkedHashMap<String,Object>>  list = baseMapper.getAllBiDataSourceCustom(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //表头
        LinkedHashMap<String,Object> heads = new LinkedHashMap<>();
        List<LinkedHashMap<String, Object>> customList = renewBiDataSourceCustom(list,heads,type);
        if (CollectionUtils.isEmpty(customList)) {
            return;
        }
        List<String> headList = new ArrayList<>();
        for (Map.Entry<String,Object> map:heads.entrySet()) {
            String value = map.getValue().toString();
            headList.add(value);
        }
        String dataTypeName = DataTypeEnum.getName(dto.getDataType());
        String head = dataTypeName;
        String fileName = dmpOrderInfoService.getFileName(dataTypeName)+ ".xlsx";
        ExcelUtil.easyUtil(headList,head,list,fileName,response);
    }

    @Override
    @Transactional
    public void importExcel(MultipartFile excelFile, HttpServletResponse response, Integer importType) {
        List<Map<String,String>> list = ExcelPrintUtils.makeData(excelFile);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //季度数据
        List<BiDictEntity> quarterList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMQUARTER.getType());
        //月份数据
        List<BiDictEntity> monthList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMMONTH.getType());

        for (Map<String,String> map:list) {
            //遍历map下的数据
            Iterator<Map.Entry<String, String>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
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
                    String key = entry.getKey().toString();
                    String value = ObjectUtils.isEmpty(entry.getValue()) ? "" : entry.getValue().toString();
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
                        BiDataSourceCustomEntity custom = getCustomByPatam(main);
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
            BiDataSourceCustomEntity custom = getCustomByPatam(entity);
            if (ObjectUtils.isEmpty(custom) && !BiDataSourceCustomTypeEnum.YEAR.getCode().equals(importType)) {
                addList.add(entity);
            } else {
                entity.setId(custom.getId());
                updateList.add(entity);
            }
            //新增数据
            if (CollectionUtils.isNotEmpty(addList)) {
                this.saveBatch(addList);
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
                this.updateBatchById(updateList);
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
    }

    @Override
    public List<String> listTargetNameByDataSource(Integer dataType) {
        LambdaQueryWrapper<BiDataSourceCustomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiDataSourceCustomEntity::getDataType,dataType);
        queryWrapper.select(BiDataSourceCustomEntity::getTargetName);
        return this.listObjs(queryWrapper, Object::toString);
    }


    /**
     * 返回字段处理
     */
    private List<LinkedHashMap<String,Object>> renewBiDataSourceCustom(List<LinkedHashMap<String,Object>> list,LinkedHashMap<String, Object> head,Integer type) {
        //返回中文类型的数据
        List<LinkedHashMap<String,Object>> cnResultMap = new ArrayList<>();

        List<BiDictEntity> dictList = new ArrayList<>();
        switch (type) {
            case 2:
                //查询成本字典数据
                dictList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMQUARTER.getType());
                break;
            case 3:
                //查询成本字典数据
                dictList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMMONTH.getType());
                break;
            default:
                break;
        }

        List<BiDataSourceCustomDetailEntity> biDataSourceCustomDetailList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            //查询明细数据
            List<String> costIds = list.stream().map((Map m) -> (String) m.get("id")).collect(Collectors.toList());
             biDataSourceCustomDetailList= biDataSourceCustomDetailService.listByCustomIds(costIds);
        }
        BiDataSourceCustomEnum[] values = BiDataSourceCustomEnum.values();
        //新增固定表头
        for (BiDataSourceCustomEnum value:values) {
            if (ObjectUtils.isEmpty(head.get(value.getCode()))) {
                head.put(value.getCode(),value.getName());
            }
        }
        if (CollectionUtils.isNotEmpty(dictList)) {
            //新增变动表头
            for (BiDictEntity dcit : dictList) {
                if (ObjectUtils.isEmpty(head.get(dcit.getValue()))) {
                    head.put(dcit.getName(),dcit.getName());
                }
            }
        }

        for (LinkedHashMap<String,Object> map: list) {
            //中文数据（用于导出）
            LinkedHashMap<String,Object> cnMap = new LinkedHashMap<>();

            for (BiDataSourceCustomEnum value:values) {
                cnMap.put(value.getName(),map.get(value.getCode()));
            }
            if (BiDataSourceCustomTypeEnum.YEAR.getCode().equals(type) || BiDataSourceCustomTypeEnum.WEEK.getCode().equals(type) || BiDataSourceCustomTypeEnum.DAY.getCode().equals(type)) {
                for (BiDataSourceCustomDetailEntity entity: biDataSourceCustomDetailList) {
                    if (BiDataSourceCustomTypeEnum.YEAR.getCode().equals(type)) {
                        map.put(entity.getYear().toString().concat("年"),entity.getYear().equals(map.get("year")) ? entity.getValue() : "");
                        cnMap.put(entity.getYear().toString().concat("年"),entity.getYear().equals(map.get("year")) ? entity.getValue() : "");
                        head.put(entity.getYear().toString().concat("年"),entity.getYear().toString().concat("年"));
                        continue;
                    }
                    if (BiDataSourceCustomTypeEnum.WEEK.getCode().equals(type)) {
                        map.put(entity.getWeekBegin().concat("-").concat(entity.getWeekEnd()),entity.getValue());
                        cnMap.put(entity.getWeekBegin().concat("-").concat(entity.getWeekEnd()),entity.getValue());
                        head.put(entity.getWeekBegin().concat("-").concat(entity.getWeekEnd()),entity.getWeekBegin().concat("-").concat(entity.getWeekEnd()));
                        continue;
                    }
                    if (BiDataSourceCustomTypeEnum.DAY.getCode().equals(type)) {
                        map.put(entity.getMonth().toString().concat("月").concat(entity.getDate().toString()).concat("日"),entity.getValue());
                        cnMap.put(entity.getMonth().toString().concat("月").concat(entity.getDate().toString()).concat("日"),entity.getValue());
                        head.put(entity.getMonth().toString().concat("月").concat(entity.getDate().toString()).concat("日"),entity.getMonth().toString().concat("月").concat(entity.getDate().toString()).concat("日"));
                        continue;
                    }
                }
            }
            if (BiDataSourceCustomTypeEnum.MONTH.getCode().equals(type) || BiDataSourceCustomTypeEnum.QUARTER.getCode().equals(type)) {
                for (BiDictEntity dcit : dictList) {
                    if (BiDataSourceCustomTypeEnum.MONTH.getCode().equals(type)) {
                        String value = biDataSourceCustomDetailList.stream().filter(obj -> obj.getCustomId().equals(map.get("id")) && obj.getMonth().equals(dcit.getValue()))
                                .map(BiDataSourceCustomDetailEntity::getValue).findFirst().orElse(null);
                        map.put(dcit.getName(), value);
                        cnMap.put(dcit.getName(), value);
                        continue;
                    }
                    if (BiDataSourceCustomTypeEnum.QUARTER.getCode().equals(type)) {
                        String value = biDataSourceCustomDetailList.stream().filter(obj -> obj.getCustomId().equals(map.get("id")) && obj.getQuarter().equals(dcit.getValue()))
                                .map(BiDataSourceCustomDetailEntity::getValue).findFirst().orElse(null);
                        map.put(dcit.getName(), value);
                        cnMap.put(dcit.getName(), value);
                        continue;
                    }
                }
            }
            map.remove("id");
            cnResultMap.add(cnMap);
        }
        return list;
    }


    private BiDataSourceCustomEntity getCustomByPatam(BiDataSourceCustomEntity entity) {
        LambdaQueryWrapper<BiDataSourceCustomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiDataSourceCustomEntity::getYear,entity.getYear());
        queryWrapper.eq(BiDataSourceCustomEntity::getType,entity.getType());
        queryWrapper.eq(BiDataSourceCustomEntity::getDataType,entity.getDataType());
        queryWrapper.eq(BiDataSourceCustomEntity::getTargetType,entity.getTargetType());
        queryWrapper.eq(BiDataSourceCustomEntity::getTargetName,entity.getTargetName());
        queryWrapper.last("limit 1");
        return  this.getOne(queryWrapper);
    }
}
