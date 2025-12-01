package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.DeliveryBoxRuleDTO;
import com.erp.model.oms.entity.DeliveryBoxRuleEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
 */
public interface DeliveryBoxRuleService extends SuperService<DeliveryBoxRuleEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-11-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DeliveryBoxRuleDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-11-24
    * @param dto
    * @return
    */
    Boolean update(DeliveryBoxRuleDTO.UpdateDTO dto);

    /**
     * 详情
     * @author wtr
     * @date: 2025-11-24
     * @param id
     * @return
     */
    DeliveryBoxRuleDTO.ViewDTO view(String id);

    /**
     * 列表查询
     * @author wtr
     * @date: 2025-11-24
     * @param dto
     * @return
     */
    PagingVO<DeliveryBoxRuleDTO.ListDTO> paging(PagingDTO<DeliveryBoxRuleDTO.PagingParamDTO> dto);


    Boolean importFile(BaseDTO.ImportDTO dto,HttpServletResponse response);

    void importDeliveryBoxRule(BaseDTO.ImportDTO dto);

    void exportList(DeliveryBoxRuleDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 根据sku查询箱规
     */
    List<DeliveryBoxRuleDTO.ListBoxRuleBySkuDTO> listBoxRuleBySku(List<DeliveryBoxRuleDTO.SkuDTO> skuList);

    void handleImportSuccessList(List<DeliveryBoxRuleDTO.ImportDTO> successList);
}
