package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.*;
import com.erp.model.mrp.enums.CfgRuleSalesDenoisingDenoisingTypeEnum;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaDefaultTypeEnum;
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
import com.erp.server.mrp.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    @Resource
    private CalcSalesInfoFavoriteService calcSalesInfoFavoriteService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO add(CfgRuleCalcDTO.AddDTO addDTO) {
        verifyDate(addDTO);
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(addDTO.getSkuIds());
        Map<String, String> skuMap = skuVOS.stream()
                .collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuNo, (o1, o2) -> o1));
        List<ShopInfoEntity> shopInfoList = shopInfoFeign.listShopInfoByIds(addDTO.getShopIds());
        Map<String, ShopInfoEntity> shopMap = shopInfoList.stream()
                .collect(Collectors.toMap(ShopInfoEntity::getId, v -> v, (o1, o2) -> o1));
        CfgRuleCalcEntity entity = CfgRuleCalcDTO.AddDTO.buildCfgRuleCalcEntity(addDTO);
        entity.setId(IdWorker.getIdStr());
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XLSS);
        entity.setCode(code);
        List<CalcSalesInfoHisEsEntity> historySaleList;
        if (HistorySalesTypeEnum.SYSTEM.getCode().equals(addDTO.getSaleType())) {
            historySaleList = getSysHistorySalesQty(addDTO, entity.getId(), skuMap, shopMap);
        } else {
            HistorySalesQtyExcelListener excelListener = new HistorySalesQtyExcelListener(skuVOS, shopInfoList, entity);
            try {
                EasyExcelFactory.read(FastDFSClientUtil.getInputStream(addDTO.getFileUrl()), CfgRuleCalcDTO.HistorySaleImportDTO.class, excelListener).headRowNumber(1).sheet(0).doRead();
                //导出错误数据
                if (!CollectionUtils.isEmpty(excelListener.getErrorList())) {
                    String url = exportErrorExcel(excelListener.getErrorList());
                    return BatchResultDTO.fail(entity.getId(), code, url);
                }
                historySaleList = excelListener.getDataList();
            } catch (ExcelCommonException e) {
                log.error("导入格式错误！", e);
                throw new ServiceException(ApiError.ERROR_1016);
            }
        }
        save(entity);
        List<CfgRuleSalesFormulaCalcEntity> formulaCalcEntities = handleSalesFormula(addDTO, entity.getId());
        cfgRuleSalesFormulaCalcService.saveBatch(formulaCalcEntities);
        List<CfgRuleSalesDenoisingCalcEntity> salesDenoising = handleSalesDenoising(addDTO, entity.getId());
        cfgRuleSalesDenoisingCalcService.saveBatch(salesDenoising);
        calcSalesInfoHisEsService.batchSave(historySaleList);
        List<CalcSalesInfoDimDTO.CalcResultDTO> calcResultList = new ArrayList<>();
        List<CalcSalesInfoDimEntity> calcSalesInfoDimList = buildCalcSalesInfoDim(addDTO, shopMap, historySaleList, entity.getId(),
                skuMap, formulaCalcEntities, salesDenoising, calcResultList);
        calcSalesInfoDimService.saveBatch(calcSalesInfoDimList);
        calcSalesInfoDimService.calcSalesInfo(calcResultList);
        return BatchResultDTO.success(entity.getId(), code);
    }

    /**
     * 校验日期
     * @param addDTO 参数
     */
    private void verifyDate(CfgRuleCalcDTO.AddDTO addDTO) {
        if (addDTO.getStartCalcDate().isAfter(LocalDate.now())) {
            throw new ServiceException(ApiError.ERROR_VERIFY_START_CALC_DATE);
        }
        if (addDTO.getStartCalcDate().isAfter(addDTO.getEndCalcDate())) {
            throw new ServiceException(ApiError.ERROR__VERIFY_END_CALC_DATE);
        }
    }

    private String exportErrorExcel(List<CfgRuleCalcDTO.HistorySaleImportDTO> errorList) {
        String name = "试算历史销量错误数据";
        StringBuilder sb = new StringBuilder();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/calcHistorySaleQtyError.xlsx";
        sb.append(".xlsx");
        try {
            byte[] bytes = new ExcelPrintUtils().patchExport(errorList, excelPath);
            return FastDFSClientUtil.uploadFile(bytes, sb.toString(), null);
        } catch (IOException e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new ServiceException(e.getMessage());
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

    @Override
    public CfgRuleCalcDTO.ViewDTO view(String id) {
        CfgRuleCalcEntity cfgRuleCalc = getById(id);
        if (ObjectUtil.isEmpty(cfgRuleCalc)) {
            throw new ServiceException(ApiError.ERROR_SYS_TYPE_NOTFOUND, "试算配置");
        }
        CfgRuleCalcDTO.ViewDTO dto = BeanMapperUtils.map(CfgRuleCalcDTO.ViewDTO.class, cfgRuleCalc);
        dto.setSkuIds(cfgRuleCalc.getSkuJson().toList(String.class));
        dto.setShopIds(cfgRuleCalc.getShopJson().toList(String.class));
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(dto.getSkuIds());
        List<CfgRuleCalcDTO.SkuDTO> skuDTOList = skuVOS.stream()
                .map(v -> new CfgRuleCalcDTO.SkuDTO(v.getSkuId(), v.getSkuNo()))
                .collect(Collectors.toList());
        dto.setSkuList(skuDTOList);
        List<CfgRuleSalesFormulaCalcEntity> formulaList = cfgRuleSalesFormulaCalcService.listByCfgRuleCalcId(id);

        //默认日销量
        CfgRuleSalesFormulaCalcEntity defaultSalesFormula = formulaList.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode()) && CharSequenceUtil.equals(obj.getCfgRuleCalcId(), id)
        ).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(defaultSalesFormula)) {
            CfgRuleSalesFormulaCalcDTO.ViewDTO defaultViewDTO = BeanMapperUtils.map(CfgRuleSalesFormulaCalcDTO.ViewDTO.class, defaultSalesFormula);
            dto.setDefaultSalesQtyDTO(defaultViewDTO);
        }
        //动态日销量
        List<CfgRuleSalesFormulaCalcEntity> dynamicSalesFormulaList = formulaList.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode()) && CharSequenceUtil.equals(obj.getCfgRuleCalcId(), id)
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(dynamicSalesFormulaList)) {
            List<CfgRuleSalesFormulaCalcDTO.ViewDTO> dynamicViewList = BeanMapperUtils.copyList(CfgRuleSalesFormulaCalcDTO.ViewDTO.class, dynamicSalesFormulaList);
            dto.setDynamicSalesQtyList(dynamicViewList);
        }
        //固定日销量
        List<CfgRuleSalesFormulaCalcEntity> fixedSalesFormulaList = formulaList.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.FIXED.getCode()) && CharSequenceUtil.equals(obj.getCfgRuleCalcId(), id)
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(fixedSalesFormulaList)) {
            List<CfgRuleSalesFormulaCalcDTO.ViewDTO> fixedViewList = BeanMapperUtils.copyList(CfgRuleSalesFormulaCalcDTO.ViewDTO.class, fixedSalesFormulaList);
            dto.setFixedSalesQtyList(fixedViewList);
        }
        List<CfgRuleSalesDenoisingCalcEntity> denoisingList = cfgRuleSalesDenoisingCalcService.listByCfgRuleCalcId(id);
        dto.setSalesDenoisingList(BeanMapperUtils.copyList(CfgRuleSalesDenoisingCalcDTO.ViewDTO.class, denoisingList));
        return dto;
    }

    @Override
    public void addFavorite(CalcSalesInfoFavoriteDTO.AddDTO dto) {
        LoginUser user = UserContext.getDefaultLoginUser();
        int count = calcSalesInfoFavoriteService.count(Wrappers.<CalcSalesInfoFavoriteEntity>lambdaQuery()
                .eq(CalcSalesInfoFavoriteEntity::getUserId, user.getUid())
                .eq(CalcSalesInfoFavoriteEntity::getCfgRuleCalcId, dto.getCfgRuleCalcId()));
        if (count > 0) {
            throw new ServiceException("试算模板已关注，无需再次关注");
        }
        CalcSalesInfoFavoriteEntity calcSalesInfoFavoriteEntity = new CalcSalesInfoFavoriteEntity();
        calcSalesInfoFavoriteEntity.setCfgRuleCalcId(dto.getCfgRuleCalcId());
        calcSalesInfoFavoriteEntity.setUserId(user.getUid());
        calcSalesInfoFavoriteService.save(calcSalesInfoFavoriteEntity);
    }

    @Override
    public void cancelFavorite(CalcSalesInfoFavoriteDTO.CancelDTO dto) {
        LoginUser user = UserContext.getDefaultLoginUser();
        calcSalesInfoFavoriteService.remove(Wrappers.<CalcSalesInfoFavoriteEntity>lambdaQuery()
                .eq(CalcSalesInfoFavoriteEntity::getUserId, user.getUid())
                .eq(CalcSalesInfoFavoriteEntity::getCfgRuleCalcId, dto.getCfgRuleCalcId()));
    }


    /**
     * 处理去噪销量规则
     * @param addDTO 参数
     * @param cfgRuleCalcId 试算配置id
     */
    List<CfgRuleSalesDenoisingCalcEntity> handleSalesDenoising(CfgRuleCalcDTO.AddDTO addDTO, String cfgRuleCalcId) {
        List<CfgRuleSalesDenoisingCalcEntity> denoisingList = BeanMapperUtils.copyList(CfgRuleSalesDenoisingCalcEntity.class, addDTO.getSalesDenoisingList());
        validateDenoisingUniqueNames(denoisingList);
        int index = MathUtil.ONE;
        for (CfgRuleSalesDenoisingCalcEntity calcEntity : denoisingList) {
            calcEntity.setCfgRuleCalcId(cfgRuleCalcId);
            checkNumericValue(calcEntity);
            //时间
            List<LocalDate> dateList = calcEntity.getDateList();
            checkDate(dateList);
            calcEntity.setStartDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(0) : null);
            calcEntity.setEndDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(1) : null);
            calcEntity.setIndex(index);
            index++;
        }
        return denoisingList;
    }

    /**
     * 建议试算销量去噪名字
     * @param list 参数
     */
    private void validateDenoisingUniqueNames(List<CfgRuleSalesDenoisingCalcEntity> list) {
        String names = list.stream().collect(Collectors.groupingBy(CfgRuleSalesDenoisingCalcEntity::getName)).entrySet().stream()
                .filter(obj -> obj.getValue().size() > MathUtil.ONE).map(Map.Entry::getKey).distinct().collect(Collectors.joining(","));
        if (!StringUtils.isEmpty(names)) {
            throw new ServiceException("销量去噪名称【{}】唯一不能添加重复数据",names);
        }
    }

    /**
     * 校验百分比去噪、固定值去噪数值不能小于1
     * @param denoisingEntity 参数
     */
    private void checkNumericValue(CfgRuleSalesDenoisingCalcEntity denoisingEntity) {
        boolean isCompare = (CharSequenceUtil.equals(denoisingEntity.getDenoisingType(), CfgRuleSalesDenoisingDenoisingTypeEnum.PERCENTAGE.getCode())
                || CharSequenceUtil.equals(denoisingEntity.getDenoisingType(), CfgRuleSalesDenoisingDenoisingTypeEnum.FIXED_VALUE.getCode()))
                && MathUtil.compareTo(denoisingEntity.getEffectiveValue(), MathUtil.ZERO) <= MathUtil.ZERO;
        if (isCompare) {
            throw new ServiceException("百分比去噪、固定值去噪数值不能小于1");
        }
    }

    /**
     * 校验时间
     * @param dateList 时间
     */
    private void checkDate(List<LocalDate> dateList) {
        if (CollectionUtils.isNotEmpty(dateList)) {
            if (CollectionUtils.isEmpty(dateList) || dateList.size() != 2) {
                throw new ServiceException("时间区间不能为空");
            }
            if (dateList.get(0).isAfter(dateList.get(1))) {
                throw new ServiceException("开始时间不能大于结束时间");
            }
        }
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
        Map<String, List<CfgRuleSalesFormulaCalcEntity>> map = list.stream().collect(Collectors.groupingBy(CfgRuleSalesFormulaCalcEntity::getType));
        for (Map.Entry<String, List<CfgRuleSalesFormulaCalcEntity>> entry : map.entrySet()) {
            List<CfgRuleSalesFormulaCalcEntity> value = entry.getValue();
            //排序
            int index = MathUtil.ONE;
            validateUniqueNames(value);
            for (CfgRuleSalesFormulaCalcEntity salesFormula : value) {
                salesFormula.setIndex(index);
                //固定销量
                if (CfgRuleSalesFormulaDefaultTypeEnum.FIXED.getCode().equals(salesFormula.getDefaultType())) {
                    salesFormula.setPercentJson(JSONUtil.parseObj(new CfgRuleSalesFormulaDTO.PercentJsonDTO()));
                }
                //动态销量
                setDynamicFormula(salesFormula);
                //百分比json
                JSONObject percentJson = JSONUtil.parseObj(salesFormula.getPercentJsonDTO());
                salesFormula.setPercentJson(percentJson);

                //时间
                List<LocalDate> dateList = salesFormula.getDateList();
                checkDate(dateList);
                salesFormula.setStartDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(0) : null);
                salesFormula.setEndDate(CollectionUtils.isNotEmpty(dateList) ? dateList.get(1) : null);
                index ++;
            }
        }
        return list;
    }


    /**
     * 获取销量名称重复数据
     * @param value 参数
     */
    private void validateUniqueNames(List<CfgRuleSalesFormulaCalcEntity> value) {
        String names = value.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getName()))
                .collect(Collectors.groupingBy(CfgRuleSalesFormulaCalcEntity::getName))
                .entrySet().stream().filter(obj -> obj.getValue().size() > MathUtil.ONE).map(Map.Entry::getKey).distinct().collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(names)) {
            throw new ServiceException("销量名称【{}】唯一不能添加重复数据",names);
        }
    }

    /**
     * 设置动态销量系数
     * @param salesFormula 销量系数
     */
    private void setDynamicFormula(CfgRuleSalesFormulaCalcEntity salesFormula) {
        if (CfgRuleSalesFormulaDefaultTypeEnum.DYNAMIC.getCode().equals(salesFormula.getDefaultType())) {
            salesFormula.setFixedValue(MathUtil.ZERO);
            //默认配置需要校验百分比之和为100
            if (CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode().equals(salesFormula.getType())) {
                Integer totalRatio = salesFormula.getPercentJsonDTO().getTotalRatio();
                if (MathUtil.compareTo(totalRatio,100) != MathUtil.ZERO) {
                    throw new ServiceException("默认动态销量系数之和必须=100%；");
                }
            }
        }
    }
}
