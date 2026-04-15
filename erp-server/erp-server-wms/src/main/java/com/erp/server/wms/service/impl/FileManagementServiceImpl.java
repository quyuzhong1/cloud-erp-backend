package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FileManagementDTO;
import com.erp.model.wms.dto.QcStandardDTO;
import com.erp.model.wms.entity.FileManagementEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.WmsFileTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.FileManagementConverter;
import com.erp.server.wms.convert.WmsAttachmentConverter;
import com.erp.server.wms.mapper.FileManagementMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 文件管理 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Slf4j
@Service
public class FileManagementServiceImpl extends SuperServiceImpl<FileManagementMapper, FileManagementEntity> implements FileManagementService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WmsAttachmentService wmsAttachmentService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private QcStandardService qcStandardService;
    @Resource
    private FileManagementMapper fileManagementMapper;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> add(FileManagementDTO.AddDTO addDTO) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<SkuVO> skuVOS = null;
        if (WmsFileTypeEnum.REVIEW_REPORT.getCode().equals(addDTO.getFileType())) {
            skuVOS = getSkuVOS(addDTO.getAttachUrl());
        }
        FileManagementEntity fileManagementEntity = FileManagementConverter.INSTANCE.addDTOToEntity(addDTO);
        WmsAttachmentEntity attachmentEntity = WmsAttachmentConverter.INSTANCE.addFileManagementToAttachment(addDTO);
        // 数据处理 应对多个sku情况，目前只有评审报告会有多个sku
        List<FileManagementEntity> entityList = handleData(fileManagementEntity, skuVOS);
        entityList.forEach(entity -> {
            if (CharSequenceUtil.isBlank(entity.getId())){
                resultDTOS.add(addEntity(entity, attachmentEntity));
            }else {
                resultDTOS.add(updateEntity(entity, attachmentEntity));
            }
        });
        return resultDTOS;
    }

    @NotNull
    private List<SkuVO> getSkuVOS(String attachUrl) {
        List<SkuVO> skuVOS;
        List<String> skuNoList = qcStandardService.listSkuNoByUrl(attachUrl);
        if (CollUtil.isEmpty(skuNoList)) {
            throw new ServiceException("评审报告未解析到SKU");
        }
        //判断是否存在相同sku
        if (skuNoList.size() != skuNoList.stream().distinct().count()) {
            throw new ServiceException("评审报告解析到的SKU存在重复");
        }
        skuVOS = plmTaskFeign.listAllStatusSkuBySkuNos(skuNoList);
        if (CollUtil.isEmpty(skuVOS)) {
            throw new ServiceException("SKU未查询到记录");
        }
        // 判断是否已存在相同SKU和文件类型的记录
        if (skuNoList.size() != skuVOS.size()) {
            List<String> collect = skuVOS.stream().map(SkuVO::getSkuNo).distinct().collect(Collectors.toList());
            String skuNos = skuNoList.stream().filter(e -> !collect.contains(e)).collect(Collectors.joining(","));
            if (CharSequenceUtil.isNotBlank(skuNos)) {
                throw new ServiceException("以下SKU未查询到记录：" + skuNos);
            }
        }
        return skuVOS;
    }

    @NotNull
    private BatchResultDTO addEntity(FileManagementEntity fileManagementEntity, WmsAttachmentEntity attachmentEntity) {
        log.info("开始新增文件管理");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_WDGL);
        fileManagementEntity.setCode(code);
        boolean save = super.save(fileManagementEntity);
        if (!save) {
            throw new ServiceException("文件管理保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "文件管理", fileManagementEntity.getCode());
        //此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FILE_MANAGEMENT.getCode(), fileManagementEntity.getId(), "新增操作");
        //新增明细（如果有明细的话）
        attachmentEntity.setBusinessId(fileManagementEntity.getId());
        String fileId = wmsAttachmentService.saveByVersion(attachmentEntity);
        // 新增成功后，返回新增的主键值
        this.lambdaUpdate().set(FileManagementEntity::getFileId, fileId).eq(FileManagementEntity::getId, fileManagementEntity.getId()).update();
        return BatchResultDTO.success(fileManagementEntity.getId(), code, "新增单据");
    }
    @NotNull
    private BatchResultDTO updateEntity(FileManagementEntity fileManagementEntity, WmsAttachmentEntity attachmentEntity) {
        FileManagementEntity old = super.getById(fileManagementEntity.getId());
        attachmentEntity.setBusinessId(old.getId());
        String fileId = wmsAttachmentService.saveByVersion(attachmentEntity);
        log.info("开始更新文件管理");
        fileManagementEntity.setFileId(fileId);
        boolean update = super.updateById(fileManagementEntity);
        if (!update) {
            throw new ServiceException("文件管理更新失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录文件管理日志数据，单号：【{}】", fileManagementEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), fileManagementEntity.getCode(), "文件管理");
        //此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, fileManagementEntity, ModuleTypeEnum.FILE_MANAGEMENT.getCode(), fileManagementEntity.getId(), msg);
        return BatchResultDTO.success(fileManagementEntity.getId(), fileManagementEntity.getCode(), "更新单据");
    }
    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> update(FileManagementDTO.UpdateDTO addOrUpdateDTO) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        FileManagementEntity fileManagementEntity = BeanMapperUtils.map(FileManagementEntity.class, addOrUpdateDTO);
        WmsAttachmentEntity attachmentEntity = WmsAttachmentConverter.INSTANCE.updateFileManagementToAttachment(addOrUpdateDTO);
        List<SkuVO> skuVOS = null;
        if (WmsFileTypeEnum.REVIEW_REPORT.getCode().equals(addOrUpdateDTO.getFileType())) {
            skuVOS = getSkuVOS(addOrUpdateDTO.getAttachUrl());
        }
        // 数据处理
        List<FileManagementEntity> entityList = handleData(fileManagementEntity, skuVOS);
        entityList.forEach(entity -> {
            if (CharSequenceUtil.isBlank(entity.getId())){
                resultDTOS.add(addEntity(entity, attachmentEntity));
            }else {
                resultDTOS.add(updateEntity(entity, attachmentEntity));
            }
        });
        return resultDTOS;
    }


    @Override
    public PagingVO<FileManagementDTO.ListDTO> paging(PagingDTO<FileManagementDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FileManagementDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 新增修改处理数据
     */
    private List<FileManagementEntity> handleData(FileManagementEntity entity, List<SkuVO> skuVOList) {
        String fileType = entity.getFileType();
        List<FileManagementEntity> entityList = new ArrayList<>();
        if (WmsFileTypeEnum.REVIEW_REPORT.getCode().equals(fileType)) {
            List<String> skuIds = skuVOList.stream().map(SkuVO::getSkuId).collect(Collectors.toList());
            List<FileManagementDTO.CountDTO> countDTOS = baseMapper.countBySkuAndFileType(skuIds, fileType, null);
            skuVOList.forEach(skuVO -> {
                entity.setSkuId(skuVO.getSkuId());
                entity.setSkuNo(skuVO.getSkuNo());
                entity.setProductName(skuVO.getSkuName());
                FileManagementDTO.CountDTO countDTO1 = countDTOS.stream().filter(countDTO -> Objects.equals(countDTO.getSkuId(), skuVO.getSkuId())).findFirst().orElse(null);
                entity.setId(countDTO1 != null ? countDTO1.getId() : null);
                entity.setCode(countDTO1 != null ? countDTO1.getCode() : null);
                entityList.add(entity);
            });
        } else if (WmsFileTypeEnum.MANUFACTURING_REPORT.getCode().equals(fileType)) {
            // 量产报告
            if (CharSequenceUtil.isBlank(entity.getSkuId())) {
                throw new ServiceException("请输入SKU");
            }
            List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(Collections.singletonList(entity.getSkuId()));
            if (CollUtil.isEmpty(skuVOS)) {
                throw new ServiceException("SKU不存在");
            }
            entity.setSkuNo(skuVOS.get(0).getSkuNo());
            entity.setProductName(skuVOS.get(0).getSkuName());
            List<FileManagementDTO.CountDTO> countDTOS = baseMapper.countBySkuAndFileType(Collections.singletonList(entity.getSkuId()), fileType, null);
            entity.setId(CollUtil.isNotEmpty(countDTOS) ? countDTOS.get(0).getId() : null);
            entity.setCode(CollUtil.isNotEmpty(countDTOS) ? countDTOS.get(0).getCode() : null);
            entityList.add(entity);
        } else if (WmsFileTypeEnum.CATEGORY_GENERAL_STANDARD.getCode().equals(fileType)) {
            // 品类通用标准
            if (CharSequenceUtil.isBlank(entity.getFirstCategoryId())) {
                throw new ServiceException("请输入品类");
            }
            List<BasicCategoryEntity> categoryEntityList = plmTaskFeign.listCategoryByIds(Collections.singletonList(entity.getFirstCategoryId()));
            if (CollUtil.isEmpty(categoryEntityList)) {
                throw new ServiceException("品类不存在");
            }
            entity.setFirstCategoryName(categoryEntityList.get(0).getName());
            List<FileManagementDTO.CountDTO> countDTOS = baseMapper.countBySkuAndFileType(null, fileType, entity.getFirstCategoryId());
            entity.setId(CollUtil.isNotEmpty(countDTOS) ? countDTOS.get(0).getId() : null);
            entity.setCode(CollUtil.isNotEmpty(countDTOS) ? countDTOS.get(0).getCode() : null);
            entityList.add(entity);
        }
        return entityList;
    }

    @Override
    public FileManagementDTO.ViewDTO view(String id) {
        FileManagementEntity fileManagementEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到文件管理数据"));
        WmsAttachmentEntity attachmentEntity = wmsAttachmentService.getByIdOpt(fileManagementEntity.getFileId()).orElseThrow(() -> new ServiceException("未找到文件附件数据"));
        return FileManagementConverter.INSTANCE.fileManagementToViewDTO(fileManagementEntity, attachmentEntity);
    }

    @Override
    public List<FileManagementDTO.VersionDTO> history(String id) {
        FileManagementEntity fileManagementEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到文件管理数据"));
        List<WmsAttachmentEntity> attachmentEntityList = wmsAttachmentService.getByBusinessId(id, fileManagementEntity.getFileType());
        return WmsAttachmentConverter.INSTANCE.entityToVersionDTO(attachmentEntityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> genQcStandard(List<String> skuNoList, String attachUrl) {
        BatchResultDTO resultDTO = qcStandardService.genQcStandardByUrl(skuNoList, attachUrl);
        return Collections.singletonList(resultDTO);
    }

    @Override
    public QcStandardDTO.AddDTO genSingleQcStandard(String id) {
        FileManagementEntity fileManagementEntity = this.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到文件管理数据"));
        //只有评审报告类型的文件允许生成质检标准
        if (!WmsFileTypeEnum.REVIEW_REPORT.getCode().equals(fileManagementEntity.getFileType())) {
            throw new ServiceException("只有评审报告类型的文件允许生成质检标准");
        }
        WmsAttachmentEntity attachmentEntity = wmsAttachmentService.getByIdOpt(fileManagementEntity.getFileId()).orElseThrow(() -> new ServiceException("未找到文件附件数据"));
        return qcStandardService.getQcStandardAddDTOByUrl(fileManagementEntity.getSkuNo(), attachmentEntity.getAttachUrl());
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<FileManagementDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for (FileManagementDTO.ListDTO data : list) {
            data.setFileTypeName(WmsFileTypeEnum.getName(data.getFileType()));
        }
    }


    @Override
    public FileManagementDTO.AttachDTO getCategoryGeneralStandardFile(String skuId) {
        return fileManagementMapper.getCategoryGeneralStandardFileUrl(skuId);
    }
}
