package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.CfgRuleDeclareEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.CfgRuleDeclareDTO;

import java.util.HashMap;

/**
 * <p>
 * 申报规则表 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
 */
public interface CfgRuleDeclareService extends SuperService<CfgRuleDeclareEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-05-08
    * @param dto
    * @return
    */
    String add(CfgRuleDeclareDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-05-08
    * @param dto
    * @return
    */
    Boolean update(CfgRuleDeclareDTO.UpdateDTO dto);

    /**
     * 申报规则分页查询
     * @param dto
     * @return
     */
    PagingVO<CfgRuleDeclareDTO.PagingViewDTO> paging(PagingDTO<CfgRuleDeclareDTO.PagingParamDTO> dto);

    /**
     * 查询详情
     * @param id
     * @return
     */
    CfgRuleDeclareDTO.ViewDTO view(String id);

    /**
     * 更新状态
     * @param dto
     * @return
     */
    Boolean updateStatus(UpdateStateDTO dto);

    /**
     * 获取申报规则匹配结果
     * @param map
     * @return
     */
    void getRuleDeclareMatchResult(HashMap<String, Object> map);
}
