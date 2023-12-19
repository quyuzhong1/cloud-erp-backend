package com.erp.server.oms.service;
import cn.hutool.json.JSONObject;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.RuleOrderApprovalDTO;
import com.erp.model.oms.entity.RuleLogisticsEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.RuleLogisticsDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流规则表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
public interface RuleLogisticsService extends SuperService<RuleLogisticsEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    String add(RuleLogisticsDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    Boolean update(RuleLogisticsDTO.UpdateDTO dto);


    /**
     * 物流规则分页
     * @author yl
     * @date 2023-09-01 9:07
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.RuleLogisticsDTO.PagingViewDTO>
     */
    PagingVO<RuleLogisticsDTO.PagingViewDTO> paging(PagingDTO<RuleLogisticsDTO.PagingParamDTO> dto);

    /**
     * 物流规则详情
     * @param id
     * @return
     */
    RuleLogisticsDTO.ViewDTO view(String id);

    /**
     * 更改启用禁用状态
     * @author yl
     * @date 2023-08-30 14:15
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateStatus(UpdateStateDTO dto);

    /**
     * 获取到物流匹配结果
     * @param map
     * @return
     */
    RuleLogisticsDTO.RuleMatchResultDTO getRuleOrderMatchResult(Map<String,Object> map);

}
