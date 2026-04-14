package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.annotation.DistributeLocker;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDateTime;
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
public class QcStandardServiceImpl extends ServiceImpl<QcStandardMapper, QcStandardEntity>
        implements QcStandardService {

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
    @DistributeLocker(keyName = "addDTO.getSkuId()")
    public void add(QcStandardDTO.AddDTO addDTO) {
        //假如数据来源是导入，并且已存在SKU的质检标准则按更新逻辑走
        if (addDTO.getIsImport()) {
            QcStandardEntity existEntity = lambdaQuery().eq(QcStandardEntity::getSkuId, addDTO.getSkuId()).last(" limit 1 ").one();
            if(Objects.nonNull(existEntity)){
                String id = existEntity.getId();
//                List<WmsAttachmentEntity> attachmentList = addDTO.getWmsAttachmentEntities();
//                if (CollectionUtils.isNotEmpty(addDTO.getAttachmentList())) {
//                    List<QcStandardDTO.AttachDTO> attachList = new ArrayList<>();
//                    List<WmsAttachmentEntity> productPhysical = attachmentList.stream().filter(e -> e.getType().equals(QcStandardImageTypeEnum.PRODUCT_PHYSICAL.getCode())).collect(Collectors.toList());
//                    if(CollectionUtils.isNotEmpty(productPhysical)){
//                        QcStandardDTO.AttachDTO attachDTO = new QcStandardDTO.AttachDTO();
//                        attachDTO.setType(QcStandardImageTypeEnum.PRODUCT_PHYSICAL.getCode());
//                        List<String> attachmentNameList = new ArrayList<>();
//                        List<String> attachmentUrlList = new ArrayList<>();
//
//                        for (WmsAttachmentEntity entity : productPhysical) {
//                            attachmentNameList.add(entity.getAttachName());
//                            attachmentUrlList.add(entity.getAttachUrl());
//                        }
//                        attachDTO.setAttachmentNameList(attachmentNameList);
//                        attachDTO.setAttachmentUrlList(attachmentUrlList);
//                        attachList.add(attachDTO);
//                    }
//                    List<WmsAttachmentEntity> packagingAccessories = attachmentList.stream().filter(e -> e.getType().equals(QcStandardImageTypeEnum.PACKAGING_ACCESSORIES.getCode())).collect(Collectors.toList());
//                    if(CollectionUtils.isNotEmpty(packagingAccessories)){
//                        QcStandardDTO.AttachDTO attachDTO = new QcStandardDTO.AttachDTO();
//                        attachDTO.setType(QcStandardImageTypeEnum.PACKAGING_ACCESSORIES.getCode());
//                        List<String> attachmentNameList = new ArrayList<>();
//                        List<String> attachmentUrlList = new ArrayList<>();
//
//                        for (WmsAttachmentEntity entity : packagingAccessories) {
//                            attachmentNameList.add(entity.getAttachName());
//                            attachmentUrlList.add(entity.getAttachUrl());
//                        }
//                        attachDTO.setAttachmentNameList(attachmentNameList);
//                        attachDTO.setAttachmentUrlList(attachmentUrlList);
//                        attachList.add(attachDTO);
//                    }
//                    addDTO.setAttachmentList(attachList);
//                }

                QcStandardDTO.UpdateDTO updateDTO = BeanMapperUtils.map(QcStandardDTO.UpdateDTO.class, addDTO);
                updateDTO.setId(id);

                QcStandardServiceImpl bean = ApplicationContextUtils.getBean(QcStandardServiceImpl.class);
                bean.update(updateDTO);
                return ;
            }
        }
        // 校验唯一性
        checkUnique(addDTO.getSkuId(), null);

        QcStandardEntity entity = BeanMapperUtils.map(QcStandardEntity.class, addDTO);
        entity.setId(IdWorker.getIdStr());
        findSkuNo(entity);

        // 保存主表
        this.save(entity);

        // 记录审计日志 (主表优先)
        String msg = StrUtil.format("用户【{}】新增质检标准，SKU编号【{}】", UserContext.getDefaultLoginUser().getUserName(),
                entity.getSkuNo());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_STANDARD.getCode(), entity.getId(), "新增操作");

        // 保存子表 明细
        List<QcStandardDetailEntity> detailList = BeanMapperUtils.copyList(QcStandardDetailEntity.class,
                addDTO.getDetailList());
        if (CollectionUtils.isNotEmpty(detailList)) {
            int sort = 0;
            for (QcStandardDetailEntity detail : detailList) {
                detail.setMainId(entity.getId());
                detail.setSort(sort++);
            }
            qcStandardDetailService.saveBatch(detailList);
        }

        // 保存附件 (标准化接收规则)
        List<WmsAttachmentEntity> wmsAttachmentEntities = addDTO.getWmsAttachmentEntities();
        if (addDTO.getIsImport() && CollectionUtils.isNotEmpty(wmsAttachmentEntities)) {
            for (WmsAttachmentEntity wmsAttachmentEntity : wmsAttachmentEntities) {
                wmsAttachmentEntity.setBusinessId(entity.getId());
                wmsAttachmentEntity.setId(IdWorker.getIdStr());
            }
            wmsAttachmentService.saveBatch(wmsAttachmentEntities);
        } else {
            addAttachments(addDTO.getAttachmentList(), entity.getId());
        }
    }

    private static void findSkuNo(QcStandardEntity entity) {
        List<ProductDetailEntity> skuList = FeignQuery.create(ProductDetailEntity.class)
                .eq(ProductDetailEntity::getId, entity.getSkuId()).list();
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.COMMON_NO_SKU);
        }
        String skuNo = skuList.get(0).getSkuNo();
        entity.setSkuNo(skuNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(keyName = "updateDTO.getSkuId()")
    public void update(QcStandardDTO.UpdateDTO updateDTO) {
        QcStandardEntity oldEntity = this.getById(updateDTO.getId());
        if (oldEntity == null) {
            throw new ServiceException(ApiError.QC_STANDARD_NOT_FOUND);
        }

        // 校验唯一性
        checkUnique(updateDTO.getSkuId(), updateDTO.getId());

        QcStandardEntity newEntity = BeanMapperUtils.map(QcStandardEntity.class, updateDTO);
        findSkuNo(newEntity);

        this.updateById(newEntity);

        // 记录审计日志 (主表优先)
        String msg = StrUtil.format("用户【{}】编辑质检标准，SKU编号【{}】", UserContext.getDefaultLoginUser().getUserName(),
                oldEntity.getSkuNo());
        operateLogService.addModuleOperateLogByObj(oldEntity, newEntity, ModuleTypeEnum.QC_STANDARD.getCode(),
                updateDTO.getId(), msg);

        // 使用差分更新明细
        List<QcStandardDetailEntity> dbDetailList = qcStandardDetailService
                .list(Wrappers.<QcStandardDetailEntity>lambdaQuery()
                        .eq(QcStandardDetailEntity::getMainId, updateDTO.getId()));
        List<QcStandardDetailEntity> newDetailList = BeanMapperUtils.copyList(QcStandardDetailEntity.class,
                updateDTO.getDetailList());
        if (CollectionUtils.isNotEmpty(newDetailList)) {
            int sort = 0;
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
            if (CollectionUtils.isNotEmpty(urlList) && CollectionUtils.isNotEmpty(nameList)
                    && urlList.size() == nameList.size()) {
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
        List<WmsAttachmentDTO.UpdateDTO> oldAttachments = wmsAttachmentService
                .getByBusinessIds(Collections.singletonList(businessId));

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
                    .filter(StringUtils::isNotBlank)
                    .filter(url -> !newUrlSet.contains(url))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deleteUrlList)) {
                List<String> ids = oldAttachments.stream().filter(e ->deleteUrlList.contains(e.getAttachUrl())).map(WmsAttachmentDTO.UpdateDTO::getId)
                        .collect(Collectors.toList());
                wmsAttachmentService.removeByIds(ids);
//                wmsAttachmentService.deleteByUrlList(deleteUrlList);
                // 记录审计日志
                List<Pair<String, String>> removePairList = oldAttachments.stream()
                        .filter(obj -> deleteUrlList.contains(obj.getAttachUrl()))
                        .map(obj -> new Pair<>(businessId, obj.getAttachName()))
                        .collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("编辑：删除附件【%s】", ModuleTypeEnum.QC_STANDARD.getCode(),
                        removePairList, "编辑操作");
            }
        }

        // 2. 处理新增：新的有，旧的没有
        Set<String> oldUrlSet = CollectionUtils.isEmpty(oldAttachments) ? Collections.emptySet()
                : oldAttachments.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toSet());

        List<WmsAttachmentEntity> addEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            for (QcStandardDTO.AttachDTO attachDTO : attachmentList) {
                List<String> urlList = attachDTO.getAttachmentUrlList();
                List<String> nameList = attachDTO.getAttachmentNameList();
                if (CollectionUtils.isNotEmpty(urlList) && CollectionUtils.isNotEmpty(nameList)
                        && urlList.size() == nameList.size()) {
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

            // 记录审计日志
            List<Pair<String, String>> addPairList = addEntities.stream()
                    .map(obj -> new Pair<>(businessId, obj.getAttachName()))
                    .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("编辑：新增附件【%s】", ModuleTypeEnum.QC_STANDARD.getCode(),
                    addPairList, "编辑操作");
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
            throw new ServiceException(ApiError.QC_STANDARD_SKU_EXISTS);
        }
    }

    @Override
    public List<QcStandardDTO.TabListDTO> tabList(PermissionsDTO permissionsDTO) {
        QcStandardDTO.PagingParamDTO searchParam = new QcStandardDTO.PagingParamDTO();
        searchParam.setPermissionSql(permissionsDTO.getPermissionSql());

        List<QcStandardDTO.TabListDTO> list = this.baseMapper.tabList(searchParam);
        List<QcStandardDTO.TabListDTO> result = new ArrayList<>();
        result.add(0, new QcStandardDTO.TabListDTO("all", "全部", 0));
        QcStandardDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals("enable")).findFirst()
                .orElse(null);
        if (Objects.isNull(enable)) {
            result.add(new QcStandardDTO.TabListDTO("enable", "启用", 0));
        } else {
            result.add(enable);
        }

        QcStandardDTO.TabListDTO disabled = list.stream().filter(e -> e.getTabFlag().equals("disabled")).findFirst()
                .orElse(null);
        if (Objects.isNull(disabled)) {
            result.add(new QcStandardDTO.TabListDTO("disabled", "禁用", 0));
        } else {
            result.add(disabled);
        }
        return result;
    }

    @Override
    public PagingVO<QcStandardDTO.ExportDTO> exportList(PagingDTO<QcStandardDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<QcStandardDTO.ExportDTO> pageData = this.baseMapper.exportList(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }

        List<String> ids = pageData.getRecords().stream().map(QcStandardDTO.ExportDTO::getId).distinct()
                .collect(Collectors.toList());

        // 载入附件图片 (标准化回显)
        Map<String, List<WmsAttachmentDTO.UpdateDTO>> grouped = new HashMap<>();
        List<WmsAttachmentDTO.UpdateDTO> allAttachments = wmsAttachmentService.getByBusinessIds(ids);
        if (CollectionUtils.isNotEmpty(allAttachments)) {
            grouped = allAttachments.stream()
                    .collect(Collectors.groupingBy(WmsAttachmentDTO.UpdateDTO::getBusinessId));
        }

        List<String> skus = pageData.getRecords().stream().map(QcStandardDTO.ListDTO::getSkuNo)
                .collect(Collectors.toList());
        Map<String, String> map = plmTaskFeign.listBySkuNos(skus).stream()
                .collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName, (o1, o2) -> o1));

        for (QcStandardDTO.ExportDTO listDTO : pageData.getRecords()) {
            listDTO.setDisabledName(listDTO.getDisabled() ? "禁用" : "启用");
            listDTO.setProductName(map.getOrDefault(listDTO.getSkuId(), ""));
            List<WmsAttachmentDTO.UpdateDTO> updateDTOS = grouped.get(listDTO.getId());
            if (CollectionUtils.isNotEmpty(updateDTOS)) {
                Map<String, List<WmsAttachmentDTO.UpdateDTO>> listMap = updateDTOS.stream()
                        .collect(Collectors.groupingBy(WmsAttachmentDTO.UpdateDTO::getType));

                List<WmsAttachmentDTO.UpdateDTO> productPhysicalList = listMap
                        .get(QcStandardImageTypeEnum.PRODUCT_PHYSICAL.getCode());
                if (CollectionUtils.isNotEmpty(productPhysicalList)) {
                    listDTO.setProductPhysicalUrl(productPhysicalList.stream()
                            .map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.joining("\n")));
                }
                List<WmsAttachmentDTO.UpdateDTO> packagingAccessoriesList = listMap
                        .get(QcStandardImageTypeEnum.PACKAGING_ACCESSORIES.getCode());
                if (CollectionUtils.isNotEmpty(packagingAccessoriesList)) {
                    listDTO.setPackagingAccessoriesUrl(packagingAccessoriesList.stream()
                            .map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.joining("\n")));
                }

            }
        }

        return new PagingVO(pageData);
    }

    @Override
    public PagingVO<QcStandardDTO.ListDTO> paging(PagingDTO<QcStandardDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<QcStandardDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<QcStandardDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> skus = list.stream().map(QcStandardDTO.ListDTO::getSkuNo).collect(Collectors.toList());
        Map<String, String> map = plmTaskFeign.listBySkuNos(skus).stream()
                .collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName, (o1, o2) -> o1));

        for (QcStandardDTO.ListDTO listDTO : list) {
            listDTO.setDisabledName(listDTO.getDisabled() ? "禁用" : "启用");
            listDTO.setProductName(map.getOrDefault(listDTO.getSkuId(), ""));
        }
    }

    @Override
    public QcStandardDTO.ViewDTO view(String id) {
        QcStandardEntity entity = this.getById(id);
        if (entity == null) {
            throw new ServiceException(ApiError.QC_STANDARD_NOT_FOUND);
        }
        return getFullViewDTO(entity);
    }

    @Override
    public QcStandardDTO.ViewDTO copyBySku(String skuNo) {
        QcStandardEntity entity = this.getOne(Wrappers.<QcStandardEntity>lambdaQuery()
                .eq(QcStandardEntity::getSkuNo, skuNo)
                .eq(QcStandardEntity::getIsDeleted, false));
        if (entity == null) {
            return null;
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

        List<ProductDetailEntity> skuList = FeignQuery.create(ProductDetailEntity.class)
                .eq(ProductDetailEntity::getId, entity.getSkuId()).list();
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.COMMON_NO_SKU);
        }
        viewDTO.setProductName(skuList.get(0).getName());
        // 载入详情
        List<QcStandardDetailEntity> details = qcStandardDetailService
                .list(Wrappers.<QcStandardDetailEntity>lambdaQuery()
                        .eq(QcStandardDetailEntity::getMainId, id)
                        .orderByAsc(QcStandardDetailEntity::getSort));
        if(CollUtil.isNotEmpty(details)){
            viewDTO.setDetailList(BeanMapperUtils.copyList(QcStandardDTO.DetailDTO.class, details));
        }

        // 载入附件图片 (标准化回显)
        List<WmsAttachmentDTO.UpdateDTO> allAttachments = wmsAttachmentService
                .getByBusinessIds(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(allAttachments)) {
            Map<String, List<WmsAttachmentDTO.UpdateDTO>> grouped = allAttachments.stream()
                    .collect(Collectors.groupingBy(WmsAttachmentDTO.UpdateDTO::getType));

            List<QcStandardDTO.AttachDTO> attachmentList = new ArrayList<>();
            grouped.forEach((type, attachments) -> {
                QcStandardDTO.AttachDTO attachDTO = new QcStandardDTO.AttachDTO();
                attachDTO.setType(type);
                attachDTO.setTypeName(QcStandardImageTypeEnum.getByCode(type));
                attachDTO.setAttachmentNameList(attachments.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName)
                        .collect(Collectors.toList()));
                attachDTO.setAttachmentUrlList(attachments.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl)
                        .collect(Collectors.toList()));
                attachmentList.add(attachDTO);
            });
            viewDTO.setAttachmentList(attachmentList);
        }
        return viewDTO;
    }

    @Override
    public void export(QcStandardDTO.PagingParamDTO params) {
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
        qcStandardDetailService.lambdaUpdate().set(QcStandardDetailEntity::getIsDeleted, true)
                .eq(QcStandardDetailEntity::getMainId, id);
        // 删除附件 (标准化逻辑)
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService
                .getByBusinessIds(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            List<String> ids = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getId)
                    .collect(Collectors.toList());
            wmsAttachmentService.removeByIds(ids);
//            wmsAttachmentService.deleteByUrlList(urlList);
        }

        // 记录审计日志
        String msg = StrUtil.format("用户【{}】删除质检标准，SKU编号【{}】", UserContext.getDefaultLoginUser().getUserName(),
                entity.getSkuNo());
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
        if(entity.getDisabled().equals(params.getDisabled())){
            return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), "状态更新成功");
        }

        QcStandardEntity updateEntity = new QcStandardEntity();
        updateEntity.setId(params.getId());
        updateEntity.setDisabled(params.getDisabled());
        this.updateById(updateEntity);

        // 记录审计日志
        String statusLabel = params.getDisabled() ? "禁用" : "启用";
        String msg = StrUtil.format("用户【{}】{}了SKU【{}】的质检标准", UserContext.getDefaultLoginUser().getUserName(),
                statusLabel, entity.getSkuNo());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_STANDARD.getCode(), entity.getId(),
                statusLabel + "操作");

        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), "状态更新成功");
    }

    private void updateDetails(String mainId, List<QcStandardDetailEntity> newList,
            List<QcStandardDetailEntity> oldList) {
        if (CollectionUtils.isEmpty(oldList)) {
            if (CollectionUtils.isNotEmpty(newList)) {
                qcStandardDetailService.saveBatch(newList);
            }
            return;
        }
        List<String> newIds = newList.stream().map(QcStandardDetailEntity::getId).filter(id -> id != null)
                .collect(Collectors.toList());
        List<String> deleteIds = oldList.stream().map(QcStandardDetailEntity::getId).filter(id -> !newIds.contains(id))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<QcStandardDetailEntity> removeList = oldList.stream().filter(e -> deleteIds.contains(e.getId()))
                    .collect(Collectors.toList());
            qcStandardDetailService.removeByIds(deleteIds);

            // 记录审计日志
            List<Pair<String, String>> removePairList = removeList.stream()
                    .map(obj -> new Pair<>(mainId, obj.getInspectItemName()))
                    .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("编辑：删除质检项【%s】", ModuleTypeEnum.QC_STANDARD.getCode(),
                    removePairList, "编辑操作");
        }
        List<QcStandardDetailEntity> addList = newList.stream().filter(e -> e.getId() == null)
                .collect(Collectors.toList());
        List<QcStandardDetailEntity> updateList = newList.stream().filter(e -> e.getId() != null)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            qcStandardDetailService.saveBatch(addList);

            // 记录审计日志
            List<Pair<String, String>> addPairList = addList.stream()
                    .map(obj -> new Pair<>(mainId, obj.getInspectItemName()))
                    .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("编辑：新增质检项【%s】", ModuleTypeEnum.QC_STANDARD.getCode(),
                    addPairList, "编辑操作");
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            qcStandardDetailService.updateBatchById(updateList);

            // 逐一记录审计日志 (差分)
            for (QcStandardDetailEntity detail : updateList) {
                QcStandardDetailEntity oldDetail = oldList.stream()
                        .filter(e -> Objects.equals(e.getId(), detail.getId())).findFirst().orElse(null);
                if (Objects.nonNull(oldDetail)) {
                    String msg = StrUtil.format("编辑质检项【{}】内容", oldDetail.getInspectItemName());
                    operateLogService.addModuleOperateLogByObj(oldDetail, detail, ModuleTypeEnum.QC_STANDARD.getCode(),
                            mainId, msg);
                }
            }
        }
    }

    @Override
    public List<String> listSkuNoByUrl(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "导入文件URL");
        }

        List<String> skus = new ArrayList<>();
        try (InputStream inputStream = FastDFSClientUtil.getInputStream(fileUrl)) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            //  提取 SKU  (前15行遍历查找关键字)
            String skuStr = "";
            for (int i = 2; i < 4; i++) { // 优化查找范围
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;
                for (int j = 0; j < 10; j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null)
                        continue;
                    String val = cell.toString().trim();
                    if (val.contains("产品SKU") || (val.equalsIgnoreCase("SKU") && val.length() == 3)) {
                        Cell valCell = row.getCell(j + 1);
                        if (valCell != null)
                            valCell.setCellType(CellType.STRING);
                            skuStr = valCell.getStringCellValue().trim();
                            break;
                    }
                }
                if (StringUtils.isNotBlank(skuStr))
                    break;
            }

            if (StringUtils.isBlank(skuStr)) {
                throw new ServiceException(ApiError.QC_STANDARD_IMPORT_SKU_NOT_FOUND);
            }

            String[] split = skuStr.split("[,，]");
            for (String s : split) {
                if (StringUtils.isNotBlank(s))
                    skus.add(s.trim());
            }
        } catch (Exception e) {
            log.error("质检报告解析SKU失败", e);
            throw new ServiceException("质检报告解析SKU失败：" + e.getMessage());
        }
        return skus;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO genQcStandardByUrl(List<String> skuNos, String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "导入文件URL");
        }

        if(CollUtil.isEmpty(skuNos)){
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "SKU");
        }
        //skuNos 转出一个String
        String skuNosStr = skuNos.stream().collect(Collectors.joining(","));
        try (InputStream inputStream = FastDFSClientUtil.getInputStream(fileUrl)) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // 1. 建立 DISPIMG ID 到 PictureData 的映射 (针对 WPS 嵌入图片)
            Map<String, PictureData> cellImageMap = new HashMap<>();
            if (workbook instanceof XSSFWorkbook) {
                try {
                    initCellImageMap((XSSFWorkbook) workbook, cellImageMap);
                } catch (Exception e) {
                    return BatchResultDTO.fail(skuNosStr, skuNosStr, "分析 cellimages.xml 失败或不包含嵌入图片");
                }
            }

            // 2. 提取 SKU 和产品名称 (前15行遍历查找关键字)
            String skuStr = "";
            for (int i = 2; i < 4; i++) { // 优化查找范围
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;
                for (int j = 0; j < 10; j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null)
                        continue;
                    String val = cell.toString().trim();
                    if (val.contains("产品SKU") || (val.equalsIgnoreCase("SKU") && val.length() == 3)) {
                        Cell valCell = row.getCell(j + 1);
                        if (valCell != null)
                            valCell.setCellType(CellType.STRING);
                            skuStr = valCell.getStringCellValue().trim();
                            break;
                    }
                }
                if (StringUtils.isNotBlank(skuStr))
                    break;
            }

            if (StringUtils.isBlank(skuStr)) {
                throw new ServiceException(ApiError.QC_STANDARD_IMPORT_SKU_NOT_FOUND);
            }

            // 3. 提取图片 (第8-11行)
            List<WmsAttachmentEntity> attachmentList = new ArrayList<>();
            extractImages(sheet, attachmentList, cellImageMap);

            // 4. 解析逻辑详情 (第15行开始)
            List<QcStandardDTO.DetailDTO> detailList = new ArrayList<>();
            for (int i = 14; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;
                Cell indexCell = row.getCell(0);
                if (indexCell == null)
                    continue;
                String indexVal = indexCell.toString().trim();

                // 正则匹配纯数字序号 (1, 2, 3...)
                if (indexVal.matches("^\\d+(\\.\\d+)?$")) {
                    Cell itemCell = row.getCell(1);
                    Cell reqCell = row.getCell(2);
                    String itemName = itemCell != null ? itemCell.toString().trim() : "";
                    String requirement = reqCell != null ? reqCell.toString().trim() : "";

                    if (StringUtils.isNotBlank(requirement)&& StringUtils.isNotBlank(itemName)) {
                        QcStandardDTO.DetailDTO detail = new QcStandardDTO.DetailDTO();
                        detail.setInspectItemName(itemName);
                        detail.setInspectRequirement(requirement);
                        detailList.add(detail);
                    }
                }
            }

            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException(ApiError.QC_STANDARD_IMPORT_DETAIL_NOT_FOUND);
            }

            // 5. 处理 SKU 拆分与覆盖保存
            String[] split = skuStr.split("[,，]");
            List<String> skus = new ArrayList<>();
            for (String s : split) {
                if (StringUtils.isNotBlank(s))
                    skus.add(s.trim());
            }

            Map<String, QcStandardEntity> existingMap = lambdaQuery().in(QcStandardEntity::getSkuNo, skus)
                    .eq(QcStandardEntity::getIsDeleted, false).list()
                    .stream()
                    .collect(Collectors.toMap(QcStandardEntity::getSkuId, Function.identity(), (o1, o2) -> o1));

            QcStandardServiceImpl bean = ApplicationContextUtils.getBean(QcStandardServiceImpl.class);
            List<ProductDetailEntity> skuVOList = plmTaskFeign.listBySkuNos(skus);

            for (ProductDetailEntity productDetailEntity : skuVOList) {
                //判断本次该SKU是否需要生成质检标准
                if(!skuNos.contains(productDetailEntity.getSkuNo())){
                    continue;
                }

                String skuId = productDetailEntity.getId();

                QcStandardDTO.AddDTO addDTO = new QcStandardDTO.AddDTO();
                addDTO.setSkuId(skuId);
                addDTO.setDetailList(detailList);
                if (CollectionUtils.isNotEmpty(attachmentList)) {
                    addDTO.setWmsAttachmentEntities(attachmentList);
                }
                addDTO.setIsImport(true);

                QcStandardEntity existing = existingMap.get(skuId);
                if (existing != null) {
                    // 1. 详情更新 (基于名称匹配)
                    updateDetailsForImport(existing.getId(), detailList);

                    // 2. 图片更新 (先删后增)
                    // 获取旧附件
                    List<WmsAttachmentDTO.UpdateDTO> oldAttachments = wmsAttachmentService
                            .getByBusinessIds(Collections.singletonList(existing.getId()));
                    if (CollectionUtils.isNotEmpty(oldAttachments)) {
                        List<String> ids = oldAttachments.stream().map(WmsAttachmentDTO.UpdateDTO::getId)
                                .collect(Collectors.toList());
                        wmsAttachmentService.removeByIds(ids);
//                        List<String> oldUrls = oldAttachments.stream()
//                                .map(WmsAttachmentDTO.UpdateDTO::getAttachUrl)
//                                .filter(StringUtils::isNotBlank)
//                                .collect(Collectors.toList());
//                        wmsAttachmentService.deleteByUrlList(oldUrls);
                    }
                    if (CollectionUtils.isNotEmpty(attachmentList)) {
                        // 插入新附件
                        for (WmsAttachmentEntity att : attachmentList) {
                            att.setBusinessId(existing.getId());
                            att.setId(IdWorker.getIdStr());
                        }
                        wmsAttachmentService.saveBatch(attachmentList);
                    }
                    // 3. 记录主表日志
                    bean.lambdaUpdate().set(QcStandardEntity::getUpdateTime, LocalDateTime.now()).eq(QcStandardEntity::getId, existing.getId()).update();
                    String msg = StrUtil.format("用户【{}】通过导入更新了质检标准，SKU编号【{}】",
                            UserContext.getDefaultLoginUser().getUserName(), existing.getSkuNo());
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.QC_STANDARD.getCode(), existing.getId(), "导入更新");
                } else {
                    bean.add(addDTO);
                }
            }

        } catch (Exception e) {
            log.error("质检报告导入失败", e);
            return BatchResultDTO.fail(skuNosStr, skuNosStr, "解析报告失败：" + e.getMessage());
        }

        return BatchResultDTO.success(skuNosStr, skuNosStr, "质检标准新增或更新成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public QcStandardDTO.AddDTO getQcStandardAddDTOByUrl(String skuNo, String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "导入文件URL");
        }

        if(StringUtils.isBlank(skuNo)){
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "SKU");
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
                    throw new ServiceException("分析 cellimages.xml 失败或不包含嵌入图片: {}", e.getMessage());
                }
            }

            // 2. 提取 SKU 和产品名称 (前15行遍历查找关键字)
            String skuStr = "";
            for (int i = 2; i < 4; i++) { // 优化查找范围
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;
                for (int j = 0; j < 10; j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null)
                        continue;
                    String val = cell.toString().trim();
                    if (val.contains("产品SKU") || (val.equalsIgnoreCase("SKU") && val.length() == 3)) {
                        Cell valCell = row.getCell(j + 1);
                        if (valCell != null)
                            valCell.setCellType(CellType.STRING);
                            skuStr = valCell.getStringCellValue().trim();
                            break;
                    }
                }
                if (StringUtils.isNotBlank(skuStr))
                    break;
            }

            if (StringUtils.isBlank(skuStr)) {
                throw new ServiceException(ApiError.QC_STANDARD_IMPORT_SKU_NOT_FOUND);
            }

            // 3. 提取图片 (第8-11行)
            List<WmsAttachmentEntity> attachmentList = new ArrayList<>();
            extractImages(sheet, attachmentList, cellImageMap);

            // 4. 解析逻辑详情 (第15行开始)
            List<QcStandardDTO.DetailDTO> detailList = new ArrayList<>();
            for (int i = 14; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;
                Cell indexCell = row.getCell(0);
                if (indexCell == null)
                    continue;
                String indexVal = indexCell.toString().trim();

                // 正则匹配纯数字序号 (1, 2, 3...)
                if (indexVal.matches("^\\d+(\\.\\d+)?$")) {
                    Cell itemCell = row.getCell(1);
                    Cell reqCell = row.getCell(2);
                    String itemName = itemCell != null ? itemCell.toString().trim() : "";
                    String requirement = reqCell != null ? reqCell.toString().trim() : "";

                    if (StringUtils.isNotBlank(requirement)&& StringUtils.isNotBlank(itemName)) {
                        QcStandardDTO.DetailDTO detail = new QcStandardDTO.DetailDTO();
                        detail.setInspectItemName(itemName);
                        detail.setInspectRequirement(requirement);
                        detailList.add(detail);
                    }
                }
            }

            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException(ApiError.QC_STANDARD_IMPORT_DETAIL_NOT_FOUND);
            }

            // 5. 处理 SKU 拆分与覆盖保存
            String[] split = skuStr.split("[,，]");
            List<String> skus = new ArrayList<>();
            for (String s : split) {
                if (StringUtils.isNotBlank(s) && s.equals(skuNo))
                    skus.add(s.trim());
            }

            if(CollUtil.isEmpty(skus)){
                throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "SKU");
            }

            Map<String, QcStandardEntity> existingMap = lambdaQuery().in(QcStandardEntity::getSkuId, skus)
                    .eq(QcStandardEntity::getIsDeleted, false).list()
                    .stream()
                    .collect(Collectors.toMap(QcStandardEntity::getSkuId, Function.identity(), (o1, o2) -> o1));

            List<ProductDetailEntity> skuVOList = plmTaskFeign.listBySkuNos(skus);

            for (ProductDetailEntity productDetailEntity : skuVOList) {
                String skuId = productDetailEntity.getId();

                QcStandardDTO.AddDTO addDTO = new QcStandardDTO.AddDTO();
                addDTO.setSkuId(skuId);
                addDTO.setSkuNo(productDetailEntity.getSkuNo());
                addDTO.setProductName(productDetailEntity.getName());
                addDTO.setDetailList(detailList);
                if (CollectionUtils.isNotEmpty(attachmentList)) {
                    addDTO.setWmsAttachmentEntities(attachmentList);

                    List<QcStandardDTO.AttachDTO> attachList = new ArrayList<>();
                    List<WmsAttachmentEntity> productPhysical = attachmentList.stream().filter(e -> e.getType().equals(QcStandardImageTypeEnum.PRODUCT_PHYSICAL.getCode())).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(productPhysical)){
                        QcStandardDTO.AttachDTO attachDTO = new QcStandardDTO.AttachDTO();
                        attachDTO.setType(QcStandardImageTypeEnum.PRODUCT_PHYSICAL.getCode());
                        List<String> attachmentNameList = new ArrayList<>();
                        List<String> attachmentUrlList = new ArrayList<>();

                        for (WmsAttachmentEntity entity : productPhysical) {
                            attachmentNameList.add(entity.getAttachName());
                            attachmentUrlList.add(entity.getAttachUrl());
                        }
                        attachDTO.setAttachmentNameList(attachmentNameList);
                        attachDTO.setAttachmentUrlList(attachmentUrlList);
                        attachList.add(attachDTO);
                    }
                    List<WmsAttachmentEntity> packagingAccessories = attachmentList.stream().filter(e -> e.getType().equals(QcStandardImageTypeEnum.PACKAGING_ACCESSORIES.getCode())).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(packagingAccessories)){
                        QcStandardDTO.AttachDTO attachDTO = new QcStandardDTO.AttachDTO();
                        attachDTO.setType(QcStandardImageTypeEnum.PACKAGING_ACCESSORIES.getCode());
                        List<String> attachmentNameList = new ArrayList<>();
                        List<String> attachmentUrlList = new ArrayList<>();

                        for (WmsAttachmentEntity entity : packagingAccessories) {
                            attachmentNameList.add(entity.getAttachName());
                            attachmentUrlList.add(entity.getAttachUrl());
                        }
                        attachDTO.setAttachmentNameList(attachmentNameList);
                        attachDTO.setAttachmentUrlList(attachmentUrlList);
                        attachList.add(attachDTO);
                    }
                    addDTO.setAttachmentList(attachList);
                }
                addDTO.setIsImport(true);
                return addDTO;
            }

        } catch (Exception e) {
            log.error("质检报告导入失败", e);
            throw new ServiceException("解析报告失败：" + e.getMessage());
        }

        return null;
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
            if (row == null)
                continue;
            for (int c = 1; c <= 13; c++) { // A列在0，图片通常在B列(1)起
                Cell cell = row.getCell(c);
                if (cell == null)
                    continue;
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
                type = QcStandardImageTypeEnum.PRODUCT_PHYSICAL.getCode();
            } else if (colIdx >= 9 && colIdx <= 15) { // 放宽到15列
                type = QcStandardImageTypeEnum.PACKAGING_ACCESSORIES.getCode();
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

    /**
     * 导入时的详情差异化更新逻辑 (基于质检项目名称匹配)
     *
     * @param mainId          主表ID
     * @param importedDetails 导入的明细列表
     */
    private void updateDetailsForImport(String mainId, List<QcStandardDTO.DetailDTO> importedDetails) {
        // 获取数据库中现有的明细
        List<QcStandardDetailEntity> dbDetails = qcStandardDetailService.list(Wrappers.<QcStandardDetailEntity>lambdaQuery()
                .eq(QcStandardDetailEntity::getMainId, mainId));

        Map<String, QcStandardDetailEntity> dbDetailMap = dbDetails.stream()
                .collect(Collectors.toMap(QcStandardDetailEntity::getInspectItemName, Function.identity(), (o1, o2) -> o1));

        Set<String> importedNames = importedDetails.stream()
                .map(QcStandardDTO.DetailDTO::getInspectItemName).collect(Collectors.toSet());

        // 1. 处理删除：DB 中有，导入中没有
        List<QcStandardDetailEntity> toDelete = dbDetails.stream()
                .filter(d -> !importedNames.contains(d.getInspectItemName()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(toDelete)) {
            List<String> deleteIds = toDelete.stream().map(QcStandardDetailEntity::getId).collect(Collectors.toList());
            qcStandardDetailService.removeByIds(deleteIds);

            // 记录审计日志
            List<Pair<String, String>> removePairList = toDelete.stream()
                    .map(obj -> new Pair<>(mainId, obj.getInspectItemName()))
                    .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("导入：删除质检项【%s】", ModuleTypeEnum.QC_STANDARD.getCode(),
                    removePairList, "导入更新");
        }

        // 2. 处理新增与更新
        int sort = 0;
        for (QcStandardDTO.DetailDTO imported : importedDetails) {
            QcStandardDetailEntity existingDetail = dbDetailMap.get(imported.getInspectItemName());
            if (existingDetail == null) {
                // 新增
                QcStandardDetailEntity newDetail = BeanMapperUtils.map(QcStandardDetailEntity.class, imported);
                newDetail.setMainId(mainId);
                newDetail.setSort(sort++);
                qcStandardDetailService.save(newDetail);

                // 记录日志
                operateLogService.addModuleOperateLog(StrUtil.format("导入：新增质检项【{}】", imported.getInspectItemName()),
                        ModuleTypeEnum.QC_STANDARD.getCode(), mainId, "导入更新");
            } else {
                // 更新
                QcStandardDetailEntity updateDetail = BeanMapperUtils.map(QcStandardDetailEntity.class, imported);
                updateDetail.setId(existingDetail.getId());
                updateDetail.setSort(sort++);
                qcStandardDetailService.updateById(updateDetail);

                // 记录日志 (自动对比差异)
                String msg = StrUtil.format("导入：更新质检项【{}】内容", existingDetail.getInspectItemName());
                operateLogService.addModuleOperateLogByObj(existingDetail, updateDetail, ModuleTypeEnum.QC_STANDARD.getCode(),
                        mainId, msg);
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
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 解析 WPS cellimages.xml 建立 ID 到数据的映射
     */
    private void initCellImageMap(XSSFWorkbook workbook, Map<String, PictureData> map) throws Exception {
        PackagePart cellImagesPart = null;
        for (PackagePart part : workbook.getPackage().getParts()) {
            String name = part.getPartName().getName();
            // 注意: 必须用 endsWith 精准匹配，避免误匹配 cellimages.xml.rels
            if (name.endsWith("cellimages.xml")) {
                cellImagesPart = part;
                break;
            }
        }
        if (cellImagesPart == null)
            return;

        // 关闭命名空间感知，才能用 "etc:cellImage" 等前缀形式的标签名匹配
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(cellImagesPart.getInputStream());

        // WPS 的节点名称: <etc:cellImage> → <xdr:cNvPr name="ID_..."/> & <a:blip
        // r:embed="rId1"/>
        NodeList imageNodes = doc.getElementsByTagName("etc:cellImage");
        // log.info("[DISPIMG] cellimages.xml 中发现 {} 个图片节点", imageNodes.getLength());

        for (int i = 0; i < imageNodes.getLength(); i++) {
            Element imgElem = (Element) imageNodes.item(i);

            // 提取图片 ID: <xdr:cNvPr name="ID_..."/>
            String name = "";
            NodeList cNvPrList = imgElem.getElementsByTagName("xdr:cNvPr");
            if (cNvPrList.getLength() > 0) {
                name = ((Element) cNvPrList.item(0)).getAttribute("name");
            }

            // 提取关系 ID: <a:blip r:embed="rId1"/>
            String rId = "";
            NodeList blipList = imgElem.getElementsByTagName("a:blip");
            if (blipList.getLength() > 0) {
                rId = ((Element) blipList.item(0)).getAttribute("r:embed");
            }

            // log.info("[DISPIMG] 解析节点 [{}]: name={}, rId={}", i, name, rId);

            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(rId)) {
                try {
                    PackagePart mediaPart = cellImagesPart.getRelatedPart(cellImagesPart.getRelationship(rId));
                    if (mediaPart != null) {
                        for (PictureData pd : workbook.getAllPictures()) {
                            if (pd instanceof XSSFPictureData) {
                                XSSFPictureData xpd = (XSSFPictureData) pd;
                                if (xpd.getPackagePart().getPartName().equals(mediaPart.getPartName())) {
                                    map.put(name, pd);
                                    // log.info("[DISPIMG] 成功映射: {} -> {}", name, mediaPart.getPartName());
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // log.warn("[DISPIMG] 映射图片 rId={} 失败: {}", rId, e.getMessage());
                }
            }
        }
    }
}
