package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.RuleOrderApprovalEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.RuleOrderApprovalDTO;

/**
 * <p>
 * 订单审核规则 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
public interface RuleOrderApprovalService extends SuperService<RuleOrderApprovalEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    String add(RuleOrderApprovalDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    Boolean update(RuleOrderApprovalDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<RuleOrderApprovalDTO.PagingViewDTO> paging(PagingDTO<RuleOrderApprovalDTO.PagingParamDTO> dto);

    
    /**
     * 更改启用禁用状态
     * @author yl
     * @date 2023-08-30 14:15
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateStatus(UpdateStateDTO dto);
}
