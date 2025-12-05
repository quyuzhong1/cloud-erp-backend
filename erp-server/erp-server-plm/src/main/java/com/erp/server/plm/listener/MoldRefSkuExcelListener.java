package com.erp.server.plm.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.excel.MoldRefSkuImportExcelDTO;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.MoldRefSkuService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author jack
 * @Classname MoldRefSkuExcelListener
 * @Date 2025-10-14
 */
public class MoldRefSkuExcelListener extends AnalysisEventListener<MoldRefSkuImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;
    //模具
    private Map<String, MoldInfoEntity> moldInfoMap ;
    //SKU
    private Map<String, SkuVO> skuMap ;

    private Set<String> moldCodeSkuNoSet ;

    private final MoldRefSkuService moldRefSkuService = SpringUtil.getBean(MoldRefSkuService.class);

    private final DocNoGenHelper docNoGenHelper = SpringUtil.getBean(DocNoGenHelper.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<MoldRefSkuImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<MoldRefSkuImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public MoldRefSkuExcelListener(String taskId,
                                   String importType,
                                   Integer importCount,
                                   Set<String> moldCodeSkuNoSet ,
                                   Map<String, SkuVO> skuMap,
                                   Map<String, MoldInfoEntity> moldInfoMap) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.moldCodeSkuNoSet = moldCodeSkuNoSet;
        this.skuMap = skuMap;
        this.moldInfoMap = moldInfoMap;
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
    public void invoke(MoldRefSkuImportExcelDTO excelDTO, AnalysisContext analysisContext) {
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

        String moldCode = excelDTO.getMoldCode();
        if(StringUtils.isNotBlank(moldCode)){
            MoldInfoEntity moldInfo = moldInfoMap.getOrDefault(moldCode, null);
            if(Objects.isNull(moldInfo)){
                errorMsgList.add("模具不存在");
            }else {
                excelDTO.setMoldId(moldInfo.getId());
                excelDTO.setMoldName(moldInfo.getName());
            }
        }

        String skuNo = excelDTO.getSkuNo();
        if(StringUtils.isNotBlank(skuNo)){
            SkuVO skuVO = skuMap.getOrDefault(skuNo, null);
            if(Objects.isNull(skuVO)){
                errorMsgList.add("SKU不存在");
            }else {
                excelDTO.setSkuId(skuVO.getSkuId());
                excelDTO.setProductName(skuVO.getSkuName());
            }
        }

        if(StringUtils.isNotBlank(moldCode) && StringUtils.isNotBlank(skuNo)){
            String key = moldCode + ":" + skuNo;
            boolean contains = moldCodeSkuNoSet.contains(key);
            if(contains){
                errorMsgList.add(StrUtil.format("模具编号【{}】和SKU【{}】的关联记录已存在，请勿重复添加",moldCode,skuNo));
            }else {
                moldCodeSkuNoSet.add(key);
            }
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_MOLD_REF_SKU);
        excelDTO.setCode(code);

        successList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT){
            try {
                List<MoldRefSkuImportExcelDTO> errorList2 = new ArrayList<>();
                moldRefSkuService.handleImportSuccessList(successList, errorList2,importType);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }


    /**
     * 数据全部解析完成后执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()){
            try {
                List<MoldRefSkuImportExcelDTO> errorList2 = new ArrayList<>();
                moldRefSkuService.handleImportSuccessList(successList, errorList2,importType);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
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
