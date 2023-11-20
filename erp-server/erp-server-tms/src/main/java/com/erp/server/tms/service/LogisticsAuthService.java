package com.erp.server.tms.service;

import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsAuthDTO;

import java.util.Map;

/**
 * <p>
 * 物流授权表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsAuthService extends SuperService<LogisticsAuthEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author Lambda
     * @date: 2023-11-02
     */
    BaseResultDTO.AddDTO add(LogisticsAuthDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author Lambda
     * @date: 2023-11-02
     */
    Boolean update(LogisticsAuthDTO.UpdateDTO dto);

    /**
     * 获取到授权详情
     *
     * @param id
     * @return
     * @author yl
     * @date 2023-11-10 17:50
     */
    LogisticsAuthDTO.ViewDTO view(String id);

    /**
     * 取消授权
     *
     * @return
     * @parms id
     * @author yl
     * @date 2023-11-15
     */
    BatchResultDTO cancel(String id);

    /**
     * 根据授权id组装授权信息
     *
     * @param authId
     * @return
     */
    Map<String, String> getLogisticsAuthConfig(String authId);
}
