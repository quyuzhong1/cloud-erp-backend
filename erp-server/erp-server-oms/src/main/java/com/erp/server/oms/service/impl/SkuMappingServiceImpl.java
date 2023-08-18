package com.erp.server.oms.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.SearchType;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.excel.SkuMapingImportExcelDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.DictBasicEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.constant.OmsConstant;
import com.erp.server.oms.listener.SkuMappingExcelListener;
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

    @Override
    public void downloadTemplate(HttpServletResponse response) {

        String path = "classpath:excel/skuMapingTemplate.xlsx";
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
            log.error("SkuMaping downloadTemplate  出错了 e>>>>>>>", e);
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
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        List<ShopInfoEntity> shopInfoList = shopInfoService.list();
        List<SkuMappingEntity> skuMappingList = this.listEffectiveList();
        String key = DictBasicEnum.PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictBasicList = dictBasicService.getByKey(key);
        List<ListingInfoEntity> list = listingInfoService.list();

        SkuMappingExcelListener excelListenerUtil = new SkuMappingExcelListener(this, skuList, shopInfoList, skuMappingList, dictBasicList,list);
        try {
            EasyExcel.read(excelFile.getInputStream(), SkuMapingImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("sku 对照表导入错误！", e);
            return Boolean.FALSE;
        }
        List<SkuMapingImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "sku对照错误信息";
            ExcelUtil.export(fileName, "error", errorList, SkuMapingImportExcelDTO.class, response);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;

    }

    /**
     * 分页查询
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
    public Boolean exportSkuMaping(SkuMappingDTO.ExportDTO dto, HttpServletResponse response) {
        String tabFlag = dto.getTabFlag();
        Boolean matchResult = null;
        if (OmsConstant.ALREADY.equals(tabFlag)) {
            matchResult = Boolean.TRUE;
        }
        if (OmsConstant.NOT.equals(tabFlag)) {
            matchResult = Boolean.FALSE;
        }
        List<SkuMappingDTO.PagingViewDTO> list = baseMapper.listExport(dto, matchResult);
        fillDb(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/skuMaping.xlsx";
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
    public String updateSkuMaping(SkuMappingDTO.UpdateDTO dto) {
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
        String platformDict = dto.getPlatformDict();
        DictBasicEntity dictBasic = dictBasicService.getByTypeAndValue(DictBasicEnum.PLATFORM.getType(), platformDict);
        if (Objects.isNull(dictBasic)) {
            throw new ServiceException(ApiError.ERROR_92053);

        }
        checkExist(id, dto.getPlatformDict(), dto.getPlatformSkuNo(), dto.getProductSkuId());
        LocalDateTime now = LocalDateTime.now();
        skuMaping.setExpireTime(now);
        skuMaping.setIsExpire(Boolean.TRUE);
        this.updateById(skuMaping);
        SkuMappingEntity addSkuMaping = new SkuMappingEntity();
        addSkuMaping.setShopId(dto.getShopId());
        addSkuMaping.setPlatformName(dictBasic.getName());
        addSkuMaping.setProductSkuNo(skuVOList.get(0).getSkuNo());
        addSkuMaping.setProductSkuId(productSkuId);
        addSkuMaping.setIsExpire(Boolean.FALSE);
        addSkuMaping.setEffectiveTime(now);
        addSkuMaping.setExpireTime(now.plusYears(100));
        this.save(addSkuMaping);
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

    private void checkExist(String id, String platformDict, String platformSkuNo, String skuId) {
        LambdaQueryWrapper<SkuMappingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuMappingEntity::getDictPlatform, platformDict);
        queryWrapper.eq(SkuMappingEntity::getPlatformName, platformSkuNo);
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
}
