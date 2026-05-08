package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPurchaseEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseLocationSuggestAfterSalesDto;
import com.erp.model.wms.dto.excel.WarehouseLocationSuggestAfterSalesExcelDto;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.entity.WarehouseLocationSuggestAfterSalesEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.listener.WarehouseLocationSuggestAfterSalesExcelListener;
import com.erp.server.wms.mapper.WarehouseLocationSuggestAfterSalesMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseLocationSuggestAfterSalesService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WAREHOUSE_LOCATION_SUGGEST_AFTER_SALES;

@Service
@Slf4j
public class WarehouseLocationSuggestAfterSalesServiceImpl extends SuperServiceImpl<WarehouseLocationSuggestAfterSalesMapper, WarehouseLocationSuggestAfterSalesEntity> implements WarehouseLocationSuggestAfterSalesService {

    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private WarehouseLocationService warehouseLocationService;


    @Override
    public PagingVO<WarehouseLocationSuggestAfterSalesDto.ListDTO> paging(PagingDTO<WarehouseLocationSuggestAfterSalesDto.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page<Object> page = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<WarehouseLocationSuggestAfterSalesEntity> pageResult = this.baseMapper.paging(page, pagingDTO.getParams());
        List<WarehouseLocationSuggestAfterSalesDto.ListDTO> list = fillViewList(pageResult.getRecords());
        return new PagingVO<>(list, (int) pageResult.getTotal(), (int) pageResult.getPages(), (int) pageResult.getCurrent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrEdit(WarehouseLocationSuggestAfterSalesDto.AddOrEditDTO dto) {
        WarehouseLocationSuggestAfterSalesEntity entity = new WarehouseLocationSuggestAfterSalesEntity();
        BeanMapperUtils.copy(dto, entity);

        LambdaQueryWrapper<WarehouseLocationSuggestAfterSalesEntity> wrapper = Wrappers.lambdaQuery(WarehouseLocationSuggestAfterSalesEntity.class)
                .eq(WarehouseLocationSuggestAfterSalesEntity::getSkuNo, entity.getSkuNo())
                .eq(WarehouseLocationSuggestAfterSalesEntity::getWarehouseId, entity.getWarehouseId())
                .eq(WarehouseLocationSuggestAfterSalesEntity::getSuggestWarehouseLocationCode, entity.getSuggestWarehouseLocationCode())
                .last("LIMIT 1");

        WarehouseLocationSuggestAfterSalesEntity oldEntity = this.getOne(wrapper, false);

        boolean isSave = (oldEntity == null);
        boolean success;

        if (!isSave) {
            // 存在则执行覆盖更新
            log.info("开始覆盖更新仓位售后推荐单 sku: [{}]", entity.getSkuNo());
            entity.setId(oldEntity.getId());
            success = this.updateById(entity);
        } else {
            // 不存在则新增
            log.info("开始新增仓位售后推荐单 sku: [{}]", entity.getSkuNo());
            success = this.save(entity);
        }

        if (!success) {
            log.error("仓位售后推荐单操作失败 sku: [{}], isSave: [{}]", entity.getSkuNo(), isSave);
            throw new ServiceException((isSave ? "新增" : "覆盖更新") + "仓位推荐信息失败");
        }
        //日志记录
        String operator = UserContext.getDefaultLoginUser().getUserName();
        String actionName = isSave ? "新增" : "覆盖更新";

        String msg = StrUtil.format("用户【{}】对 SKU【{}】推荐仓位编码【{}】 执行了【{}】相关的【{}】操作",
                operator, entity.getSkuNo(), entity.getSuggestWarehouseLocationCode(), actionName, "仓位售后推荐");

        operateLogService.addModuleOperateLogByObj(
                oldEntity,
                entity,
                ModuleTypeEnum.WAREHOUSE_LOCATION_SUGGEST_AFTERSALES.getCode(),
                entity.getId(),
                msg
        );

        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(WarehouseLocationSuggestAfterSalesEntity entity) {
        this.removeById(entity.getId());
        //日志记录
        String operator = UserContext.getDefaultLoginUser().getUserName();
        String msg = StrUtil.format("用户【{}】对 SKU【{}】推荐仓位编码【{}】 执行了【{}】相关的【{}】操作",
                operator, entity.getSkuNo(), entity.getSuggestWarehouseLocationCode(), "删除", "仓位售后推荐");

        operateLogService.addModuleOperateLog(
                msg,
                ModuleTypeEnum.WAREHOUSE_LOCATION_SUGGEST_AFTERSALES.getCode(),
                entity.getId(),
                "删除操作"
        );
        return BatchResultDTO.success(entity.getId(), null, "删除成功");
    }

    @Override
    public void exportExcel(WarehouseLocationSuggestAfterSalesDto.ExportParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("售后仓位推荐数据导出", EXPORT_WAREHOUSE_LOCATION_SUGGEST_AFTER_SALES.getCode(), dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(MultipartFile file, HttpServletResponse response) {
        LoginUser user = UserContext.getNonLoginUser();
        String userName = user.getUserName();
        String uid = user.getUid();

        WarehouseLocationSuggestAfterSalesExcelListener listener = new WarehouseLocationSuggestAfterSalesExcelListener();
        try {
            EasyExcel.read(file.getInputStream(), listener).sheet(0).doRead();
        } catch (IOException e) {
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        }

        List<WarehouseLocationSuggestAfterSalesExcelDto> errorList = listener.getErrorList();
        List<WarehouseLocationSuggestAfterSalesExcelDto> verifyList = listener.getSuccessList();
        List<WarehouseLocationSuggestAfterSalesExcelDto> allList = listener.getAllList();

        if (!errorList.isEmpty()) {
            ExcelUtil.export("错误数据", "sheet1", allList, WarehouseLocationSuggestAfterSalesExcelDto.class, response);
        } else {
            log.info("开始封装实体数据，当前操作人：{}", userName);
            // 统一使用应用服务器时间
            LocalDateTime now = LocalDateTime.now();

            List<WarehouseLocationSuggestAfterSalesEntity> entities = verifyList
                    .stream()
                    //增加去重逻辑，防止同一批次出现重复行导致数据库报错
                    .collect(Collectors.toMap(
                            dto -> dto.getSkuNo() + "_" + dto.getWarehouseId() + "_" + dto.getSuggestWarehouseLocationId(),
                            dto -> dto,
                            (oldVal, newVal) -> newVal
                    ))
                    .values()
                    .stream()
                    .map(dto -> {
                        WarehouseLocationSuggestAfterSalesEntity entity = new WarehouseLocationSuggestAfterSalesEntity();
                        BeanMapperUtils.copy(dto, entity);
                        entity.setCreateUserId(uid);
                        entity.setCreateUserName(userName);
                        entity.setCreateTime(now);
                        entity.setUpdateUserId(uid);
                        entity.setUpdateUserName(userName);
                        entity.setUpdateTime(now);
                        entity.setVersion(0);
                        return entity;
                    })
                    .collect(Collectors.toList());

            // 分批执行高速 UPSERT (每批 500 条)
            if (!entities.isEmpty()) {
                List<List<WarehouseLocationSuggestAfterSalesEntity>> batches = ListUtils.partition(entities, 500);
                for (List<WarehouseLocationSuggestAfterSalesEntity> batch : batches) {
                    try {
                        baseMapper.upsertBatch(batch);
                    } catch (Exception e) {
                        throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
                    }
                }
            }
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "excel/warehouseLocationSuggestAfterSalesExport.xlsx";
        String excelName = "售后仓位推荐导入模板.xlsx";

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
            throw new ServiceException(ApiError.FILE_IMPORT_TEMPLATE_DOWNLOAD_FAILED);
        }
    }


    /**
     * 填充字段
     *
     * @param entityList 查询结果
     * @return 填充后的结果
     */
    private List<WarehouseLocationSuggestAfterSalesDto.ListDTO> fillViewList(List<WarehouseLocationSuggestAfterSalesEntity> entityList) {
        if (entityList.isEmpty()) {
            return Collections.emptyList();
        }


        List<WarehouseLocationSuggestAfterSalesDto.ListDTO> dtoList = new ArrayList<>(entityList.size());
        //批量获取sku信息
        List<String> skuNoList = entityList.stream().map(WarehouseLocationSuggestAfterSalesEntity::getSkuNo).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuDetailEntities = plmTaskFeign.listBySkuNos(skuNoList);
        Map<String, ProductDetailEntity> skuDetailMap = skuDetailEntities.stream().collect(Collectors.toMap(ProductDetailEntity::getSkuNo, item -> item));
        //获取ena码信息
        Map<String, String> skuAndEnaMap = new HashMap<>();
        if (CollUtil.isNotEmpty(skuDetailEntities)) {
            List<String> skuIdList = skuDetailEntities.stream().map(ProductDetailEntity::getId).distinct().collect(Collectors.toList());
            List<ProductPurchaseEntity> productPurchaseEntities = plmTaskFeign.listProductPurchaseBySkuId(skuIdList);
            skuAndEnaMap = productPurchaseEntities
                    .stream().filter(i -> StrUtil.isNotBlank(i.getEan())).collect(Collectors.toMap(ProductPurchaseEntity::getSkuId, ProductPurchaseEntity::getEan));
        }

        //批量获取仓库信息
        List<String> warehouseIdList = entityList.stream().map(WarehouseLocationSuggestAfterSalesEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.getBaseMapper().selectBatchIds(warehouseIdList);
        Map<String, String> warehouseIdNameMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        //批量获取库区/仓位信息
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.getBaseMapper().selectList(new QueryWrapper<WarehouseLocationEntity>()
                .in("warehouse_id", warehouseIdList)
                .eq("is_deleted", false)
        );

        for (WarehouseLocationSuggestAfterSalesEntity entity : entityList) {
            WarehouseLocationSuggestAfterSalesDto.ListDTO dto = new WarehouseLocationSuggestAfterSalesDto.ListDTO();
            dto.setId(entity.getId());

            dto.setSkuNo(entity.getSkuNo());
            ProductDetailEntity skuDetailEntity = skuDetailMap.get(entity.getSkuNo());
            if (skuDetailEntity != null) {
                dto.setEnaNo(skuAndEnaMap.get(skuDetailEntity.getId()));
                dto.setProductName(skuDetailEntity.getName());
            }

            dto.setWarehouseId(entity.getWarehouseId());
            String warehouseName = warehouseIdNameMap.get(entity.getWarehouseId());
            dto.setWarehouseName(warehouseName);

            WarehouseLocationEntity areaEntity = warehouseLocationList.stream()
                    .filter(item -> item.getCode().equals(entity.getWarehouseAreaCode()))
                    .findFirst().orElse(new WarehouseLocationEntity());

            dto.setWarehouseAreaId(areaEntity.getId());
            dto.setWarehouseAreaCode(areaEntity.getCode());
            dto.setWarehouseAreaName(areaEntity.getName());


            WarehouseLocationEntity locationEntity = warehouseLocationList.stream()
                    .filter(item -> item.getCode().equals(entity.getSuggestWarehouseLocationCode()))
                    .findFirst().orElse(new WarehouseLocationEntity());
            dto.setSuggestWarehouseLocationId(locationEntity.getId());
            dto.setSuggestWarehouseLocationCode(locationEntity.getCode());
            dto.setSuggestWarehouseLocationName(locationEntity.getName());

            dto.setPriority(entity.getPriority());
            dto.setStatusName(entity.getDisabled() ? "禁用" : "启用");

            dtoList.add(dto);
        }
        return dtoList;
    }
}
