package com.erp.server.wms.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OperateLogDTO;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.excel.StocktakingTaskDetailExcelDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.StocktakingModeEnum;
import com.erp.model.wms.enums.StocktakingStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.listener.StocktakingTaskDetailExcelListener;
import com.erp.server.wms.mapper.StocktakingTaskDetailMapper;
import com.erp.server.wms.mapper.StocktakingTaskMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_STOCKTAKING_TASK_DETAIL;

/**
 * <p>
 * 盘点任务明细表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Slf4j
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

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private StocktakingTaskMapper stocktakingTaskMapper;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public Boolean exportExcel(StocktakingTaskDTO.BaseIdDTO dto) {
        dto.checkAndGetMainId();
        downloadTaskFeign.saveExportTask("盘点任务明细列表", EXPORT_WMS_STOCKTAKING_TASK_DETAIL.getCode(), dto);
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
        //状态
        StocktakingStatusEnum status = task.getStatus();
        List<StocktakingStatusEnum> statusList = Arrays.asList(StocktakingStatusEnum.NOT_STARTED, StocktakingStatusEnum.RECOUNT);
        if (!statusList.contains(status)) {
            throw new ServiceException("只有复盘中,未开始的盘点任务才能修改盘点库存");
        }

        List<StocktakingTaskDetailEntity> taskDetailList = this.listBaseByMainIds(Collections.singletonList(mainId));
        StocktakingTaskDetailExcelListener excelListener = new StocktakingTaskDetailExcelListener(this, task.getCode(), taskDetailList, warehouseService, operateLogService);
        try {
            EasyExcel.read(excelFile.getInputStream(), StocktakingTaskDetailExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("盘点任务明细导入错误！>>>>>{}", e);
            return Boolean.FALSE;
        }
        List<StocktakingTaskDetailExcelDTO> errorList = excelListener.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "盘点任务明细错误信息";
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
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"");
        }
        List<String> idList = list.stream().map(StocktakingTaskDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"");
        }
        List<StocktakingTaskDetailEntity> taskDetailList = this.listByIds(idList);
        List<StocktakingTaskDetailEntity> updateTaskDetailList = new ArrayList<>(taskDetailList.size());
        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        String moduleType = ModuleTypeEnum.STOCKTAKING_TASK.getCode();

        for (StocktakingTaskDetailDTO.UpdateDTO item : list) {
            StocktakingTaskDetailEntity taskDetail = taskDetailList.stream().
                    filter(t -> t.getId().equals(item.getId())).
                    findFirst().orElse(null);
            if (Objects.isNull(taskDetail)) {
                continue;
            }
            //这是修改的数量
            Integer updateQty = item.getQty();
            //这是数控
            Integer dbQty = taskDetail.getQty();
            //是否秀发i
            Boolean isUpdate = !updateQty.equals(dbQty);
            if (isUpdate) {
                OperateLogDTO.AddModuleOperateLogDTO addModuleOperateLogDTO = new OperateLogDTO.AddModuleOperateLogDTO();
                addModuleOperateLogDTO.setOperation("修改操作");
                addModuleOperateLogDTO.setBusinessId(taskDetail.getMainId());
                addModuleOperateLogDTO.setModuleType(moduleType);
                StringBuffer sb = new StringBuffer("盘点任务单");
                sb.append(" 修改");
                sb.append(taskDetail.getSkuNo());
                sb.append("盘点库存由原来的:");
                sb.append(taskDetail.getQty());
                Integer qty = item.getQty();
                sb.append("修改为:").append(qty);
                addModuleOperateLogDTO.setContent(sb.toString());
                operateLogList.add(addModuleOperateLogDTO);

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
        }
        if (CollectionUtils.isNotEmpty(updateTaskDetailList)) {
            operateLogService.batchAddModuleOperateLog(operateLogList);
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
        List<StocktakingTaskDetailEntity> dbList = this.listBaseByMainIds(Collections.singletonList(mainId));
        List<StocktakingTaskDetailDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, StocktakingTaskDetailDTO.ViewDTO.class);
        List<String> skuIdList = resultList.stream().map(StocktakingTaskDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listProductDetailByIds(skuIdList);
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = dbList.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.listByWarehouseIdAndCode(paramList);
        for (StocktakingTaskDetailDTO.ViewDTO item : resultList) {
            String skuId = item.getSkuId();
            String skuName = skuList.stream().filter(s -> s.getId().equals(skuId)).findFirst().
                    map(ProductDetailEntity::getName).orElse("");
            item.setProductName(skuName);
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(e -> e.getWarehouseId().equals(item.getWarehouseId()) && e.getCode().equals(item.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            item.setWarehouseLocationName(warehouseLocationEntity.getName());
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("盘点任务明细 downloadTemplate  出错了 e==={}", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeByMainId(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.TRUE;
        }
        remove(new LambdaQueryWrapper<StocktakingTaskDetailEntity>().in(StocktakingTaskDetailEntity::getMainId, mainIds));
        return Boolean.TRUE;
    }


    /**
     * 根据一些信息 获取到明细信息
     *
     * @param mainId
     * @param skuNo
     * @param warehouseId
     * @param warehouseLocation
     * @return com.erp.model.wms.entity.StocktakingTaskDetailEntity
     * @author yl
     * @date 2023-08-21 17:51
     */
    @Override
    public StocktakingTaskDetailEntity getTaskDetail(String mainId, String skuNo, String warehouseId, String warehouseLocation) {
        return this.lambdaQuery().eq(StocktakingTaskDetailEntity::getMainId, mainId).
                eq(StocktakingTaskDetailEntity::getSkuNo, skuNo).
                eq(StocktakingTaskDetailEntity::getWarehouseId, warehouseId).
                eq(StocktakingTaskDetailEntity::getWarehouseLocation, warehouseLocation).
                last("LIMIT 1").
                one();
    }

    /**
     * 更改差异数量
     *
     * @param taskDetailList
     * @return void
     * @author yl
     * @date 2023-08-22 11:57
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQty(List<StocktakingTaskDetailEntity> taskDetailList) {
        if (CollectionUtils.isNotEmpty(taskDetailList)) {
            for (StocktakingTaskDetailEntity item : taskDetailList) {
                //可用库存
                Integer usableQty = item.getUsableQty();
                //冻结数量
                Integer frozenQty = item.getFrozenQty();
                Integer qty = item.getQty();
                //差异数量 等于盘点库存-可用库存-冻结库存
                Integer diffQty = qty - usableQty - frozenQty;
                item.setDiffQty(diffQty);
            }
            this.updateBatchById(taskDetailList);
        }
    }

    @Override
    public PagingVO<StocktakingTaskDetailDTO.ExportDTO> exportStocktakingTaskDetail(PagingDTO<StocktakingTaskDTO.BaseIdDTO> dto) {
        String mainId = dto.getParams().checkAndGetMainId();

        StocktakingTaskDTO.ViewDTO view = stocktakingTaskMapper.getViewById(mainId);
        if(Objects.isNull(view)){
            throw new ServiceException("盘点任务不存在");
        }
        //盘点方式
        StocktakingModeEnum stocktakingMode = view.getStocktakingMode();
        String stocktakingModeName = Objects.nonNull(stocktakingMode) ? stocktakingMode.getName() : "";
        //是否盲盘
        Boolean isBlindCount = StocktakingModeEnum.BLIND_COUNT.equals(stocktakingMode);
        //盘点人信息
        List<StocktakingTaskUserEntity> taskUserList = stocktakingTaskUserService.listBaseBySourceIdList(Collections.singletonList(mainId));
        String stocktakingUserName = taskUserList.stream().
                map(StocktakingTaskUserEntity::getUserName).collect(Collectors.joining(","));
        List<StocktakingTaskDetailDTO.ExportDTO> exportList = baseMapper.listExportByMainId(mainId);
        if (CollectionUtils.isEmpty(exportList)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        List<String> skuIdList = exportList.stream().map(StocktakingTaskDetailDTO.ExportDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listProductDetailByIds(skuIdList);

        for (StocktakingTaskDetailDTO.ExportDTO item : exportList) {
            item.setStocktakingUserName(stocktakingUserName);
            String skuName = skuList.stream().filter(s -> s.getId().equals(item.getSkuId())).findFirst().
                    map(ProductDetailEntity::getName).orElse("");
            item.setProductName(skuName);
            item.setStocktakingModeName(stocktakingModeName);
            //如果是盲盘就要清空一些数据
            if(isBlindCount){
                item.setUsableQty(null);
                item.setDiffQty(null);
                item.setFrozenQty(null);
            }

        }
        return new PagingVO<>(exportList, exportList.size(), dto.getPageSize(), dto.getCurrPage());
    }
}
