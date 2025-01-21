package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.RulePromptWordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.RulePromptWordDTO;

/**
 * <p>
 * 汉化管理规则表 服务类
 * </p>
 *
 * @author lrp
 * @since 2025-01-17
 */
public interface RulePromptWordService extends SuperService<RulePromptWordEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2025-01-17
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RulePromptWordDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2025-01-17
    * @param dto
    * @return
    */
    Boolean update(RulePromptWordDTO.UpdateDTO dto);


}
