package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.dto.ExhibitionOrderImportDetailExcelDTO;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.excel.SoDetailImportExcelDTO;
import com.erp.model.oms.entity.ExhibitionOrderDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.erp.model.wms.enums.SampleLedgerTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.SampleLedgerFeign;
import com.erp.server.oms.listener.ExhibitionOrderDetailExcelListener;
import com.erp.server.oms.mapper.ExhibitionOrderDetailMapper;
import com.erp.server.oms.service.ExhibitionOrderDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.erp.server.oms.service.SoDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.ExhibitionOrderDetailDTO;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

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
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoDetailService soDetailService;
    @Resource
    private SampleLedgerFeign sampleLedgerFeign;

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
                item.setUseUserId(sampleLedgerEntity.getUseUserId());
                item.setUseUserName(sampleLedgerEntity.getUseUserName());
                item.setSampleLedgerId(sampleLedgerEntity.getId());
            }
        }
        return resultList;
    }

    @Override
    public ExhibitionOrderDetailDTO.ImportDTO importFile(MultipartFile excelFile,String id, String recipientUserId, Boolean isTax, HttpServletResponse response) {
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();

        // 构造查询条件：根据用户ID和SKU列表查询样品台账中的可用数量
        SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
        dto.setUserId(recipientUserId);
        dto.setType(SampleLedgerTypeEnum.EXHIBITION.getCode());
//        dto.setChildId(id);
        List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerFeign.listLedgerByUserId(dto);

        Map<String, SkuVO> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, Function.identity(), (o1, o2) -> o1));
        ExhibitionOrderDetailExcelListener excelListenerUtil = new ExhibitionOrderDetailExcelListener(skuMap,skuAvailableQtyDTOS, isTax);
        try {
            EasyExcel.read(excelFile.getInputStream(), ExhibitionOrderImportDetailExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入错误=={}", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        ExhibitionOrderDetailDTO.ImportDTO result = new ExhibitionOrderDetailDTO.ImportDTO();
        List<ExhibitionOrderDetailDTO.SkuDTO> successList = excelListenerUtil.getSuccessList();
        List<String> skuIdList = successList.stream().map(ExhibitionOrderDetailDTO.SkuDTO::getSkuId).collect(Collectors.toList());
        //sku的历史价格
        List<SoDetailDTO.SkuHistoryPriceDTO> skuPriceHistoryList = soDetailService.listSkuPriceHistory(skuIdList);
        for (ExhibitionOrderDetailDTO.SkuDTO item : successList) {
            String skuId = item.getSkuId();
            //销售数量
            Integer qty = item.getQty();
            //税率
            if(Objects.isNull(item.getTaxRate())){
                item.setTaxRate(BigDecimal.ZERO);
            }
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //单价
            BigDecimal price = item.getPrice();
            item.setAmount(MathUtil.multiplyWithTwo(price, qty));
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiplyWithTwo(price, multiplyTax);

            item.setTaxPrice(taxPrice);
            //价税金额
            BigDecimal taxAmount = MathUtil.multiplyWithTwo(taxPrice, qty);
            item.setTaxAmount(taxAmount);

            //历史价格
            SoDetailDTO.SkuHistoryPriceDTO skuHistoryPrice = skuPriceHistoryList.stream().
                    filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuHistoryPrice != null) {
                item.setMaxPrice(skuHistoryPrice.getMaxPrice());
                item.setMinPrice(skuHistoryPrice.getMinPrice());
                item.setAvgPrice(skuHistoryPrice.getAvgPrice());
            } else {
                item.setMaxPrice(price);
                item.setMinPrice(price);
                item.setAvgPrice(price);
            }
        }
        result.setSuccessList(successList);
        //导出错误数据
        List<ExhibitionOrderImportDetailExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "展会订单明细错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, ExhibitionOrderImportDetailExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }


}
