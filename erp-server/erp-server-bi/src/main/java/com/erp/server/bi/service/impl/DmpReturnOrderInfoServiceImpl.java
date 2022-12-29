package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoExcelDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import com.erp.server.bi.enums.ReturnOrderStatusEnum;
import com.erp.server.bi.enums.SettleMethodEnum;
import com.erp.server.bi.mapper.DmpReturnOrderInfoMapper;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpReturnOrderInfoService;
import com.erp.server.bi.service.DmpReturnOrderItemService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

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
        if (CollectionUtils.isNotEmpty(pageData.getRecords())) {
            pageData.getRecords().forEach(obj -> obj.setStatusName(ReturnOrderStatusEnum.getName(obj.getStatus())));
        }
        return new PagingVO(pageData);
    }

    @Override
    public BigDecimal sumRefundAmount(List<String> orderIds, BiFilterDTO dto) {
// 没有sku情况
        BigDecimal amount = BigDecimal.ZERO;
        QueryWrapper<DmpReturnOrderInfoEntity> query = new QueryWrapper<>();

        if(CollectionUtils.isEmpty(dto.getSku())){
            if (SettleMethodEnum.ORIGINAL_CURRENCY.equals(dto.getSettleMethod())) {
                if (BiFilterDTO.validOriginalCurrency(dto)){
                    query.select("sum(order_fee) as order_fee");
                }else {
                    return BigDecimal.ZERO;
                }
            }else if(SettleMethodEnum.CNY_SETTLE.equals(dto.getSettleMethod())){
                query.select("sum(order_fee*cny_settle_rate) as order_fee");
            }else{
                query.select("sum(order_fee*currency_rate) as order_fee");
            }
            DmpReturnOrderInfoEntity dmpReturnOrderInfoEntity = baseMapper.selectOne(query);
            amount = dmpReturnOrderInfoEntity.getOrderFee();
        }else {
            // 根据订单号获取订单详情，筛选sku
            amount = dmpReturnOrderItemService.sumReturnAmountBySKu(dto);
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
        list.forEach(obj ->obj.setStatusName(ReturnOrderStatusEnum.getName(obj.getStatus())));
        //导出销售数据
        List<DmpReturnOrderInfoExcelDTO> excelList = BeanMapperUtils.copyList(DmpReturnOrderInfoExcelDTO.class, list);
        String fileName = dmpOrderInfoService.getFileName("退货数据导出");
        ExcelUtil.export(fileName, "退货数据导出", excelList, DmpReturnOrderInfoExcelDTO.class, response);
        return;
    }

    @Override
    public DmpReturnOrderInfoEntity getByReturnOrderId(String returnOrderId) {
        LambdaQueryWrapper<DmpReturnOrderInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DmpReturnOrderInfoEntity::getReturnOrderId,returnOrderId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }
}




