package com.erp.server.oms.service.impl;

import com.common.business.dto.FindUserDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerSellerEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.CustomerSellerMapper;
import com.erp.server.oms.service.CustomerSellerService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
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

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 检查开始日期 结束日期
     *
     * @param sellerList
     * @return void
     * @author yl
     * @date 2023-05-12 15:04
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


    /**
     * 批量保存销售员信息
     *
     * @param mainId
     * @param sellerList
     * @return void
     * @author yl
     * @date 2023-05-12 16:04
     */
    @Override
    public void saveBatchSeller(String mainId, List<SellerDTO.AddDTO> sellerList) {
        if (CollectionUtils.isEmpty(sellerList)) {
            return;
        }
        List<CustomerSellerEntity> addList = BeanMapper.copyList(sellerList, CustomerSellerEntity.class);
        List<String> userIdList = addList.stream().map(CustomerSellerEntity::getSellerId).collect(Collectors.toList());
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
        for (CustomerSellerEntity item : addList) {
            item.setMainId(mainId);
            String userId = item.getSellerId();
            String userName = userList.stream().filter(d -> d.getUserId().equals(userId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
            item.setSellerName(userName);
        }

        this.saveBatch(addList);

    }
}
