package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SampleRecipientDetailDTO;
import com.erp.model.wms.dto.excel.SampleRecipientDetailImportExcelDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 样品领用单明细导入监听器
 * @author wuhaotian
 * @Classname SampleRecipientDetailExcelListener
 * @Date 2025-09-01
 */
public class SampleRecipientDetailExcelListener extends AnalysisEventListener<SampleRecipientDetailImportExcelDTO> {

    //sku信息
    private Map<String, SkuVO> skuMap;
    //用户
    private List<FindUserDTO> userList;

    /**
     * 错误信息
     */
    private List<SampleRecipientDetailImportExcelDTO> errorList = new ArrayList<>();

    private List<SampleRecipientDetailDTO.AddDTO> addList = new ArrayList<>();

    public SampleRecipientDetailExcelListener(Map<String, SkuVO> skuMap,
                                          List<FindUserDTO> userList
                                          ) {
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
    public void invoke(SampleRecipientDetailImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        
        SampleRecipientDetailDTO.AddDTO addDTO = new SampleRecipientDetailDTO.AddDTO();

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
            }
        }

        //领用数量
        String recipientQtyStr = excelDTO.getRecipientQty();
        if (StringUtils.isNotBlank(recipientQtyStr)) {
            try {
                Integer recipientQty = Integer.valueOf(recipientQtyStr);
                if (recipientQty <= 0) {
                    errorMsgList.add("领用数量必须大于0");
                } else {
                    addDTO.setRecipientQty(recipientQty);
                    // 默认已出库数量为0
                    addDTO.setDeliveryQty(0);
                }
            } catch (NumberFormatException e) {
                errorMsgList.add("领用数量格式错误，必须为正整数");
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

    public List<SampleRecipientDetailImportExcelDTO> getErrorList() {
        return errorList;
    }

    public List<SampleRecipientDetailDTO.AddDTO> getSuccessList() {
        return addList;
    }
}
