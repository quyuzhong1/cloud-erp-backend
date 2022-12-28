package com.erp.server.bi.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCustomGraphicalDTO;
import com.erp.model.bi.dto.BiDataSourceCustomSearchDTO;
import com.erp.model.bi.dto.BiDataSourceCustomTableDTO;
import com.erp.model.bi.entity.BiDataSourceCustomDetailEntity;
import com.erp.model.bi.entity.BiDataSourceCustomEntity;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.bi.entity.BiSysModuleEntity;
import com.erp.model.bi.vo.ChartVO;
import com.erp.model.bi.vo.SeriesVO;
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

    @Resource
    private BiSysModuleService biSysModuleService;


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
        ExcelUtil.easyUtilStr(headList,head,list,fileName,response);
    }

    @Override
    @Transactional
    public void importExcel(MultipartFile excelFile, HttpServletResponse response, Integer importType) {
        //季度数据
        List<BiDictEntity> quarterList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMQUARTER.getType());
        //月份数据
        List<BiDictEntity> monthList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMMONTH.getType());
        BiDataSourceCustomExcelListener excelListenerUtil = new BiDataSourceCustomExcelListener(this,biDataSourceCustomDetailService,quarterList,monthList,importType);
        try {
            EasyExcel.read(excelFile.getInputStream(), excelListenerUtil).sheet(0).doRead();
            List<Map<Integer, String>> list = excelListenerUtil.getDateList();
            if (CollectionUtils.isEmpty(list) || list.size() == 0) {
                return;
            }
            List<String> headList = excelListenerUtil.getHead();
            String head = "自助数据表";
            String fileName = dmpOrderInfoService.getFileName("自助数据表导出")+ ".xlsx";
            ExcelUtil.easyUtil(headList,head,list,fileName, response);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> listTargetNameByDataSource(Integer dataType) {
        LambdaQueryWrapper<BiDataSourceCustomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiDataSourceCustomEntity::getDataType,dataType);
        queryWrapper.select(BiDataSourceCustomEntity::getTargetName);
        List<String> strings = this.listObjs(queryWrapper, Object::toString);
        if (CollectionUtils.isNotEmpty(strings)) {
            strings = strings.stream().distinct().collect(Collectors.toList());
        }
        return strings;
    }

    @Override
    public List<String> listTargetType(BiDataSourceCustomTableDTO dto) {
        BiSysModuleEntity biSysModuleEntity = biSysModuleService.getByName(dto.getModuleName());
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
    public ChartVO listGraphicalData(String moduleName, Integer year) {
        BiSysModuleEntity biSysModuleEntity = biSysModuleService.getByName(moduleName);
        ChartVO chartVO = new ChartVO();
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
        SeriesVO seriesVO = new SeriesVO();
        String desc = BiDataSourceCustomTypeEnum.getDesc(dataDimension);
        seriesVO.setName(desc.concat("图"));
        seriesVO.setData(dataList);
        chartVO.setSeries(Arrays.asList(seriesVO));
        return chartVO;
    }

    @Override
    public ChartVO listTableData(BiDataSourceCustomTableDTO dto) {
        BiSysModuleEntity biSysModuleEntity = biSysModuleService.getByName(dto.getModuleName());
        ChartVO chartVO = new ChartVO();
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
        List<LinkedHashMap<String,Object>> list = this.getCustomByParams(dto.getType(), dataSource, targetNameList, dto.getYear(),dto.getTargetType());
        if (CollectionUtils.isEmpty(list)) {
            return chartVO;
        }
        LinkedHashMap<String, Object> headMap = new LinkedHashMap<>();
        renewBiDataSourceCustom(list,headMap,dataDimension);
        BiDataSourceCustomEnum[] values = BiDataSourceCustomEnum.values();
        //删除新增固定表头,保留指标名称、目标值
        for (BiDataSourceCustomEnum value:values) {
            if (!value.getCode().equals(BiDataSourceCustomEnum.TARGETNAME.getCode()) && !value.getCode().equals(BiDataSourceCustomEnum.TARGEVALUE.getCode())) {
                headMap.remove(value.getCode());
            }
        }
        List<String> headList = headMap.values().stream().map(String::valueOf).collect(Collectors.toList());

        for (LinkedHashMap<String,Object> map: list) {
            List<String> valueList = new ArrayList<>();
            for (String head : headList) {
                String value = ObjectUtils.isEmpty(map.get(head)) ? BigDecimal.ZERO.toString() : String.valueOf(map.get(head));
                valueList.add(value);
            }
        }
        chartVO.setXAxis(headList);
        SeriesVO seriesVO = new SeriesVO();
        return null;
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
