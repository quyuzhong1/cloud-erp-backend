package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.ExcelUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCustomSearchDTO;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.dmp.entity.BiDataSourceCustomDetailEntity;
import com.erp.model.dmp.entity.BiDataSourceCustomEntity;
import com.erp.server.bi.enums.BiDataSourceCustomEnum;
import com.erp.server.bi.enums.BiDataSourceCustomTypeEnum;
import com.erp.server.bi.enums.DictEnum;
import com.erp.server.bi.mapper.BiDataSourceCustomMapper;
import com.erp.server.bi.service.BiDataSourceCustomDetailService;
import com.erp.server.bi.service.BiDataSourceCustomService;
import com.erp.server.bi.service.BiDictService;
import com.erp.server.bi.service.DmpOrderInfoService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
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
        renewBiDataSourceCustom(pageData.getRecords(),dto.getParams().getType());
        return new PagingVO(pageData);
    }

    @Override
    public void exportExcel(BiDataSourceCustomSearchDTO dto, HttpServletResponse response,Integer type) {
        //查询所有数据
        List<LinkedHashMap<String,Object>>  list = baseMapper.getAllBiDataSourceCustom(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<LinkedHashMap<String, Object>> customList = renewBiDataSourceCustom(list,type);
        if (CollectionUtils.isEmpty(customList)) {
            return;
        }
        List<String> heads = new ArrayList<>();		//表头信息
        String head = "自助数据表";
        String fileName = dmpOrderInfoService.getFileName("自助数据表")+ ".xlsx";
        BiDataSourceCustomEnum[] values = BiDataSourceCustomEnum.values();
        List<String> enumList = Arrays.stream(values).map(BiDataSourceCustomEnum::getName).collect(Collectors.toList());
        heads.addAll(enumList);
        if (BiDataSourceCustomTypeEnum.QUARTER.getCode().equals(type)) {
            //查询成本字典数据
            List<BiDictEntity> dictList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMQUARTER.getType());
            if (CollectionUtils.isNotEmpty(dictList)) {
                List<String> nameList = dictList.stream().map(BiDictEntity::getName).collect(Collectors.toList());
                heads.addAll(nameList);
            }
        }
        if (BiDataSourceCustomTypeEnum.MONTH.getCode().equals(type)) {
            //查询成本字典数据
            List<BiDictEntity> dictList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMQUARTER.getType());
            if (CollectionUtils.isNotEmpty(dictList)) {
                List<String> nameList = dictList.stream().map(BiDictEntity::getName).collect(Collectors.toList());
                heads.addAll(nameList);
            }
        }
        ExcelUtil.easyUtil(heads,head,list,fileName);
    }

    @Override
    public void importExcel(MultipartFile excelFile, HttpServletResponse response, Integer importType) {

    }


    /**
     * 返回字段处理
     */
    private List<LinkedHashMap<String,Object>> renewBiDataSourceCustom(List<LinkedHashMap<String,Object>> list,Integer type) {
        if (CollectionUtils.isEmpty(list))  {
            return list;
        }
        List<BiDictEntity> dictList = new ArrayList<>();
        switch (type) {
            case 2:
                //查询成本字典数据
                dictList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMQUARTER.getType());
            case 3:
                //查询成本字典数据
                dictList = biDictService.listEntityByType(DictEnum.DATASOURCECUSTOMMONTH.getType());
            default:
                ;
        }

        if (CollectionUtils.isEmpty(dictList))  {
            return list;
        }
        List<String> costIds = list.stream().map((Map m) -> (String) m.get("id")).collect(Collectors.toList());
        List<BiDataSourceCustomDetailEntity> biDataSourceCustomDetailList= biDataSourceCustomDetailService.listByCustomIds(costIds);
        if (CollectionUtils.isEmpty(biDataSourceCustomDetailList)) {
            return list;
        }
        for (LinkedHashMap<String,Object> map: list) {
            BiDataSourceCustomEnum[] values = BiDataSourceCustomEnum.values();
            for (BiDataSourceCustomEnum value:values) {
                map.put(value.getName(),map.get(value.getCode()));
            }
            if (BiDataSourceCustomTypeEnum.YEAR.getCode().equals(type) || BiDataSourceCustomTypeEnum.WEEK.getCode().equals(type) || BiDataSourceCustomTypeEnum.DAY.getCode().equals(type)) {
                for (BiDataSourceCustomDetailEntity entity: biDataSourceCustomDetailList) {
                    if (BiDataSourceCustomTypeEnum.YEAR.getCode().equals(type)) {
                        map.put(entity.getYear().toString().concat("年"),map.get(entity.getValue()));
                        continue;
                    }
                    if (BiDataSourceCustomTypeEnum.WEEK.getCode().equals(type)) {
                        map.put(entity.getWeekBegin().concat("-").concat(entity.getWeekEnd()),map.get(entity.getValue()));
                        continue;
                    }
                    if (BiDataSourceCustomTypeEnum.DAY.getCode().equals(type)) {
                        map.put(entity.getMonth().toString().concat("月").concat(entity.getDate().toString()).concat("日"),map.get(entity.getValue()));
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
                        continue;
                    }
                    if (BiDataSourceCustomTypeEnum.QUARTER.getCode().equals(type)) {
                        String value = biDataSourceCustomDetailList.stream().filter(obj -> obj.getCustomId().equals(map.get("id")) && obj.getQuarter().equals(dcit.getValue()))
                                .map(BiDataSourceCustomDetailEntity::getValue).findFirst().orElse(null);
                        map.put(dcit.getName(), value);
                        continue;
                    }
                }
            }
        }
        return list;
    }

}
