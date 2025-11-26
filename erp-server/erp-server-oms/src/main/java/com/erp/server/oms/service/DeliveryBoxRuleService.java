package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.DeliveryBoxRuleDTO;
import com.erp.model.oms.entity.DeliveryBoxRuleEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

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
    /**
     * 删除
     * @author wtr
     * @date: 2025-11-24
     * @param dto
     * @return
     */
    List<BatchResultDTO> deleteByIds(BaseIdsDTO.IdsDTO dto);

    Boolean importFile(BaseDTO.ImportDTO dto);

    /**
     * 根据skuNo查询箱规
     */
    List<DeliveryBoxRuleDTO.ViewDTO> listBoxRuleBySkuNo(List<String> skuNoList);
}
