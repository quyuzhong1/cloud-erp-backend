package com.erp.server.scm.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.excel.PurchasePriceDetailImportExcelDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.listener.PurchasePriceDetailExcelListener;
import com.erp.server.scm.mapper.PurchasePriceDetailMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品采购价格明细表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Slf4j
@Service
public class PurchasePriceDetailServiceImpl extends SuperServiceImpl<PurchasePriceDetailMapper, PurchasePriceDetailEntity> implements PurchasePriceDetailService {


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;
    /**
     * 检查sku 区间报价
     *
     * @param purchasePriceDetailList
     * @return void
     * @author yl
     * @date 2023-03-24 14:01
     */
    @Override
    public void checkSkuInterval(List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList) {
        if (CollectionUtils.isNotEmpty(purchasePriceDetailList)) {
            //以sku 分组
            Map<String, List<PurchasePriceDetailDTO.AddDTO>> map = purchasePriceDetailList.stream().collect(Collectors.groupingBy(PurchasePriceDetailDTO.AddDTO::getSkuId));
            for (Map.Entry<String, List<PurchasePriceDetailDTO.AddDTO>> item : map.entrySet()) {
                //skuId
                String skuId = item.getKey();
                //对应的报价
                List<PurchasePriceDetailDTO.AddDTO> skuPriceList = item.getValue();
                //查询是否有无区间的
                long noInterval = skuPriceList.stream().filter(s -> (s.getMaxQty() == null || s.getMaxQty() == 0) && (s.getMinQty() == null || s.getMinQty() == 0)).count();
                //表示有无区间的
                if (noInterval > 1) {
                    throw new ServiceException(ApiError.ERROR_REPEAT_SKU);
                } else {
                    //没有无区间 就要检查又没有不同区间的
                    List<Integer> intervalList = new ArrayList<>(10);
                    for (PurchasePriceDetailDTO.AddDTO interval : skuPriceList) {
                        if (interval.getMinQty() != null) {
                            intervalList.add(interval.getMinQty());
                        }
                        if (interval.getMaxQty() != null) {
                            intervalList.add(interval.getMaxQty());
                        }
                    }
                    //判断是否是按顺序的
                    boolean isSortedResult = isSorted(intervalList);
                    //当不是的时候
                    if (!isSortedResult) {
                        throw new ServiceException(ApiError.ERROR_INTERVAL_OVERLAP);
                    }
                    long distCount = intervalList.stream().distinct().count();
                    if (distCount != intervalList.size()) {
                        throw new ServiceException(ApiError.ERROR_INTERVAL_OVERLAP);
                    }
                }
            }
        }
    }

    /**
     * 添加明细
     *
     * @param purchasePriceId
     * @param purchasePriceDetailList
     * @return void
     * @author yl
     * @date 2023-03-24 15:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPriceDetail(String purchasePriceId, List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList) {
        if (CollectionUtils.isEmpty(purchasePriceDetailList)) {
            return;
        }
        List<PurchasePriceDetailEntity> addList = BeanMapper.copyList(purchasePriceDetailList, PurchasePriceDetailEntity.class);
        List<String> skuIds = addList.stream().map(PurchasePriceDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        LocalDate localDate = LocalDate.now();
        for (PurchasePriceDetailEntity item : addList) {
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuVO != null) {
                item.setSkuNo(skuVO.getSkuNo());
                item.setProductName(skuVO.getSpuName());
            }
            item.setPurchasePriceId(purchasePriceId);
            //失效时间
            item.setExpireDate(localDate.plusYears(100));
            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
            item.setTaxRate(rate);
        }
        this.saveBatch(addList);
    }


    /**
     * 根据价目表id 获取产品明细信息
     *
     * @param purchasePriceId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-27 9:48
     */
    @Override
    public List<PurchasePriceDetailDTO.ViewDTO> getByPurchasePriceId(String purchasePriceId) {
        List<PurchasePriceDetailEntity> list = this.getListByPurchasePriceId(purchasePriceId);
        List<PurchasePriceDetailDTO.ViewDTO> viewList = BeanMapper.copyList(list, PurchasePriceDetailDTO.ViewDTO.class);
        List<String> currencyIdList = viewList.stream().map(PurchasePriceDetailDTO.ViewDTO::getCurrency).collect(Collectors.toList());
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        BigDecimal hundred = new BigDecimal("100");

        for (PurchasePriceDetailDTO.ViewDTO item : viewList) {
            //币种
            String currency = item.getCurrency();
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
            item.setCurrencySymbol(currencySymbol);
            BigDecimal taxRate = item.getTaxRate();
            item.setTaxRate(taxRate.multiply(hundred));
        }

        return viewList;
    }


    /**
     * 修改产品明细
     *
     * @param purchasePriceId
     * @param purchasePriceDetailList
     * @return void
     * @author yl
     * @date 2023-03-27 11:18
     */
    @Override
    public void updatePriceDetail(String purchasePriceId, List<PurchasePriceDetailDTO.UpdateDTO> purchasePriceDetailList) {
        if (CollectionUtils.isEmpty(purchasePriceDetailList)) {
            return;
        }
        List<PurchasePriceDetailEntity> dbList = this.getListByPurchasePriceId(purchasePriceId);
        //获取到删除id集合
        List<String> deleteIdList = getDeleteIds(purchasePriceDetailList, dbList);
        List<PurchasePriceDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());


        this.removeByIds(deleteIdList);
        List<String> skuIds = purchasePriceDetailList.stream().map(PurchasePriceDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        List<PurchasePriceDetailEntity> saveOrUpdateList = new ArrayList<>(purchasePriceDetailList.size());
        LocalDate localDate = LocalDate.now();
        for (PurchasePriceDetailDTO.UpdateDTO item : purchasePriceDetailList) {
            PurchasePriceDetailEntity entity = new PurchasePriceDetailEntity();
            BeanMapper.copy(item, entity);
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuVO != null) {
                entity.setSkuNo(skuVO.getSkuNo());
                entity.setProductName(skuVO.getSpuName());
            }
            entity.setPurchasePriceId(purchasePriceId);
            //失效时间
            entity.setExpireDate(localDate.plusYears(100));
            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
            entity.setTaxRate(rate);
            saveOrUpdateList.add(entity);
        }

        //这是要添加的
        List<PurchasePriceDetailEntity> addList = saveOrUpdateList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //这是修改的
        List<PurchasePriceDetailEntity> updateList = saveOrUpdateList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(purchasePriceId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_PRICE.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(purchasePriceId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.PURCHASE_PRICE.getCode(), addPairList, "编辑操作");

        //修改的
        for (PurchasePriceDetailEntity update : updateList) {
            String id = update.getId();
            PurchasePriceDetailEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if(old!=null){
                moduleOperateLogService.addModuleOperateLogByObj(old,update, ModuleTypeEnum.PURCHASE_PRICE.getCode(),purchasePriceId,"","");
            }
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }


    /**
     * 下载模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-27 16:08
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/purchasePriceDetail.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("warehouse downloadTemplate  出错了 e==", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }

    }

    /**
     * 导入产品信息
     *
     * @param excelFile
     * @return com.erp.model.scm.dto.PurchasePriceDetailDTO.ImportDTO
     * @author yl
     * @date 2023-03-27 16:51
     */
    @Override
    public PurchasePriceDetailDTO.ImportDTO importFile(MultipartFile excelFile,List<String> skuIds,HttpServletResponse response) {
        //查询所有审核通过的sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        PurchasePriceDetailExcelListener excelListenerUtil = new PurchasePriceDetailExcelListener(skuList);
        try {
            EasyExcel.read(excelFile.getInputStream(), PurchasePriceDetailImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        PurchasePriceDetailDTO.ImportDTO result = new PurchasePriceDetailDTO.ImportDTO();
        //导入数据处理
        List<PurchasePriceDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();

        //导出错误数据
        List<PurchasePriceDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        result.setSuccessList(successList);
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "采购价目详情错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, PurchasePriceDetailImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }


    /**
     * 批量更改禁用状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 10:03
     */
    @Override
    public Boolean updateDisabled(UpdateStateDTO.BatchUpdateDTO dto) {
        List<String> ids = dto.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<PurchasePriceDetailEntity> detailList = this.listByIds(ids);
        Boolean disabled = dto.getDisabled();
        long count = detailList.stream().filter(d -> !d.getDisabled() == disabled).count();
        if (count != detailList.size()) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        detailList.forEach(d -> d.setDisabled(disabled));

        return this.updateBatchById(detailList);
    }


    /**
     * 获取到删除的集合
     *
     * @param purchasePriceDetailList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-27 11:27
     */
    private List<String> getDeleteIds(List<PurchasePriceDetailDTO.UpdateDTO> purchasePriceDetailList, List<PurchasePriceDetailEntity> dbList) {
        List<String> ids = purchasePriceDetailList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchasePriceDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(PurchasePriceDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<PurchasePriceDetailEntity> getListByPurchasePriceId(String purchasePriceId) {
        LambdaQueryWrapper<PurchasePriceDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchasePriceDetailEntity::getPurchasePriceId, purchasePriceId);
        return this.list(queryWrapper);

    }

    @Override
    public List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> getTaxPrice(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto) {
        Pair<String, List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>> pair = listPurchaseTaxPriceView(dto);
        String error = pair.getKey();
        if (StringUtils.isNotBlank(error)) {
            throw new ServiceException(new ApiResult(1, error));
        }
        return pair.getValue();
    }

    @Override
    public Pair<String, List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>> listPurchaseTaxPriceView(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto) {
        List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> list = baseMapper.getTaxPrice(dto);
        //未找到报价信息
        if (StringUtils.isNotBlank(dto.getSupplierId()) && CollectionUtils.isEmpty(list)) {
            String error = String.format("SKU【%s】未找到数量【%s】的供应商报价信息", dto.getSkuNo(), dto.getPurchaseQty());
            log.error(error);
            return new Pair<>(error, list);
        }
        List<String> currencyList = list.stream().map(PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> viewList = sysUserFeign.listByCurrency(currencyList);
        if (CollectionUtils.isEmpty(viewList)) {
            String error = "未发现币种对应符号";
            log.error(error);
            return new Pair<>(error, list);
        }
        for (PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO viewDTO : list) {
            CurrencyDTO.ViewDTO currencyDTO = viewList.stream().filter(obj -> obj.getId().equals(viewDTO.getCurrency())).findFirst().orElse(null);
            viewDTO.setCurrencySymbol(currencyDTO.getSymbol());
        }
        return new Pair<>("", list);
    }


    /**
     * 判断是否按顺序排序
     *
     * @param list
     * @return boolean
     * @author yl
     * @date 2023-03-24 14:28
     */
    private boolean isSorted(List<Integer> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) > list.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

}
