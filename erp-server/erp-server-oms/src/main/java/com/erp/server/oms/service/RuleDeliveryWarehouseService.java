package com.erp.server.oms.service;
import cn.hutool.json.JSONObject;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.RuleOrderApprovalDTO;
import com.erp.model.oms.entity.RuleDeliveryWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.RuleDeliveryWarehouseDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 发货仓库规则表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
public interface RuleDeliveryWarehouseService extends SuperService<RuleDeliveryWarehouseEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    String add(RuleDeliveryWarehouseDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    Boolean update(RuleDeliveryWarehouseDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author yl
     * @date 2023-08-31 17:50
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.RuleDeliveryWarehouseDTO.PagingViewDTO>
     */
    PagingVO<RuleDeliveryWarehouseDTO.PagingViewDTO> paging(PagingDTO<RuleDeliveryWarehouseDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @author yl
     * @date 2023-08-31 18:31
     * @param id
     * @return com.erp.model.oms.dto.RuleDeliveryWarehouseDTO.ViewDTO
     */
    RuleDeliveryWarehouseDTO.ViewDTO view(String id);

    /**
     * 更改启用禁用状态
     * @author yl
     * @date 2023-08-30 14:15
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateStatus(UpdateStateDTO dto);

    /**
     * 获取到订单审核匹配结果
     * @param map
     * @return
     */
    RuleDeliveryWarehouseDTO.RuleMatchResultDTO getRuleOrderMatchResult(Map<String,Object> map);
}
