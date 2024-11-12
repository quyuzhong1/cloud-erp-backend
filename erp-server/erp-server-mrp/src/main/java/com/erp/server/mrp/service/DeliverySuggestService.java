package com.erp.server.mrp.service;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.DeliverySuggestDTO;

import java.util.List;

/**
 * <p>
 * 发货计划 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-27
 */
public interface DeliverySuggestService extends SuperService<DeliverySuggestEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DeliverySuggestDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    Boolean update(DeliverySuggestDTO.UpdateDTO dto);

    /**
     * 列表查询
     * @author will
     * @date 2024/9/9 14:12
     * @param params
     * @return List<DeliverySuggestEntity>
     */
    List<DeliverySuggestDTO.ListDTO> list(DeliverySuggestDTO.ListParamDTO params);

    /**
     * 发货
     * @param detailId 明细id
     */
    List<DeliverySuggestEntity> listByReplenishmentId(String detailId);
    /**
     * 查询发货建议导出数据
     * @author will
     * @date 2024/10/12 14:56
     * @param dto
     * @return PagingVO<DeliverySuggestionDTO>
     */
    PagingVO<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> listDeliverySuggestion(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);
}
