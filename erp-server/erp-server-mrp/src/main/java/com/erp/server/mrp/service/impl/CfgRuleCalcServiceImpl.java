package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;
import com.erp.model.mrp.entity.CalcSalesInfoDimEntity;
import com.erp.model.mrp.entity.CfgRuleCalcEntity;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingCalcEntity;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaCalcEntity;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaTypeEnum;
import com.erp.model.mrp.enums.HistorySalesTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.es.entity.CalcSalesInfoHisEsEntity;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.service.CalcSalesInfoHisEsService;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import com.erp.server.mrp.listener.HistorySalesQtyExcelListener;
import com.erp.server.mrp.mapper.CfgRuleCalcMapper;
import com.erp.server.mrp.service.CalcSalesInfoDimService;
import com.erp.server.mrp.service.CfgRuleCalcService;
import com.erp.server.mrp.service.CfgRuleSalesDenoisingCalcService;
import com.erp.server.mrp.service.CfgRuleSalesFormulaCalcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 试算配置 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CfgRuleCalcServiceImpl extends SuperServiceImpl<CfgRuleCalcMapper, CfgRuleCalcEntity> implements CfgRuleCalcService {
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CfgRuleSalesFormulaCalcService cfgRuleSalesFormulaCalcService;
    @Resource
    private OrderHistorySalesEsService orderHistorySalesEsService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private CfgRuleSalesDenoisingCalcService cfgRuleSalesDenoisingCalcService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private CalcSalesInfoHisEsService calcSalesInfoHisEsService;

    @Resource
    private CalcSalesInfoDimService calcSalesInfoDimService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleCalcDTO.AddDTO addDTO, HttpServletResponse response) {
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(addDTO.getSkuIds());
        Map<String, String> skuMap = skuVOS.stream()
                .collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuNo, (o1, o2) -> o1));
        List<ShopInfoEntity> shopInfoList = shopInfoFeign.listShopInfoByIds(addDTO.getShopIds());
        Map<String, ShopInfoEntity> shopMap = shopInfoList.stream()
                .collect(Collectors.toMap(ShopInfoEntity::getId, v -> v, (o1, o2) -> o1));
        CfgRuleCalcEntity entity = CfgRuleCalcDTO.AddDTO.buildCfgRuleCalcEntity(addDTO);
        entity.setId(IdWorker.getIdStr());
        List<CalcSalesInfoHisEsEntity> historySaleList = new ArrayList<>();
        if (HistorySalesTypeEnum.SYSTEM.getCode().equals(addDTO.getSaleType())) {
            historySaleList = getSysHistorySalesQty(addDTO, entity.getId(), skuMap, shopMap);
        } else {
            HistorySalesQtyExcelListener excelListener = new HistorySalesQtyExcelListener(skuVOS, shopInfoList, entity);
            try {
                EasyExcel.read(FastDFSClientUtil.getInputStream(addDTO.getFileUrl()), CfgRuleCalcDTO.HistorySaleImportDTO.class, excelListener).headRowNumber(1).sheet(0).doRead();
                //导出错误数据
                exportErrorExcel(response,excelListener.getErrorList());
                historySaleList = excelListener.getDataList();
            } catch (ExcelCommonException e) {
                log.error("导入格式错误！", e);
                throw new ServiceException(ApiError.ERROR_1016);
            }
        }
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XLSS);
        entity.setCode(code);
        save(entity);
        List<CfgRuleSalesFormulaCalcEntity> formulaCalcEntities = handleSalesFormula(addDTO, entity.getId());
        cfgRuleSalesFormulaCalcService.saveBatch(formulaCalcEntities);
        List<CfgRuleSalesDenoisingCalcEntity> salesDenoising = handleSalesDenoising(addDTO, entity.getId());
        cfgRuleSalesDenoisingCalcService.saveBatch(salesDenoising);
        calcSalesInfoHisEsService.batchSave(historySaleList);
        List<CalcSalesInfoDimDTO.CalcResultDTO> calcResultList = new ArrayList<>();
        List<CalcSalesInfoDimEntity> calcSalesInfoDimList = buildCalcSalesInfoDim(addDTO, shopMap, historySaleList, entity.getId(), skuMap, formulaCalcEntities, salesDenoising, calcResultList);
        calcSalesInfoDimService.saveBatch(calcSalesInfoDimList);
        calcSalesInfoDimService.calcSalesInfo(calcResultList);
        return new BaseResultDTO.AddDTO(entity.getId(), code);
    }

    private void exportErrorExcel(HttpServletResponse response, List<CfgRuleCalcDTO.HistorySaleImportDTO> errorList) {
        if (CollectionUtils.isEmpty(errorList)) {
            return;
        }
        String name = "试算历史销量错误数据";
        StringBuilder sb = new StringBuilder();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/calcHistorySaleQtyError.xlsx";
        try {
            new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("导出失败 原因{}", e.getMessage(), e);
            throw new ServiceException("试算历史销量错误数据导出失败");
        }
    }

    /**
     * 构造
     *
     * @param addDTO              入参
     * @param shopMap             店铺
     * @param historySaleList     历史销量
     * @param cfgRuleCalcId       id
     * @param skuMap              sku
     * @param formulaCalcEntities 试算销量公式
     * @param salesDenoising      试算销量去噪信息
     * @param calcResultList      试算结果
     */
    private static List<CalcSalesInfoDimEntity> buildCalcSalesInfoDim(CfgRuleCalcDTO.AddDTO addDTO, Map<String, ShopInfoEntity> shopMap,
                                                                      List<CalcSalesInfoHisEsEntity> historySaleList, String cfgRuleCalcId,
                                                                      Map<String, String> skuMap, List<CfgRuleSalesFormulaCalcEntity> formulaCalcEntities,
                                                                      List<CfgRuleSalesDenoisingCalcEntity> salesDenoising,
                                                                      List<CalcSalesInfoDimDTO.CalcResultDTO> calcResultList) {
        List<CalcSalesInfoDimEntity> calcSalesInfoDimList = new ArrayList<>();
        for (String shopId : addDTO.getShopIds()) {
            ShopInfoEntity info = Optional.ofNullable(shopMap.get(shopId)).orElse(new ShopInfoEntity());
            for (String skuId : addDTO.getSkuIds()) {
                Map<LocalDate, Integer> historySaleMap = historySaleList.stream()
                        .filter(v -> v.getSkuId().equals(skuId))
                        .filter(v -> v.getShopId().equals(shopId))
                        .collect(Collectors.toMap(CalcSalesInfoHisEsEntity::getDate, CalcSalesInfoHisEsEntity::getQty, Integer::sum));
                CalcSalesInfoDimEntity salesInfoDimEntity = new CalcSalesInfoDimEntity();
                salesInfoDimEntity.setId(IdWorker.getIdStr());
                salesInfoDimEntity.setCfgRuleCalcId(cfgRuleCalcId);
                salesInfoDimEntity.setSkuId(skuId);
                salesInfoDimEntity.setSkuNo(skuMap.get(skuId));
                salesInfoDimEntity.setShopId(shopId);
                salesInfoDimEntity.setCountry(info.getDictCountryCode());
                salesInfoDimEntity.setPlatform(info.getDictPlatform());
                calcSalesInfoDimList.add(salesInfoDimEntity);
                CalcSalesInfoDimDTO.CalcResultDTO resultDTO = new CalcSalesInfoDimDTO.CalcResultDTO();
                resultDTO.setCalcSalesInfoDimId(salesInfoDimEntity.getId());
                resultDTO.setStartCalcDate(addDTO.getStartCalcDate());
                resultDTO.setEndCalcDate(addDTO.getEndCalcDate());
                resultDTO.setSalesHistoryMap(historySaleMap);
                resultDTO.setFormulaCalcEntities(formulaCalcEntities);
                resultDTO.setSalesDenoising(salesDenoising);
                calcResultList.add(resultDTO);
            }
        }
        return calcSalesInfoDimList;
    }

    /**
     * 获取系统历史销量
     *
     * @param addDTO  参数
     * @param skuMap  sku
     * @param shopMap 店铺
     */
    private List<CalcSalesInfoHisEsEntity> getSysHistorySalesQty(CfgRuleCalcDTO.AddDTO addDTO, String id,
                                                                 Map<String, String> skuMap, Map<String, ShopInfoEntity> shopMap) {
        List<OrderHistorySalesEsEntity> salesInfos = orderHistorySalesEsService.findByShopIdInAndSkuIdInAndDateBetween(addDTO.getShopIds(), addDTO.getSkuIds(),
                addDTO.getStartCalcDate().minusDays(361), addDTO.getStartCalcDate().minusDays(1));
        return new ArrayList<>(salesInfos.stream()
                .collect(Collectors.toMap(
                        v -> new CfgRuleCalcDTO.GroupDTO(v.getSkuId(), v.getShopId(), v.getDate()),
                        v -> {
                            CalcSalesInfoHisEsEntity dto = CalcSalesInfoHisEsEntity.buildCalcSalesInfoHis(v);
                            dto.setShopName(Optional.ofNullable(shopMap.get(v.getShopId())).orElse(new ShopInfoEntity()).getName());
                            dto.setSkuNo(skuMap.get(v.getSkuId()));
                            dto.setCfgRuleCalcId(id);
                            return dto;
                        },
                        (v1, v2) -> {
                            v1.setQty(v1.getQty() + v2.getQty());
                            return v1;
                        }
                ))
                .values());
    }

    @Override
    public void downloadHistorySales(CfgRuleCalcDTO.DownloadDTO dto) {
        downloadTaskFeign.saveDownloadTask("历史销量导出(销量试算)", FileTaskEventEnum.EXPORT_MRP_HISTORY_SALES_CALC.getCode(), dto);
    }

    @Override
    public void downloadRuleTemplate(HttpServletResponse response) {
        String path = "classpath:excel/calcHistorySaleQtyTemplate.xlsx";
        String excelName = "template.xlsx";
        ExcelUtil.downloadTemplate(path,excelName,response);
    }


    List<CfgRuleSalesDenoisingCalcEntity> handleSalesDenoising(CfgRuleCalcDTO.AddDTO addDTO, String cfgRuleCalcId) {
        List<CfgRuleSalesDenoisingCalcEntity> denoisingList = BeanMapperUtils.copyList(CfgRuleSalesDenoisingCalcEntity.class, addDTO.getSalesDenoisingList());
        denoisingList.forEach(obj -> obj.setCfgRuleCalcId(cfgRuleCalcId));
        return denoisingList;
    }


    /**
     * 构造销量计算参数
     *
     * @param addDTO 参数
     */
    private List<CfgRuleSalesFormulaCalcEntity> handleSalesFormula(CfgRuleCalcDTO.AddDTO addDTO, String cfgRuleCalcId) {
        List<CfgRuleSalesFormulaCalcEntity> list = new ArrayList<>();
        //默认日销量
        if (ObjectUtil.isNotEmpty(addDTO.getDefaultSalesQtyDTO())) {
            CfgRuleSalesFormulaCalcEntity defaultDTO = BeanMapperUtils.map(CfgRuleSalesFormulaCalcEntity.class, addDTO.getDefaultSalesQtyDTO());
            defaultDTO.setType(CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode()).setPriority(MathUtil.THREE);
            defaultDTO.setCfgRuleCalcId(cfgRuleCalcId);
            list.add(defaultDTO);
        }
        //动态日销量
        if (CollectionUtils.isNotEmpty(addDTO.getDynamicSalesQtyList())) {
            List<CfgRuleSalesFormulaCalcEntity> dynamicList = BeanMapperUtils.copyList(CfgRuleSalesFormulaCalcEntity.class, addDTO.getDynamicSalesQtyList());
            dynamicList.forEach(obj -> {
                obj.setType(CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode()).setPriority(MathUtil.TWO);
                obj.setCfgRuleCalcId(cfgRuleCalcId);
            });
            list.addAll(dynamicList);
        }
        //固定日销量
        if (CollectionUtils.isNotEmpty(addDTO.getFixedSalesQtyList())) {
            List<CfgRuleSalesFormulaCalcEntity> fixedList = BeanMapperUtils.copyList(CfgRuleSalesFormulaCalcEntity.class, addDTO.getFixedSalesQtyList());
            fixedList.forEach(obj -> {
                obj.setType(CfgRuleSalesFormulaTypeEnum.FIXED.getCode()).setPriority(MathUtil.ONE);
                obj.setCfgRuleCalcId(cfgRuleCalcId);
            });
            list.addAll(fixedList);
        }
        return list;
    }
}
