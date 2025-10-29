package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SampleInitialLedgerDetailDTO;
import com.erp.model.wms.dto.excel.SampleInitialLedgerDetailImportExcelDTO;
import com.erp.model.wms.entity.SampleInitialLedgerEntity;
import com.erp.server.wms.service.SampleInitialLedgerService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 样品期初台账明细导入监听器
 * @author wuhaotian
 * @Classname SampleInitialLedgerDetailExcelListener
 * @Date 2025-09-01
 */
public class SampleInitialLedgerDetailExcelListener extends AnalysisEventListener<SampleInitialLedgerDetailImportExcelDTO> {

    //sku信息
    private Map<String, SkuVO> skuMap;
    //用户
    private List<FindUserDTO> userList;

    /**
     * 错误信息
     */
    private List<SampleInitialLedgerDetailImportExcelDTO> errorList = new ArrayList<>();

    private List<SampleInitialLedgerDetailDTO.AddDTO> addList = new ArrayList<>();

    public SampleInitialLedgerDetailExcelListener(Map<String, SkuVO> skuMap,
                                          List<FindUserDTO> userList) {
        this.skuMap = skuMap;
        this.userList = userList;
    }

    /**
     * 每解析一行数据回调一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author wuhaotian
     * @date 2025-09-01
     */
    @Override
    public void invoke(SampleInitialLedgerDetailImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        
        SampleInitialLedgerDetailDTO.AddDTO addDTO = new SampleInitialLedgerDetailDTO.AddDTO();

        //备注
        addDTO.setRemark(excelDTO.getRemark());

        //sku
        String skuNo = excelDTO.getSkuNo();
        if (StringUtils.isNotBlank(skuNo)) {
            SkuVO skuVO = skuMap.getOrDefault(skuNo, null);
            if (Objects.isNull(skuVO)) {
                errorMsgList.add("SKU不存在");
            } else {
                addDTO.setSkuId(skuVO.getSkuId());
                addDTO.setSkuNo(skuVO.getSkuNo());
                addDTO.setProductName(skuVO.getSkuName());
            }
        }

        //期初数量
        String qtyStr = excelDTO.getQty();
        if (StringUtils.isNotBlank(qtyStr)) {
            try {
                Integer qty = Integer.valueOf(qtyStr);
                addDTO.setQty(qty);
            } catch (NumberFormatException e) {
                errorMsgList.add("期初数量格式错误，必须为正整数");
            }
        }

        if (CollUtil.isNotEmpty(errorMsgList)) {
            excelDTO.setErrorMsg(String.join(",", errorMsgList));
            errorList.add(excelDTO);
        } else {
            addList.add(addDTO);
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
    }

    public List<SampleInitialLedgerDetailImportExcelDTO> getErrorList() {
        return errorList;
    }

    public List<SampleInitialLedgerDetailDTO.AddDTO> getSuccessList() {
        return addList;
    }
}
