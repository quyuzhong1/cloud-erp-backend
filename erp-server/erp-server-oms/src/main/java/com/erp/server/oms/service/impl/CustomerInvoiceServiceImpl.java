package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.InvoiceDTO;
import com.erp.model.oms.entity.CustomerInvoiceEntity;
import com.erp.server.oms.mapper.CustomerInvoiceMapper;
import com.erp.server.oms.service.CustomerInvoiceService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 客户发票信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerInvoiceServiceImpl extends SuperServiceImpl<CustomerInvoiceMapper, CustomerInvoiceEntity> implements CustomerInvoiceService {


    /**
     * 检查默认银行是否是多个
     * @author yl
     * @date 2023-05-12 14:59
     * @param invoiceList
     * @return void
     */
    @Override
    public void checkIsDefault(List<InvoiceDTO.AddDTO> invoiceList) {
        long count = invoiceList.stream().filter(c -> c.getIsDefault() != null && c.getIsDefault()).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_92007);
        }

    }

    /**
     * 批量保存发票信息
     * @author yl
     * @date 2023-05-12 16:01
     * @param mainId
     * @param invoiceList
     * @return void
     */
    @Override
    public void saveBatchInvoice(String mainId, List<InvoiceDTO.AddDTO> invoiceList) {
        if (CollectionUtils.isEmpty(invoiceList)) {
            return;
        }
        List<CustomerInvoiceEntity> addList = BeanMapper.copyList(invoiceList, CustomerInvoiceEntity.class);
        addList.forEach(c -> c.setMainId(mainId));
        this.saveBatch(addList);
    }
}
