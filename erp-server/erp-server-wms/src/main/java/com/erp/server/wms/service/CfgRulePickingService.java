package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.entity.CfgRulePickingEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 拣货规则表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
public interface CfgRulePickingService extends SuperService<CfgRulePickingEntity> {

    PagingVO<CfgRulePickingDTO.PagingView> paging(PagingDTO<CfgRulePickingDTO.PagingParam> dto);

    void add(CfgRulePickingDTO.Add dto);

    void update(CfgRulePickingDTO.Add dto, String id);

    CfgRulePickingDTO.View view(String id);

    void delete(List<String> ids);

    void updateStatus(UpdateStateDTO.BatchUpdateDTO dto);
}
