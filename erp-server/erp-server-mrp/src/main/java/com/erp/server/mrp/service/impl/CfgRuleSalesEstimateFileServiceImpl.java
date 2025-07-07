package com.erp.server.mrp.service.impl;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.CfgRuleSalesEstimateFileDTO;
import com.erp.model.mrp.entity.CfgRuleSalesEstimateFileEntity;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.mrp.es.service.CustomerSalesEstimateEsService;
import com.erp.server.mrp.listener.SalesEstimateExcelFileListener;
import com.erp.server.mrp.mapper.CfgRuleSalesEstimateFileMapper;
import com.erp.server.mrp.service.CfgRuleSalesEstimateFileService;
import com.erp.server.mrp.service.CfgRuleSalesQtyService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 预估销量文件 服务实现类
 * </p>
 *
 * @author liao
 * @since 2025-02-19
 */
@Service
public class CfgRuleSalesEstimateFileServiceImpl extends SuperServiceImpl<CfgRuleSalesEstimateFileMapper, CfgRuleSalesEstimateFileEntity> implements CfgRuleSalesEstimateFileService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private CustomerSalesEstimateEsService customerSalesEstimateEsService;

    @Resource
    @Lazy
    private CfgRuleSalesQtyService cfgRuleSalesQtyService;
    @Resource
    private FileFeign filefeign;

    @Override
    public PagingVO<CfgRuleSalesEstimateFileDTO.PagingView> filePage(PagingDTO<CfgRuleSalesEstimateFileDTO.PagingParamDTO> params) {
        Page<CfgRuleSalesEstimateFileEntity> page = page(new Page<>(params.getCurrPage(), params.getPageSize()),
                Wrappers.<CfgRuleSalesEstimateFileEntity>lambdaQuery().eq(CfgRuleSalesEstimateFileEntity::getSalesQtyId, params.getParams().getCfgRuleSalesQtyId()));
        List<CfgRuleSalesEstimateFileDTO.PagingView> pagingViews = BeanMapperUtils.copyList(CfgRuleSalesEstimateFileDTO.PagingView.class, page.getRecords());
        return new PagingVO<>(pagingViews, (int) page.getTotal(), params.getPageSize(), params.getCurrPage());
    }

    @Override
    public void downloadRuleTemplate(HttpServletResponse response) {
        String path = "classpath:excel/cfgSalesEstimateTemplate.xlsx";
        String excelName = "template.xlsx";
        ExcelUtil.downloadTemplate(path,excelName,response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importFile(MultipartFile excelFile, String platform, HttpServletResponse response) {

        CfgRuleSalesQtyEntity salesQty = cfgRuleSalesQtyService.getOne(Wrappers.<CfgRuleSalesQtyEntity>lambdaQuery()
                .eq(CfgRuleSalesQtyEntity::getPlatform, platform)
                .eq(CfgRuleSalesQtyEntity::getRefId,"")
                .last("LIMIT 1")
        );
        List<SkuVO> skuVOS = plmTaskFeign.listApproveSku();
        Map<String, String> skuMap = skuVOS.stream()
                .collect(Collectors.toMap(SkuVO::getSkuNo, SkuVO::getSkuId, (o1, o2) -> o1));
        List<ShopInfoEntity> shopInfoList = shopInfoFeign.list().getData();
        List<com.erp.model.oms.entity.DictBasicEntity> salesPlatformList = FeignQuery.create(com.erp.model.oms.entity.DictBasicEntity.class)
                .eq(com.erp.model.oms.entity.DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType())
                .eq(com.erp.model.oms.entity.DictBasicEntity::getStatus, Boolean.TRUE)
                .eq(com.erp.model.oms.entity.DictBasicEntity::getIsDeleted, Boolean.FALSE)
                .list();
        String platformName = salesPlatformList.stream()
                .filter(v -> v.getValue().equals(platform))
                .map(DictBasicEntity::getName)
                .findFirst()
                .orElse("");
        if (ObjectUtils.isEmpty(salesQty)) {
            throw new ServiceException(ApiError.ERROR_CFG_RULE_SALES_NOT_EXIST, platformName);
        }
        Map<String, String> platformMap = salesPlatformList.stream()
                .collect(Collectors.toMap(com.erp.model.oms.entity.DictBasicEntity::getName, com.erp.model.oms.entity.DictBasicEntity::getValue, (o1, o2) -> o1));
        SalesEstimateExcelFileListener excelListenerUtil = new SalesEstimateExcelFileListener(skuMap, shopInfoList, platformMap, platformName);
        try {
            EasyExcelFactory.read(excelFile.getInputStream(), CfgRuleSalesEstimateFileDTO.ExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error(ApiError.ERROR_95124.msg, e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error(ApiError.ERROR_1016.msg, e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<CfgRuleSalesEstimateFileDTO.ExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (!CollectionUtils.isEmpty(errorList)) {
            StringBuilder sb = new StringBuilder();
            String excelPath = "excel/cfgSalesEstimateError.xlsx";
            String name = "预估日销量导入失败";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
        } else {
            customerSalesEstimateEsService.removeByPlatform(platform);
            // 保存数据
            customerSalesEstimateEsService.saveAll(excelListenerUtil.getSuccessList());
            String fileUrl = filefeign.uploadFile(excelFile);
            CfgRuleSalesEstimateFileEntity salesEstimateFile = new CfgRuleSalesEstimateFileEntity();
            salesEstimateFile.setSalesQtyId(salesQty.getId());
            salesEstimateFile.setFileName(excelFile.getOriginalFilename());
            salesEstimateFile.setFileUrl(fileUrl);
            save(salesEstimateFile);
        }
    }
}
