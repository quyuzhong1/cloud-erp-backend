package com.erp.server.oms.service;

import cn.hutool.json.JSONObject;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.RuleOrderApprovalEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.RuleOrderApprovalDTO;

import java.util.List;
import java.util.Map;

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
     *
     * @param dto
     * @return
     * @author Lambda
     * @date: 2023-08-28
     */
    String add(RuleOrderApprovalDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author Lambda
     * @date: 2023-08-28
     */
    Boolean update(RuleOrderApprovalDTO.UpdateDTO dto);

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    PagingVO<RuleOrderApprovalDTO.PagingViewDTO> paging(PagingDTO<RuleOrderApprovalDTO.PagingParamDTO> dto);


    /**
     * 更改启用禁用状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-30 14:15
     */
    Boolean updateStatus(UpdateStateDTO dto);

    /**
     * 订单规则详情
     *
     * @param id
     * @return
     */
    RuleOrderApprovalDTO.ViewDTO view(String id);

    /**
     * 获取到订单审核匹配结果
     * @param jsonObject
     * @return
     */
    RuleOrderApprovalDTO.RuleMatchDTO getRuleOrderMatchResult(Map<String,Object> jsonObject);

}
