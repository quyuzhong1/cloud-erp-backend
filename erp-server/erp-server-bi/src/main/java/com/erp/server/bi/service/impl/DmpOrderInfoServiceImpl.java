package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.bi.dto.IndicatorSaleDTO;
import com.erp.model.bi.vo.IndicatorSaleSumVO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.server.bi.enums.IndicatorTimeTypeEnum;
import com.erp.server.bi.mapper.DmpOrderInfoMapper;
import com.erp.server.bi.service.DmpOrderInfoService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * 订单服务类
 * @author Cloud
 */
@Service
public class DmpOrderInfoServiceImpl extends ServiceImpl<DmpOrderInfoMapper, DmpOrderInfoEntity>
    implements DmpOrderInfoService {

    @Override
    public PagingVO<DmpOrderInfoDTO> paging(PagingDTO<DmpReturnOrderInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpReturnOrderInfoSearchDTO params = dto.getParams();
        IPage<DmpOrderInfoDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }

    @Override
    public IndicatorSaleSumVO countSales(IndicatorSaleDTO dto) {
        Integer count = lambdaQuery()
                .ge(dto.getTimeType().equals(IndicatorTimeTypeEnum.DELIVERY_TIME.getType()), DmpOrderInfoEntity::getPlatformCreateTime, dto.getStartTime())
                .le(dto.getTimeType().equals(IndicatorTimeTypeEnum.DELIVERY_TIME.getType()), DmpOrderInfoEntity::getPlatformCreateTime, dto.getEndTime())
                // TODO 订单时间字段待确认
//                .ge(dto.getTimeType().equals(IndicatorTimeTypeEnum.ORDER_TIME.getType()), DmpOrderInfoEntity::getPlatformCreateTime, dto.getStartTime())
//                .le(dto.getTimeType().equals(IndicatorTimeTypeEnum.ORDER_TIME.getType()), DmpOrderInfoEntity::getPlatformCreateTime, dto.getEndTime())
                // TODO  高级筛选字段待完善 事业部 站点 品类 品牌 人员
                // 平台
                .in(CollectionUtils.isNotEmpty(dto.getPlatform()), DmpOrderInfoEntity::getPlatformSign, dto.getPlatform())
                // 店铺
                .in(CollectionUtils.isNotEmpty(dto.getShop()), DmpOrderInfoEntity::getShopName, dto.getShop())
                //  TODO SKu
//                .in(CollectionUtils.isNotEmpty(dto.getSku()), DmpOrderInfoEntity::)
                .count();


        return null;
    }


}




