package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerSellerEntity;
import com.erp.server.oms.mapper.CustomerSellerMapper;
import com.erp.server.oms.service.CustomerSellerService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 客户销售员信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerSellerServiceImpl extends SuperServiceImpl<CustomerSellerMapper, CustomerSellerEntity> implements CustomerSellerService {


    /**
     * 检查开始日期 结束日期
     * @author yl
     * @date 2023-05-12 15:04
     * @param sellerList
     * @return void
     */
    @Override
    public void checkDate(List<SellerDTO.AddDTO> sellerList) {
        if (CollectionUtils.isNotEmpty(sellerList)) {
            List<SellerDTO.AddDTO> list = sellerList.stream().filter(c -> c.getEndDate() != null && c.getStartDate() != null).collect(Collectors.toList());
            long count = list.stream().filter(c -> c.getEndDate().compareTo(c.getStartDate()) < 0).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_92008);
            }

        }

    }
}
