package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
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
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.dto.excel.AfterSalesWarehouseLocationSuggestExcelDto;
import com.erp.model.wms.entity.AfterSalesWarehouseLocationSuggestEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.listener.AfterSalesWarehouseLocationSuggestExcelListener;
import com.erp.server.wms.mapper.AfterSalesWarehouseLocationSuggestMapper;
import com.erp.server.wms.service.AfterSalesWarehouseLocationSuggestService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WarehouseLocationService;
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
public class AfterSalesWarehouseLocationSuggestServiceImpl extends SuperServiceImpl<AfterSalesWarehouseLocationSuggestMapper, AfterSalesWarehouseLocationSuggestEntity> implements AfterSalesWarehouseLocationSuggestService {

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
    public PagingVO<AfterSalesWarehouseLocationSuggestDto.ListDTO> paging(PagingDTO<AfterSalesWarehouseLocationSuggestDto.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page<Object> page = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<AfterSalesWarehouseLocationSuggestEntity> pageResult = this.baseMapper.paging(page, pagingDTO.getParams());
        List<AfterSalesWarehouseLocationSuggestDto.ListDTO> list = fillViewList(pageResult.getRecords());
        return new PagingVO<>(list, (int) pageResult.getTotal(), (int) pageResult.getPages(), (int) pageResult.getCurrent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrEdit(AfterSalesWarehouseLocationSuggestDto.AddOrEditDTO dto) {
        AfterSalesWarehouseLocationSuggestEntity entity = new AfterSalesWarehouseLocationSuggestEntity();
        BeanMapperUtils.copy(dto, entity);

        // 1. 严格通过 ID 判断是新增还是修改
        boolean isSave = StrUtil.isBlank(entity.getId());
        AfterSalesWarehouseLocationSuggestEntity oldEntity = null;

        // 2. 查出当前业务组合（SKU+仓库+仓位）在数据库中的记录，用于冲突判定
        LambdaQueryWrapper<AfterSalesWarehouseLocationSuggestEntity> conflictWrapper = Wrappers.lambdaQuery(AfterSalesWarehouseLocationSuggestEntity.class)
                .eq(AfterSalesWarehouseLocationSuggestEntity::getSkuNo, entity.getSkuNo())
                .eq(AfterSalesWarehouseLocationSuggestEntity::getWarehouseId, entity.getWarehouseId())
                .eq(AfterSalesWarehouseLocationSuggestEntity::getWarehouseLocationCode, entity.getWarehouseLocationCode())
                .last("LIMIT 1");
        AfterSalesWarehouseLocationSuggestEntity conflictEntity = this.getOne(conflictWrapper, false);

        if (isSave) {
            // 【新增逻辑】
            if (conflictEntity != null) {
                throw new ServiceException(StrUtil.format("新增失败：SKU【{}】在该仓库已存在相同的推荐仓位", entity.getSkuNo()));
            }
            log.info("开始新增仓位售后推荐单 sku: [{}]", entity.getSkuNo());
        } else {
            // 【修改逻辑】
            // 获取修改前的原始数据（用于日志对比）
            oldEntity = this.getById(entity.getId());
            if (oldEntity == null) {
                throw new ServiceException("修改失败：原记录不存在或已被删除");
            }

            // 冲突检查：如果根据业务组合查到了记录，但 ID 不是当前这条，说明改后会产生重复
            if (conflictEntity != null && !conflictEntity.getId().equals(entity.getId())) {
                throw new ServiceException(StrUtil.format("修改失败：SKU【{}】的新推荐组合与已有记录冲突", entity.getSkuNo()));
            }
            log.info("开始更新仓位售后推荐单 ID: [{}]", entity.getId());
        }

        //
        boolean success = this.saveOrUpdate(entity);

        if (!success) {
            log.error("仓位售后推荐单操作失败 ID: [{}], isSave: [{}]", entity.getId(), isSave);
            throw new ServiceException((isSave ? "新增" : "更新") + "仓位推荐信息失败");
        }

        // 日志记录
        String operator = UserContext.getDefaultLoginUser().getUserName();
        String actionName = isSave ? "新增" : "修改覆盖";
        String msg = StrUtil.format("用户【{}】对 SKU【{}】执行了【{}】相关的仓位售后推荐操作",
                operator, entity.getSkuNo(), actionName);

        operateLogService.addModuleOperateLogByObj(
                oldEntity, // 新增时为 null，修改时为数据库原值
                entity,
                ModuleTypeEnum.AFTERSALES_WAREHOUSE_LOCATION_SUGGEST.getCode(),
                entity.getId(),
                msg
        );

        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(AfterSalesWarehouseLocationSuggestEntity entity) {
        this.removeById(entity.getId());
        //日志记录
        String operator = UserContext.getDefaultLoginUser().getUserName();
        String msg = StrUtil.format("用户【{}】对 SKU【{}】推荐仓位编码【{}】 执行了【{}】相关的【{}】操作",
                operator, entity.getSkuNo(), entity.getWarehouseLocationCode(), "删除", "仓位售后推荐");

        operateLogService.addModuleOperateLog(
                msg,
                ModuleTypeEnum.AFTERSALES_WAREHOUSE_LOCATION_SUGGEST.getCode(),
                entity.getId(),
                "删除操作"
        );
        return BatchResultDTO.success(entity.getId(), null, "删除成功");
    }

    /**
     * 兼容前端勾选导出：仅传 inList 且未带 field 时，按主表主键解析为 awls.id。
     */
    private static void normalizeExportAdvanceQuery(AfterSalesWarehouseLocationSuggestDto.SearchParamDTO params) {
        if (params == null || CollUtil.isEmpty(params.getAdvanceQueryDTOList())) {
            return;
        }
        for (AdvanceQueryDTO q : params.getAdvanceQueryDTOList()) {
            if (q == null || StrUtil.isNotBlank(q.getField())) {
                continue;
            }
            if (!"inList".equals(q.getCompare())) {
                continue;
            }
            Object val = q.getValue();
            if (!(val instanceof Collection) || CollUtil.isEmpty((Collection<?>) val)) {
                continue;
            }
            Object first = ((Collection<?>) val).iterator().next();
            if (first != null && looksLikeSnowflakeId(first.toString())) {
                q.setField("awls.id");
            }
        }
    }

    private static boolean looksLikeSnowflakeId(String s) {
        return s != null && s.matches("\\d{15,22}");
    }

    @Override
    public void exportExcel(PagingDTO<AfterSalesWarehouseLocationSuggestDto.ExportParamDTO> pagingDTO) {
        if (pagingDTO == null) {
            throw new ServiceException("导出参数不能为空");
        }
        if (pagingDTO.getParams() == null) {
            pagingDTO.setParams(new AfterSalesWarehouseLocationSuggestDto.ExportParamDTO());
        }
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        normalizeExportAdvanceQuery(pagingDTO.getParams());
        downloadTaskFeign.saveDownloadTask("售后仓位推荐数据导出", EXPORT_WAREHOUSE_LOCATION_SUGGEST_AFTER_SALES.getCode(), pagingDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importExcel(MultipartFile file, HttpServletResponse response) {
        LoginUser user = UserContext.getNonLoginUser();
        String userName = user.getUserName();
        String uid = user.getUid();

        AfterSalesWarehouseLocationSuggestExcelListener listener = new AfterSalesWarehouseLocationSuggestExcelListener();
        try {
            ExcelReaderBuilder read = EasyExcel.read(file.getInputStream(), listener);
            ExcelReaderSheetBuilder sheet = read.sheet(0);
            sheet.doRead();
        } catch (IOException e) {
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        }

        List<AfterSalesWarehouseLocationSuggestExcelDto> errorList = listener.getErrorList();
        List<AfterSalesWarehouseLocationSuggestExcelDto> verifyList = listener.getSuccessList();
        List<AfterSalesWarehouseLocationSuggestExcelDto> allList = listener.getAllList();

        if (!errorList.isEmpty()) {
            ExcelUtil.export("错误数据", "sheet1", allList, AfterSalesWarehouseLocationSuggestExcelDto.class, response);
        } else {
            log.info("开始封装实体数据，当前操作人：{}", userName);
            // 统一使用应用服务器时间
            LocalDateTime now = LocalDateTime.now();

            List<AfterSalesWarehouseLocationSuggestEntity> entities = verifyList
                    .stream()
                    //增加去重逻辑，防止同一批次出现重复行导致数据库报错
                    .collect(Collectors.toMap(
                            dto -> dto.getSkuNo() + "_" + dto.getWarehouseId() + "_" + dto.getWarehouseLocationId(),
                            dto -> dto,
                            (oldVal, newVal) -> newVal
                    ))
                    .values()
                    .stream()
                    .map(dto -> {
                        AfterSalesWarehouseLocationSuggestEntity entity = new AfterSalesWarehouseLocationSuggestEntity();
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
                List<List<AfterSalesWarehouseLocationSuggestEntity>> batches = ListUtils.partition(entities, 500);
                for (List<AfterSalesWarehouseLocationSuggestEntity> batch : batches) {
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
        String path = "excel/afterSalesWarehouseLocationSuggestExport.xlsx";
        String excelName = "售后仓位推荐导入模板.xlsx";

        ResourceLoader resourceLoader = new DefaultResourceLoader();

        // 使用 try-with-resources 自动管理资源
        try (InputStream inputStream = resourceLoader.getResource(path).getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(inputStream);
             OutputStream output = response.getOutputStream()) {

            response.reset();
            // 设置文件头及编码，防止中文文件名乱码
            String encodedFileName = new String(excelName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            wb.write(output);
            output.flush(); // 显式刷新缓冲区
        } catch (IOException e) {
            log.error("下载仓位推荐模板失败", e);
            throw new ServiceException(ApiError.FILE_IMPORT_TEMPLATE_DOWNLOAD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDisabled(AfterSalesWarehouseLocationSuggestDto.UpdateStatusDto dto) {
        LoginUser user = UserContext.getNonLoginUser();
        AfterSalesWarehouseLocationSuggestEntity entity = new AfterSalesWarehouseLocationSuggestEntity();
        entity.setId(dto.getId());
        entity.setDisabled(Boolean.valueOf(dto.getDisabled()));
        baseMapper.updateById(entity);
        operateLogService.addModuleOperateLog(String.format("更新售后推荐仓位状态：%s", entity.getDisabled() ? "禁用" : "启用"), ModuleTypeEnum.AFTERSALES_WAREHOUSE_LOCATION_SUGGEST.getCode(), entity.getId(), "状态变更", user.getUid(), user.getUserName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> updateStatusBatch(AfterSalesWarehouseLocationSuggestDto.UpdateStatusDto dto) {
        if (CollectionUtils.isEmpty(dto.getIds())) {
            return Collections.emptyList();
        }
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        LoginUser user = UserContext.getNonLoginUser();

        List<AfterSalesWarehouseLocationSuggestEntity> list = baseMapper.selectBatchIds(dto.getIds());

        List<AfterSalesWarehouseLocationSuggestEntity> updateList = new ArrayList<>();
        for (AfterSalesWarehouseLocationSuggestEntity entity : list) {
            if (entity.getDisabled().equals(Boolean.parseBoolean(dto.getDisabled()))) {
                continue;
            }
            entity.setDisabled(Boolean.valueOf(dto.getDisabled()));
            updateList.add(entity);
            operateLogService.addModuleOperateLog(String.format("更新售后推荐仓位状态：%s", entity.getDisabled() ? "禁用" : "启用"), ModuleTypeEnum.AFTERSALES_WAREHOUSE_LOCATION_SUGGEST.getCode(), entity.getId(), "状态变更", user.getUid(), user.getUserName());
        }
        if (!updateList.isEmpty()) {
            this.updateBatchById(updateList);
        }
        //暂时没有对单个数据的修改状态做判断是否成功失败只返回空的结果后续可以加逻辑
        return resultDTOList;
    }


    /**
     * 填充视图
     *
     * @param entityList 实体列表
     * @return 列表视图
     */
    private List<AfterSalesWarehouseLocationSuggestDto.ListDTO> fillViewList(List<AfterSalesWarehouseLocationSuggestEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        // 1. 提取所有关联 ID/Code 提升批量查询效率
        List<String> skuNoList = entityList.stream().map(AfterSalesWarehouseLocationSuggestEntity::getSkuNo).distinct().collect(Collectors.toList());
        List<String> warehouseIdList = entityList.stream().map(AfterSalesWarehouseLocationSuggestEntity::getWarehouseId).distinct().collect(Collectors.toList());

        // 2. 批量获取外部数据并转为 Map (空间换时间)
        // SKU 基础信息
        List<ProductDetailEntity> skuDetailEntities = plmTaskFeign.listBySkuNos(skuNoList);
        Map<String, ProductDetailEntity> skuDetailMap = skuDetailEntities.stream()
                .collect(Collectors.toMap(ProductDetailEntity::getSkuNo, item -> item, (k1, k2) -> k1));

        // 仓库名称 Map
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);
        Map<String, String> warehouseIdNameMap = warehouseList.stream()
                .collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName, (k1, k2) -> k1));

        // 关键优化点：将所有库位/库区信息转成 Map 结构，Key 使用 "warehouseId_code" 保证唯一性
        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.list(Wrappers.lambdaQuery(WarehouseLocationEntity.class)
                .in(WarehouseLocationEntity::getWarehouseId, warehouseIdList)
                .eq(WarehouseLocationEntity::getIsDeleted, false));

        Map<String, WarehouseLocationEntity> locationMap = warehouseLocationList.stream()
                .collect(Collectors.toMap(
                        item -> item.getWarehouseId() + "_" + item.getCode() + "_" + item.getType().toUpperCase(),
                        item -> item,
                        (oldVal, newVal) -> oldVal));

        // 3. 组装数据，直接通过 Map 获取，复杂度降至 O(1)
        List<AfterSalesWarehouseLocationSuggestDto.ListDTO> dtoList = new ArrayList<>(entityList.size());
        for (AfterSalesWarehouseLocationSuggestEntity entity : entityList) {
            AfterSalesWarehouseLocationSuggestDto.ListDTO dto = new AfterSalesWarehouseLocationSuggestDto.ListDTO();
            BeanMapperUtils.copy(entity, dto);

            // 匹配 SKU 信息
            ProductDetailEntity skuDetail = skuDetailMap.get(entity.getSkuNo());
            if (skuDetail != null) {
                dto.setProductName(skuDetail.getName());
            }

            // 匹配仓库
            dto.setWarehouseName(warehouseIdNameMap.get(entity.getWarehouseId()));

            // 匹配库区 (使用组合 Key)
            String areaKey = entity.getWarehouseId() + "_" + entity.getWarehouseAreaCode() + "_" + WarehouseLocationTypeEnum.AREA;
            WarehouseLocationEntity areaEntity = locationMap.getOrDefault(areaKey, new WarehouseLocationEntity());
            dto.setWarehouseAreaId(areaEntity.getId());
            dto.setWarehouseAreaName(areaEntity.getName());

            // 匹配仓位 (使用组合 Key)
            String locationKey = entity.getWarehouseId() + "_" + entity.getWarehouseLocationCode() + "_" + WarehouseLocationTypeEnum.LOCATION;
            WarehouseLocationEntity locationEntity = locationMap.getOrDefault(locationKey, new WarehouseLocationEntity());
            dto.setWarehouseLocationId(locationEntity.getId());
            dto.setWarehouseLocationName(locationEntity.getName());

            dto.setDisabled(entity.getDisabled());
            dto.setStatusName(entity.getDisabled()?"禁用":"启用");
            dto.setUpdateUser(entity.getUpdateUserName());
            dtoList.add(dto);
        }
        return dtoList;
    }


    @Override
    public List<AfterSalesWarehouseLocationSuggestDto.PdaListDto> getSuggestWarehouseLocationList(AfterSalesWarehouseLocationSuggestDto.PdaSearchDto searchDto) {
        LambdaQueryWrapper<AfterSalesWarehouseLocationSuggestEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(
                        AfterSalesWarehouseLocationSuggestEntity::getWarehouseLocationCode,
                        AfterSalesWarehouseLocationSuggestEntity::getSort,
                        AfterSalesWarehouseLocationSuggestEntity::getDisabled
                )
                .eq(AfterSalesWarehouseLocationSuggestEntity::getSkuNo, searchDto.getSkuNo())
                .eq(AfterSalesWarehouseLocationSuggestEntity::getWarehouseId, searchDto.getWarehouseId());

        List<AfterSalesWarehouseLocationSuggestEntity> entityList = list(wrapper);
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }
        List<AfterSalesWarehouseLocationSuggestDto.PdaListDto> dtoList = new ArrayList<>(entityList.size());
        for (AfterSalesWarehouseLocationSuggestEntity entity : entityList) {
            AfterSalesWarehouseLocationSuggestDto.PdaListDto dto = new AfterSalesWarehouseLocationSuggestDto.PdaListDto();
            BeanMapperUtils.copy(entity, dto);
            dtoList.add(dto);
        }
        return dtoList;
    }
}
