package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerInfoService extends SuperService<CustomerInfoEntity> {

    /**
     * 获取到分组的id 集合
     * @author yl
     * @date 2023-05-11 18:10
     * @param
     * @return java.util.List<java.lang.String>
     */
    List<String> listGroup();

    
    /**
     * 添加客户信息
     * @author yl
     * @date 2023-05-12 10:30
     * @param dto
     * @return java.lang.String
     */
    String add(CustomerDTO.AddDTO dto);

    
    /**
     * 提交
     * @author yl
     * @date 2023-05-12 16:47
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);
}
