package com.erp.server.scm.service.impl;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.SupplierContactDTO;
import com.erp.model.scm.entity.SupplierContactEntity;
import com.erp.server.scm.mapper.SupplierContactMapper;
import com.erp.server.scm.service.SupplierContactService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 供应商联系人表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierContactServiceImpl extends SuperServiceImpl<SupplierContactMapper, SupplierContactEntity> implements SupplierContactService {


    /**
     * 保存供应商联系信息
     *
     * @param supplierId
     * @param contactList
     * @return void
     * @author yl
     * @date 2023-03-17 16:07
     */
    @Override
    public void saveBatchContact(String supplierId, List<SupplierContactDTO.AddDTO> contactList) {
        if (CollectionUtils.isEmpty(contactList)) {
            return;
        }
        List<SupplierContactEntity> addList = BeanMapper.copyList(contactList, SupplierContactEntity.class);
        addList.forEach(c -> c.setSupplierId(supplierId));
        this.saveBatch(addList);

    }

    /**
     * 检查联系人 是否有多个默认人
     *
     * @param contactList
     * @return void
     * @author yl
     * @date 2023-03-17 16:39
     */

    @Override
    public void checkIsDefault(List<SupplierContactDTO.AddDTO> contactList) {
        long count = contactList.stream().filter(c -> c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_98001);
        }
    }
}
