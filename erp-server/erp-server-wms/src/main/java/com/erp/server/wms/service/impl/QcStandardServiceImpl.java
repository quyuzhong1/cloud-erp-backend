package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.QcStandardDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.QcStandardDetailEntity;
import com.erp.model.wms.entity.QcStandardEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.QcStandardMapper;
import com.erp.server.wms.service.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.common.business.threadlocal.UserContext;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
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

    @Autowired
    private DictBasicService dictBasicService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(keyName = "#addDTO.skuId")
    public void add(QcStandardDTO.AddDTO addDTO) {
        // 校验唯一性
        checkUnique(addDTO.getSkuId(), null);

        QcStandardEntity entity = BeanMapperUtils.map(QcStandardEntity.class, addDTO);

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
        addAttachments(addDTO.getAttachmentList(), entity.getId());
    }

    private static void findSkuNo(QcStandardEntity entity) {
        List<ProductDetailEntity> skuList = FeignQuery.create(ProductDetailEntity.class).eq(ProductDetailEntity::getId, entity.getSkuId()).list();
        if(CollectionUtils.isNotEmpty(skuList)){
            throw new ServiceException(ApiError.COMMON_NO_SKU);
        }
        String skuNo = skuList.get(0).getSkuNo();
        entity.setSkuNo(skuNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(keyName = "#updateDTO.skuId")
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
        return list;
    }

    @Override
    public PagingVO<QcStandardDTO.ListDTO> paging(PagingDTO<QcStandardDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<QcStandardDTO.ListDTO> pageData = this.baseMapper.paging(query,pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
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

            Map<String, String> qcStandardImageTypeMap = dictBasicService.getByKey("qcStandardImageType").stream().collect(Collectors.toMap(DictBasicDTO.ListDTO::getValue, DictBasicDTO.ListDTO::getName, (o1, o2) -> o1));


            List<QcStandardDTO.AttachDTO> attachmentList = new ArrayList<>();
            grouped.forEach((type, attachments) -> {
                QcStandardDTO.AttachDTO attachDTO = new QcStandardDTO.AttachDTO();
                attachDTO.setType(type);
                attachDTO.setTypeName(qcStandardImageTypeMap.get(type));
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
    @DistributeLocker(keyName = "#id")
    public BatchResultDTO delete(String id) {
        QcStandardEntity entity = this.getById(id);
        if (entity == null) {
            return BatchResultDTO.fail(id, id, "数据不存在");
        }
        this.removeById(id);
        qcStandardDetailService.remove(Wrappers.<QcStandardDetailEntity>lambdaQuery().eq(QcStandardDetailEntity::getMainId, id));
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
    @DistributeLocker(keyName = "#params.id")
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
}
