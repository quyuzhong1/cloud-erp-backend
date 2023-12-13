package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.RefundOrderDTO;
import com.erp.model.oms.entity.RefundOrderEntity;
import com.erp.model.oms.enums.RefundOrderStatusEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.server.oms.mapper.RefundOrderMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.RefundOrderService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 退款订单 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-25
 */
@Service
public class RefundOrderServiceImpl extends SuperServiceImpl<RefundOrderMapper, RefundOrderEntity> implements RefundOrderService {


    @Resource
    private DictBasicService dictBasicService;

    /**
     * 售后订单分页
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.RefundOrderDTO.PagingViewDTO>
     * @author yl
     * @date 2023-08-25 14:09
     */
    @Override
    public PagingVO<RefundOrderDTO.PagingViewDTO> paging(PagingDTO<RefundOrderDTO.PagingParamDTO> dto) {
        RefundOrderDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<RefundOrderDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }

    /**
     * 填充数据
     *
     * @param list
     */
    private void fillDb(List<RefundOrderDTO.PagingViewDTO> list) {
        String key = DictBasicTypeEnum.PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictBasicList = dictBasicService.getByKey(key);
        for (RefundOrderDTO.PagingViewDTO item : list) {
            String dictPlatform = item.getDictPlatform();
            String platformName = dictBasicList.stream().filter(d -> d.getValue().equals(dictPlatform)).
                    map(DictBasicDTO.ViewDTO::getName).findFirst().orElse("");
            item.setPlatformName(platformName);
            String status = item.getStatus();
            String name = RefundOrderStatusEnum.getName(status);
            item.setStatusName(name);
        }
    }
}
