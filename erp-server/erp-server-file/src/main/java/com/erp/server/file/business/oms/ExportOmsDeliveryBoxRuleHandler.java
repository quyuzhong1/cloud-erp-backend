package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.DeliveryBoxRuleDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_DELIVERY_BOX_RULE;

/**
 * @Author: wtr
 * @Date: 2025/11/27 14:31
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
@Slf4j
public class ExportOmsDeliveryBoxRuleHandler extends AbstractPageFileEventHandler<DeliveryBoxRuleDTO.ListDTO, DeliveryBoxRuleDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected PagingVO<DeliveryBoxRuleDTO.ListDTO> getPageData(PagingDTO<DeliveryBoxRuleDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportDeliveryBoxRule(dto);
    }

    @Override
    protected List<DeliveryBoxRuleDTO.ListDTO> getData(FileTask fileTask) {
        DeliveryBoxRuleDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DeliveryBoxRuleDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/oms/deliveryBoxRule.xlsx";
    }
    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_DELIVERY_BOX_RULE;
    }

}