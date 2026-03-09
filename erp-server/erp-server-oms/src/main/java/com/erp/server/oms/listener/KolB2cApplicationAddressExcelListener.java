package com.erp.server.oms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.KolB2cApplicationAddressImportExcelDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.enums.DictValueEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.service.KolB2cApplicationService;
import jodd.util.StringUtil;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jack
 * @Classname KolB2cApplicationExcelListener
 * @Date 2025-12-08
 */
public class KolB2cApplicationAddressExcelListener extends AnalysisEventListener<KolB2cApplicationAddressImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    private Map<String,String> partnerMap ;
    private Map<String,String> dictCountryMap ;
    private Map<String,String> provinceMap;
    private Map<String,String> cityMap;
    private Map<String,String> districtMap ;


    private final KolB2cApplicationService kolB2cApplicationService = SpringUtil.getBean(KolB2cApplicationService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<KolB2cApplicationAddressImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<KolB2cApplicationAddressImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public KolB2cApplicationAddressExcelListener(String taskId, String importType, Integer importCount, Map<String, String> partnerMap, Map<String, String> dictCountryMap,
                                                 Map<String,String> provinceMap,
                                                 Map<String,String> cityMap,
                                                 Map<String,String> districtMap) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.partnerMap = partnerMap;
        this.dictCountryMap = dictCountryMap;
        this.provinceMap = provinceMap;
        this.cityMap = cityMap;
        this.districtMap = districtMap;
    }

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy/M/d");
    private final DateTimeFormatter dateTimeFormatter3 = DateTimeFormatter.ofPattern("yyyy/MM/dd");


    /**
     * 每解析一行数据回调一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author jack
     * @date 2025-08-26
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(KolB2cApplicationAddressImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        count += 1;
        //已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount){
            return;
        }

        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        //达人昵称
        String nickname = excelDTO.getNickname();
        if(StringUtil.isNotBlank(nickname)){
            if(!partnerMap.containsKey(nickname)){
                errorMsgList.add("达人昵称【"+nickname+"】不存在");
            }else {
                excelDTO.setPartnerId(partnerMap.get(nickname));
            }
        }

        //国家
        String countryName = excelDTO.getCountryName();
        if(StringUtil.isNotBlank(countryName)){
            if(!dictCountryMap.containsKey(countryName)){
                errorMsgList.add("国家【"+countryName+"】不存在");
            }else {
                excelDTO.setCountryId(dictCountryMap.get(countryName));
            }
        }
        //省
        String provinceId = provinceMap.getOrDefault(excelDTO.getProvince(), "");
        if(StringUtils.isNotBlank(provinceId)){
            excelDTO.setProvinceId(provinceId);
        }else {
            errorMsgList.add("省不存在");
        }
        //市
        String cityId = cityMap.getOrDefault(excelDTO.getCity(), "");
        if(StringUtils.isNotBlank(cityId)){
            excelDTO.setCityId(cityId);
        }else {
            errorMsgList.add("市不存在");
        }
        //区域
        String districtId = districtMap.getOrDefault(excelDTO.getDistrict(), "");
        if(StringUtils.isNotBlank(districtId)){
            excelDTO.setDistrictId(districtId);
        }else {
            if(DictValueEnum.CN.getCode().equals(excelDTO.getCountryId())){
                errorMsgList.add("区域不存在");
            }
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
    }


    /**
     * 数据全部解析完成后执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
//        if (!successList.isEmpty()){
//            try {
//                List<String> errorNoList = errorList.stream().map(KolB2cApplicationAddressImportExcelDTO::getNo).distinct().collect(Collectors.toList());
//                List<KolB2cApplicationAddressImportExcelDTO> errorList2 = new ArrayList<>();
////                kolB2cApplicationService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
//                errorList.addAll(errorList2);
//            }catch (Exception e){
//                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
//                errorList.addAll(successList);
//            }
//            successList.clear();
//            updateTask(count);
//        }
    }

    private void updateTask(Integer count){
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }

}
