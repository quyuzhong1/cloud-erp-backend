package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.ClientTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.FindUserDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import com.erp.model.wms.dto.SampleTransferDetailDTO;
import com.erp.model.wms.entity.SampleTransferDetailEntity;
import com.erp.model.wms.entity.SampleTransferInfoEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.SampleLedgerTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.SampleTransferInfoMapper;
import com.erp.server.wms.service.SampleLedgerFlowService;
import com.erp.server.wms.service.SampleLedgerService;
import com.erp.server.wms.service.SampleTransferDetailService;
import com.erp.server.wms.service.SampleTransferInfoService;
import com.erp.server.wms.service.SampleLedgerFlowBuilder;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import com.erp.server.wms.service.WmsAttachmentService;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleTransferInfoDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import static com.common.business.enums.FileTaskEventEnum.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import java.util.function.Function;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 样品转移单主表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-10-28
 */
@Slf4j
@Service
public class SampleTransferInfoServiceImpl extends SuperServiceImpl<SampleTransferInfoMapper, SampleTransferInfoEntity> implements SampleTransferInfoService, SampleLedgerFlowBuilder {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Resource
    private SampleTransferDetailService sampleTransferDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private WmsAttachmentService wmsAttachmentService;
    @Resource
    private SampleLedgerService sampleLedgerService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private SampleLedgerFlowService sampleLedgerFlowService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleTransferInfoDTO.AddDTO addDTO) {
        SampleTransferInfoEntity sampleTransferInfoEntity = new SampleTransferInfoEntity();
        BeanMapperUtils.copy(addDTO, sampleTransferInfoEntity);

        // 数据处理
        handleData(sampleTransferInfoEntity);

        log.info("开始新增样品转移单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YPZY);
        sampleTransferInfoEntity.setCode(code);
        boolean save = super.save(sampleTransferInfoEntity);
        if(!save) {
            throw new ServiceException("样品转移单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format(addDTO.getClientType().getName()+"用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品转移单" , sampleTransferInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), sampleTransferInfoEntity.getId(), "新增操作");
        
        // 明细处理
        List<SampleTransferDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        
        // 不允许重复添加相同的台账ID
        long sampleLedgerIdCount = detailList.stream().map(SampleTransferDetailDTO.AddDTO::getSampleLedgerId).distinct().count();
        if(sampleLedgerIdCount != detailList.size()){
            throw new ServiceException(ApiError.ERROR_REPEAT_SKU);
        }
        
        List<SampleTransferDetailEntity> sampleTransferDetailEntities = BeanMapperUtils.copyList(SampleTransferDetailEntity.class, detailList);
        List<String> skuIds = sampleTransferDetailEntities.stream().map(SampleTransferDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        
        // 获取SKU信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));
        
        for (SampleTransferDetailEntity sampleTransferDetailEntity : sampleTransferDetailEntities) {
            sampleTransferDetailEntity.setMainId(sampleTransferInfoEntity.getId());
            
            SkuVO skuVO = skuMap.getOrDefault(sampleTransferDetailEntity.getSkuId(), null);
            if(Objects.nonNull(skuVO)){
                sampleTransferDetailEntity.setSkuNo(skuVO.getSkuNo());
                sampleTransferDetailEntity.setProductName(skuVO.getSkuName());
            }
        }
        
        // 批量保存明细
        sampleTransferDetailService.saveBatch(sampleTransferDetailEntities);
        
        // 附件处理
        addAttachment(addDTO, sampleTransferInfoEntity);
        
        return new BaseResultDTO.AddDTO(sampleTransferInfoEntity.getId(), code);
    }
    
    /**
     * 添加附件信息
     * @param addDTO 包含附件URL和名称列表的数据传输对象
     * @param sampleTransferInfoEntity 样品转移单实体
     */
    private void addAttachment(SampleTransferInfoDTO.AddDTO addDTO, SampleTransferInfoEntity sampleTransferInfoEntity) {
        // 附件集合
        List<String> attachmentUrlList = addDTO.getAttachmentUrlList();
        // 附件名
        List<String> attachmentNameList = addDTO.getAttachmentNameList();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<SampleTransferInfoEntity> credentialClass = SampleTransferInfoEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            // 获取到表名
            String type = tableName.value();
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                attachment.setAttachUrl(attachmentUrlList.get(i));
                attachment.setAttachName(attachmentNameList.get(i));
                attachment.setBusinessId(sampleTransferInfoEntity.getId());
                attachment.setType(type);
                batchAttachmentList.add(attachment);
            }
            if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                wmsAttachmentService.saveBatch(batchAttachmentList);
            }
        }
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleTransferInfoDTO.UpdateDTO addOrUpdateDTO) {
        SampleTransferInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品转移单主单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SampleTransferInfoEntity sampleTransferInfoEntity =  BeanMapperUtils.map(SampleTransferInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleTransferInfoEntity);
        log.info("编辑 开始修改样品转移单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(sampleTransferInfoEntity);
        if(!save) {
            throw new ServiceException("样品转移单保存失败");
        }
        
        // 记录主单操作日志
        log.info("编辑 开始记录样品转移单日志数据，单号：【{}】", old.getCode());
        String msg = StrUtil.format(addOrUpdateDTO.getClientType().getName()+"用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "样品转移单");
        operateLogService.addModuleOperateLogByObj(old, sampleTransferInfoEntity, ModuleTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), sampleTransferInfoEntity.getId(), msg);
        
        // 明细处理
        updateDetail(addOrUpdateDTO, sampleTransferInfoEntity);
        
        // 附件处理
        updateAttachment(addOrUpdateDTO, old);
        
        return Boolean.TRUE;
    }
    
    /**
     * 更新样品转移单明细数据
     * <p>
     * 该方法根据传入的更新DTO对象，对样品转移单明细进行增删改操作，并记录相应的操作日志。
     * 具体包括：
     * - 删除旧明细中存在但新数据中不存在的记录；
     * - 新增新数据中ID为空的明细记录；
     * - 更新新数据中ID不为空的明细记录；
     * 同时为上述操作添加对应的操作日志。
     *
     * @param addOrUpdateDTO 包含待更新明细数据的DTO对象
     * @param sampleTransferInfoEntity 主表实体对象
     */
    private void updateDetail(SampleTransferInfoDTO.UpdateDTO addOrUpdateDTO, SampleTransferInfoEntity sampleTransferInfoEntity) {
        List<SampleTransferDetailDTO.UpdateDTO> detailList = addOrUpdateDTO.getDetailList();
        
        // 不允许重复添加相同的台账ID
        long sampleLedgerIdCount = detailList.stream().map(SampleTransferDetailDTO.UpdateDTO::getSampleLedgerId).distinct().count();
        if(sampleLedgerIdCount != detailList.size()){
            throw new ServiceException(ApiError.ERROR_REPEAT_SKU);
        }
        
        // 查询旧的明细列表
        List<SampleTransferDetailEntity> oldList = sampleTransferDetailService.listByMainId(sampleTransferInfoEntity.getId());
        
        List<SampleTransferDetailEntity> sampleTransferDetailEntities = BeanMapperUtils.copyList(SampleTransferDetailEntity.class, detailList);
        
        // 提取所有SKU编号，用于后续查询SKU信息
        List<String> skuIds = sampleTransferDetailEntities.stream().map(SampleTransferDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        
        // 获取SKU信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));
        
        for (SampleTransferDetailEntity sampleTransferDetailEntity : sampleTransferDetailEntities) {
            sampleTransferDetailEntity.setMainId(sampleTransferInfoEntity.getId());
            
            SkuVO skuVO = skuMap.getOrDefault(sampleTransferDetailEntity.getSkuId(), null);
            if(Objects.nonNull(skuVO)){
                sampleTransferDetailEntity.setSkuNo(skuVO.getSkuNo());
                sampleTransferDetailEntity.setProductName(skuVO.getSkuName());
            }
        }
        
        // 处理删除的明细数据
        if(CollUtil.isNotEmpty(oldList)){
            List<String> detailIds = detailList.stream()
                    .map(SampleTransferDetailDTO.UpdateDTO::getId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            
            List<SampleTransferDetailEntity> remove = oldList.stream()
                    .filter(oldEntity -> !detailIds.contains(oldEntity.getId()))
                    .collect(Collectors.toList());
            
            if(CollUtil.isNotEmpty(remove)){
                sampleTransferDetailService.removeByIds(remove.stream()
                        .map(SampleTransferDetailEntity::getId)
                        .collect(Collectors.toList()));
                
                // 添加删除日志
                List<Pair<String, String>> removePairList = remove.stream()
                        .map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo()))
                        .collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog(
                        addOrUpdateDTO.getClientType().getName()+"删除SKU【%s】", 
                        ModuleTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), 
                        removePairList, 
                        "编辑操作"
                );
            }
        }
        
        // 处理需要新增的明细数据（ID为空）
        List<SampleTransferDetailEntity> addList = sampleTransferDetailEntities.stream()
                .filter(e -> StringUtils.isBlank(e.getId()))
                .collect(Collectors.toList());
        
        if(CollUtil.isNotEmpty(addList)){
            sampleTransferDetailService.saveBatch(addList);
            
            // 添加新增日志
            List<Pair<String, String>> addPairList = addList.stream()
                    .map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo()))
                    .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(
                    addOrUpdateDTO.getClientType().getName()+"添加SKU【%s】", 
                    ModuleTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), 
                    addPairList, 
                    "编辑操作"
            );
        }
        
        // 处理需要更新的明细数据（ID不为空）
        List<SampleTransferDetailEntity> updateList = sampleTransferDetailEntities.stream()
                .filter(e -> StringUtils.isNotBlank(e.getId()))
                .collect(Collectors.toList());
        
        if(CollUtil.isNotEmpty(updateList)){
            sampleTransferDetailService.updateBatchById(updateList);
            
            // 添加更新日志
            for (SampleTransferDetailEntity sampleTransferDetailEntity : updateList) {
                SampleTransferDetailEntity oldDetail = oldList.stream()
                        .filter(e -> Objects.equals(e.getId(), sampleTransferDetailEntity.getId()))
                        .findFirst()
                        .orElse(null);
                
                if(Objects.nonNull(oldDetail)){
                    operateLogService.addModuleOperateLogByObj(
                            oldDetail, 
                            sampleTransferDetailEntity, 
                            ModuleTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), 
                            sampleTransferInfoEntity.getId(), 
                            String.format(addOrUpdateDTO.getClientType().getName()+"编辑SKU【%s】", oldDetail.getSkuNo())
                    );
                }
            }
        }
    }
    
    /**
     * 更新附件信息
     * <p>
     * 根据传入的更新数据和旧数据，对比附件URL列表，执行附件的删除和新增操作。
     * 删除不再需要的附件，新增新增的附件，并与业务ID关联。
     *
     * @param addOrUpdateDTO 包含附件URL和名称列表的更新数据传输对象
     * @param old 旧的样品转移单实体，用于获取业务ID
     */
    private void updateAttachment(SampleTransferInfoDTO.UpdateDTO addOrUpdateDTO, SampleTransferInfoEntity old) {
        List<String> attachmentUrlList = addOrUpdateDTO.getAttachmentUrlList();
        List<String> attachmentNameList = addOrUpdateDTO.getAttachmentNameList();
        
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()){
            // 查询旧的附件列表
            List<WmsAttachmentDTO.UpdateDTO> oldAttachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(old.getId()));
            
            if(CollUtil.isNotEmpty(oldAttachmentList)){
                // 处理删除的附件（旧的有但新的没有）
                List<WmsAttachmentDTO.UpdateDTO> remove = oldAttachmentList.stream()
                        .filter(oldAttachment -> !attachmentUrlList.contains(oldAttachment.getAttachUrl()))
                        .collect(Collectors.toList());
                
                if(CollUtil.isNotEmpty(remove)){
                    wmsAttachmentService.deleteByUrlList(remove.stream()
                            .map(WmsAttachmentDTO.UpdateDTO::getAttachUrl)
                            .collect(Collectors.toList()));
                }
            }
            
            // 处理需要新增的附件
            List<String> oldUrlList = oldAttachmentList.stream()
                    .map(WmsAttachmentDTO.UpdateDTO::getAttachUrl)
                    .collect(Collectors.toList());
            
            List<String> add = attachmentUrlList.stream()
                    .filter(url -> !oldUrlList.contains(url))
                    .collect(Collectors.toList());
            
            if(CollUtil.isNotEmpty(add)){
                Class<SampleTransferInfoEntity> credentialClass = SampleTransferInfoEntity.class;
                TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
                // 获取到表名
                String type = tableName.value();
                List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
                
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    if(!add.contains(attachmentUrlList.get(i))){
                        continue;
                    }
                    WmsAttachmentEntity addAttachment = new WmsAttachmentEntity();
                    addAttachment.setAttachUrl(attachmentUrlList.get(i));
                    addAttachment.setAttachName(attachmentNameList.get(i));
                    addAttachment.setBusinessId(old.getId());
                    addAttachment.setType(type);
                    batchAttachmentList.add(addAttachment);
                }
                
                if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                    wmsAttachmentService.saveBatch(batchAttachmentList);
                }
            }
        }
    }


    @Override
    public PagingVO<SampleTransferInfoDTO.ListDTO> paging(PagingDTO<SampleTransferInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleTransferInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SampleTransferInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        SampleTransferInfoDTO.PagingParamDTO searchParam = new SampleTransferInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SampleTransferInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SampleTransferInfoDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new SampleTransferInfoDTO.TabListDTO(status, 0));
            }
        });
        
        // 设置状态名称
        list.forEach(e -> {
            e.setTabFlagName(ApproveStatusEnum.getName(e.getTabFlag()));
        });
        
        // 按照指定顺序排序（使用枚举常量）
        List<String> orderList = Arrays.asList(
            ApproveStatusEnum.WAIT_SUBMIT.getStatus(), 
            ApproveStatusEnum.APPROVE_ING.getStatus(), 
            ApproveStatusEnum.APPROVE.getStatus(), 
            ApproveStatusEnum.REJECT.getStatus()
        );
        
        list.sort((a, b) -> {
            int indexA = orderList.indexOf(a.getTabFlag());
            int indexB = orderList.indexOf(b.getTabFlag());
            if (indexA == -1) indexA = Integer.MAX_VALUE;
            if (indexB == -1) indexB = Integer.MAX_VALUE;
            return Integer.compare(indexA, indexB);
        });
        
        // 在列表开头添加"全部"统计
        list.add(0, new SampleTransferInfoDTO.TabListDTO("all", list.stream().mapToInt(SampleTransferInfoDTO.TabListDTO::getCount).sum()));
        
        return list;
    }

    @Override
    public void exportList(SampleTransferInfoDTO.ExportDTO param, HttpServletResponse response) {
        // 异步导出任务
        downloadTaskFeign.saveDownloadTask("样品转移单导出", EXPORT_WMS_SAMPLE_TRANSFER_INFO_REPORT.getCode(), param);
    }

    /**
     * 获取样品转移单分页数据（用于异步导出）
     * @author wuhaotian
     * @date: 2025-10-28
     * @param dto
     * @return
     */
    @Override
    public PagingVO<SampleTransferInfoDTO.ListDTO> getSampleTransferInfoPageData(PagingDTO<SampleTransferInfoDTO.ExportDTO> dto) {
        Page<SampleTransferInfoDTO.ExportDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SampleTransferInfoDTO.ListDTO> pageData = this.baseMapper.listExport(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id, ClientTypeEnum clientType) {
        SampleTransferInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到样品转移单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改样品转移单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动样品转移单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录样品转移单日志数据，id：【{}】", id);
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品转移单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SampleTransferInfoDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId(), dto.getClientType());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SampleTransferInfoDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId(), dto.getClientType());
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto, ClientTypeEnum clientType) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SampleTransferInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品转移单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SampleTransferInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SAMPLE_TRANSFER_INFO.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id, ClientTypeEnum clientType) {
        SampleTransferInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品转移单主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 记录台账流水（反审核）- 转入人
        try {
            SampleLedgerFlowDTO.AddFlowDTO transferInFlowDTO = buildTransferInFlow(entity.getId(), entity.getCode(), ApproveTypeEnum.DIS_APPROVE);
            if (transferInFlowDTO != null) {
                sampleLedgerFlowService.addSampleLedgerFlow(transferInFlowDTO);
                log.info("样品转移单反审核转入人台账流水记录成功，单据编号：{}", entity.getCode());
            }
        } catch (Exception e) {
            log.error("样品转移单反审核转入人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
            throw new ServiceException(StrUtil.format("样品转移单反审核转入人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage()));
        }
        
        // 记录台账流水（反审核）- 转出人
        try {
            SampleLedgerFlowDTO.AddFlowDTO transferOutFlowDTO = buildTransferOutFlow(entity.getId(), entity.getCode(), ApproveTypeEnum.DIS_APPROVE);
            if (transferOutFlowDTO != null) {
                sampleLedgerFlowService.addSampleLedgerFlow(transferOutFlowDTO);
                log.info("样品转移单反审核转出人台账流水记录成功，单据编号：{}", entity.getCode());
            }
        } catch (Exception e) {
            log.error("样品转移单反审核转出人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
            throw new ServiceException(StrUtil.format("样品转移单反审核转出人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage()));
        }

        // 操作日志
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品转移单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SampleTransferInfoEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id, ClientTypeEnum clientType) {
        SampleTransferInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品转移单主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        
        // 删除主单数据
        log.info("删除 开始删除样品转移单主单数据，id：【{}】", id);
        super.removeById(id);
        
        // 删除明细数据
        sampleTransferDetailService.lambdaUpdate()
                .set(SampleTransferDetailEntity::getIsDeleted, Boolean.TRUE)
                .eq(SampleTransferDetailEntity::getMainId, id)
                .update();
        
        // 删除附件
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(id));
        if(CollUtil.isNotEmpty(attachmentList)){
            List<String> urlList = attachmentList.stream()
                    .map(WmsAttachmentDTO.UpdateDTO::getAttachUrl)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            wmsAttachmentService.deleteByUrlList(urlList);
        }
        
        // 删除日志数据
        log.info("删除 开始删除样品转移单日志数据，id：【{}】", id);
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品转移单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), entity.getId(), "删除样品转移单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        SampleTransferInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品转移单主单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus().getStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改样品转移单主单状态数据，id：【{}】", id);
        lambdaUpdate().eq(SampleTransferInfoEntity::getId, id)
            .set(SampleTransferInfoEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(SampleTransferInfoEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品转移单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SampleTransferInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品转移单主单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改样品转移单主单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品转移单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SAMPLE_TRANSFER_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SampleTransferInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        
        // 只有审核通过和反审核才记录台账流水
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (ApproveTypeEnum.PASS.equals(approveType) || ApproveTypeEnum.DIS_APPROVE.equals(approveType)) {
            // 记录台账流水 - 转入人
            try {
                SampleLedgerFlowDTO.AddFlowDTO transferInFlowDTO = buildTransferInFlow(entity.getId(), entity.getCode(), approveType);
                if (transferInFlowDTO != null) {
                    sampleLedgerFlowService.addSampleLedgerFlow(transferInFlowDTO);
                    log.info("样品转移单转入人台账流水记录成功，单据编号：{}，审核类型：{}", entity.getCode(), approveType.getName());
                }
            } catch (Exception e) {
                log.error("样品转移单转入人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
                throw new ServiceException(StrUtil.format("样品转移单转入人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage()));
            }
            
            // 记录台账流水 - 转出人
            try {
                SampleLedgerFlowDTO.AddFlowDTO transferOutFlowDTO = buildTransferOutFlow(entity.getId(), entity.getCode(), approveType);
                if (transferOutFlowDTO != null) {
                    sampleLedgerFlowService.addSampleLedgerFlow(transferOutFlowDTO);
                    log.info("样品转移单转出人台账流水记录成功，单据编号：{}，审核类型：{}", entity.getCode(), approveType.getName());
                }
            } catch (Exception e) {
                log.error("样品转移单转出人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
                throw new ServiceException(StrUtil.format("样品转移单转出人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage()));
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public SampleTransferInfoDTO.ViewDTO view(String id) {
        SampleTransferInfoEntity sampleTransferInfoEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到样品转移单主单数据"));
        SampleTransferInfoDTO.ViewDTO data = BeanMapperUtils.map(SampleTransferInfoDTO.ViewDTO.class, sampleTransferInfoEntity);
        data.setApproveStatus(sampleTransferInfoEntity.getApproveStatus().getStatus());
        // 数据填充处理
        fillOne(data);

        // 查询明细列表
        List<SampleTransferDetailEntity> sampleTransferDetailEntities = sampleTransferDetailService.listByMainId(id);
        List<SampleTransferDetailDTO.ViewDTO> detailDTOList = BeanMapperUtils.copyList(SampleTransferDetailDTO.ViewDTO.class, sampleTransferDetailEntities);

        // 提取所有样品台账ID，用于后续查询可用数量和使用方信息
        List<String> sampleLedgerIds = sampleTransferDetailEntities.stream()
                .map(SampleTransferDetailEntity::getSampleLedgerId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(sampleLedgerIds)) {
            // 构造查询条件：根据转出人ID和台账ID列表查询样品台账中的可用数量
            SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
            dto.setUserId(sampleTransferInfoEntity.getTransferOutUserId());
            dto.setIds(sampleLedgerIds);
            dto.setType(SampleLedgerTypeEnum.TRANSFER.getCode());
            dto.setChildId(id); // 排除当前单据
            List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(dto);
            Map<String, SampleLedgerDTO.SkuAvailableQtyDTO> sampleLedgerMap = skuAvailableQtyDTOS.stream()
                    .collect(Collectors.toMap(SampleLedgerDTO.SkuAvailableQtyDTO::getSampleLedgerId, Function.identity(), (o1, o2) -> o1));

            // 填充明细中的可用数量和使用方信息
            detailDTOList.forEach(detailDTO -> {
                String sampleLedgerId = detailDTO.getSampleLedgerId();
                if (StrUtil.isNotBlank(sampleLedgerId)) {
                    SampleLedgerDTO.SkuAvailableQtyDTO sampleLedger = sampleLedgerMap.getOrDefault(sampleLedgerId, null);
                    if (Objects.nonNull(sampleLedger)) {
                        detailDTO.setAvailableQty(sampleLedger.getAvailableQty());
                        detailDTO.setUseUserId(sampleLedger.getUseUserId());
                        detailDTO.setUseUserName(sampleLedger.getUseUserName());
                    }
                }
            });
        }

        // 设置明细列表到主数据对象中
        data.setDetailList(detailDTOList);

        // 查询相关的附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(id));
        if (CollUtil.isNotEmpty(attachmentList)) {
            // 分别提取附件名称和URL列表设置到返回对象中
            data.setAttachmentNameList(attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList()));
            data.setAttachmentUrlList(attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
        }
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(SampleTransferInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SAMPLE_TRANSFER_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(SampleTransferInfoDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SampleTransferInfoEntity::getId, id)
            .set(SampleTransferInfoEntity::getApproveUserId, userInfo.getUid())
            .set(SampleTransferInfoEntity::getApproveUserName, userInfo.getUserName())
            .set(SampleTransferInfoEntity::getApproveStatus, approveStatus)
            .set(SampleTransferInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new SampleTransferInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SampleTransferInfoEntity::getId, id)
            .set(SampleTransferInfoEntity::getApproveUserId, "")
            .set(SampleTransferInfoEntity::getApproveUserName, "")
            .set(SampleTransferInfoEntity::getApproveStatus, approveStatus)
            .set(SampleTransferInfoEntity::getApproveTime, null)
            .update(new SampleTransferInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SampleTransferInfoEntity::getId, id)
        .set(SampleTransferInfoEntity::getApproveStatus, approveStatus)
        .update(new SampleTransferInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SampleTransferInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 查询最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SAMPLE_TRANSFER_INFO.getCode(), obj.getId()));
        });
        
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }

        // 属性赋值
        for(SampleTransferInfoDTO.ListDTO data : list) {
            // 设置审批状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            // 设置作废状态名称
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            
            // 设置最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())&&StringUtils.isBlank(data.getApproveUserName())) {
                String curApprove = listApiResult.getData().stream()
                        .filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName()))
                        .map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName)
                        .collect(Collectors.joining(","));
                data.setApproveUserName(curApprove);
            }
        }
    }
    /**
    * 提交审核前的数据校验
    */
    private void validateSubmit(SampleTransferInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus()) || entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        // 校验转出人的样品台账数量
        validateQty(entity);
    }
    
    /**
     * 校验样品转移明细中的转移数量是否超过转出人台账中的可用数量。
     * <p>
     * 该方法会根据传入的转移主表信息，查询其所有明细记录，并针对每条明细校验：
     * 转移数量不能超过转出人在对应台账中该SKU的可用数量。
     * 若发现某条明细的转移数量超出，则抛出业务异常。
     *
     * @param entity 样品转移主表实体对象，用于获取转出人ID、主表ID等信息
     */
    private void validateQty(SampleTransferInfoEntity entity) {
        List<SampleTransferDetailEntity> detailList = sampleTransferDetailService.lambdaQuery()
                .eq(SampleTransferDetailEntity::getMainId, entity.getId())
                .list();
        
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        
        List<String> skuIds = detailList.stream()
                .map(SampleTransferDetailEntity::getSkuId)
                .distinct()
                .collect(Collectors.toList());
        
        // 查询转出人的样品台账可用数量
        SampleLedgerDTO.SearchDTO searchDTO = new SampleLedgerDTO.SearchDTO();
        searchDTO.setUserId(entity.getTransferOutUserId());
        searchDTO.setType(SampleLedgerTypeEnum.TRANSFER.getCode());  // 使用转移类型查询
        searchDTO.setChildId(entity.getId());
        searchDTO.setSkuIds(skuIds);
        
        List<SampleLedgerDTO.SkuAvailableQtyDTO> ledgerList = sampleLedgerService.listLedgerByUserId(searchDTO);
        Map<String, Integer> ledgerMap = ledgerList.stream()
                .collect(Collectors.toMap(
                        SampleLedgerDTO.SkuAvailableQtyDTO::getSampleLedgerId, 
                        SampleLedgerDTO.SkuAvailableQtyDTO::getLedgerQty, 
                        (o1, o2) -> o1
                ));
        
        // 校验每个明细的转移数量（需要按明细查询台账，因为每个明细的使用方可能不同）
        for (SampleTransferDetailEntity detail : detailList) {
            Integer transferQty = detail.getTransferQty();
            if (transferQty == null || transferQty <= 0) {
                continue; // 跳过无效数量
            }
            
            Integer ledgerQty = ledgerMap.getOrDefault(detail.getSampleLedgerId(), 0);
            if (transferQty > ledgerQty) {
                throw new ServiceException(StrUtil.format("SKU【{}】转移数量【{}】不能大于转出人台账数量【{}】",
                        detail.getSkuNo(), transferQty, ledgerQty));
            }
            
            // 防止明细里还有重复的台账ID，逐个扣减
            ledgerMap.put(detail.getSampleLedgerId(), ledgerQty - transferQty);
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleTransferInfoEntity sampleTransferInfoEntity) {
        // 校验转入人和转出人不能相同
        String transferInUserId = sampleTransferInfoEntity.getTransferInUserId();
        String transferOutUserId = sampleTransferInfoEntity.getTransferOutUserId();
        if(Objects.equals(transferInUserId, transferOutUserId)){
            throw new ServiceException("转入人和转出人不能相同");
        }

        // 查询用户信息
        List<FindUserDTO> users = sysUserFeign.getUserListByUserIds(Arrays.asList(transferInUserId, transferOutUserId));
        if(CollUtil.isEmpty(users)){
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }

        FindUserDTO transferInUser = users.stream()
                .filter(e -> Objects.equals(e.getUserId(), transferInUserId))
                .findFirst()
                .orElse(null);
        if(Objects.isNull(transferInUser)){
            throw new ServiceException(ApiError.NOT_EXIST, "转入人");
        }
        
        FindUserDTO transferOutUser = users.stream()
                .filter(e -> Objects.equals(e.getUserId(), transferOutUserId))
                .findFirst()
                .orElse(null);
        if(Objects.isNull(transferOutUser)){
            throw new ServiceException(ApiError.NOT_EXIST, "转出人");
        }

        // 查询部门信息
        String transferInDeptId = sampleTransferInfoEntity.getTransferInDeptId();
        String transferOutDeptId = sampleTransferInfoEntity.getTransferOutDeptId();
        List<SysDepartmentEntity> sysDepartmentEntities = sysUserFeign.listDeptByIds(Arrays.asList(transferInDeptId, transferOutDeptId));
        if(CollUtil.isEmpty(sysDepartmentEntities)){
            throw new ServiceException(ApiError.ERROR_9029);
        }

        SysDepartmentEntity transferInDept = sysDepartmentEntities.stream()
                .filter(e -> Objects.equals(e.getId(), transferInDeptId))
                .findFirst()
                .orElse(null);
        if(Objects.isNull(transferInDept)){
            throw new ServiceException(ApiError.NOT_EXIST, "转入部门");
        }
        
        SysDepartmentEntity transferOutDept = sysDepartmentEntities.stream()
                .filter(e -> Objects.equals(e.getId(), transferOutDeptId))
                .findFirst()
                .orElse(null);
        if(Objects.isNull(transferOutDept)){
            throw new ServiceException(ApiError.NOT_EXIST, "转出部门");
        }

        // 赋值
        sampleTransferInfoEntity.setTransferInUserName(transferInUser.getUserName());
        sampleTransferInfoEntity.setTransferOutUserName(transferOutUser.getUserName());
        sampleTransferInfoEntity.setTransferInDeptName(transferInDept.getName());
        sampleTransferInfoEntity.setTransferOutDeptName(transferOutDept.getName());
    }

    // ==================== 实现 SampleLedgerFlowBuilder 接口 ====================
    
    @Override
    public String getSupportedSourceType() {
        return SourceTypeEnum.SAMPLE_TRANSFER_INFO.getCode();
    }

    @Override
    public SampleLedgerFlowDTO.AddFlowDTO buildFlow(String sourceId, String sourceCode, ApproveTypeEnum approveType) {
        // 转移单使用buildTransferInFlow来构建转入人的流水
        return buildTransferInFlow(sourceId, sourceCode, approveType);
    }

    /**
     * 计算转入人数量：审核为+X，反审核为-X
     */
    @Override
    public Integer calculateQty(Integer originalQty, ApproveTypeEnum approveType) {
        if (originalQty == null) {
            return 0;
        }
        
        if (ApproveTypeEnum.PASS.equals(approveType)) {
            return originalQty; // 审核：+X（转入人增加库存）
        } else if (ApproveTypeEnum.DIS_APPROVE.equals(approveType)) {
            return -originalQty; // 反审核：-X（转入人减少库存）
        }
        
        return 0;
    }

    /**
     * 计算转出人数量：审核为-X，反审核为+X（与转入人相反）
     */
    private Integer calculateTransferOutQty(Integer originalQty, ApproveTypeEnum approveType) {
        if (originalQty == null) {
            return 0;
        }
        
        if (ApproveTypeEnum.PASS.equals(approveType)) {
            return -originalQty; // 审核：-X（转出人减少库存）
        } else if (ApproveTypeEnum.DIS_APPROVE.equals(approveType)) {
            return originalQty; // 反审核：+X（转出人增加库存）
        }
        
        return 0;
    }

    /**
     * 构建转入人的台账流水
     */
    private SampleLedgerFlowDTO.AddFlowDTO buildTransferInFlow(String sourceId, String sourceCode, ApproveTypeEnum approveType) {
        try {
            // 获取样品转移单主表信息
            SampleTransferInfoEntity entity = this.getById(sourceId);
            if (entity == null) {
                log.error("获取样品转移单失败，sourceId：{}", sourceId);
                return null;
            }

            // 获取样品转移单明细
            List<SampleTransferDetailEntity> detailList = sampleTransferDetailService.listByMainId(sourceId);

            if (detailList.isEmpty()) {
                log.warn("样品转移单明细为空，sourceId：{}", sourceId);
                return null;
            }

            // 构建流水明细
            List<SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO> flowDetails = new ArrayList<>();
            for (SampleTransferDetailEntity detail : detailList) {
                // 计算数量：审核为+X，反审核为-X（转入人视角，转入是增加库存）
                Integer qty = calculateQty(detail.getTransferQty(), approveType);
                
                SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO flowDetail = new SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO();
                flowDetail.setSourceDetailId(detail.getId());
                flowDetail.setSkuNo(detail.getSkuNo());
                flowDetail.setSkuId(detail.getSkuId());
                flowDetail.setProductName(detail.getProductName());
                flowDetail.setQty(qty);
                // 转入人会新增台账，所以不设置sampleLedgerId
                flowDetail.setSampleLedgerId(null);
                flowDetails.add(flowDetail);
            }

            // 构建流水主表数据 - 转入人台账
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = new SampleLedgerFlowDTO.AddFlowDTO();
            flowDTO.setSourceType(getSupportedSourceType());
            flowDTO.setApproveType(approveType.getStatus());
            flowDTO.setOperateTime(LocalDateTime.now());
            flowDTO.setBillDate(entity.getTransferDate());
            flowDTO.setSourceName("样品转移单");
            flowDTO.setSourceCode(sourceCode);
            flowDTO.setSourceId(sourceId);
            flowDTO.setUserId(entity.getTransferInUserId());
            flowDTO.setUserName(entity.getTransferInUserName());
            flowDTO.setDeptId(entity.getTransferInDeptId());
            // 根据部门ID查询部门名称
            flowDTO.setDeptName(getDeptNameById(entity.getTransferInDeptId()));
            flowDTO.setDetailList(flowDetails);

            return flowDTO;
        } catch (Exception e) {
            log.error("构建样品转移单转入人台账流水失败，sourceId：{}，错误：{}", sourceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建转出人的台账流水
     */
    private SampleLedgerFlowDTO.AddFlowDTO buildTransferOutFlow(String sourceId, String sourceCode, ApproveTypeEnum approveType) {
        try {
            // 获取样品转移单主表信息
            SampleTransferInfoEntity entity = this.getById(sourceId);
            if (entity == null) {
                log.error("获取样品转移单失败，sourceId：{}", sourceId);
                return null;
            }

            // 获取样品转移单明细
            List<SampleTransferDetailEntity> detailList = sampleTransferDetailService.listByMainId(sourceId);

            if (detailList.isEmpty()) {
                log.warn("样品转移单明细为空，sourceId：{}", sourceId);
                return null;
            }

            // 构建流水明细
            List<SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO> flowDetails = new ArrayList<>();
            for (SampleTransferDetailEntity detail : detailList) {
                // 计算数量：审核为-X，反审核为+X（转出人视角，转出是减少库存）
                Integer qty = calculateTransferOutQty(detail.getTransferQty(), approveType);
                
                SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO flowDetail = new SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO();
                flowDetail.setSourceDetailId(detail.getId());
                flowDetail.setSkuNo(detail.getSkuNo());
                flowDetail.setSkuId(detail.getSkuId());
                flowDetail.setProductName(detail.getProductName());
                flowDetail.setQty(qty);
                // 设置样品台账ID（转出人是在现有台账上扣减）
                flowDetail.setSampleLedgerId(detail.getSampleLedgerId());
                flowDetails.add(flowDetail);
            }

            // 构建流水主表数据 - 转出人台账
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = new SampleLedgerFlowDTO.AddFlowDTO();
            flowDTO.setSourceType(getSupportedSourceType());
            flowDTO.setApproveType(approveType.getStatus());
            flowDTO.setOperateTime(LocalDateTime.now());
            flowDTO.setBillDate(entity.getTransferDate());
            flowDTO.setSourceName("样品转移单");
            flowDTO.setSourceCode(sourceCode);
            flowDTO.setSourceId(sourceId);
            // 使用方信息将通过每个明细的sampleLedgerId在SampleLedgerFlowServiceImpl中查询获取
            flowDTO.setUserId(entity.getTransferOutUserId());
            flowDTO.setUserName(entity.getTransferOutUserName());
            flowDTO.setDeptId(entity.getTransferOutDeptId());
            // 根据部门ID查询部门名称
            flowDTO.setDeptName(getDeptNameById(entity.getTransferOutDeptId()));
            flowDTO.setDetailList(flowDetails);

            return flowDTO;
        } catch (Exception e) {
            log.error("构建样品转移单转出人台账流水失败，sourceId：{}，错误：{}", sourceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据部门ID查询部门名称
     */
    private String getDeptNameById(String deptId) {
        if (StrUtil.isBlank(deptId)) {
            return null;
        }

        try {
            // 调用部门服务根据ID查询部门信息
            SysDepartmentDTO department = sysUserFeign.getUserDeptById(deptId);

            if (department != null && StrUtil.isNotBlank(department.getName())) {
                return department.getName();
            }

            log.warn("未找到部门ID：{}", deptId);
            return null;
        } catch (Exception e) {
            log.error("查询部门名称失败，部门ID：{}，错误：{}", deptId, e.getMessage(), e);
            return null;
        }
    }
}
