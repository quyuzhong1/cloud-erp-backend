package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.tms.dto.excel.LogisticsBillCostExcelDTO;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.erp.model.wms.dto.excel.SampleScrapImportExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.service.SampleLedgerService;
import com.erp.server.wms.service.SampleScrapInfoService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jack
 * @Classname SampleScrapAsynExcelListener
 * @Date 2025-08-26
 */
public class SampleScrapAsynExcelListener extends AnalysisEventListener<SampleScrapImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;


    //sku信息
    private Map<String,SkuVO> skuMap ;
    //用户
    private List<FindUserDTO> userList ;
    //部门
    private List<SysDepartmentDTO> deptList ;

    private final SampleLedgerService sampleLedgerService = SpringUtil.getBean(SampleLedgerService.class);

    private final SampleScrapInfoService sampleScrapInfoService = SpringUtil.getBean(SampleScrapInfoService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<SampleScrapImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<SampleScrapImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public SampleScrapAsynExcelListener(String taskId,
                                        String importType,
                                        Integer importCount,
                                        List<SysDepartmentDTO> deptList,
                                        Map<String,SkuVO> skuMap,
                                        List<FindUserDTO> userList) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.skuMap = skuMap;
        this.deptList = deptList;
        this.userList = userList;
    }

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 每解析一行数据回调一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author jack
     * @date 2025-06-27
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SampleScrapImportExcelDTO excelDTO, AnalysisContext analysisContext) {
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
        //报废人
        String scrapUserName = excelDTO.getScrapUserName();
        FindUserDTO findUserDTO = userList.stream().filter(e -> scrapUserName.equals(e.getUserName())).findFirst().orElse(null);
        if(Objects.isNull(findUserDTO)){
            errorMsgList.add("报废人不存在");
        }else {
            excelDTO.setScrapUserId(findUserDTO.getUserId());
            excelDTO.setScrapUserName(findUserDTO.getUserName());
        }

        //报废日期
        String scrapDateStr = excelDTO.getScrapDateStr();
        if(StringUtils.isNotBlank(scrapDateStr)){
            try {
                LocalDate scrapDate = StringUtils.isBlank(scrapDateStr) ? null : LocalDate.parse(scrapDateStr, dateTimeFormatter);
                excelDTO.setScrapDate(scrapDate);
            }catch (Exception e){
                errorMsgList.add("报废时间格式错误、请使用yyyy-MM-dd格式");
            }
        }

        String scrapDeptName = excelDTO.getScrapDeptName();
        SysDepartmentDTO sysDepartmentDTO = deptList.stream().filter(e -> scrapDeptName.equals(e.getName())).findFirst().orElse(null);
        if(Objects.isNull(sysDepartmentDTO)){
            errorMsgList.add("报废部门不存在");
        }else {
            excelDTO.setScrapDeptId(sysDepartmentDTO.getId());
            excelDTO.setScrapDeptName(sysDepartmentDTO.getName());
        }

        //sku
        String skuNo = excelDTO.getSkuNo();
        if(StringUtils.isNotBlank(skuNo)){
            SkuVO skuVO = skuMap.getOrDefault(skuNo, null);
            if(Objects.isNull(skuVO)){
                errorMsgList.add("SKU不存在");
            }else {
                excelDTO.setSkuId(skuVO.getSkuId());
                excelDTO.setSkuNo(skuVO.getSkuNo());
                excelDTO.setProductName(skuVO.getSkuName());
            }
        }

        if(Objects.nonNull(findUserDTO) && StringUtils.isNotBlank(excelDTO.getUseUserName())){
            // 构造查询条件：根据用户ID和SKU列表查询样品台账中的可用数量
            SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
            dto.setUserId(excelDTO.getScrapUserId());
            dto.setSkuNo(excelDTO.getSkuNo());
            dto.setType("scrap");
            List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(dto);
            if(CollUtil.isEmpty(skuAvailableQtyDTOS)){
                errorMsgList.add("样品台账不存在");
            }else {
                SampleLedgerDTO.SkuAvailableQtyDTO skuAvailableQtyDTO = skuAvailableQtyDTOS.stream().filter(e -> e.getSkuNo().equals(skuNo) && e.getUseUserName().equals(excelDTO.getUseUserName())).findFirst().orElse(null);
                if(Objects.isNull(skuAvailableQtyDTO)){
                    errorMsgList.add("样品台账不存在");
                }else {
                    excelDTO.setSampleLedgerId(skuAvailableQtyDTO.getSampleLedgerId());
                }
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT){
            try {
                List<String> errorNoList = errorList.stream().map(SampleScrapImportExcelDTO::getNo).distinct().collect(Collectors.toList());
                List<SampleScrapImportExcelDTO> errorList2 = new ArrayList<>();
                sampleScrapInfoService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
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
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-30 9:51
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()){
            try {
                List<String> errorNoList = errorList.stream().map(SampleScrapImportExcelDTO::getNo).distinct().collect(Collectors.toList());
                List<SampleScrapImportExcelDTO> errorList2 = new ArrayList<>();
                sampleScrapInfoService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
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
