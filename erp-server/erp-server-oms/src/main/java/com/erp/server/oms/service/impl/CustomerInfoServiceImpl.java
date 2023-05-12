package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.server.oms.mapper.CustomerInfoMapper;
import com.erp.server.oms.service.CustomerInfoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerInfoServiceImpl extends SuperServiceImpl<CustomerInfoMapper, CustomerInfoEntity> implements CustomerInfoService {


    /**
     * 获取到分组的id 集合
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-11 18:10
     */
    @Override
    public List<String> listGroup() {
        List<CustomerInfoEntity> list = this.list();
        return list.stream().map(CustomerInfoEntity::getGroupId).distinct().collect(Collectors.toList());
    }


    /**
     * 添加客户信息
     * @author yl
     * @date 2023-05-12 10:30
     * @param dto
     * @return java.lang.String
     */
    @Override
    public String add(CustomerDTO.AddDTO dto) {


        return null;
    }
}
