package com.erp.server.oms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.oms.entity.CfgRuleOrderHandleEntity;
import com.erp.model.tms.vo.request.LogisticsOrderRuleVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateOutboundReq;

import java.util.Map;

/**
 * <p>
 * 订单处理规则表 服务类
 * </p>
 *
 * @author will
 * @since 2024-05-09
 */
public interface CfgRuleOrderHandleService extends SuperService<CfgRuleOrderHandleEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-05-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleOrderHandleDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-05-09
    * @param dto
    * @return
    */
    Boolean update(CfgRuleOrderHandleDTO.UpdateDTO dto);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/5/9 11:34
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<CfgRuleOrderHandleDTO.ListDTO> paging(PagingDTO<CfgRuleOrderHandleDTO.PagingParamDTO> dto);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2024/5/9 11:43
     * @param id
     * @return ViewDTO
     */
    CfgRuleOrderHandleDTO.ViewDTO view(String id);
    /**
     * @description: 更新状态
     * @author Will
     * @date: 2024/5/9 11:52
     * @param dto
     * @return Boolean
     */
    Boolean updateStatus(UpdateStateDTO dto);


    /**
     * @param jsonObject
     * @return RuleMatchDTO
     * @description: 返回规则匹配结果
     * @author Will
     * @date: 2024/5/9 16:23
     */
    CfgRuleOrderHandleDTO.RuleMatchDTO getRuleOrderHandleMatchResult(Map<String, Object> jsonObject);

    LogisticsOrderVO handleRuleOrderLogistic(LogisticsOrderRuleVO logisticsOrderRuleVO);

    ThirdWarehouseCreateOutboundReq handleRuleOrderThirdWarehouse(ThirdWarehouseCreateOutboundReq createOutboundReq, Map<String, Object> map);

    SoMultiChannelDTO.ReceiverInfo handleRuleOrderSoMultiChannel(SoMultiChannelDTO.ReceiverInfo receiverInfo, Map<String, Object> map);
}
