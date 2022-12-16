package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpReturnOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoExcelDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.server.bi.enums.TargetSettleMethodEnum;
import com.erp.server.bi.mapper.DmpReturnOrderInfoMapper;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpReturnOrderInfoService;
import com.erp.server.bi.service.DmpReturnOrderItemService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

/**
 * 退货订单服务
 */
@Service
public class DmpReturnOrderInfoServiceImpl extends ServiceImpl<DmpReturnOrderInfoMapper, DmpReturnOrderInfoEntity>
    implements DmpReturnOrderInfoService {


    @Resource
    private DmpReturnOrderItemService dmpReturnOrderItemService;


    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Override
    public PagingVO<DmpReturnOrderInfoDTO> paging(PagingDTO<DmpReturnOrderInfoSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DmpReturnOrderInfoSearchDTO params = dto.getParams();
        IPage<DmpReturnOrderInfoDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }

    @Override
    public BigDecimal sumRefundAmount(List<String> orderIds, BiFilterDTO dto) {
// 没有sku情况
        BigDecimal amount = BigDecimal.ZERO;
        QueryWrapper<DmpReturnOrderInfoEntity> query = new QueryWrapper<>();

        if(CollectionUtils.isEmpty(dto.getSku())){
            if (TargetSettleMethodEnum.ORIGINAL_CURRENCY.equals(dto.getSettleMethod())) {
                query.select("sum(order_fee*currency_rate) as order_fee");
            }else if(TargetSettleMethodEnum.CNY_SETTLE.equals(dto.getSettleMethod())){
                query.select("sum(order_fee*currency_rate) as order_fee");
            }else if (BiFilterDTO.validOriginalCurrency(dto)){
                query.select("sum(order_fee*currency_rate) as order_fee");
            }
            DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = baseMapper.selectOne(query);
            amount = dmpReturnOrderInfoEntity.getOrderFee();
        }else {
            // 条件存在sku的情况
            // 先查询订单号
            query.select("id");
            List<DmpReturnOrderInfoEntity> list = baseMapper.selectList(query);
            if(CollectionUtils.isEmpty(list)){
                return amount;
            }
            List<String> returnOrderIds = list.stream().map(DmpReturnOrderInfoEntity::getId).collect(Collectors.toList());
            // 根据订单号获取订单详情，筛选sku
            amount = dmpReturnOrderItemService.sumReturnAmountBySKu(returnOrderIds, dto);
        }
        return amount;
    }

    @Override
    public void exportExcel(DmpReturnOrderInfoSearchDTO dto, HttpServletResponse response) {
        //查询所有数据
        List<DmpReturnOrderInfoDTO> list = baseMapper.getAllDmpReturnOrderInfo(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //导出销售数据
        List<DmpReturnOrderInfoExcelDTO> excelList = BeanMapperUtils.copyList(DmpReturnOrderInfoExcelDTO.class, list);
        String fileName = dmpOrderInfoService.getFileName("退货数据导出");
        ExcelUtil.export(fileName, "退货数据导出", excelList, DmpReturnOrderInfoExcelDTO.class, response);
        return;
    }
}




