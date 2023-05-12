package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.server.oms.mapper.CustomerAddressMapper;
import com.erp.server.oms.service.CustomerAddressService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 客户地址信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerAddressServiceImpl extends SuperServiceImpl<CustomerAddressMapper, CustomerAddressEntity> implements CustomerAddressService {


    /**
     * 检查默认地址是否存在多个
     * @author yl
     * @date 2023-05-12 14:55
     * @param addressList
     * @return void
     */
    @Override
    public void checkIsDefault(List<CustomerAddressDTO.AddDTO> addressList) {
        long count = addressList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_92006);
        }
    }

    /**
     * 批量保存地址信息
     * @author yl
     * @date 2023-05-12 15:56
     * @param mainId
     * @param addressList
     * @return void
     */
    @Override
    public void saveBatchAddress(String mainId, List<CustomerAddressDTO.AddDTO> addressList) {
        if (CollectionUtils.isEmpty(addressList)) {
            return;
        }
        List<CustomerAddressEntity> addList = BeanMapper.copyList(addressList, CustomerAddressEntity.class);
        addList.forEach(c -> c.setMainId(mainId));
        this.saveBatch(addList);
    }
}
