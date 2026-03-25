package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.QcStandardDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.QcStandardDetailEntity;
import com.erp.model.wms.entity.QcStandardEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.QcStandardImageTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.QcStandardMapper;
import com.erp.server.wms.service.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.common.business.threadlocal.UserContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_QC_STANDARD;

/**
 * 质检标准主表 Service 实现类
 *
 * @author jack
 * @since 2026-03-22
 */
@Service
public class QcStandardServiceImpl extends ServiceImpl<QcStandardMapper, QcStandardEntity> implements QcStandardService {

    @Autowired
    private QcStandardDetailService qcStandardDetailService;

    @Autowired
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(QcStandardDTO.AddDTO addDTO) {
        // 校验唯一性
        List<WmsAttachmentEntity> wmsAttachmentEntities = addDTO.getWmsAttachmentEntities();
        if(CollectionUtils.isEmpty(wmsAttachmentEntities)){
            checkUnique(addDTO.getSkuId(), null);
        }

        QcStandardEntity entity = BeanMapperUtils.map(QcStandardEntity.class, addDTO);
        entity.setId(IdWorker.getIdStr());
        findSkuNo(entity);

        // 保存主表
        this.save(entity);

        // 记录审计日志 (主表优先)
        String msg = StrUtil.format("用户【{}】新增质检标准，SKU编号【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getSkuNo());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_STANDARD.getCode(), entity.getId(), "新增操作");

        // 保存子表 明细
        List<QcStandardDetailEntity> detailList = BeanMapperUtils.copyList(QcStandardDetailEntity.class, addDTO.getDetailList());
        if (CollectionUtils.isNotEmpty(detailList)) {
            int sort = 0;
            for (QcStandardDetailEntity detail : detailList) {
                detail.setMainId(entity.getId());
                detail.setSort(sort++);
            }
            qcStandardDetailService.saveBatch(detailList);
        }

        // 保存附件 (标准化接收规则)

        if(CollectionUtils.isNotEmpty(wmsAttachmentEntities)){
            for (WmsAttachmentEntity wmsAttachmentEntity : wmsAttachmentEntities) {
                wmsAttachmentEntity.setBusinessId(entity.getId());
                wmsAttachmentEntity.setId(IdWorker.getIdStr());
            }
            wmsAttachmentService.saveBatch(wmsAttachmentEntities);
        }else {
            addAttachments(addDTO.getAttachmentList(), entity.getId());
        }

    }

    private static void findSkuNo(QcStandardEntity entity) {
        List<ProductDetailEntity> skuList = FeignQuery.create(ProductDetailEntity.class).eq(ProductDetailEntity::getId, entity.getSkuId()).list();
        if(CollectionUtils.isEmpty(skuList)){
            throw new ServiceException(ApiError.COMMON_NO_SKU);
        }
        String skuNo = skuList.get(0).getSkuNo();
        entity.setSkuNo(skuNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(QcStandardDTO.UpdateDTO updateDTO) {
        QcStandardEntity oldEntity = this.getById(updateDTO.getId());
        if (oldEntity == null) {
            throw new ServiceException("质检标准不存在");
        }
        
        // 校验唯一性
        checkUnique(updateDTO.getSkuId(), updateDTO.getId());

        QcStandardEntity newEntity = BeanMapperUtils.map(QcStandardEntity.class, updateDTO);
        findSkuNo(newEntity);

        this.updateById(newEntity);

        // 记录审计日志 (主表优先)
        String msg = StrUtil.format("用户【{}】编辑质检标准，SKU编号【{}】", UserContext.getDefaultLoginUser().getUserName(), oldEntity.getSkuNo());
        operateLogService.addModuleOperateLogByObj(oldEntity, newEntity, ModuleTypeEnum.QC_STANDARD.getCode(), updateDTO.getId(), msg);

        // 使用差分更新明细
        List<QcStandardDetailEntity> dbDetailList = qcStandardDetailService.list(Wrappers.<QcStandardDetailEntity>lambdaQuery()
                .eq(QcStandardDetailEntity::getMainId, updateDTO.getId()));
        List<QcStandardDetailEntity> newDetailList = BeanMapperUtils.copyList(QcStandardDetailEntity.class, updateDTO.getDetailList());
        if (CollectionUtils.isNotEmpty(newDetailList)) {
            int sort =0;
            for (QcStandardDetailEntity detail : newDetailList) {
                detail.setMainId(updateDTO.getId());
                detail.setSort(sort++);
            }
        } else {
            newDetailList = new ArrayList<>();
        }
        updateDetails(updateDTO.getId(), newDetailList, dbDetailList);

        // 更新保存附件 (标准化更新规则)
        updateAttachments(updateDTO.getAttachmentList(), updateDTO.getId());
    }

    /**
     * 添加附件信息 (标准化逻辑)
     */
    private void addAttachments(List<QcStandardDTO.AttachDTO> attachmentList, String businessId) {
        if (CollectionUtils.isEmpty(attachmentList)) {
            return;
        }
        List<WmsAttachmentEntity> entities = new ArrayList<>();
        for (QcStandardDTO.AttachDTO attachDTO : attachmentList) {
            List<String> urlList = attachDTO.getAttachmentUrlList();
            List<String> nameList = attachDTO.getAttachmentNameList();
            if (CollectionUtils.isNotEmpty(urlList) && CollectionUtils.isNotEmpty(nameList) && urlList.size() == nameList.size()) {
                for (int i = 0; i < urlList.size(); i++) {
                    WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                    attachment.setAttachUrl(urlList.get(i));
                    attachment.setAttachName(nameList.get(i));
                    attachment.setBusinessId(businessId);
                    attachment.setType(attachDTO.getType());
                    entities.add(attachment);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(entities)) {
            wmsAttachmentService.saveBatch(entities);
        }
    }

    /**
     * 更新附件信息 (标准化逻辑 - 差分)
     */
    private void updateAttachments(List<QcStandardDTO.AttachDTO> attachmentList, String businessId) {
        // 获取旧数据
        List<WmsAttachmentDTO.UpdateDTO> oldAttachments = wmsAttachmentService.getByBusinessIds(Collections.singletonList(businessId));
        
        // 准备新的 URL 集合用于比对 (扁平化)
        Set<String> newUrlSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            attachmentList.forEach(dto -> {
                if (CollectionUtils.isNotEmpty(dto.getAttachmentUrlList())) {
                    newUrlSet.addAll(dto.getAttachmentUrlList());
                }
            });
        }

        // 1. 处理删除：旧的有，新的没有
        if (CollectionUtils.isNotEmpty(oldAttachments)) {
            List<String> deleteUrlList = oldAttachments.stream()
                    .map(WmsAttachmentDTO.UpdateDTO::getAttachUrl)
                    .filter(url -> !newUrlSet.contains(url))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deleteUrlList)) {
                wmsAttachmentService.deleteByUrlList(deleteUrlList);
            }
        }

        // 2. 处理新增：新的有，旧的没有
        Set<String> oldUrlSet = CollectionUtils.isEmpty(oldAttachments) ? Collections.emptySet() :
                oldAttachments.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toSet());
        
        List<WmsAttachmentEntity> addEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            for (QcStandardDTO.AttachDTO attachDTO : attachmentList) {
                List<String> urlList = attachDTO.getAttachmentUrlList();
                List<String> nameList = attachDTO.getAttachmentNameList();
                if (CollectionUtils.isNotEmpty(urlList) && CollectionUtils.isNotEmpty(nameList) && urlList.size() == nameList.size()) {
                    for (int i = 0; i < urlList.size(); i++) {
                        String url = urlList.get(i);
                        if (!oldUrlSet.contains(url)) {
                            WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                            attachment.setAttachUrl(url);
                            attachment.setAttachName(nameList.get(i));
                            attachment.setBusinessId(businessId);
                            attachment.setType(attachDTO.getType());
                            addEntities.add(attachment);
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(addEntities)) {
            wmsAttachmentService.saveBatch(addEntities);
        }
    }

    private void checkUnique(String skuId, String excludeId) {
        LambdaQueryWrapper<QcStandardEntity> wrapper = Wrappers.<QcStandardEntity>lambdaQuery()
                .eq(QcStandardEntity::getSkuId, skuId)
                .eq(QcStandardEntity::getIsDeleted, false);
        if (excludeId != null) {
            wrapper.ne(QcStandardEntity::getId, excludeId);
        }
        if (this.count(wrapper) > 0) {
            throw new ServiceException("该SKU已存在质检标准");
        }
    }

    @Override
    public List<QcStandardDTO.TabListDTO> tabList(PermissionsDTO permissionsDTO) {
        QcStandardDTO.PagingParamDTO searchParam = new QcStandardDTO.PagingParamDTO();
        searchParam.setPermissionSql(permissionsDTO.getPermissionSql());

        List<QcStandardDTO.TabListDTO> list = this.baseMapper.tabList(searchParam);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(0, new QcStandardDTO.TabListDTO("all", "全部", 0));
        QcStandardDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals("enable")).findFirst().orElse(null);
        if(Objects.isNull(enable)){
            list.add( new QcStandardDTO.TabListDTO("enable", "启用", 0));
        }else{
            list.add( enable);
        }
        QcStandardDTO.TabListDTO disabled = list.stream().filter(e -> e.getTabFlag().equals("disabled")).findFirst().orElse(null);
        if(Objects.isNull(disabled)){
            list.add( new QcStandardDTO.TabListDTO("disabled", "禁用", 0));
        }else{
            list.add( disabled);
        }
        return list;
    }


    @Override
    public PagingVO<QcStandardDTO.ExportDTO> exportList(PagingDTO<QcStandardDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<QcStandardDTO.ExportDTO> pageData = this.baseMapper.exportList(query,pagingParamDTO.getParams());
        if(CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }

        List<String> ids = pageData.getRecords().stream().map(QcStandardDTO.ExportDTO::getId).distinct().collect(Collectors.toList());

        // 载入附件图片 (标准化回显)
        Map<String, List<WmsAttachmentDTO.UpdateDTO>> grouped = new HashMap<>();
        List<WmsAttachmentDTO.UpdateDTO> allAttachments = wmsAttachmentService.getByBusinessIds(ids);
        if (CollectionUtils.isNotEmpty(allAttachments)) {
            grouped = allAttachments.stream()
                    .collect(Collectors.groupingBy(WmsAttachmentDTO.UpdateDTO::getBusinessId));
        }


        for (QcStandardDTO.ExportDTO listDTO : pageData.getRecords()) {
            listDTO.setDisabledName(listDTO.getDisabled() ? "禁用" : "启用" );
            List<WmsAttachmentDTO.UpdateDTO> updateDTOS = grouped.get(listDTO.getId());
            if(CollectionUtils.isNotEmpty(updateDTOS)){
                Map<String, List<WmsAttachmentDTO.UpdateDTO>> listMap = updateDTOS.stream().collect(Collectors.groupingBy(WmsAttachmentDTO.UpdateDTO::getType));

                List<WmsAttachmentDTO.UpdateDTO> productPhysicalList = listMap.get("productPhysical");
                //url用回车换行拼接
                listDTO.setProductPhysicalUrl(productPhysicalList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.joining("\n")));

                List<WmsAttachmentDTO.UpdateDTO> packagingAccessoriesList = listMap.get("packagingAccessories");
                listDTO.setPackagingAccessoriesUrl(packagingAccessoriesList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.joining("\n")));
            }
        }

        return new PagingVO(pageData);
    }


    @Override
    public PagingVO<QcStandardDTO.ListDTO> paging(PagingDTO<QcStandardDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<QcStandardDTO.ListDTO> pageData = this.baseMapper.paging(query,pagingParamDTO.getParams());
        if(CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<QcStandardDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        for (QcStandardDTO.ListDTO listDTO : list) {
            listDTO.setDisabledName(listDTO.getDisabled() ? "禁用" : "启用" );
        }
    }

    @Override
    public QcStandardDTO.ViewDTO view(String id) {
        QcStandardEntity entity = this.getById(id);
        if (entity == null) {
            throw new ServiceException("质检标准不存在");
        }
        return getFullViewDTO(entity);
    }

    @Override
    public QcStandardDTO.ViewDTO copyBySku(String skuNo) {
        QcStandardEntity entity = this.getOne(Wrappers.<QcStandardEntity>lambdaQuery()
                .eq(QcStandardEntity::getSkuNo, skuNo)
                .eq(QcStandardEntity::getIsDeleted, false));
        if (entity == null) {
            throw new ServiceException(ApiError.QC_STANDARD_SKU_NOT_FOUND, skuNo);
        }
        QcStandardDTO.ViewDTO viewDTO = getFullViewDTO(entity);
        // 清除 ID，以便前端作为新记录处理（可选，根据业务规范通常由前端处理，但后端返回干净数据更优）
        viewDTO.setId(null);
        if (CollectionUtils.isNotEmpty(viewDTO.getDetailList())) {
            viewDTO.getDetailList().forEach(d -> d.setId(null));
        }
        return viewDTO;
    }
    /**
     * 构建完整的详情 DTO (包含明细和附件)
     */
    private QcStandardDTO.ViewDTO getFullViewDTO(QcStandardEntity entity) {
        String id = entity.getId();
        QcStandardDTO.ViewDTO viewDTO = BeanMapperUtils.map(QcStandardDTO.ViewDTO.class, entity);
        
        // 载入详情
        List<QcStandardDetailEntity> details = qcStandardDetailService.list(Wrappers.<QcStandardDetailEntity>lambdaQuery()
                .eq(QcStandardDetailEntity::getMainId, id)
                .orderByAsc(QcStandardDetailEntity::getSort));
        viewDTO.setDetailList(BeanMapperUtils.copyList(QcStandardDTO.DetailDTO.class, details));
        
        // 载入附件图片 (标准化回显)
        List<WmsAttachmentDTO.UpdateDTO> allAttachments = wmsAttachmentService.getByBusinessIds(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(allAttachments)) {
            Map<String, List<WmsAttachmentDTO.UpdateDTO>> grouped = allAttachments.stream()
                    .collect(Collectors.groupingBy(WmsAttachmentDTO.UpdateDTO::getType));


            List<QcStandardDTO.AttachDTO> attachmentList = new ArrayList<>();
            grouped.forEach((type, attachments) -> {
                QcStandardDTO.AttachDTO attachDTO = new QcStandardDTO.AttachDTO();
                attachDTO.setType(type);
                attachDTO.setTypeName(QcStandardImageTypeEnum.getByCode(type));
                attachDTO.setAttachmentNameList(attachments.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList()));
                attachDTO.setAttachmentUrlList(attachments.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
                attachmentList.add(attachDTO);
            });
            viewDTO.setAttachmentList(attachmentList);
        }
        return viewDTO;
    }

    @Override
    public void export(PagingDTO<QcStandardDTO.PagingParamDTO> pagingParamDTO) {
        QcStandardDTO.PagingParamDTO params = pagingParamDTO.getParams();
        params.setPermissionSql(pagingParamDTO.getPermissionSql());
        downloadTaskFeign.saveDownloadTask("质检标准导出", EXPORT_WMS_QC_STANDARD.getCode(), params);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        QcStandardEntity entity = this.getById(id);
        if (entity == null) {
            return BatchResultDTO.fail(id, id, "数据不存在");
        }
        this.removeById(id);
        qcStandardDetailService.lambdaUpdate().set(QcStandardDetailEntity::getIsDeleted,true).eq(QcStandardDetailEntity::getMainId, id);
        // 删除附件 (标准化逻辑)
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            List<String> urlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            wmsAttachmentService.deleteByUrlList(urlList);
        }

        // 记录审计日志
        String msg = StrUtil.format("用户【{}】删除质检标准，SKU编号【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getSkuNo());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_STANDARD.getCode(), entity.getId(), "删除操作");

        return BatchResultDTO.success(id, entity.getSkuNo(), "删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(QcStandardDTO.UpdateStatusDTO params) {
        QcStandardEntity entity = this.getById(params.getId());
        if (entity == null) {
            return BatchResultDTO.fail(params.getId(), params.getId(), "数据不存在");
        }
        QcStandardEntity updateEntity = new QcStandardEntity();
        updateEntity.setId(params.getId());
        updateEntity.setDisabled(params.getDisabled());
        this.updateById(updateEntity);

        // 记录审计日志
        String statusLabel = params.getDisabled() ? "禁用" : "启用";
        String msg = StrUtil.format("用户【{}】{}了SKU【{}】的质检标准", UserContext.getDefaultLoginUser().getUserName(), statusLabel, entity.getSkuNo());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_STANDARD.getCode(), entity.getId(), statusLabel + "操作");

        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), "状态更新成功");
    }
    private void updateDetails(String mainId, List<QcStandardDetailEntity> newList, List<QcStandardDetailEntity> oldList) {
        if (CollectionUtils.isEmpty(oldList)) {
            if (CollectionUtils.isNotEmpty(newList)) {
                qcStandardDetailService.saveBatch(newList);
            }
            return;
        }
        List<String> newIds = newList.stream().map(QcStandardDetailEntity::getId).filter(id -> id != null).collect(Collectors.toList());
        List<String> deleteIds = oldList.stream().map(QcStandardDetailEntity::getId).filter(id -> !newIds.contains(id)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<QcStandardDetailEntity> removeList = oldList.stream().filter(e -> deleteIds.contains(e.getId())).collect(Collectors.toList());
            qcStandardDetailService.removeByIds(deleteIds);

            // 记录审计日志
            List<Pair<String, String>> removePairList = removeList.stream()
                    .map(obj -> new Pair<>(mainId, obj.getInspectItemName()))
                    .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("编辑：删除质检项【%s】", ModuleTypeEnum.QC_STANDARD.getCode(), removePairList, "编辑操作");
        }
        List<QcStandardDetailEntity> addList = newList.stream().filter(e -> e.getId() == null).collect(Collectors.toList());
        List<QcStandardDetailEntity> updateList = newList.stream().filter(e -> e.getId() != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            qcStandardDetailService.saveBatch(addList);

            // 记录审计日志
            List<Pair<String, String>> addPairList = addList.stream()
                    .map(obj -> new Pair<>(mainId, obj.getInspectItemName()))
                    .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("编辑：新增质检项【%s】", ModuleTypeEnum.QC_STANDARD.getCode(), addPairList, "编辑操作");
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            qcStandardDetailService.updateBatchById(updateList);

            // 逐一记录审计日志 (差分)
            for (QcStandardDetailEntity detail : updateList) {
                QcStandardDetailEntity oldDetail = oldList.stream().filter(e -> Objects.equals(e.getId(), detail.getId())).findFirst().orElse(null);
                if (Objects.nonNull(oldDetail)) {
                    String msg = StrUtil.format("编辑质检项【{}】内容", oldDetail.getInspectItemName());
                    operateLogService.addModuleOperateLogByObj(oldDetail, detail, ModuleTypeEnum.QC_STANDARD.getCode(), mainId, msg);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importFile(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            throw new ServiceException("导入文件URL不能为空");
        }

        try (InputStream inputStream = FastDFSClientUtil.getInputStream(fileUrl)) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // 1. 建立 DISPIMG ID 到 PictureData 的映射 (针对 WPS 嵌入图片)
            Map<String, PictureData> cellImageMap = new HashMap<>();
            if (workbook instanceof XSSFWorkbook) {
                try {
                    initCellImageMap((XSSFWorkbook) workbook, cellImageMap);
                } catch (Exception e) {
                    throw new ServiceException("分析 cellimages.xml 失败或不包含嵌入图片: {}",e.getMessage());
                }
            }

            // 2. 提取 SKU 和产品名称 (前15行遍历查找关键字)
            String skuStr = "";
            for (int i = 2; i < 4; i++) { // 优化查找范围
                Row row = sheet.getRow(i);
                if (row == null) continue;
                for (int j = 0; j < 10; j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) continue;
                    String val = cell.toString().trim();
                    if (val.contains("产品SKU") || (val.equalsIgnoreCase("SKU") && val.length() == 3)) {
                        Cell valCell = row.getCell(j + 1);
                        if (valCell != null) skuStr = valCell.toString().trim();
                        break;
                    }
                }
                if (StringUtils.isNotBlank(skuStr)) break;
            }

            if (StringUtils.isBlank(skuStr)) {
                throw new ServiceException("未在Excel中找到“产品SKU”关键字或对应数值");
            }

            // 3. 提取图片 (第8-11行)
            List<WmsAttachmentEntity> attachmentList = new ArrayList<>();
            extractImages(sheet, attachmentList, cellImageMap);

            // 4. 解析逻辑详情 (第15行开始)
            List<QcStandardDTO.DetailDTO> detailList = new ArrayList<>();
            for (int i = 14; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Cell indexCell = row.getCell(0);
                if (indexCell == null) continue;
                String indexVal = indexCell.toString().trim();

                // 正则匹配纯数字序号 (1, 2, 3...)
                if (indexVal.matches("^\\d+(\\.\\d+)?$")) {
                    Cell itemCell = row.getCell(1);
                    Cell reqCell = row.getCell(2);
                    String itemName = itemCell != null ? itemCell.toString().trim() : "";
                    String requirement = reqCell != null ? reqCell.toString().trim() : "";

                    if (StringUtils.isNotBlank(requirement)) {
                        QcStandardDTO.DetailDTO detail = new QcStandardDTO.DetailDTO();
                        detail.setInspectItemName(itemName);
                        detail.setInspectRequirement(requirement);
                        detailList.add(detail);
                    }
                }
            }

            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException("未发现有效的质检明细（请确保从第15行开始有数字序号的明细项）");
            }

            // 5. 处理 SKU 拆分与覆盖保存
            String[] split = skuStr.split("[,，]");
            List<String> skus = new ArrayList<>();
            for (String s : split) {
                if (StringUtils.isNotBlank(s)) skus.add(s.trim());
            }

            Map<String, QcStandardEntity> existingMap = lambdaQuery().in(QcStandardEntity::getSkuId, skus).eq(QcStandardEntity::getIsDeleted, false).list()
                    .stream().collect(Collectors.toMap(QcStandardEntity::getSkuId, Function.identity(), (o1, o2) -> o1));

            QcStandardServiceImpl bean = ApplicationContextUtils.getBean(QcStandardServiceImpl.class);
            List<ProductDetailEntity> skuVOList = plmTaskFeign.listBySkuNos(skus);
            
            for (ProductDetailEntity productDetailEntity : skuVOList) {
                String skuId = productDetailEntity.getId();
                QcStandardEntity existing = existingMap.get(skuId);
                if (existing != null) {
                    bean.delete(existing.getId());
                }

                QcStandardDTO.AddDTO addDTO = new QcStandardDTO.AddDTO();
                addDTO.setSkuId(skuId);
                addDTO.setDetailList(detailList);
                if (CollectionUtils.isNotEmpty(attachmentList)) {
                    // 深度拷贝附件实体，防止 businessId 冲突
                    List<WmsAttachmentEntity> copyAttachments = attachmentList.stream().map(a -> {
                        WmsAttachmentEntity copy = new WmsAttachmentEntity();
                        copy.setAttachUrl(a.getAttachUrl());
                        copy.setAttachName(a.getAttachName());
                        copy.setType(a.getType());
                        return copy;
                    }).collect(Collectors.toList());
                    addDTO.setWmsAttachmentEntities(copyAttachments);
                }
                bean.add(addDTO);
            }

        } catch (Exception e) {
            log.error("质检报告导入失败", e);
            throw new ServiceException("解析报告失败：" + e.getMessage());
        }
    }

    /**
     * 提取图片并根据行列坐标进行分类
     * 支持浮动图片（Shapes）和 WPS 嵌入式图片（DISPIMG）
     */
    private void extractImages(Sheet sheet, List<WmsAttachmentEntity> list, Map<String, PictureData> cellImageMap) {
        // A. 抓取浮动图片 (Shapes)
        Drawing<?> drawing = sheet.getDrawingPatriarch();
        if (drawing instanceof XSSFDrawing) {
            XSSFDrawing xssfDrawing = (XSSFDrawing) drawing;
            for (XSSFShape shape : xssfDrawing.getShapes()) {
                if (shape instanceof XSSFPicture) {
                    XSSFPicture picture = (XSSFPicture) shape;
                    ClientAnchor anchor = picture.getClientAnchor();
                    processAndAddImage(picture.getPictureData(), anchor.getRow1(), anchor.getCol1(), list);
                }
            }
        }

        // B. 扫描 8-11 行单元格，查找 DISPIMG 公式 (WPS 嵌入图片)
        for (int r = 7; r <= 10; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            for (int c = 1; c <= 13; c++) { // A列在0，图片通常在B列(1)起
                Cell cell = row.getCell(c);
                if (cell == null) continue;
                if (cell.getCellType() == CellType.FORMULA) {
                    String formula = cell.getCellFormula();
                    if (formula.contains("DISPIMG")) {
                        String imgId = extractDispImgId(formula);
                        if (StringUtils.isNotBlank(imgId) && cellImageMap.containsKey(imgId)) {
                            processAndAddImage(cellImageMap.get(imgId), r, c, list);
                        }
                    }
                }
            }
        }
    }

    private void processAndAddImage(PictureData data, int rowIdx, int colIdx, List<WmsAttachmentEntity> list) {
        // 确认在 8-11 行范围内 (index 7-10)，放宽 1 行误差
        if (rowIdx >= 7 && rowIdx <= 11) {
            String type = null;
            // 分类判断: A-I (index 0-8) -> 实物; J-M (index 9-12) -> 配件
            // 放宽列判定: J列起 index 为 9
            if (colIdx >= 0 && colIdx <= 8) {
                type = "productPhysical";
            } else if (colIdx >= 9 && colIdx <= 15) { // 放宽到15列
                type = "packagingAccessories";
            }

            if (type != null) {
                byte[] bytes = data.getData();
                String ext = data.suggestFileExtension();
                String fileName = UUID.randomUUID().toString() + "." + ext;
                String url = FastDFSClientUtil.uploadFile(bytes, fileName, null);
                if (StringUtils.isNotBlank(url)) {
                    WmsAttachmentEntity att = new WmsAttachmentEntity();
                    att.setAttachUrl(url);
                    att.setAttachName(fileName);
                    att.setType(type);
                    list.add(att);
                }
            }
        }
    }

    private String extractDispImgId(String formula) {
        try {
            int start = formula.indexOf("\"") + 1;
            int end = formula.indexOf("\"", start);
            if (start > 0 && end > start) {
                return formula.substring(start, end).trim();
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * 解析 WPS cellimages.xml 建立 ID 到数据的映射
     */
    private void initCellImageMap(XSSFWorkbook workbook, Map<String, PictureData> map) throws Exception {
        PackagePart cellImagesPart = null;
        for (PackagePart part : workbook.getPackage().getParts()) {
            String name = part.getPartName().getName();
            if (name.contains("cellimages.xml")) {
                cellImagesPart = part;
                break;
            }
        }
        if (cellImagesPart == null) return;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(cellImagesPart.getInputStream());
        
        // WPS 使用 etc:cellImage，命名空间可能不同，这里尝试通用获取
        NodeList imageNodes = doc.getElementsByTagNameNS("*", "cellImage");
        if (imageNodes.getLength() == 0) imageNodes = doc.getElementsByTagName("cellImage");
        if (imageNodes.getLength() == 0) imageNodes = doc.getElementsByTagName("etc:cellImage");

        for (int i = 0; i < imageNodes.getLength(); i++) {
            Element imgElem = (Element) imageNodes.item(i);
            
            // 提取 ID (name 属性)
            String name = "";
            NodeList cNvPrList = imgElem.getElementsByTagNameNS("*", "cNvPr");
            if (cNvPrList.getLength() == 0) cNvPrList = imgElem.getElementsByTagName("cNvPr");
            if (cNvPrList.getLength() > 0) {
                name = ((Element) cNvPrList.item(0)).getAttribute("name");
            }

            // 提取 rId
            String rId = "";
            NodeList blipList = imgElem.getElementsByTagNameNS("*", "blip");
            if (blipList.getLength() == 0) blipList = imgElem.getElementsByTagName("a:blip");
            if (blipList.getLength() > 0) {
                rId = ((Element) blipList.item(0)).getAttribute("r:embed");
            }

            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(rId)) {
                try {
                    PackagePart mediaPart = cellImagesPart.getRelatedPart(cellImagesPart.getRelationship(rId));
                    if (mediaPart != null) {
                        for (PictureData pd : workbook.getAllPictures()) {
                            if (pd instanceof XSSFPictureData) {
                                XSSFPictureData xpd = (XSSFPictureData) pd;
                                if (xpd.getPackagePart().getPartName().equals(mediaPart.getPartName())) {
                                    map.put(name, pd);
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}
