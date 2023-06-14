package com.erp.server.sys.service;
import com.erp.model.sys.dto.CfgUserRangeDTO;
import com.erp.model.sys.entity.CfgUserRangeEntity;
import com.common.business.service.SuperService;

import java.util.List;


/**
 * <p>
 * 用户区间配置表 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-12
 */
public interface CfgUserRangeService extends SuperService<CfgUserRangeEntity> {

    /**
     * 新增
     * @param userRangeDTO
     */
    void add(CfgUserRangeDTO.SaveDTO userRangeDTO);

    /**
     * 修改
     * @param userRangeDTO
     */
    void update(CfgUserRangeDTO.SaveDTO userRangeDTO);

    /**
     * 保存
     * @param userRangeDTO
     */
    void save(CfgUserRangeDTO.SaveDTO userRangeDTO);

    /**
     * 详情
     * @param type
     * @return
     */
    List<CfgUserRangeDTO.UserRangeDataDTO> detail(String type);

}
