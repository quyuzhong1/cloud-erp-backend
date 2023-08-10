package com.erp.server.wms.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.excel.KingdeeBusinessOperatorImportExcelDTO;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.model.wms.dto.excel.StocktakingTaskDetailExcelDTO;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.model.wms.entity.StocktakingTaskUserEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.listener.StocktakingTaskDetailExcelListener;
import com.erp.server.wms.mapper.StocktakingTaskDetailMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.StocktakingTaskService;
import com.erp.server.wms.service.StocktakingTaskUserService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘点任务明细表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Service
public class StocktakingTaskDetailServiceImpl extends SuperServiceImpl<StocktakingTaskDetailMapper, StocktakingTaskDetailEntity> implements StocktakingTaskDetailService {

    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private StocktakingTaskUserService stocktakingTaskUserService;

    @Resource
    private StocktakingTaskService stocktakingTaskService;

    @Resource
    private WarehouseService warehouseService;

    @Override
    public Boolean exportExcel(BaseIdDTO dto, HttpServletResponse response) {
        String mainId = dto.getId();
        //盘点人信息
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseByTaskIds(Arrays.asList(mainId));
        String stocktakingUserName = taskUserList.stream().
                map(StocktakingTaskUserEntity::getUserName).collect(Collectors.joining(","));
        List<StocktakingTaskDetailDTO.ExportDTO> exportList = baseMapper.listExportByMainId(mainId);
        if (CollectionUtils.isEmpty(exportList)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        List<String> skuIdList = exportList.stream().map(StocktakingTaskDetailDTO.ExportDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.ListProductDetailByIds(skuIdList);

        for (StocktakingTaskDetailDTO.ExportDTO item : exportList) {
            item.setStocktakingUserName(stocktakingUserName);
            String skuName = skuList.stream().filter(s -> s.getId().equals(item.getSkuId())).findFirst().
                    map(ProductDetailEntity::getName).orElse("");
            item.setSkuName(skuName);
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/StocktakingTaskDetail.xlsx";
        String name = "盘点任务明细列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(exportList, response, sb.toString(), excelPath);
        } catch (Exception e) {
            log.error("盘点任务明细列表导出 出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }

    /**
     * 导入明细
     *
     * @param excelFile
     * @param response
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importFile(String mainId, MultipartFile excelFile, HttpServletResponse response) {
        StocktakingTaskEntity task = stocktakingTaskService.getById(mainId);
        if (Objects.isNull(task)) {
            throw new ServiceException(ApiError.ERROR_BILL_NOT_EXIST);
        }
        List<StocktakingTaskDetailEntity> taskDetailList = this.listBaseByMainIds(Arrays.asList(mainId));
        List<WarehouseEntity> warehouseList = warehouseService.list();
        StocktakingTaskDetailExcelListener excelListener = new StocktakingTaskDetailExcelListener(this, task.getCode(), taskDetailList, warehouseList);
        try {
            EasyExcel.read(excelFile.getInputStream(), StocktakingTaskDetailExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("盘点任务明细导入错误！>>>>>{}", e);
            return Boolean.FALSE;
        }
        List<StocktakingTaskDetailExcelDTO> errorList = excelListener.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "金蝶业务员错误信息";
            ExcelUtil.export(fileName, "error", errorList, StocktakingTaskDetailExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }

    /**
     * 更新明细
     *
     * @param list
     * @return
     * @author yl
     * @date 2023-08-03 17:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBatchDetail(List<StocktakingTaskDetailDTO.UpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99091);
        }
        List<String> idList = list.stream().map(StocktakingTaskDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            throw new ServiceException(ApiError.ERROR_99091);
        }
        List<StocktakingTaskDetailEntity> taskDetailList = this.listByIds(idList);
        List<StocktakingTaskDetailEntity> updateTaskDetailList = new ArrayList<>(taskDetailList.size());
        for (StocktakingTaskDetailDTO.UpdateDTO item : list) {
            StocktakingTaskDetailEntity taskDetail = taskDetailList.stream().
                    filter(t -> t.getIsDeleted().equals(item.getId())).
                    findFirst().orElse(null);
            if (Objects.isNull(taskDetail)) {
                continue;
            }
            Integer qty = item.getQty();
            taskDetail.setQty(qty);
            //可用库存
            Integer usableQty = taskDetail.getUsableQty();
            //冻结数量
            Integer frozenQty = taskDetail.getFrozenQty();
            //差异数量 等于盘点库存-可用库存-冻结库存
            Integer diffQty = qty - usableQty - frozenQty;
            taskDetail.setDiffQty(diffQty);
            updateTaskDetailList.add(taskDetail);
        }
        if (CollectionUtils.isNotEmpty(updateTaskDetailList)) {
            return this.updateBatchById(updateTaskDetailList);
        }
        return Boolean.TRUE;
    }

    /**
     * 根据仓库id 集合 获取到任务明细
     *
     * @param warehouseIdList
     * @return
     */
    @Override
    public List<StocktakingTaskDetailEntity> listByWarehouseIds(List<String> warehouseIdList) {
        if (CollectionUtils.isEmpty(warehouseIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(StocktakingTaskDetailEntity::getWarehouseId, warehouseIdList).list();
    }


    /**
     * 根据主表id 获取到详情
     *
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.entity.StocktakingTaskDetailEntity>
     * @author yl
     * @date 2023-08-08 12:08
     */
    @Override
    public List<StocktakingTaskDetailEntity> listBaseByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(StocktakingTaskDetailEntity::getMainId, mainIdList).list();
    }

    /**
     * 获取到对应的详情
     *
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.StocktakingTaskDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-08-08 16:48
     */
    @Override
    public List<StocktakingTaskDetailDTO.ViewDTO> listByMainId(String mainId) {
        List<StocktakingTaskDetailEntity> dbList = this.listBaseByMainIds(Arrays.asList(mainId));
        List<StocktakingTaskDetailDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, StocktakingTaskDetailDTO.ViewDTO.class);
        List<String> skuIdList = resultList.stream().map(StocktakingTaskDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.ListProductDetailByIds(skuIdList);
        for (StocktakingTaskDetailDTO.ViewDTO item : resultList) {
            String skuId = item.getSkuId();
            String skuName = skuList.stream().filter(s -> s.getId().equals(skuId)).findFirst().
                    map(ProductDetailEntity::getName).orElse("");
            item.setSkuName(skuName);
        }
        return resultList;
    }


    /**
     * 下载模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-08-09 14:04
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/StocktakingTaskDetailTemplate.xlsx";
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
            log.error("盘点任务明细 downloadTemplate  出错了 e==={}", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }
}
