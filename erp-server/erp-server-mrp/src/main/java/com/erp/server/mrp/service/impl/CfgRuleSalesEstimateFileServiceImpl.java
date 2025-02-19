package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleSalesEstimateFileDTO;
import com.erp.model.mrp.entity.CfgRuleSalesEstimateFileEntity;
import com.erp.server.mrp.mapper.CfgRuleSalesEstimateFileMapper;
import com.erp.server.mrp.service.CfgRuleSalesEstimateFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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

    @Override
    public PagingVO<CfgRuleSalesEstimateFileDTO.PagingView> filePage(PagingDTO<CfgRuleSalesEstimateFileDTO.PagingParamDTO> params) {
        Page<CfgRuleSalesEstimateFileEntity> page = page(new Page<>(params.getCurrPage(), params.getPageSize()));
        List<CfgRuleSalesEstimateFileDTO.PagingView> pagingViews = BeanMapperUtils.copyList(CfgRuleSalesEstimateFileDTO.PagingView.class, page.getRecords());
        return new PagingVO<>(pagingViews, (int) page.getTotal(), params.getPageSize(), params.getCurrPage());
    }

    @Override
    public void importFile(MultipartFile excelFile, HttpServletResponse response) {
//        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(addDTO.getSkuIds());
//        Map<String, String> skuMap = skuVOS.stream()
//                .collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuNo, (o1, o2) -> o1));
//        List<ShopInfoEntity> shopInfoList = shopInfoFeign.listShopInfoByIds(addDTO.getShopIds());
//        Map<String, ShopInfoEntity> shopMap = shopInfoList.stream()
//                .collect(Collectors.toMap(ShopInfoEntity::getId, v -> v, (o1, o2) -> o1));
//        List<com.erp.model.oms.entity.DictBasicEntity> salesPlatformList = FeignQuery.create(com.erp.model.oms.entity.DictBasicEntity.class)
//                .eq(com.erp.model.oms.entity.DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType())
//                .eq(com.erp.model.oms.entity.DictBasicEntity::getStatus, Boolean.TRUE)
//                .eq(com.erp.model.oms.entity.DictBasicEntity::getIsDeleted, Boolean.FALSE)
//                .list();
//        Map<String, String> platformMap = salesPlatformList.stream()
//                .collect(Collectors.toMap(com.erp.model.oms.entity.DictBasicEntity::getName, DictBasicEntity::getValue, (o1, o2) -> o1));
//        SalesEstimateExcelFileListener excelListenerUtil = new SalesEstimateExcelFileListener(skuVOS, shopInfoList, platformMap);
//        try {
//            EasyExcelFactory.read(excelFile.getInputStream(), excelListenerUtil).sheet(0).doRead();
//        } catch (IOException e) {
//            log.error(ApiError.ERROR_95124.msg, e);
//            throw new ServiceException(ApiError.ERROR_95124);
//        } catch (ExcelCommonException e) {
//            log.error(ApiError.ERROR_1016.msg, e);
//            throw new ServiceException(ApiError.ERROR_1016);
//        }
//        //验证导入数据是否为空
//        List<JSONObject> excelDateList = excelListenerUtil.getAllList();
//        if (CollectionUtils.isEmpty(excelDateList)) {
//            throw new ServiceException(ApiError.ERROR_95123);
//        }
//        //导入数据处理
//        List<JSONObject> successList = excelListenerUtil.getSuccessList();
//        //导出错误数据
//        List<JSONObject> errorList = excelListenerUtil.getErrorList();
//        //表头
//        List<String> headList = excelListenerUtil.getHeadList();
//
//        //处理校验导入成功数据
//        handleImportSalesEstimate(successList, errorList,headList,platformType);
//        //导入文件名称
//        String originalFilename = excelFile.getOriginalFilename();
//        //上传正确数据
//        upLoadSuccessExcel(originalFilename,successList,headList,platformType);
//
//        if (CollectionUtils.isEmpty(errorList)) {
//            return;
//        }
//        String fileName = "运营月销预估";
//        ExcelUtil.customExportUtil(headList,errorList,fileName, response);
    }
}
