package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.core.enums.ApiError;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.dto.SampleScrapDetailDTO;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.erp.model.wms.dto.excel.SampleScrapDetailImportExcelDTO;
import com.erp.model.wms.enums.SampleLedgerTypeEnum;
import com.erp.server.wms.service.SampleLedgerService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author jack
 * @Classname SampleScrapDetailExcelListener
 * @Date 2025-08-26
 */
public class SampleScrapDetailExcelListener extends AnalysisEventListener<SampleScrapDetailImportExcelDTO> {

    //sku信息
    private Map<String,SkuVO> skuMap ;
    //用户
    private List<FindUserDTO> userList ;

    private SampleLedgerService sampleLedgerService;
    /**
     * 错误信息
     */
    private List<SampleScrapDetailImportExcelDTO> errorList = new ArrayList<>();

    private List<SampleScrapDetailDTO.AddDTO> addList = new ArrayList<>();

    public SampleScrapDetailExcelListener(SampleLedgerService sampleLedgerService,
                                          Map<String,SkuVO> skuMap,
                                          List<FindUserDTO> userList) {
        this.sampleLedgerService = sampleLedgerService;
        this.skuMap = skuMap;
        this.userList = userList;
    }

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
    public void invoke(SampleScrapDetailImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        SampleScrapDetailDTO.AddDTO addDTO = new SampleScrapDetailDTO.AddDTO();
        //报废人
        String scrapUserName = excelDTO.getScrapUserName();
        FindUserDTO findUserDTO = userList.stream().filter(e -> scrapUserName.equals(e.getUserName())).findFirst().orElse(null);
        if(Objects.isNull(findUserDTO)){
            errorMsgList.add("报废人不存在");
        }

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

        //报废数量
        String scrapQty = excelDTO.getScrapQty();
        if(StringUtils.isNotBlank(scrapQty)){
            addDTO.setScrapQty(Integer.parseInt(scrapQty));
        }

        //使用方
        String useUserName = excelDTO.getUseUserName();
        // 构造查询条件：根据用户ID和SKU列表查询样品台账中的可用数量
        if(StringUtils.isNotBlank(addDTO.getSkuId()) && Objects.nonNull(findUserDTO) && StringUtils.isNotBlank(useUserName)){
            SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
            dto.setUserId(findUserDTO.getUserId());
            dto.setSkuIds(Arrays.asList(addDTO.getSkuId()));
            dto.setType(SampleLedgerTypeEnum.SCRAP.getCode());
            List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(dto);
            if(CollUtil.isEmpty(skuAvailableQtyDTOS)){
                errorMsgList.add(ApiError.ERROR_SAMPLE_LEDGER_NOT_EXIST.msg);
            }else {
                SampleLedgerDTO.SkuAvailableQtyDTO skuAvailableQtyDTO = skuAvailableQtyDTOS.stream().filter(e -> e.getSkuId().equals(addDTO.getSkuId()) && e.getUseUserName().equals(useUserName)).findFirst().orElse(null);
                if(Objects.isNull(skuAvailableQtyDTO)){
                    errorMsgList.add(ApiError.ERROR_SAMPLE_LEDGER_NOT_EXIST.msg);
                }else {
                    addDTO.setSampleLedgerId(skuAvailableQtyDTO.getSampleLedgerId());
                    addDTO.setAvailableQty(skuAvailableQtyDTO.getAvailableQty());
                    addDTO.setUseUserId(skuAvailableQtyDTO.getUseUserId());
                    addDTO.setUseUserName(skuAvailableQtyDTO.getUseUserName());
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
     * @author yl
     * @date 2023-03-30 9:51
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<SampleScrapDetailImportExcelDTO> getErrorList() {
        return errorList;
    }

    public List<SampleScrapDetailDTO.AddDTO> getSuccessList() {
        return addList;
    }

}
