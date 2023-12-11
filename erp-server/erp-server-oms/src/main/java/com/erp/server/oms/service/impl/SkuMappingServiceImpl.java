package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.SearchType;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.excel.SkuMappingImportExcelDTO;
import com.erp.model.oms.dto.excel.SkuMappingWarehouseImportExcelDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.oms.constant.OmsConstant;
import com.erp.server.oms.listener.SkuMappingExcelListener;
import com.erp.server.oms.listener.SkuMappingWarehouseExcelListener;
import com.erp.server.oms.mapper.SkuMappingMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.ShopInfoService;
import com.erp.server.oms.service.SkuMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * sku 对照表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Service
@Slf4j
public class SkuMappingServiceImpl extends SuperServiceImpl<SkuMappingMapper, SkuMappingEntity> implements SkuMappingService {


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private ListingInfoService listingInfoService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;

    @Override
    public void downloadTemplate(String type, HttpServletResponse response) {
        if (StringUtils.isBlank(type)) {
            throw new ServiceException("下载模板类型不能为空");
        }
        //平台
        String platform = RuleTypeEnum.PLATFORM.getCode();
        //库存
        String warehouse = RuleTypeEnum.WAREHOUSE.getCode();
        List<String> typeList = Arrays.asList(platform, warehouse);
        if (!typeList.contains(type)) {
            throw new ServiceException("下载模板类型有误");
        }
        String path = "classpath:excel/skuMappingTemplate.xlsx";
        if (warehouse.equals(type)) {
            path = "classpath:excel/skuMappingWarehouseTemplate.xlsx";
        }
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
            log.error("SkuMaping downloadTemplate  出错了 e>>>>>>>{}", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }


    /**
     * 导入sku对照信息
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-29 11:01
     */
    @Override
    public Boolean importExcel(MultipartFile excelFile, String type, HttpServletResponse response) {
        if (StringUtils.isBlank(type)) {
            throw new ServiceException("导入类型不能为空");
        }
        //平台
        String platform = RuleTypeEnum.PLATFORM.getCode();
        //库存
        String warehouse = RuleTypeEnum.WAREHOUSE.getCode();
        List<String> typeList = Arrays.asList(platform, warehouse);
        if (!typeList.contains(type)) {
            throw new ServiceException("导入类型有误");
        }
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        List<SkuMappingEntity> skuMappingList = this.listEffectiveList();
        List<ListingInfoEntity> list = listingInfoService.list();
        if (platform.equals(type)) {
            String key = DictBasicTypeEnum.SALES_PLATFORM.getType();
            List<DictBasicDTO.ViewDTO> dictBasicList = dictBasicService.getByKey(key);
            List<ShopInfoEntity> shopInfoList = shopInfoService.list();
            SkuMappingExcelListener excelListenerUtil = new SkuMappingExcelListener(this, skuList, shopInfoList, skuMappingList, dictBasicList, list, listingInfoService);
            try {
                EasyExcel.read(excelFile.getInputStream(), SkuMappingImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            } catch (Exception e) {
                log.error("sku 对照表导入错误！>>>>{}", e);
                return Boolean.FALSE;
            }
            List<SkuMappingImportExcelDTO> errorList = excelListenerUtil.getErrorList();
            if (errorList.size() > 0) {
                String fileName = "sku对照错误信息";
                ExcelUtil.export(fileName, "error", errorList, SkuMappingImportExcelDTO.class, response);
                return Boolean.FALSE;
            }
        }
        //仓库sku 对照
        if (warehouse.equals(type)) {
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();
            warehouseList = warehouseList.stream().filter(w -> !w.getDisabled()).collect(Collectors.toList());
            SkuMappingWarehouseExcelListener excelListenerUtil = new SkuMappingWarehouseExcelListener(this, skuList, skuMappingList, warehouseList, list, listingInfoService);
            try {
                EasyExcel.read(excelFile.getInputStream(), SkuMappingWarehouseImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            } catch (Exception e) {
                log.error("sku 对照表导入错误！>>>>{}", e);
                return Boolean.FALSE;
            }
            List<SkuMappingWarehouseImportExcelDTO> errorList = excelListenerUtil.getErrorList();
            if (errorList.size() > 0) {
                String fileName = "sku对照错误信息";
                ExcelUtil.export(fileName, "error", errorList, SkuMappingWarehouseImportExcelDTO.class, response);
                return Boolean.FALSE;
            }
        }


        return Boolean.TRUE;

    }

    /**
     * 平台sku对照表分页查询
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMapingDTO.PagingViewDTO>
     * @author yl
     * @date 2023-06-29 18:07
     */
    @Override
    public PagingVO<SkuMappingDTO.PagingViewDTO> paging(PagingDTO<SkuMappingDTO.PagingParamDTO> dto) {
        SkuMappingDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        String tabFlag = params.getTabFlag();
        Boolean matchResult = null;
        if (OmsConstant.ALREADY.equals(tabFlag)) {
            matchResult = Boolean.TRUE;
        }
        if (OmsConstant.NOT.equals(tabFlag)) {
            matchResult = Boolean.FALSE;
        }
        params.setType(RuleTypeEnum.PLATFORM.getCode());
        IPage pageData = baseMapper.paging(query, params, matchResult);
        List<SkuMappingDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        fillDb(list);
        return new PagingVO<>(pageData);

    }

    /**
     * 导出sku 对照表
     *
     * @param dto
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-30 9:35
     */
    @Override
    public Boolean exportPlatformSku(SkuMappingDTO.ExportDTO dto, HttpServletResponse response) {
        String tabFlag = dto.getTabFlag();
        Boolean matchResult = null;
        if (OmsConstant.ALREADY.equals(tabFlag)) {
            matchResult = Boolean.TRUE;
        }
        if (OmsConstant.NOT.equals(tabFlag)) {
            matchResult = Boolean.FALSE;
        }
        dto.setType(RuleTypeEnum.PLATFORM.getCode());
        List<SkuMappingDTO.PagingViewDTO> list = baseMapper.listExport(dto, matchResult);

        fillDb(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/PlatformSkuMapping.xlsx";
        String name = "sku对照列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("sku对照表导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 获取tab 列表
     *
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SkuMapingDTO.TabListDTO>
     * @author yl
     * @date 2023-06-30 9:45
     */
    @Override
    public List<SkuMappingDTO.TabListDTO> tabList(SkuMappingDTO.FindTabDTO dto) {
        List<SkuMappingDTO.TabListDTO> resultList = new ArrayList<>(3);
        List<SkuMappingDTO.MatchCountDTO> matchCountList = baseMapper.listMatchCount(dto);
        //所有
        SkuMappingDTO.TabListDTO all = new SkuMappingDTO.TabListDTO();
        int allCount = matchCountList.stream().mapToInt(SkuMappingDTO.MatchCountDTO::getCount).sum();
        all.setCount(allCount);
        all.setTabFlag(SearchType.ALL);
        resultList.add(all);
        //未匹配
        SkuMappingDTO.TabListDTO not = new SkuMappingDTO.TabListDTO();
        int notCount = matchCountList.stream().filter(m -> !m.getMatchResult()).findFirst().
                map(SkuMappingDTO.MatchCountDTO::getCount).orElse(0);
        not.setCount(notCount);
        not.setTabFlag(OmsConstant.NOT);
        resultList.add(not);

        //已匹配
        SkuMappingDTO.TabListDTO already = new SkuMappingDTO.TabListDTO();
        int alreadyCount = matchCountList.stream().filter(m -> m.getMatchResult()).findFirst().
                map(SkuMappingDTO.MatchCountDTO::getCount).orElse(0);
        already.setCount(alreadyCount);
        already.setTabFlag(OmsConstant.ALREADY);
        resultList.add(already);
        return resultList;
    }

    /**
     * 更改sku 对照表
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-06-30 10:21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updatePlatformSku(SkuMappingDTO.UpdatePlatformDTO dto) {
        String id = dto.getId();
        SkuMappingEntity skuMaping = this.getById(id);
        if (Objects.isNull(skuMaping)) {
            throw new ServiceException(ApiError.ERROR_92051);
        }
        String productSkuId = dto.getProductSkuId();
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(Arrays.asList(productSkuId));
        if (CollectionUtils.isEmpty(skuVOList)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }
        String platformDict = dto.getDictPlatform();
        DictBasicEntity dictBasic = dictBasicService.getByTypeAndValue(DictBasicTypeEnum.SALES_PLATFORM.getType(), platformDict);
        if (Objects.isNull(dictBasic)) {
            throw new ServiceException(ApiError.ERROR_92053);
        }
        String platformSkuNo = dto.getPlatformSkuNo();
        ListingInfoEntity listing = listingInfoService.getByPlatformSkuNo(platformDict, platformSkuNo);
        if (Objects.isNull(listing)) {
            throw new ServiceException("平台sku不存在");
        }
        checkExist(id, listing.getId());
        // listing 更新匹配关系
        listing.setMatchResult(true);
        if (!listingInfoService.updateById(listing)) {
            throw new ServiceException("[listing] 更新失败");
        }
        // 无修改
        if (skuMaping.getProductSkuId().equalsIgnoreCase(productSkuId)) {
            return skuMaping.getId();
        }
        LocalDateTime now = LocalDateTime.now();
        skuMaping.setExpireTime(now);
        skuMaping.setIsExpire(Boolean.TRUE);
        if (!this.updateById(skuMaping)) {
            throw new ServiceException("[SkuMapping] 历史映射修改失败");
        }
        SkuMappingEntity addSkuMaping = new SkuMappingEntity();
        addSkuMaping.setShopId(dto.getShopId());
        addSkuMaping.setDictPlatform(skuMaping.getDictPlatform());
        addSkuMaping.setPlatformName(skuMaping.getPlatformName());
        addSkuMaping.setProductSkuNo(skuVOList.get(0).getSkuNo());
        addSkuMaping.setProductSkuId(productSkuId);
        addSkuMaping.setListingId(listing.getId());
        addSkuMaping.setType(RuleTypeEnum.PLATFORM);
        addSkuMaping.setIsExpire(Boolean.FALSE);
        addSkuMaping.setEffectiveTime(now);
        addSkuMaping.setExpireTime(now.plusYears(100));
        if (!this.save(addSkuMaping)) {
            throw new ServiceException("[SkuMapping] 映射修改新增失败");
        }
        return addSkuMaping.getId();
    }


    /**
     * 销售订单添加客户sku
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMapingDTO.ProductSkuInfoDTO>
     * @author yl
     * @date 2023-07-01 9:19
     */
    @Override
    public PagingVO<SkuMappingDTO.ProductSkuInfoDTO> listPaging(PagingDTO<SkuMappingDTO.ListParamDTO> dto) {
        SkuMappingDTO.ListParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.listPaging(query, params);
        List<SkuMappingDTO.ProductSkuInfoDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> skuIdList = list.stream().map(SkuMappingDTO.ProductSkuInfoDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (SkuMappingDTO.ProductSkuInfoDTO item : list) {
            String skuId = item.getSkuId();
            String skuName = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().map(SkuVO::getSkuName).orElse("");
            item.setSkuName(skuName);
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 添加库存sku 对照信息
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-08-18 16:32
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addWarehouseSku(SkuMappingDTO.AddWarehouseSkuDTO dto) {
        String skuId = dto.getProductSkuId();
        String warehouseSkuNo = dto.getWarehouseSkuNo();
        String warehouseId = dto.getWarehouseId();
        String productSkuId = dto.getProductSkuId();
        RuleTypeEnum warehouseType = RuleTypeEnum.WAREHOUSE;
        // 产品SKU在该仓库是否已绑定
        SkuMappingEntity oldSkuMappingEntity = this.getByAttribute(productSkuId, warehouseId, warehouseType);
        if (null != oldSkuMappingEntity) {
            // 产品SKU【{}】已在【{}】仓库绑定
            throw new ServiceException(ApiError.ERROR_DUPLICATE_MAPPING_SKU_ID, oldSkuMappingEntity.getProductSkuNo(), oldSkuMappingEntity.getWarehouseName());
        }
        String warehouseProductName = dto.getWarehouseProductName();
        String listingId = listingInfoService.addWarehouseSku(warehouseSkuNo, warehouseProductName);
        if (StringUtils.isBlank(listingId)) {
            throw new ServiceException(warehouseSkuNo + "未找到");
        }
        checkWarehouseSkuExist("", listingId, warehouseId, skuId);
//        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        // 查询当前仓库的平台类型
        List<WarehouseDTO.ListDTO> warehouseList = wmsWarehouseFeign.listByIds(Collections.singletonList(dto.getWarehouseId()));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException("仓库不存在");
        }
        WarehouseDTO.ListDTO currenWareHouse = warehouseList.stream().findFirst().orElse(null);
        OmsPlatformEnum platformEnum = OmsPlatformEnum.getByCode(currenWareHouse.getDictPlatform());
        if (null != platformEnum) {
            throw new ServiceException(platformEnum.getName() + "服务商仓库不允许新增");
        }
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(Arrays.asList(skuId));
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException("sku不存在");
        }

        SkuMappingEntity skuMappingEntity = new SkuMappingEntity();
        skuMappingEntity.setWarehouseId(warehouseId);
        skuMappingEntity.setWarehouseName(warehouseList.get(0).getName());
        skuMappingEntity.setType(RuleTypeEnum.WAREHOUSE);
        skuMappingEntity.setProductSkuId(skuId);
        skuMappingEntity.setProductSkuNo(skuList.get(0).getSkuNo());
        skuMappingEntity.setProductName(skuList.get(0).getSkuName());
        skuMappingEntity.setListingId(listingId);
        skuMappingEntity.setDictPlatform("");
        skuMappingEntity.setHasMappingAll(dto.checkAndGetHasMappingAll());
        LocalDateTime now = LocalDateTime.now();
        //生效时间
        skuMappingEntity.setEffectiveTime(now);
        skuMappingEntity.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
        if (this.save(skuMappingEntity)) {
            return skuMappingEntity.getId();
        }
        return "";
    }


    /**
     * 导出库存sku 对照表
     *
     * @param dto
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-21 10:22
     */
    @Override
    public Boolean exportWarehouseSku(SkuMappingDTO.ExportWarehouseSkuDTO dto, HttpServletResponse response) {
        String tabFlag = dto.getTabFlag();
        Boolean matchResult = null;
        if (OmsConstant.ALREADY.equals(tabFlag)) {
            matchResult = Boolean.TRUE;
        }
        if (OmsConstant.NOT.equals(tabFlag)) {
            matchResult = Boolean.FALSE;
        }
        dto.setType(RuleTypeEnum.WAREHOUSE.getCode());
        List<SkuMappingDTO.WarehousePagingViewDTO> list = baseMapper.listWarehouseExport(dto, matchResult);

        fillWarehouseDb(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/WarehouseSkuMapping.xlsx";
        String name = "sku对照列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("sku对照表导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;


    }

    /**
     * 更改库存sku 对照
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-08-21 11:40
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateWarehouseSku(SkuMappingDTO.UpdateWarehouseSkuDTO dto) {
        String id = dto.getId();
        SkuMappingEntity skuMapping = this.getById(id);
        if (Objects.isNull(skuMapping)) {
            throw new ServiceException(ApiError.ERROR_92051);
        }
        String productSkuId = dto.getProductSkuId();
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(Arrays.asList(productSkuId));
        if (CollectionUtils.isEmpty(skuVOList)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }
        String warehouseSkuNo = dto.getWarehouseSkuNo();
        String warehouseId = dto.getWarehouseId();

        ListingInfoEntity listingInfo = listingInfoService.getById(skuMapping.getListingId());
        String listingId = "";
        if (Objects.nonNull(listingInfo)) {
            listingId = listingInfo.getId();
            // listing 更新匹配关系
            listingInfo.setMatchResult(true);
            if (!listingInfoService.updateById(listingInfo)) {
                throw new ServiceException("[listing] 更新失败");
            }
        } else {
            String warehouseProductName = dto.getWarehouseProductName();
            listingId = listingInfoService.addWarehouseSku(warehouseSkuNo, warehouseProductName);
        }
        if (StringUtils.isBlank(listingId)) {
            throw new ServiceException(warehouseSkuNo + "未找到");
        }
        checkWarehouseSkuExist(id, listingId, warehouseId, productSkuId);
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException("仓库不存在");
        }
        //更改原有的
        LocalDateTime now = LocalDateTime.now();
        skuMapping.setExpireTime(now);
        skuMapping.setIsExpire(Boolean.TRUE);
        this.updateById(skuMapping);
        SkuMappingEntity addSkuMapping = new SkuMappingEntity();
        addSkuMapping.setWarehouseId(warehouseId);
        addSkuMapping.setWarehouseName(warehouseList.get(0).getName());
        addSkuMapping.setType(RuleTypeEnum.WAREHOUSE);
        addSkuMapping.setProductSkuId(productSkuId);
        addSkuMapping.setProductSkuNo(skuVOList.get(0).getSkuNo());
        addSkuMapping.setListingId(listingId);
        addSkuMapping.setDictPlatform(skuMapping.getDictPlatform());
        addSkuMapping.setPlatformName(skuMapping.getPlatformName());
        //生效时间
        addSkuMapping.setEffectiveTime(now);
        addSkuMapping.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
        if (this.save(addSkuMapping)) {
            return addSkuMapping.getId();
        }
        return "";

    }


    @Override
    public List<SkuMappingDTO.ListSkuDTO> listBySkuNoList(List<SkuMappingDTO.ListSkuParamDTO> dataList) {
        if (CollectionUtils.isEmpty(dataList)) {
            return Collections.EMPTY_LIST;
        }
        List<String> skuNoList = dataList.stream().map(SkuMappingDTO.ListSkuParamDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        if (CollectionUtils.isEmpty(skuList)) {
            return Collections.EMPTY_LIST;
        }
        //获取到skumappping 的对应关系
        List<SkuMappingEntity> list = lambdaQuery().in(SkuMappingEntity::getProductSkuNo, skuNoList).eq(SkuMappingEntity::getIsExpire, Boolean.FALSE).list();

        List<ListingInfoEntity> listingList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> listingIds = list.stream().map(SkuMappingEntity::getListingId).collect(Collectors.toList());
            listingList = listingInfoService.listByIds(listingIds);
        }
        //库存
        RuleTypeEnum warehouseType = RuleTypeEnum.WAREHOUSE;
        List<SkuMappingDTO.ListSkuDTO> resultList = new ArrayList<>();
        for (SkuMappingDTO.ListSkuParamDTO listSkuParamDTO : dataList) {
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuNo().equals(listSkuParamDTO.getSkuNo())).findFirst().orElse(new SkuVO());
            SkuMappingDTO.ListSkuDTO listSkuDTO = new SkuMappingDTO.ListSkuDTO();
            listSkuDTO.setProductSkuId(skuVO.getSkuId());
            listSkuDTO.setProductSkuNo(listSkuParamDTO.getSkuNo());
            listSkuDTO.setProductName(skuVO.getSkuName());
            listSkuDTO.setAdvicePrice(skuVO.getRetailPrice());
            listSkuDTO.setVariantProperty(skuVO.getVariantProperty());
            listSkuDTO.setImageUrl(skuVO.getSkuImagesUrl());
            listSkuDTO.setTaxCost(MathUtil.compareTo(skuVO.getActualTaxCost(), MathUtil.ZERO) == MathUtil.ZERO ? skuVO.getTargetTaxCost() : skuVO.getActualTaxCost());
            listSkuDTO.setWarehouseId(listSkuParamDTO.getWarehouseId());
            listSkuDTO.setDictPlatform(listSkuParamDTO.getDictPlatform());
            //查询库存sku映射
            SkuMappingEntity warehouseSkuMapping = list.stream().filter(
                    obj -> obj.getProductSkuId().equals(listSkuDTO.getProductSkuId()) &&
                            obj.getWarehouseId().equals(listSkuParamDTO.getWarehouseId()) &&
                            warehouseType.equals(obj.getType())
            ).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(warehouseSkuMapping)) {
                //库存sku信息
                ListingInfoEntity warehouseListing = listingList.stream().filter(obj -> obj.getId().equals(warehouseSkuMapping.getListingId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(warehouseListing)) {
                    listSkuDTO.setWarehouseSkuNo(warehouseListing.getPlatformSkuNo());
                    listSkuDTO.setWarehouseProductName(warehouseListing.getPlatformSkuName());

                }
            }
            //查询平台sku信息
            SkuMappingEntity platformSkuMapping = list.stream()
                    .filter(obj -> obj.getProductSkuId().equals(listSkuDTO.getProductSkuId())
                            && StringUtils.isEmpty(obj.getWarehouseId())
                            && obj.getDictPlatform().equals(listSkuParamDTO.getDictPlatform())
                            && !warehouseType.equals(obj.getType())
                    ).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(platformSkuMapping)) {
                //库存sku信息
                ListingInfoEntity platformListing = listingList.stream().filter(obj -> obj.getId().equals(platformSkuMapping.getListingId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(platformListing)) {
                    listSkuDTO.setPlatformSkuNo(platformListing.getPlatformSkuNo());
                    listSkuDTO.setPlatformProductName(platformListing.getPlatformSkuName());
                    listSkuDTO.setPlatformSpuNo(platformListing.getPlatformSpuNo());
                }
            }
            resultList.add(listSkuDTO);
        }
        return resultList;
    }

    /**
     * 根据平台sku noList 获取对应的数据
     *
     * @param platformSkuNoList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.ListSkuDTO>
     * @author yl
     * @date 2023-09-04 17:11
     */
    @Override
    public List<SkuMappingDTO.SkuDTO> listByPlatformSkuNoList(List<String> platformSkuNoList) {
        if (CollectionUtils.isEmpty(platformSkuNoList)) {
            return Collections.emptyList();
        }
        List<SkuMappingDTO.SkuDTO> list = baseMapper.listByPlatformSkuNoList(platformSkuNoList);
        String platformCode = RuleTypeEnum.PLATFORM.getCode();
        for (SkuMappingDTO.SkuDTO item : list) {
            String type = item.getType();
            String platformSkuNo = item.getPlatformSkuNo();
            String platformProductName = item.getPlatformProductName();
            if (!platformCode.equals(type)) {
                item.setFlagSkuNo(platformSkuNo);
                item.setFlagProductName(platformProductName);
                item.setPlatformSkuNo("");
                item.setPlatformProductName("");
            }
        }
        return list;
    }

    @Override
    public List<SkuMappingDTO.MappingSkuViewDTO> listByPlatformSkuNoAndPlatform(ListingInfoParamDTO listingInfoParamDTO) {
        return baseMapper.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
    }

    @Override
    public List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> listStockSkuNoByProductSkuIds(List<String> productSkuIdList) {
        if (CollectionUtils.isEmpty(productSkuIdList)) {
            return Collections.emptyList();
        }
        List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> listStockSkuNoByProductSkuIdViews = baseMapper.listStockSkuNoByProductSkuIds(productSkuIdList);

        List<String> skuIds = listStockSkuNoByProductSkuIdViews.stream().map(req -> req.getProductSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.getSkuInfoByIds(skuIds);

        for (SkuMappingDTO.ListStockSkuNoByProductSkuIdView view : listStockSkuNoByProductSkuIdViews) {
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(view.getProductSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                view.setProductSkuName(skuVO.getSkuName());
            }
        }
        return listStockSkuNoByProductSkuIdViews;
    }

    /**
     * 库存sku 对照表分页
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMappingDTO.WarehousePagingViewDTO>
     * @author yl
     * @date 2023-08-21 9:56
     */
    @Override
    public PagingVO<SkuMappingDTO.WarehousePagingViewDTO> warehousePaging(PagingDTO<SkuMappingDTO.WarehousePagingParamDTO> dto) {
        SkuMappingDTO.WarehousePagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        String tabFlag = params.getTabFlag();
        Boolean matchResult = null;
        if (OmsConstant.ALREADY.equals(tabFlag)) {
            matchResult = Boolean.TRUE;
        }
        if (OmsConstant.NOT.equals(tabFlag)) {
            matchResult = Boolean.FALSE;
        }
        params.setType(RuleTypeEnum.WAREHOUSE.getCode());
        IPage pageData = baseMapper.warehousePaging(query, params, matchResult);
        List<SkuMappingDTO.WarehousePagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        fillWarehouseDb(list);
        return new PagingVO<>(pageData);
    }


    /**
     * 填充库存sku
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-08-21 10:15
     */
    private void fillWarehouseDb(List<SkuMappingDTO.WarehousePagingViewDTO> list) {
        List<String> skuIdList = list.stream().map(SkuMappingDTO.WarehousePagingViewDTO::getProductSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (SkuMappingDTO.WarehousePagingViewDTO item : list) {
            Boolean matchResult = item.getMatchResult();
            String skuId = item.getProductSkuId();
            String skuName = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().map(SkuVO::getSkuName).orElse("");
            item.setProductName(skuName);
            item.setMatchResultStr(matchResult ? "已匹配" : "未匹配");
            item.setHasMappingAllStr(item.getHasMappingAll() ? "是" : "否");
        }

    }

    /**
     * 检查库存sku 是否存在
     *
     * @param id
     * @param listingId
     * @param warehouseId
     */
    private void checkWarehouseSkuExist(String id, String listingId, String warehouseId, String skuId) {
        LambdaQueryWrapper<SkuMappingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuMappingEntity::getListingId, listingId);
        queryWrapper.eq(SkuMappingEntity::getIsExpire, Boolean.FALSE);
        queryWrapper.eq(SkuMappingEntity::getType, RuleTypeEnum.WAREHOUSE);
        queryWrapper.eq(SkuMappingEntity::getWarehouseId, warehouseId);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(SkuMappingEntity::getId, id);
        }
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException("同仓库库存SKU只能对应一个产品SKU");
        }

        List<SkuMappingEntity> list = this.lambdaQuery().
                ne(StringUtils.isNotBlank(id), SkuMappingEntity::getId, id).
                eq(SkuMappingEntity::getWarehouseId, warehouseId).
                eq(SkuMappingEntity::getProductSkuId, skuId).
                eq(SkuMappingEntity::getType, RuleTypeEnum.WAREHOUSE).list();
        long skuCount = list.stream().map(SkuMappingEntity::getListingId).distinct().count();
        if (skuCount > 0) {
            throw new ServiceException("SKU在该仓库已关联其他库存SKU，请更换其他SKU");
        }
    }

    private void checkExist(String id, String listingId) {
        LambdaQueryWrapper<SkuMappingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuMappingEntity::getListingId, listingId);
        queryWrapper.eq(SkuMappingEntity::getIsExpire, Boolean.FALSE);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(SkuMappingEntity::getId, id);
        }
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_92052);
        }


    }

    /**
     * 填充数据
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-06-29 19:15
     */
    private void fillDb(List<SkuMappingDTO.PagingViewDTO> list) {
        List<String> skuIdList = list.stream().map(SkuMappingDTO.PagingViewDTO::getProductSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (SkuMappingDTO.PagingViewDTO item : list) {
            Boolean matchResult = item.getMatchResult();
            String skuId = item.getProductSkuId();
            String skuName = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().map(SkuVO::getSkuName).orElse("");
            item.setProductName(skuName);
            item.setMatchResultStr(matchResult ? "已匹配" : "未匹配");
        }
    }


    /**
     * 获取到有效的sku 对照表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.entity.SkuMapingEntity>
     * @author yl
     * @date 2023-06-29 11:29
     */
    private List<SkuMappingEntity> listEffectiveList() {
        List<SkuMappingEntity> resultList = this.lambdaQuery().eq(SkuMappingEntity::getIsExpire, Boolean.FALSE).list();
        return resultList;
    }

    @Override
    public List<SkuMappingEntity> listByListingIds(List<String> listingIds) {
        return lambdaQuery()
                .in(SkuMappingEntity::getListingId, listingIds)
                .eq(SkuMappingEntity::getIsExpire, false)
                .list();
    }

    @Override
    public SkuMappingEntity getByAttribute(String productSkuId, String warehouseId, RuleTypeEnum typeEnum) {
        return lambdaQuery()
                .eq(SkuMappingEntity::getProductSkuId, productSkuId)
                .eq(SkuMappingEntity::getWarehouseId, warehouseId)
                .eq(SkuMappingEntity::getType, typeEnum)
                .eq(SkuMappingEntity::getIsExpire, false)
                .last(" LIMIT 1")
                .one();
    }

    @Override
    public Boolean add(SkuMappingDTO.AddSkuMappingDTO addSkuMappingDTO) {
        SkuMappingEntity entity = new SkuMappingEntity();
        BeanMapperUtils.copy(addSkuMappingDTO,entity);
        LocalDateTime now=LocalDateTime.now();
        entity.setEffectiveTime(LocalDateTime.now());
        entity.setExpireTime(now.plusYears(MathUtil.NUMBER_100));
        return this.save(entity);
    }

    @Override
    public List<ListingInfoWithSkuMappingDTO> findListDto(ListingInfoParamDTO dto) {
        return baseMapper.listByParams(dto);
    }

}
