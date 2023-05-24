package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoChangeDetailDTO;
import com.erp.model.oms.entity.SoChangeDetailEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.SoChangeDetailMapper;
import com.erp.server.oms.service.SoChangeDetailService;
import com.erp.server.oms.service.SoDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单变更明细 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoChangeDetailServiceImpl extends SuperServiceImpl<SoChangeDetailMapper, SoChangeDetailEntity> implements SoChangeDetailService {

    @Resource
    private SoDetailService soDetailService;


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    /**
     * 添加变更详情信息
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-24 14:12
     */
    @Override
    public void addDetailList(String mainId, List<SoChangeDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SoChangeDetailEntity> addList = new ArrayList<>(detailList.size());
        //销售订单的详情id 集合
        List<String> soDetailIdList = detailList.stream().map(SoChangeDetailDTO.AddDTO::getSoDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = CollectionUtils.isNotEmpty(soDetailIdList) ? soDetailService.listByIds(soDetailIdList) : Collections.emptyList();
        //币种列表
        List<String> currencyList = detailList.stream().map(SoChangeDetailDTO.AddDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);
        List<String> skuIdList = detailList.stream().map(SoChangeDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        for (SoChangeDetailDTO.AddDTO item : detailList) {
            SoChangeDetailEntity soChangeDetail = new SoChangeDetailEntity();
            String skuId = item.getSkuId();
            //销售订单详情
            String soDetailId = item.getSoDetailId();
            //原来的销售订单
            SoDetailEntity soDetail = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).findFirst().orElse(null);
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (isGift) {
                price = BigDecimal.ZERO;
            }
            String currency = item.getCurrency();
            String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");

            //金额
            BigDecimal amount = MathUtil.multiply(price, qty);

            soChangeDetail.setIsGift(isGift);
            soChangeDetail.setPrice(price);
            soChangeDetail.setCurrency(currency);
            soChangeDetail.setCurrencySymbol(symbol);
            soChangeDetail.setIsReissue(item.getIsReissue());
            soChangeDetail.setAmount(amount);
            soChangeDetail.setMainId(mainId);
            soChangeDetail.setSkuId(skuId);
            soChangeDetail.setRemark(item.getRemark());
            soChangeDetail.setChangeType(item.getChangeType());
            soChangeDetail.setQty(item.getQty());
            soChangeDetail.setTaxRate(item.getTaxRate());
            soChangeDetail.setOldPrice(soDetail != null ? soDetail.getPrice() : BigDecimal.ZERO);
            soChangeDetail.setOldAmount(soDetail != null ? soDetail.getAmount() : BigDecimal.ZERO);
            soChangeDetail.setOldCurrency(soDetail != null ? soDetail.getCurrency() : "");
            soChangeDetail.setOldCurrencySymbol(soDetail != null ? soDetail.getCurrencySymbol() : "");
            soChangeDetail.setOldQty(soDetail != null ? soDetail.getQty() : 0);
            soChangeDetail.setOldTaxRate(soDetail != null ? soDetail.getTaxRate() : BigDecimal.ZERO);
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            soChangeDetail.setSkuNo(skuNo);
            addList.add(soChangeDetail);
        }
        this.saveBatch(addList);


    }
}
