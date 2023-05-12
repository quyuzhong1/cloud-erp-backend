package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.CustomerContactDTO;
import com.erp.model.oms.entity.CustomerContactEntity;
import com.erp.server.oms.mapper.CustomerContactMapper;
import com.erp.server.oms.service.CustomerContactService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 客户联系人信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerContactServiceImpl extends SuperServiceImpl<CustomerContactMapper, CustomerContactEntity> implements CustomerContactService {


    /**
     * 检查客户默认联系人是否多个
     * @author yl
     * @date 2023-05-12 14:00
     * @param contactList
     * @return void
     */
    @Override
    public void checkIsDefault(List<CustomerContactDTO.AddDTO> contactList) {
        long count = contactList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_92005);
        }
    }



    /**
     * 保存联系人信息
     * @author yl
     * @date 2023-05-12 15:53
     * @param mainId
     * @param contactList
     * @return void
     */
    @Override
    public void saveBatchContact(String mainId, List<CustomerContactDTO.AddDTO> contactList) {
        if (CollectionUtils.isEmpty(contactList)) {
            return;
        }
        List<CustomerContactEntity> addList = BeanMapper.copyList(contactList, CustomerContactEntity.class);
        addList.forEach(c -> c.setMainId(mainId));
        this.saveBatch(addList);
    }
}
