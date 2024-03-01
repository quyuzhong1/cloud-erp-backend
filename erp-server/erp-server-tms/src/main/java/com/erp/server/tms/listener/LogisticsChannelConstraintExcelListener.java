package com.erp.server.tms.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.entity.ProductPurchaseEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.LogisticsChannelConstraintDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.excel.LogisticsChannelConstraintExcelDTO;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.tms.service.LogisticsChannelService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class LogisticsChannelConstraintExcelListener extends AnalysisEventListener<LogisticsChannelConstraintExcelDTO>{

    private final LogisticsChannelService logisticsChannelService;

    private final SysDictFeign sysDictFeign;
    /**
     * 错误信息
     */
    @Getter
    private List<LogisticsChannelConstraintExcelDTO> errorList = new ArrayList<>();

    /**
     * excel导入数据
     */
    private List<LogisticsChannelConstraintExcelDTO> importList = new ArrayList<>();

    /**
     * 需要新增或导出数据
     */
    @Getter
    private List<LogisticsChannelConstraintDTO.AddOrUpdateDTO> addOrUpdateDTOList = new ArrayList<>();

    public LogisticsChannelConstraintExcelListener(){
        logisticsChannelService = SpringUtil.getBean(LogisticsChannelService.class);
        sysDictFeign = SpringUtil.getBean(SysDictFeign.class);
    }
    @Override
    public void invoke(LogisticsChannelConstraintExcelDTO data, AnalysisContext context) {
        List<String> errorMsgList = FieldValidUtil.fieldValid(data);
        //添加错误数据
        if (!errorMsgList.isEmpty()) {
            data.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(data);
            return;
        }
        data.setChannelCode(data.getChannelCode().trim());
        data.setLogisticsProvider(data.getLogisticsProvider().trim());
        data.setCountryName(data.getCountryName().trim());
        importList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if(CollectionUtils.isEmpty(importList)){
            return;
        }
        if(importList.size()+errorList.size() > 5000){
            throw new ServiceException("每次导入条数不能超过5000行");
        }
        List<String> countryNameList = importList.stream().map(LogisticsChannelConstraintExcelDTO::getCountryName).distinct().collect(Collectors.toList());
        List<String> logisticsProviderList = importList.stream().map(LogisticsChannelConstraintExcelDTO::getLogisticsProvider).distinct().collect(Collectors.toList());
        List<String> channelCodeList = importList.stream().map(LogisticsChannelConstraintExcelDTO::getChannelCode).distinct().collect(Collectors.toList());
        //物流供应商渠道关系
        List<LogisticsChannelDTO.ProvideChannelDTO> provideChannelDTOList = logisticsChannelService.getProvideChannel(channelCodeList,logisticsProviderList);
        Map<String,LogisticsChannelDTO.ProvideChannelDTO> provideChannelMap = provideChannelDTOList.stream().collect(Collectors.toMap(v->v.getLogisticsSupplierName()+v.getChannelCode(), Function.identity(),(v1,v2)->v1));
        Set<String> countryNameSet = provideChannelDTOList.stream().map(LogisticsChannelDTO.ProvideChannelDTO::getLogisticsSupplierName).collect(Collectors.toSet());
        //国家信息
        List<DictCountryEntity> dictCountryEntityList = sysDictFeign.listCountryByNames(countryNameList);
        Map<String,DictCountryEntity> countryEntityMap = dictCountryEntityList.stream().collect(Collectors.toMap(DictCountryEntity::getNameCn,Function.identity(),(v1,v2)->v1));
        Iterator<LogisticsChannelConstraintExcelDTO> iterator = importList.iterator();
        while (iterator.hasNext()){
            LogisticsChannelConstraintExcelDTO data = iterator.next();
            List<String> errorMsgList = new ArrayList<>();
            if(!provideChannelMap.containsKey(data.getLogisticsProvider()+data.getChannelCode())){
                if(countryNameSet.contains(data.getLogisticsProvider())){
                    errorMsgList.add("物流商没有该物流渠道");
                }else{
                    errorMsgList.add("物流商在系统不存在");
                }
            }
            if(!countryEntityMap.containsKey(data.getCountryName())){
                errorMsgList.add("国家名称不存在");
            }
            if(!data.isValid()){
                errorMsgList.add("重量，尺寸，报关至少填写一个大于0的数，不能为空");
            }
            if(!data.isValidSize()){
                errorMsgList.add("超尺寸填写其中一个，其他字段必须填写完整");
            }
            if (!errorMsgList.isEmpty()) {
                data.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(data);
                iterator.remove();
                continue;
            }
            data.setChannelId(provideChannelMap.get(data.getLogisticsProvider()+data.getChannelCode()).getChannelId());
            data.setCountry(countryEntityMap.get(data.getCountryName()).getId());
        }
        if(CollectionUtils.isNotEmpty(importList)){
            Map<String,List<LogisticsChannelConstraintExcelDTO>> importDataMap = importList.stream().collect(Collectors.groupingBy(LogisticsChannelConstraintExcelDTO::getChannelId));
            importDataMap.forEach((key,val)->{
                LogisticsChannelConstraintDTO.AddOrUpdateDTO addOrUpdateDTO = new LogisticsChannelConstraintDTO.AddOrUpdateDTO();
                addOrUpdateDTO.setChannelId(key);
                List<LogisticsChannelConstraintDTO.CommonDTO> commonDTOList = BeanMapper.copyList(val, LogisticsChannelConstraintDTO.CommonDTO.class);
                addOrUpdateDTO.setCommonDTOList(commonDTOList);
                addOrUpdateDTOList.add(addOrUpdateDTO);
            });
        }
    }
}
