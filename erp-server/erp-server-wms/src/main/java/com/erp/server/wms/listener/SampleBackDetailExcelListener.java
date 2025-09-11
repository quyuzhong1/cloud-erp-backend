package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.core.enums.ApiError;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.dto.SampleBackDetailDTO;
import com.erp.model.wms.dto.excel.SampleBackDetailImportExcelDTO;
import com.erp.model.wms.enums.SampleLedgerTypeEnum;
import com.erp.server.wms.service.SampleLedgerService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 样品退回详情Excel监听器
 * @author wuhaotian
 * @Classname SampleBackDetailExcelListener
 * @Date 2025-08-21
 */
public class SampleBackDetailExcelListener extends AnalysisEventListener<SampleBackDetailImportExcelDTO> {

    private String backUserId;
    //sku信息
    private Map<String,SkuVO> skuMap ;

    private SampleLedgerService sampleLedgerService;
    /**
     * 错误信息
     */
    private List<SampleBackDetailImportExcelDTO> errorList = new ArrayList<>();

    private List<SampleBackDetailDTO.AddDTO> addList = new ArrayList<>();

    public SampleBackDetailExcelListener(SampleLedgerService sampleLedgerService,
                                       Map<String,SkuVO> skuMap,
                                       String backUserId) {
        this.sampleLedgerService = sampleLedgerService;
        this.skuMap = skuMap;
        this.backUserId = backUserId;
    }

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 每解析一行数据回调一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author wuhaotian
     * @date 2025-08-21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SampleBackDetailImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        SampleBackDetailDTO.AddDTO addDTO = new SampleBackDetailDTO.AddDTO();

        //备注
        addDTO.setRemark(excelDTO.getDetailRemark());

        //sku
        String skuNo = excelDTO.getSkuNo();
        if(StringUtils.isNotBlank(skuNo)){
            SkuVO skuVO = skuMap.getOrDefault(skuNo, null);
            if(Objects.isNull(skuVO)){
                errorMsgList.add("SKU不存在");
            }else {
                addDTO.setSkuId(skuVO.getSkuId());
                addDTO.setSkuNo(skuVO.getSkuNo());
                addDTO.setProductName(skuVO.getSkuName());
            }
        }

        //退回数量
        String qty = excelDTO.getQty();
        if(StringUtils.isNotBlank(qty)){
            addDTO.setQty(Integer.parseInt(qty));
        }

        //使用方
        String useUserName = excelDTO.getUseUserName();
        // 构造查询条件：根据用户ID和SKU列表查询样品台账中的可用数量
        if(StringUtils.isNotBlank(addDTO.getSkuId()) && StringUtils.isNotBlank(useUserName)){
            SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
            dto.setUserId(backUserId);
            dto.setSkuIds(Arrays.asList(addDTO.getSkuId()));
            dto.setType(SampleLedgerTypeEnum.BACK.getCode());
            List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(dto);
            if(CollUtil.isEmpty(skuAvailableQtyDTOS)){
                errorMsgList.add(ApiError.ERROR_SAMPLE_LEDGER_NOT_EXIST.msg);
            }else {
                SampleLedgerDTO.SkuAvailableQtyDTO skuAvailableQtyDTO = skuAvailableQtyDTOS.stream().filter(e -> e.getSkuId().equals(addDTO.getSkuId()) && e.getUseUserName().equals(useUserName)).findFirst().orElse(null);
                if(Objects.isNull(skuAvailableQtyDTO)){
                    errorMsgList.add(ApiError.ERROR_SAMPLE_LEDGER_NOT_EXIST.msg);
                }else {
                    addDTO.setUseUserId(skuAvailableQtyDTO.getUseUserId());
                    addDTO.setUseUserName(skuAvailableQtyDTO.getUseUserName());
                    addDTO.setSampleLedgerId(skuAvailableQtyDTO.getSampleLedgerId());
                    // 校验退回数量不能超过可用数量
                    if(addDTO.getQty() != null && addDTO.getQty() > skuAvailableQtyDTO.getAvailableQty()){
                        errorMsgList.add("退回数量不能超过可用数量：" + skuAvailableQtyDTO.getAvailableQty());
                    }
                }
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        addList.add(addDTO);
    }


    /**
     * 数据全部解析完成后执行
     *
     * @param analysisContext
     * @return void
     * @author wuhaotian
     * @date 2025-08-21
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<SampleBackDetailImportExcelDTO> getErrorList() {
        return errorList;
    }

    public List<SampleBackDetailDTO.AddDTO> getSuccessList() {
        return addList;
    }

}
