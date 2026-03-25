package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FileManagementDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.FileManagementEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.WmsFileTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.FileManagementConverter;
import com.erp.server.wms.convert.WmsAttachmentConverter;
import com.erp.server.wms.mapper.FileManagementMapper;
import com.erp.server.wms.service.FileManagementService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcStandardService;
import com.erp.server.wms.service.WmsAttachmentService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.bcel.generic.IF_ACMPEQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FileManagementDTO.AddDTO addDTO) {
        FileManagementEntity fileManagementEntity = new FileManagementEntity();
        BeanMapperUtils.copy(addDTO, fileManagementEntity);
        WmsAttachmentEntity attachmentEntity = WmsAttachmentConverter.INSTANCE.addFileManagementToAttachment(addDTO);
        // 数据处理
        handleData(fileManagementEntity);

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
        return new BaseResultDTO.AddDTO(fileManagementEntity.getId(), code);
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FileManagementDTO.UpdateDTO addOrUpdateDTO) {
        FileManagementEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "文件管理"));
        FileManagementEntity fileManagementEntity = BeanMapperUtils.map(FileManagementEntity.class, addOrUpdateDTO);
        WmsAttachmentEntity attachmentEntity = WmsAttachmentConverter.INSTANCE.updateFileManagementToAttachment(addOrUpdateDTO);
        fileManagementEntity.setFileId(wmsAttachmentService.saveByVersion(attachmentEntity));
        // 数据处理
        handleData(fileManagementEntity);
        log.info("编辑 开始修改文件管理数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(fileManagementEntity);
        if (!save) {
            throw new ServiceException("文件管理保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录文件管理日志数据，单号：【{}】", fileManagementEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), fileManagementEntity.getCode(), "文件管理");
        //此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, fileManagementEntity, ModuleTypeEnum.FILE_MANAGEMENT.getCode(), fileManagementEntity.getId(), msg);
        return Boolean.TRUE;
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
    private void handleData(FileManagementEntity entity) {
        String fileType = entity.getFileType();
        if (WmsFileTypeEnum.REVIEW_REPORT.getCode().equals(fileType) || WmsFileTypeEnum.MANUFACTURING_REPORT.getCode().equals(fileType)) {
            // 评审报告 量产报告
            if (CharSequenceUtil.isBlank(entity.getSkuId())){
                throw new ServiceException("请输入SKU");
            }
            List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(Collections.singletonList(entity.getSkuId()));
            if (CollUtil.isEmpty(skuVOS)){
                throw new ServiceException("SKU不存在");
            }
            entity.setSkuNo(skuVOS.get(0).getSkuNo());
            entity.setProductName(skuVOS.get(0).getSkuName());
            Integer count = this.lambdaQuery().eq(FileManagementEntity::getSkuId, entity.getSkuId()).eq(FileManagementEntity::getFileType, fileType)
                    .ne(CharSequenceUtil.isNotBlank(entity.getId()), FileManagementEntity::getId, entity.getId()).count();
            if (count > 0){
                throw new ServiceException(ApiError.FILE_MANAGEMENT_SKU_TYPE_EXIST, skuVOS.get(0).getSkuName(), fileType);
            }
        } else if (WmsFileTypeEnum.CATEGORY_GENERAL_STANDARD.getCode().equals(fileType)) {
            // 品类通用标准
            if (CharSequenceUtil.isBlank(entity.getFirstCategoryId())){
                throw new ServiceException("请输入品类");
            }
            List<BasicCategoryEntity> categoryEntityList = plmTaskFeign.listCategoryByIds(Collections.singletonList(entity.getFirstCategoryId()));
            if (CollUtil.isEmpty(categoryEntityList)){
                throw new ServiceException("品类不存在");
            }
            entity.setFirstCategoryName(categoryEntityList.get(0).getName());
            Integer count = this.lambdaQuery().eq(FileManagementEntity::getFirstCategoryId, entity.getFirstCategoryId()).eq(FileManagementEntity::getFileType, fileType)
                    .ne(CharSequenceUtil.isNotBlank(entity.getId()), FileManagementEntity::getId, entity.getId()).count();
            if (count > 0){
                throw new ServiceException(ApiError.FILE_MANAGEMENT_CATEGORY_TYPE_EXIST, categoryEntityList.get(0).getName(), fileType);
            }
        }
    }

    @Override
    public FileManagementDTO.ViewDTO view(String id) {
        FileManagementEntity fileManagementEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到文件管理数据"));
        WmsAttachmentEntity attachmentEntity = wmsAttachmentService.getByIdOpt(fileManagementEntity.getFileId()).orElseThrow(() -> new ServiceException("未找到文件附件数据"));
        return FileManagementConverter.INSTANCE.fileManagementToViewDTO(fileManagementEntity,attachmentEntity);
    }

    @Override
    public List<FileManagementDTO.VersionDTO> history(String id) {
        FileManagementEntity fileManagementEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到文件管理数据"));
        List<WmsAttachmentEntity> attachmentEntityList = wmsAttachmentService.getByBusinessId(id, fileManagementEntity.getFileType());
        return WmsAttachmentConverter.INSTANCE.entityToVersionDTO(attachmentEntityList);
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
}
