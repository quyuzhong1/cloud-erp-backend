package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationSafetyInventoryDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.entity.WarehouseLocationSafetyInventoryEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.listener.WarehouseLocationSafetyInventoryExcelListener;
import com.erp.server.wms.mapper.WarehouseLocationSafetyInventoryMapper;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.WarehouseLocationSafetyInventoryService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_WAREHOUSE_LOCATION_SAFETY_INVENTORY;

/**
 * 仓位安全库存
 * @date 2024-06-21
 * @author tanmujin
 */
@Component
public class WarehouseLocationSafetyInventoryServiceImpl extends SuperServiceImpl<WarehouseLocationSafetyInventoryMapper, WarehouseLocationSafetyInventoryEntity> implements WarehouseLocationSafetyInventoryService {

    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public PagingVO<WarehouseLocationSafetyInventoryDTO.ViewDTO> paging(PagingDTO<WarehouseLocationSafetyInventoryDTO.SearchParamDTO> paramDto) {
        Page<Object> page = new Page<>(paramDto.getCurrPage(), paramDto.getPageSize());
        IPage<WarehouseLocationSafetyInventoryDTO.ViewDTO> result = this.baseMapper.paging(page, paramDto.getParams());
        fillViewList(result.getRecords());
        return new PagingVO<>(result);
    }

    @Override
    public BaseResultDTO.UpdateDTO updateInventory(WarehouseLocationSafetyInventoryDTO.UpdateParamDTO updateParamDto) {
        WarehouseLocationSafetyInventoryEntity entity = new WarehouseLocationSafetyInventoryEntity();
        BeanMapper.copy(updateParamDto, entity);
        int count = this.baseMapper.updateById(entity);
        if(count > 0){
            return null;
        }else {
            WarehouseLocationSafetyInventoryEntity inventoryEntity = this.baseMapper.selectById(updateParamDto.getId());
            String code = "sku：" + inventoryEntity.getSkuNo() + " 仓位：" + inventoryEntity.getWarehouseLocation();
            return new BaseResultDTO.UpdateDTO(updateParamDto.getId(), code);
        }
    }

    @Override
    public boolean importExcel(MultipartFile file, HttpServletResponse response) {
        WarehouseLocationSafetyInventoryExcelListener listener = new WarehouseLocationSafetyInventoryExcelListener();
        try {
            EasyExcel.read(file.getInputStream(), WarehouseLocationSafetyInventoryDTO.importExcelDTO.class, listener).sheet(0).doRead();
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95124);
        }

        //校验失败的数据
        List<WarehouseLocationSafetyInventoryDTO.importExcelDTO> errorList = listener.getErrorList();

        //校验成功的数据
        List<WarehouseLocationSafetyInventoryDTO.importExcelDTO> successList = listener.getSuccessList();
        if(successList.isEmpty()){
            return exportErrorFile(errorList, response);
        }

        List<String> warehouseNameList = successList.stream().map(item -> item.getWarehouseName()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.getByNames(warehouseNameList);
        Map<String, String> warehouseMap = warehouseList.stream().collect(Collectors.toMap(item1 -> item1.getName(), item2 -> item2.getId()));

        List<String> warehouseAreaNameList = successList.stream().map(item -> item.getWarehouseAreaName()).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> areaList = warehouseLocationService.getBaseMapper().selectList(new QueryWrapper<WarehouseLocationEntity>()
                .eq("type", "area")
                .eq("is_deleted", false)
                .in("name", warehouseAreaNameList)
        );
        for (WarehouseLocationSafetyInventoryDTO.importExcelDTO importExcelDto : successList) {
            //数据填充，将仓库名称转为仓库ID，将库区名称转为库区ID
            WarehouseLocationSafetyInventoryEntity entity = new WarehouseLocationSafetyInventoryEntity();
            BeanMapper.copy(importExcelDto, entity);

            //填充仓库ID
            String warehouseId = warehouseMap.get(importExcelDto.getWarehouseName());
            if(CharSequenceUtil.isBlank(warehouseId)){
                importExcelDto.setErrorInfo(importExcelDto.getErrorInfo() + ", 仓库不存在");
                errorList.add(importExcelDto);
                continue;
            }
            entity.setWarehouseId(warehouseId);

            //填充库区编码
            WarehouseLocationEntity areaEntity = areaList.stream()
                    .filter(item -> item.getWarehouseId().equals(warehouseId) && item.getName().equals(importExcelDto.getWarehouseAreaName()))
                    .findFirst()
                    .orElse(new WarehouseLocationEntity());
            entity.setWarehouseArea(areaEntity.getCode() == null ? "" : areaEntity.getCode());

            update(new UpdateWrapper<WarehouseLocationSafetyInventoryEntity>()
                    .eq("warehouse_id", entity.getWarehouseId())
                    .eq("warehouse_location", entity.getWarehouseLocation())
                    .eq("sku_no", entity.getSkuNo())
                    .set(entity.getSafetyQty() != null, "safety_qty", entity.getSafetyQty())
                    .set(entity.getMaxQty() != null, "max_qty", entity.getMaxQty()));
        }

        if(errorList.isEmpty()){
            return Boolean.TRUE;
        }

        //返回错误数据
        Boolean exportResult = exportErrorFile(errorList, response);
        return exportResult;
    }

    private Boolean exportErrorFile(List<WarehouseLocationSafetyInventoryDTO.importExcelDTO> errorList, HttpServletResponse response){
        try {
            String fileName = "仓位安全库存-导入错误" + DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            String excelPath = "excel/warehouseLocationSafetyInventoryExport.xlsx";
            new ExcelPrintUtils().patchExport(errorList, response, fileName, excelPath);
        } catch (IOException e) {
            log.error("仓位安全库存导出错误：{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public boolean exportExcel(WarehouseLocationSafetyInventoryDTO.exportParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("仓位安全库存", EXPORT_WMS_WAREHOUSE_LOCATION_SAFETY_INVENTORY.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/warehouseLocationSafetyInventoryImport.xlsx";
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
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public PagingVO<WarehouseLocationSafetyInventoryDTO.ViewDTO> exportWarehouseLocationSafetyInventory(PagingDTO<WarehouseLocationSafetyInventoryDTO.exportParamDTO> dto) {
        Page<WarehouseLocationSafetyInventoryEntity> page = this.baseMapper.listByParam(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        List<WarehouseLocationSafetyInventoryDTO.ViewDTO> viewList = BeanMapper.copyList(page.getRecords(), WarehouseLocationSafetyInventoryDTO.ViewDTO.class);
        fillViewList(viewList);
        return new PagingVO<>(viewList,(int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    /**
     * 填充列表空字段
     * @param records 原始列表
     * @return void
     * @date: 2024-06-21
     * @author: tanmujin
     */
    private void fillViewList(List<WarehouseLocationSafetyInventoryDTO.ViewDTO> records) {
        if(records.isEmpty()){
            return;
        }
        List<String> warehouseIds = records.stream().map(item -> item.getWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.getBaseMapper().selectBatchIds(warehouseIds);
        Map<String, String> idNameMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));
        List<WarehouseLocationEntity> areaList = warehouseLocationService.getBaseMapper().selectList(new QueryWrapper<WarehouseLocationEntity>()
                .in("warehouse_id", warehouseIds)
                .eq("type", "area")
        );
        List<DictBasicEntity> dictEntityList = dictBasicService.getBaseMapper().selectList(new QueryWrapper<DictBasicEntity>().eq("type", "warehouseAreaType"));
        Map<String, String> dictValueNameMap = dictEntityList.stream().collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName));

        WarehouseLocationEntity emptyAreaEntity = new WarehouseLocationEntity();
        emptyAreaEntity.setName("");
        for (WarehouseLocationSafetyInventoryDTO.ViewDTO dto : records) {
            String warehouseArea = dto.getWarehouseArea();
            WarehouseLocationEntity areaEntity = areaList.stream().filter(item -> item.getWarehouseId().equals(dto.getWarehouseId()) && item.getCode().equals(warehouseArea)).findFirst().orElse(emptyAreaEntity);
            dto.setWarehouseAreaName(areaEntity.getName());

            String warehouseId = dto.getWarehouseId();
            dto.setWarehouseName(idNameMap.get(warehouseId));

            String areaTypeCode = dto.getWarehouseAreaType();
            dto.setWarehouseAreaType(dictValueNameMap.get(areaTypeCode));
        }
    }
}
