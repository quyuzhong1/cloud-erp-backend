package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.ExhibitionOrderDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.mapper.ExhibitionOrderDetailMapper;
import com.erp.server.oms.service.ExhibitionOrderDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.erp.server.oms.service.SoDetailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.ExhibitionOrderDetailDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 展会订单详情 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-29
 */
@Slf4j
@Service
public class ExhibitionOrderDetailServiceImpl extends SuperServiceImpl<ExhibitionOrderDetailMapper, ExhibitionOrderDetailEntity> implements ExhibitionOrderDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoDetailService soDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ExhibitionOrderDetailDTO.AddDTO addDTO) {
        ExhibitionOrderDetailEntity exhibitionOrderDetailEntity = new ExhibitionOrderDetailEntity();
        BeanMapperUtils.copy(addDTO, exhibitionOrderDetailEntity);

        log.info("开始新增展会订单详情");
        boolean save = super.save(exhibitionOrderDetailEntity);
        if(!save) {
            throw new ServiceException("展会订单详情保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "展会订单详情" , exhibitionOrderDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, exhibitionOrderDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(exhibitionOrderDetailEntity.getId(), exhibitionOrderDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ExhibitionOrderDetailDTO.UpdateDTO addOrUpdateDTO) {
        ExhibitionOrderDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "展会订单详情"));
        ExhibitionOrderDetailEntity exhibitionOrderDetailEntity =  BeanMapperUtils.map(ExhibitionOrderDetailEntity.class, addOrUpdateDTO);

        log.info("编辑 开始修改展会订单详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(exhibitionOrderDetailEntity);
        if(!save) {
            throw new ServiceException("展会订单详情保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录展会订单详情日志数据，id：【{}】", exhibitionOrderDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), exhibitionOrderDetailEntity.getId(), "展会订单详情");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, exhibitionOrderDetailEntity, null, exhibitionOrderDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<ExhibitionOrderDetailDTO.ViewDTO> listViewByMainId(String id) {
        if(StringUtils.isBlank(id)){
            return Collections.emptyList();
        }

        List<ExhibitionOrderDetailEntity> list = lambdaQuery().eq(ExhibitionOrderDetailEntity::getMainId, id).list();
        if(CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }

        List<ExhibitionOrderDetailDTO.ViewDTO> resultList = BeanMapper.copyList(list, ExhibitionOrderDetailDTO.ViewDTO.class);

        List<String> sourceDetailIdList = resultList.stream().map(ExhibitionOrderDetailDTO.ViewDTO::getSampleLedgerId).collect(Collectors.toList());
        Map<String, SampleLedgerEntity> sampleLedgerMap = new HashMap<>();
        if(CollUtil.isNotEmpty(sourceDetailIdList)){
            List<SampleLedgerEntity> sampleLedgerEntities = FeignQuery.getByIds(SampleLedgerEntity.class, sourceDetailIdList);
            sampleLedgerMap = sampleLedgerEntities.stream().collect(Collectors.toMap(SampleLedgerEntity::getId, Function.identity(), (v1, v2) -> v1));

            List<ExhibitionOrderDetailDTO.SkuQtyDetailDTO> skuQtyDetailList = this.baseMapper.listBySourceDetailIds(sourceDetailIdList);
            // 根据 sourceDetailId 维度对 qty 进行合计，空值当作 0 处理
            Map<String, Integer> skuQtySumMap = skuQtyDetailList.stream()
                    .filter(e -> !Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getCode()))
                    .collect(Collectors.toMap(
                            ExhibitionOrderDetailDTO.SkuQtyDetailDTO::getSampleLedgerId,
                            dto -> Optional.ofNullable(dto.getQty()).orElse(0),
                            Integer::sum
                    ));

            for (Map.Entry<String, SampleLedgerEntity> entry : sampleLedgerMap.entrySet()) {
                String sourceDetailId = entry.getKey();
                SampleLedgerEntity sampleLedgerEntity = entry.getValue();
                Integer usedQty = skuQtySumMap.getOrDefault(sourceDetailId, 0);
                Integer availableQty = Optional.ofNullable(sampleLedgerEntity.getQty()).orElse(0) - usedQty;
                sampleLedgerEntity.setQty(availableQty);
            }
        }

        List<String> skuIdList = resultList.stream().map(ExhibitionOrderDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));

        //sku的历史价格
        List<SoDetailDTO.SkuHistoryPriceDTO> skuPriceHistoryList = soDetailService.listSkuPriceHistory(skuIdList);

        for (ExhibitionOrderDetailDTO.ViewDTO item : resultList) {

            String skuId = item.getSkuId();

            //单价
            BigDecimal price = item.getPrice();
            //汇率
            BigDecimal exchangeRate = item.getExchangeRate();
            if (Objects.isNull(exchangeRate)) {
                exchangeRate = MathUtil.BigDecimal_1;
            }
            //销售单价(本位币)
            item.setPriceLc(MathUtil.multiplyWithTwo(price, exchangeRate,4));
            //含税单价
            BigDecimal taxPrice = item.getTaxPrice();
            item.setTaxPrice(taxPrice);
            //含税单价(本位币)
            item.setTaxPriceLc(MathUtil.multiplyWithTwo(taxPrice, exchangeRate,4));

            SkuVO skuVO = skuMap.getOrDefault(skuId, null);
            if(Objects.nonNull(skuVO)){
                item.setUnit(skuVO.getUnitName());
            }

            //历史价格
            SoDetailDTO.SkuHistoryPriceDTO skuHistoryPrice = skuPriceHistoryList.stream().
                    filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuHistoryPrice != null) {
                item.setMaxPrice(skuHistoryPrice.getMaxPrice());
                item.setMinPrice(skuHistoryPrice.getMinPrice());
                item.setAvgPrice(skuHistoryPrice.getAvgPrice());
            }

            //可销售数量
            SampleLedgerEntity sampleLedgerEntity = sampleLedgerMap.getOrDefault(item.getSampleLedgerId(), null);
            if(Objects.nonNull(sampleLedgerEntity)){
                item.setAvailableQty(sampleLedgerEntity.getQty());
            }
        }
        return resultList;
    }
}
