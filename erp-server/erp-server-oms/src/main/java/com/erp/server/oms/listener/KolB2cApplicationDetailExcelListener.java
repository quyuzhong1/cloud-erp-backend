package com.erp.server.oms.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.KolB2cApplicationDetailImportExcelDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.service.KolB2cApplicationService;
import jodd.util.StringUtil;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jack
 * @Classname KolB2cApplicationExcelListener
 * @Date 2025-12-08
 */
public class KolB2cApplicationDetailExcelListener extends AnalysisEventListener<KolB2cApplicationDetailImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;


    //sku信息
    private Map<String,SkuVO> skuMap ;
    private Map<String, String> partnerMap;
    private Map<String, String> cfgKolOptionMap;

    private final KolB2cApplicationService kolB2cApplicationService = SpringUtil.getBean(KolB2cApplicationService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<KolB2cApplicationDetailImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<KolB2cApplicationDetailImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public KolB2cApplicationDetailExcelListener(String taskId, String importType, Integer importCount, Map<String, SkuVO> skuMap, Map<String, String> partnerMap, Map<String, String> cfgKolOptionMap) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.skuMap = skuMap;
        this.partnerMap = partnerMap;
        this.cfgKolOptionMap = cfgKolOptionMap;
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
    public void invoke(KolB2cApplicationDetailImportExcelDTO excelDTO, AnalysisContext analysisContext) {
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
        //SKU
        String skuNo = excelDTO.getSkuNo();
        if(StringUtil.isNotBlank(skuNo)){
            SkuVO skuVO = skuMap.get(skuNo);
            if (Objects.isNull(skuVO)) {
                errorMsgList.add("SKU不存在");
            }else {
                excelDTO.setSkuId(skuVO.getSkuId());

                //品牌
                excelDTO.setBrandId(skuVO.getBrandId());
                excelDTO.setBrandName(skuVO.getBrandName());
            }
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
        //预计回片日期
        String planFeedbackDateStr = excelDTO.getPlanFeedbackDateStr();
        if(StringUtils.isNotBlank(planFeedbackDateStr)){
            LocalDate planFeedbackDate = null;
            try {
                planFeedbackDate = LocalDate.parse(planFeedbackDateStr, dateTimeFormatter);
            } catch (Exception e1) {
                try {
                    planFeedbackDate = LocalDate.parse(planFeedbackDateStr, dateTimeFormatter2);
                } catch (Exception e2) {
                    try {
                        planFeedbackDate = LocalDate.parse(planFeedbackDateStr, dateTimeFormatter3);
                    } catch (Exception e3) {
                        errorMsgList.add("预计回片日期格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
                    }
                }
            }
            excelDTO.setPlanFeedbackDate(planFeedbackDate);
        }

        //项目名称
        String projectTagName = excelDTO.getProjectTagName();
        if(StringUtils.isNotBlank(projectTagName)){
            List<String> list = Arrays.asList(projectTagName.split(","));
            List<String> projectTagIdList = new ArrayList<>();
            for (String str : list) {
                String name = str.trim();
                if(!cfgKolOptionMap.containsKey(name)){
                    errorMsgList.add(StrUtil.format("项目名称【{}】不存在", name));
                }else {
                    projectTagIdList.add(cfgKolOptionMap.get(name));
                }
            }
            excelDTO.setProjectTag(String.join(",", projectTagIdList));
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
//                List<String> errorNoList = errorList.stream().map(KolB2cApplicationDetailImportExcelDTO::getNo).distinct().collect(Collectors.toList());
//                List<KolB2cApplicationDetailImportExcelDTO> errorList2 = new ArrayList<>();
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
