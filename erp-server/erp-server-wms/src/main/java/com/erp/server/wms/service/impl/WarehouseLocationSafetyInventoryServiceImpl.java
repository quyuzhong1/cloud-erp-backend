package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
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
import com.erp.server.wms.listener.WarehouseLocationSafetyInventoryExcelListener;
import com.erp.server.wms.mapper.WarehouseLocationSafetyInventoryMapper;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.WarehouseLocationSafetyInventoryService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
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
import java.util.*;
import java.util.stream.Collectors;

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
            entity.setWarehouseId(warehouseId);

            //填充库区编码
            WarehouseLocationEntity areaEntity = areaList.stream()
                    .filter(item -> item.getWarehouseId().equals(warehouseId) && item.getName().equals(importExcelDto.getWarehouseAreaName()))
                    .findFirst()
                    .orElse(new WarehouseLocationEntity());
            entity.setWarehouseArea(areaEntity.getCode() == null ? "" : areaEntity.getCode());

            int insertResult = this.baseMapper.insert(entity);
            if(insertResult == 0) {
                importExcelDto.setErrorInfo(importExcelDto.getErrorInfo() + ", 保存失败");
                errorList.add(importExcelDto);
            }
        }

        if(errorList.isEmpty()){
            return Boolean.TRUE;
        }

        //返回错误数据
        try {
            String fileName = "仓位安全库存-导入错误" + DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            String excelPath = "excel/warehouseLocationSafetyInventoryExport.xlsx";
            new ExcelPrintUtils().patchExport(errorList, response, fileName, excelPath);
        } catch (IOException e) {
            log.error("仓位安全库存导出错误：{}", e);
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean exportExcel(WarehouseLocationSafetyInventoryDTO.exportParamDTO dto, HttpServletResponse response) {
        Optional<AdvanceQueryDTO> checkIdsOptional = dto.getAdvanceQueryDTOList().stream().filter(item -> item.getCompare().equals("inList")).findFirst();
        List<WarehouseLocationSafetyInventoryEntity> entityList;
        if(checkIdsOptional.isPresent() && !ObjectUtil.isEmpty(checkIdsOptional.get().getValue())){
            List<String> ids = (List<String>) checkIdsOptional.get().getValue();
            entityList = this.baseMapper.selectBatchIds(ids);
        }else {
            WarehouseLocationSafetyInventoryDTO.SearchParamDTO searchParamDto = new WarehouseLocationSafetyInventoryDTO.SearchParamDTO();
            BeanMapper.copy(dto, searchParamDto);
            entityList = this.baseMapper.listByParam(searchParamDto);
        }
        List<WarehouseLocationSafetyInventoryDTO.ViewDTO> viewList = BeanMapper.copyList(entityList, WarehouseLocationSafetyInventoryDTO.ViewDTO.class);
        fillViewList(viewList);
        try {
            String fileName = "仓位安全库存" + DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            String excelPath = "excel/warehouseLocationSafetyInventoryExport.xlsx";
            new ExcelPrintUtils().patchExport(viewList, response, fileName, excelPath);
        } catch (IOException e) {
            log.error("仓位安全库存导出失败：{}", e);
            return Boolean.FALSE;
        }
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_95131);
        }
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
                .eq("warehouse_id", warehouseIds.get(0))
                .eq("type", "area")
                .eq("is_deleted", false)
        );
        List<DictBasicEntity> dictEntityList = dictBasicService.getBaseMapper().selectList(new QueryWrapper<DictBasicEntity>().eq("type", "warehouseAreaType"));
        Map<String, String> dictValueNameMap = dictEntityList.stream().collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName));

        WarehouseLocationEntity emptyAreaEntity = new WarehouseLocationEntity();
        emptyAreaEntity.setName("");
        for (WarehouseLocationSafetyInventoryDTO.ViewDTO dto : records) {
            String warehouseArea = dto.getWarehouseArea();
            WarehouseLocationEntity areaEntity = areaList.stream().filter(item -> item.getCode().equals(warehouseArea)).findFirst().orElse(emptyAreaEntity);
            dto.setWarehouseAreaName(areaEntity.getName());

            String warehouseId = dto.getWarehouseId();
            dto.setWarehouseName(idNameMap.get(warehouseId));

            String areaTypeCode = dto.getWarehouseAreaType();
            dto.setWarehouseAreaType(dictValueNameMap.get(areaTypeCode));
        }
    }
}
