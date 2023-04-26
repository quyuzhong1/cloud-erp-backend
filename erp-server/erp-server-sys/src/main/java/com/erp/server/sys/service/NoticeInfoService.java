package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.dto.NoticeDTO;
import com.erp.model.sys.entity.NoticeInfoEntity;

/**
 * <p>
 * 通知表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-20
 */
public interface NoticeInfoService extends SuperService<NoticeInfoEntity> {

    
    /**
     * 添加通知
     * @author yl
     * @date 2023-04-20 20:43
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean add(NoticeDTO.AddDTO dto);

    
    /**
     * 编辑通知
     * @author yl
     * @date 2023-04-26 15:47
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean edit(NoticeDTO.UpdateDTO dto);
}
