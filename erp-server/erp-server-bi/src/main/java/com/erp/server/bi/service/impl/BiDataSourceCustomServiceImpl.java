package com.erp.server.bi.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.ChartVO;
import com.common.business.vo.PagingVO;
import com.common.business.vo.SeriesVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.bi.dto.BiDataSourceCustomGraphicalDTO;
import com.erp.model.bi.dto.BiDataSourceCustomSearchDTO;
import com.erp.model.bi.dto.BiDataSourceCustomTableDTO;
import com.erp.model.bi.dto.BiTargetTypeDTO;
import com.erp.model.bi.entity.*;
import com.erp.server.bi.enums.BiDataSourceCustomEnum;
import com.erp.server.bi.enums.BiDataSourceCustomTypeEnum;
import com.erp.server.bi.enums.DataTypeEnum;
import com.erp.server.bi.enums.DictEnum;
import com.erp.server.bi.listener.BiDataSourceCustomExcelListener;
import com.erp.server.bi.mapper.BiDataSourceCustomMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import static com.alibaba.excel.EasyExcelFactory.read;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/14 16:48
 */
@Service
public class BiDataSourceCustomServiceImpl extends ServiceImpl<BiDataSourceCustomMapper, BiDataSourceCustomEntity>
        implements BiDataSourceCustomService {

    @Resource
    private BiOrderInfoService biOrderInfoService;

    @Resource
    private BiDictService biDictService;

    @Resource
    private BiDataSourceCustomDetailService biDataSourceCustomDetailService;

    @Resource
    private BiSysModuleService biSysModuleService;

    @Resource
    private BiModuleService biModuleService;


    @Override
    public PagingVO<LinkedHashMap<String,Object>> paging(PagingDTO<BiDataSourceCustomSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BiDataSourceCustomSearchDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        IPage<LinkedHashMap<String,Object>> pageData = baseMapper.paging(query, params);
        LinkedHashMap<String,Object> resultMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> headMap = new LinkedHashMap<>();
        renewBiDataSourceCustom(pageData.getRecords(),headMap,dto.getParams().getType());
        headMap.remove("dataType");
        resultMap.put("head",headMap);
        resultMap.put("data",pageData.getRecords());
        pageData.setRecords(Arrays.asList(resultMap));
        return new PagingVO<LinkedHashMap<String,Object>>(pageData);
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
        renewBiDataSourceCustom(list,heads,type);
        List<String> headList = new ArrayList<>();
        for (Map.Entry<String,Object> map:heads.entrySet()) {
            String value = map.getValue().toString();
            headList.add(value);
        }
        String dataTypeName = DataTypeEnum.getName(dto.getDataType());
        String head = dataTypeName;
        String fileName = biOrderInfoService.getFileName(dataTypeName)+ ".xlsx";
        ExcelUtil.easyUtilStr(headList,head,list,fileName,response);
    }

    @Override
    @Transactional
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response, Integer importType,Integer dataType) {
        //季度数据
        List<BiDictEntity> quarterList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMQUARTER.getType());
        //月份数据
        List<BiDictEntity> monthList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMMONTH.getType());
        BiDataSourceCustomExcelListener excelListenerUtil = new BiDataSourceCustomExcelListener(this,biDataSourceCustomDetailService,quarterList,monthList,importType,dataType);
        try {
            read(excelFile.getInputStream(), excelListenerUtil).sheet(0).doRead();
            List<Map<Integer, String>> list = excelListenerUtil.getDateList();
            if (CollectionUtils.isEmpty(list) || list.size() == 0) {
                return true;
            }
            List<String> headList = excelListenerUtil.getHead();
            String head = "自助数据表";
            String fileName = biOrderInfoService.getFileName("自助数据表导出")+ ".xlsx";
            ExcelUtil.easyUtil(headList,head,list,fileName, response);
        } catch (IOException e) {
            throw new ServiceException(ApiError.Default);
        }
        return false;
    }

    @Override
    public List<String> listTargetNameByDataSource(Integer dataType,Integer dataDimension) {
        LambdaQueryWrapper<BiDataSourceCustomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiDataSourceCustomEntity::getDataType,dataType);
        queryWrapper.eq(BiDataSourceCustomEntity::getType,dataDimension);
        queryWrapper.select(BiDataSourceCustomEntity::getTargetName);
        List<String> strings = this.listObjs(queryWrapper, Object::toString);
        if (CollectionUtils.isNotEmpty(strings)) {
            strings = strings.stream().distinct().collect(Collectors.toList());
        }
        return strings;
    }

    @Override
    public List<String> listTargetType(BiDataSourceCustomTableDTO dto) {

        BiModuleEntity biModuleEntity = biModuleService.getById(dto.getModuleId());
        if (ObjectUtils.isEmpty(biModuleEntity)) {
            return new ArrayList<>();
        }
        BiSysModuleEntity biSysModuleEntity = biSysModuleService.getById(biModuleEntity.getSysModuleId());
        if (ObjectUtils.isEmpty(biSysModuleEntity)) {
            return new ArrayList<>();
        }
        //数据类型
        Integer dataSource = biSysModuleEntity.getDataSource();
        //指标名称
        String targetNames = biSysModuleEntity.getTargetNames();

        if (ObjectUtils.isEmpty(dataSource) || StringUtils.isBlank(targetNames) ) {
            return new ArrayList<>();
        }
        List<String> targetNameList = Arrays.stream(targetNames.split(",")).collect(Collectors.toList());
        LambdaQueryWrapper<BiDataSourceCustomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiDataSourceCustomEntity::getType,dto.getType());
        queryWrapper.eq(BiDataSourceCustomEntity::getYear,dto.getYear());
        queryWrapper.eq(BiDataSourceCustomEntity::getDataType,dataSource);
        queryWrapper.in(BiDataSourceCustomEntity::getTargetName,targetNameList);
        queryWrapper.select(BiDataSourceCustomEntity::getTargetType);
        return this.listObjs(queryWrapper,Object::toString);
    }

    @Override
    public ChartVO<BiDataSourceCustomGraphicalDTO> listGraphicalData(String moduleId, Integer year) {
        ChartVO<BiDataSourceCustomGraphicalDTO> chartVO = new ChartVO();
        BiModuleEntity biModuleEntity = biModuleService.getById(moduleId);
        if (ObjectUtils.isEmpty(biModuleEntity)) {
            return chartVO;
        }
        BiSysModuleEntity biSysModuleEntity = biSysModuleService.getById(biModuleEntity.getSysModuleId());

        if (ObjectUtils.isEmpty(biSysModuleEntity)) {
            return chartVO;
        }
        //数据类型
        Integer dataSource = biSysModuleEntity.getDataSource();
        //指标名称
        String targetNames = biSysModuleEntity.getTargetNames();
        //数据维度(趋势图类型)
        Integer dataDimension = biSysModuleEntity.getDataDimension();
        if (ObjectUtils.isEmpty(dataSource) || StringUtils.isBlank(targetNames) || ObjectUtils.isEmpty(dataDimension)) {
            return chartVO;
        }
        List<String> targetNameList = Arrays.stream(targetNames.split(",")).collect(Collectors.toList());
        //根据类型、数据类型、指标名称、年份查询
        List<LinkedHashMap<String,Object>> list = getCustomByParams(dataDimension, dataSource, targetNameList, year,null);
        if (CollectionUtils.isEmpty(list)) {
            return chartVO;
        }
        LinkedHashMap<String, Object> headMap = new LinkedHashMap<>();
        renewBiDataSourceCustom(list,headMap,dataDimension);
        BiDataSourceCustomEnum[] values = BiDataSourceCustomEnum.values();
        //删除新增固定表头
        for (BiDataSourceCustomEnum value:values) {
            headMap.remove(value.getCode());
        }
        List<String> headList = headMap.values().stream().map(String::valueOf).collect(Collectors.toList());
        List<BiDataSourceCustomGraphicalDTO> dataList = new ArrayList<>();
        for (LinkedHashMap<String,Object> map: list) {
            BiDataSourceCustomGraphicalDTO dto = new BiDataSourceCustomGraphicalDTO();
            //目标值
            BigDecimal targetValue = MathUtil.valueOf(map.get(BiDataSourceCustomEnum.TARGEVALUE.getCode()));
            //指标名称
            String targetName = map.get(BiDataSourceCustomEnum.TARGETNAME.getCode()).toString();
            dto.setTargetValue(targetValue);
            dto.setTargetName(targetName);
            List<BigDecimal> valueList = new ArrayList<>();
            for (String head : headList) {
                BigDecimal value = ObjectUtils.isEmpty(map.get(head)) ? BigDecimal.ZERO : MathUtil.valueOf(map.get(head));
                valueList.add(value);
            }
            dto.setValues(valueList);
            dataList.add(dto);
        }
        chartVO.setXAxis(headList);
        SeriesVO<BiDataSourceCustomGraphicalDTO> seriesVO = new SeriesVO();
        String desc = BiDataSourceCustomTypeEnum.getDesc(dataDimension);
        seriesVO.setName(desc.concat("图"));
        seriesVO.setData(dataList);
        chartVO.setSeries(Arrays.asList(seriesVO));
        return chartVO;
    }

    @Override
    public LinkedHashMap<String,Object> listTableData(BiDataSourceCustomTableDTO dto) {
        LinkedHashMap<String,Object> resultMap = new LinkedHashMap<>();
        BiModuleEntity biModuleEntity = biModuleService.getById(dto.getModuleId());
        if (ObjectUtils.isEmpty(biModuleEntity)) {
            return resultMap;
        }
        BiSysModuleEntity biSysModuleEntity = biSysModuleService.getById(biModuleEntity.getSysModuleId());
        if (ObjectUtils.isEmpty(biSysModuleEntity)) {
            return resultMap;
        }
        //判断是否是固定4个分析报表
        Integer dataType = DataTypeEnum.getCodeByDesc(biSysModuleEntity.getName());
        if (ObjectUtils.isEmpty(dataType)) {
            return resultMap;
        }
        //根据类型、数据类型、指标名称、年份查询
        List<LinkedHashMap<String,Object>> list = this.getCustomByParams(dto.getType(), dataType, null, dto.getYear(),dto.getTargetType());
        if (CollectionUtils.isEmpty(list)) {
            return resultMap;
        }
        List<LinkedHashMap<String,Object>> dataMapList = new ArrayList<>();
        LinkedHashMap<String, Object> headMap = new LinkedHashMap<>();
        renewBiDataSourceCustom(list,headMap,dto.getType());
        BiDataSourceCustomEnum[] values = BiDataSourceCustomEnum.values();
        //删除新增固定表头,保留指标名称、目标值
        for (BiDataSourceCustomEnum value:values) {
            if (!value.getCode().equals(BiDataSourceCustomEnum.TARGETNAME.getCode()) && !value.getCode().equals(BiDataSourceCustomEnum.TARGEVALUE.getCode())) {
                headMap.remove(value.getCode());
            }
        }
        List<String> headList = headMap.keySet().stream().map(String::valueOf).collect(Collectors.toList());
        for (LinkedHashMap<String,Object> map: list) {
            LinkedHashMap<String,Object> dataMap = new LinkedHashMap<>();
            for (String head : headList) {
                String value = ObjectUtils.isEmpty(map.get(head)) ? BigDecimal.ZERO.toString() : String.valueOf(map.get(head));
                dataMap.put(head,value);
            }
            dataMapList.add(dataMap);
        }
        resultMap.put("head",headMap);
        resultMap.put("data",dataMapList);
        return resultMap;
    }

    @Override
    public List<String> listAllTargetNameDropDown() {
        LambdaQueryWrapper<BiDataSourceCustomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiDataSourceCustomEntity::getTargetName);
        List<String> list = this.listObjs(queryWrapper,Object::toString);
        if (CollectionUtils.isNotEmpty(list)) {
            list = list.stream().distinct().collect(Collectors.toList());
        }
        return list;
    }

    @Override
    public List<String> listAllTargetTypeDropDown() {
        LambdaQueryWrapper<BiDataSourceCustomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiDataSourceCustomEntity::getTargetType);
        List<String> list = this.listObjs(queryWrapper,Object::toString);
        if (CollectionUtils.isNotEmpty(list)) {
            list = list.stream().distinct().collect(Collectors.toList());
        }
        return list;
    }

    @Override
    public void updateTargetType(BiTargetTypeDTO dto) {
        LambdaUpdateWrapper<BiDataSourceCustomEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(BiDataSourceCustomEntity::getTargetName,dto.getTargetNameList());
        updateWrapper.set(BiDataSourceCustomEntity::getTargetType,dto.getTargetType());
        this.update(updateWrapper);
    }

    /**
     * 根据类型、数据类型、指标名称、年份查询
     */
    @Override
    public  BiDataSourceCustomEntity getCustomByParam(BiDataSourceCustomEntity entity) {
        LambdaQueryWrapper<BiDataSourceCustomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiDataSourceCustomEntity::getYear,entity.getYear());
        queryWrapper.eq(BiDataSourceCustomEntity::getType,entity.getType());
        queryWrapper.eq(BiDataSourceCustomEntity::getDataType,entity.getDataType());
        queryWrapper.eq(BiDataSourceCustomEntity::getTargetType,entity.getTargetType());
        queryWrapper.eq(BiDataSourceCustomEntity::getTargetName,entity.getTargetName());
        queryWrapper.last("limit 1");
        return  this.getOne(queryWrapper);
    }

    private void renewBiDataSourceCustom(List<LinkedHashMap<String, Object>> list, LinkedHashMap<String, Object> head, Integer type) {
        List<BiDictEntity> dictList = getDictListByType(type);
        List<BiDataSourceCustomDetailEntity> biDataSourceCustomDetailList = getBiDataSourceCustomDetailList(list);

        // 添加固定表头
        addFixedHeaders(head);

        // 添加变动表头
        addDynamicHeaders(head, dictList);

        // 根据主表id赋值
        if (CollectionUtils.isNotEmpty(biDataSourceCustomDetailList)) {
            processDetailList(biDataSourceCustomDetailList, list, head, type);
        }
    }

    private List<BiDictEntity> getDictListByType(Integer type) {
        switch (type) {
            case 2:
                return biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMQUARTER.getType());
            case 3:
                return biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMMONTH.getType());
            default:
                return new ArrayList<>();
        }
    }

    private List<BiDataSourceCustomDetailEntity> getBiDataSourceCustomDetailList(List<LinkedHashMap<String, Object>> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> costIds = list.stream()
                    .map(m -> (String) m.get("id"))
                    .collect(Collectors.toList());
            return biDataSourceCustomDetailService.listByCustomIds(costIds);
        }
        return new ArrayList<>();
    }

    private void addFixedHeaders(LinkedHashMap<String, Object> head) {
        for (BiDataSourceCustomEnum value : BiDataSourceCustomEnum.values()) {
            if (ObjectUtils.isEmpty(head.get(value.getCode()))) {
                head.put(value.getCode(), value.getName());
            }
        }
    }

    private void addDynamicHeaders(LinkedHashMap<String, Object> head, List<BiDictEntity> dictList) {
        if (CollectionUtils.isNotEmpty(dictList)) {
            for (BiDictEntity dictEntity : dictList) {
                if (ObjectUtils.isEmpty(head.get(dictEntity.getValue()))) {
                    head.put(dictEntity.getName(), dictEntity.getName());
                }
            }
        }
    }

    private void processDetailList(List<BiDataSourceCustomDetailEntity> biDataSourceCustomDetailList,
                                   List<LinkedHashMap<String, Object>> list, LinkedHashMap<String, Object> head, Integer type) {
        // 根据自定义类型处理详细信息
        if (BiDataSourceCustomTypeEnum.YEAR.getCode().equals(type) ||
                BiDataSourceCustomTypeEnum.WEEK.getCode().equals(type) ||
                BiDataSourceCustomTypeEnum.DAY.getCode().equals(type)) {
            for (BiDataSourceCustomDetailEntity entity : biDataSourceCustomDetailList) {
                LinkedHashMap<String, Object> map = list.stream()
                        .filter(obj -> entity.getCustomId().equals(obj.get("id")))
                        .findFirst()
                        .orElse(null);
                if (ObjectUtils.isEmpty(map)) continue;

                updateMapForCustomType(map, entity, type, head);
            }
        }

        // 更新每个 map
        updateMapsForEachList(list, head, type, biDataSourceCustomDetailList);
    }

    private void updateMapForCustomType(LinkedHashMap<String, Object> map, BiDataSourceCustomDetailEntity entity, Integer type, LinkedHashMap<String, Object> head) {
        if (BiDataSourceCustomTypeEnum.YEAR.getCode().equals(type)) {
            map.put("实际值", entity.getYear().equals(map.get("year")) ? entity.getValue() : "");
            head.put("实际值", "实际值");
        } else if (BiDataSourceCustomTypeEnum.WEEK.getCode().equals(type)) {
            String week = entity.getWeekBegin().concat("-").concat(entity.getWeekEnd());
            map.put(week, entity.getValue());
            head.put(week, week);
        } else if (BiDataSourceCustomTypeEnum.DAY.getCode().equals(type)) {
            String day = entity.getMonth().toString().concat("月").concat(entity.getDate().toString()).concat("日");
            map.put(day, entity.getValue());
            head.put(day, day);
        }
    }

    private void updateMapsForEachList(List<LinkedHashMap<String, Object>> list, LinkedHashMap<String, Object> head, Integer type, List<BiDataSourceCustomDetailEntity> biDataSourceCustomDetailList) {
        // 获取dictList
        List<BiDictEntity> dictList = getDictListByType(type);

        for (LinkedHashMap<String, Object> map : list) {
            if (BiDataSourceCustomTypeEnum.YEAR.getCode().equals(type) ||
                    BiDataSourceCustomTypeEnum.WEEK.getCode().equals(type) ||
                    BiDataSourceCustomTypeEnum.DAY.getCode().equals(type)) {
                List<String> keyList = head.keySet().stream().collect(Collectors.toList());
                for (String key : keyList) {
                    Object value = map.get(key);
                    map.remove(key);
                    map.put(key, ObjectUtils.isEmpty(value) ? "" : value);
                }
            }

            for (BiDictEntity dictEntity : dictList) {
                if (BiDataSourceCustomTypeEnum.MONTH.getCode().equals(type)) {
                    String value = getValueForMonth(biDataSourceCustomDetailList, map, dictEntity);
                    map.put(dictEntity.getName(), value);
                } else if (BiDataSourceCustomTypeEnum.QUARTER.getCode().equals(type)) {
                    String value = getValueForQuarter(biDataSourceCustomDetailList, map, dictEntity);
                    map.put(dictEntity.getName(), value);
                }
            }

            map.remove("id");
        }
    }

    private String getValueForMonth(List<BiDataSourceCustomDetailEntity> biDataSourceCustomDetailList, LinkedHashMap<String, Object> map, BiDictEntity dictEntity) {
        return biDataSourceCustomDetailList.stream()
                .filter(obj -> obj.getCustomId().equals(map.get("id").toString()) && obj.getMonth().toString().equals(dictEntity.getValue()))
                .map(BiDataSourceCustomDetailEntity::getValue)
                .findFirst()
                .orElse("");
    }

    private String getValueForQuarter(List<BiDataSourceCustomDetailEntity> biDataSourceCustomDetailList, LinkedHashMap<String, Object> map, BiDictEntity dictEntity) {
        return biDataSourceCustomDetailList.stream()
                .filter(obj -> obj.getCustomId().equals(map.get("id").toString()) && obj.getQuarter().toString().equals(dictEntity.getValue()))
                .map(BiDataSourceCustomDetailEntity::getValue)
                .findFirst()
                .orElse("");
    }



    /**
     * 根据类型、数据类型、指标名称、年份查询
     */
    private List<LinkedHashMap<String, Object>> getCustomByParams(Integer type, Integer dataType, List<String> targetNameList, Integer year,String targetType) {
        BiDataSourceCustomEntity entity = new BiDataSourceCustomEntity();
        entity.setType(type);
        entity.setDataType(dataType);
        entity.setYear(year);
        entity.setTargetType(targetType);
        return  this.baseMapper.getCustomByParams(targetNameList,entity);
    }
}
