package com.erp.server.sys.service;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.erp.model.sys.dto.SampleUseUserDTO;
import com.erp.model.sys.entity.DictSampleUseUserEntity;

import java.util.List;

/**
 * <p>
 * 示例用户 字典表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2025-01-27
 */
public interface DictSampleUseUserService extends SuperService<DictSampleUseUserEntity> {

    /**
     * 保存或者修改示例用户
     * @author Lambda
     * @date 2025-01-27 16:25
     * @param userList
     * @return java.lang.Boolean
     */
    Boolean saveOrUpdateBatchUser(ValidList<SampleUseUserDTO.AddOrUpdateDTO> userList);

    /**
     * 获取示例用户列表
     * @author Lambda
     * @date 2025-01-27 16:33
     * @param
     * @return java.util.List<com.erp.model.sys.dto.SampleUseUserDTO.ViewDTO>
     */
    List<SampleUseUserDTO.ViewDTO> getList();

    List<BaseIdDTO> getByIds(List<String> ids);

    /**
     * 根据条件模糊查询示例用户列表
     * @author Lambda
     * @date 2025-01-27 16:33
     * @param queryDTO 查询条件
     * @return java.util.List<com.erp.model.sys.dto.SampleUseUserDTO.ViewDTO>
     */
    List<SampleUseUserDTO.ViewDTO> getListByCondition(SampleUseUserDTO.QueryDTO queryDTO);
}
