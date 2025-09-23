package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.InstockTypeEnum;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.model.wms.enums.SampleLedgerTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SampleBackInfoMapper;
import com.erp.server.wms.service.*;
import com.common.core.utils.ExcelUtil;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.model.wms.dto.excel.SampleBackInfoImportExcelDTO;
import com.erp.server.wms.listener.SampleBackInfoExcelListener;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.enums.ImportTypeEnum;
import com.erp.model.wms.dto.SampleBackDetailDTO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 样品退回单 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleBackInfoServiceImpl extends SuperServiceImpl<SampleBackInfoMapper, SampleBackInfoEntity> implements SampleBackInfoService,SampleLedgerFlowBuilder {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private SampleLedgerFlowService sampleLedgerFlowService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private SampleBackDetailService sampleBackDetailService;
    @Autowired
    private SampleLedgerService sampleLedgerService;

    @Autowired
    private WmsAttachmentService attachmentService;

    @Autowired
    private OtherInstockService otherInstockService;

    @Autowired
    private OtherInstockDetailService otherInstockDetailService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private FileFeign fileFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleBackInfoDTO.AddDTO addDTO) {
        SampleBackInfoEntity sampleBackInfoEntity = new SampleBackInfoEntity();
        BeanMapperUtils.copy(addDTO, sampleBackInfoEntity);

        // 数据处理
        handleData(sampleBackInfoEntity);

        log.info("开始新增样品退回单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YPTH);
        sampleBackInfoEntity.setCode(code);
        
        // 设置默认审批状态为待提交
        sampleBackInfoEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        
        boolean save = super.save(sampleBackInfoEntity);
        if(!save) {
            throw new ServiceException("样品退回单保存失败");
        }

        // 保存明细
        if (CollUtil.isNotEmpty(addDTO.getDetailList())) {
            for (SampleBackDetailDTO.AddDTO detailDTO : addDTO.getDetailList()) {
                SampleBackDetailEntity detailEntity = new SampleBackDetailEntity();
                BeanMapperUtils.copy(detailDTO, detailEntity);
                detailEntity.setMainId(sampleBackInfoEntity.getId());
                if (detailEntity.getSourceDetailId()==null){
                    detailEntity.setSourceDetailId("");
                }

                // 根据归属人、使用方和SKU查询sample_ledger_id
                String sampleLedgerId = getSampleLedgerId(sampleBackInfoEntity.getUserId(), detailEntity.getUseUserId(), detailEntity.getSkuId());
                detailEntity.setSampleLedgerId(sampleLedgerId);

                boolean detailSave = sampleBackDetailService.save(detailEntity);
                if (!detailSave) {
                    throw new ServiceException("样品退回单明细保存失败");
                }
            }
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品退回单" , sampleBackInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), sampleBackInfoEntity.getId(), "新增操作");

        // 处理附件
        addAttachment(addDTO, sampleBackInfoEntity);

        return new BaseResultDTO.AddDTO(sampleBackInfoEntity.getId(), code);
    }

    /**
     * 添加附件信息
     * @param addDTO 包含附件URL和名称列表的数据传输对象
     * @param sampleBackInfoEntity
     */
    private void addAttachment(SampleBackInfoDTO.AddDTO addDTO, SampleBackInfoEntity sampleBackInfoEntity) {
        //附件集合
        List<String> attachmentUrlList = addDTO.getAttachmentUrlList();
        //附件名
        List<String> attachmentNameList = addDTO.getAttachmentNameList();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<SampleBackInfoEntity> credentialClass = SampleBackInfoEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                attachment.setAttachUrl(attachmentUrlList.get(i));
                attachment.setAttachName(attachmentNameList.get(i));
                attachment.setBusinessId(sampleBackInfoEntity.getId());
                attachment.setType(type);
                batchAttachmentList.add(attachment);
            }
            if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                attachmentService.saveBatch(batchAttachmentList);
            }
        }
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleBackInfoDTO.UpdateDTO addOrUpdateDTO) {
        SampleBackInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品退回单"));
        
        // 检查单据是否已作废
        if (InvalidStatusEnum.VOIDED.getStatus().equals(old.getInvalidStatus())) {
            throw new ServiceException("已作废的样品退回单不支持修改操作");
        }
        
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SampleBackInfoEntity sampleBackInfoEntity =  BeanMapperUtils.map(SampleBackInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleBackInfoEntity);
        
        // 回填部门名称和仓库名称
        fillDeptAndWarehouseNames(old);
        fillDeptAndWarehouseNames(sampleBackInfoEntity);
        log.info("编辑 开始修改样品退回单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(sampleBackInfoEntity);
        if(!save) {
            throw new ServiceException("样品退回单保存失败");
        }
        // 处理明细数据（包含增删改）
        if (CollUtil.isNotEmpty(addOrUpdateDTO.getDetailList())) {
            // 获取原有明细列表
            LambdaQueryWrapper<SampleBackDetailEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SampleBackDetailEntity::getMainId, addOrUpdateDTO.getId());
            List<SampleBackDetailEntity> existingDetails = sampleBackDetailService.list(wrapper);
            
            // 创建原有明细的ID集合，用于判断哪些需要删除
            Set<String> existingDetailIds = existingDetails.stream()
                .map(SampleBackDetailEntity::getId)
                .collect(Collectors.toSet());
            
            // 创建新明细的ID集合，用于判断哪些需要新增
            Set<String> newDetailIds = addOrUpdateDTO.getDetailList().stream()
                .map(SampleBackDetailDTO.UpdateDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            // 删除不再存在的明细
            Set<String> toDeleteIds = existingDetailIds.stream()
                .filter(id -> !newDetailIds.contains(id))
                .collect(Collectors.toSet());
            if (!toDeleteIds.isEmpty()) {
                // 查询要删除的明细信息用于日志记录
                List<SampleBackDetailEntity> deleteDetails = existingDetails.stream()
                    .filter(detail -> toDeleteIds.contains(detail.getId()))
                    .collect(Collectors.toList());
                
                sampleBackDetailService.removeByIds(toDeleteIds);
                
                // 批量添加删除日志
                List<Pair<String, String>> deletePairList = deleteDetails.stream()
                    .map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo()))
                    .collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除SKU【%s】", ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), deletePairList, "编辑操作");
            }
            
            // 分离新增和更新的明细
            List<SampleBackDetailDTO.UpdateDTO> addList = addOrUpdateDTO.getDetailList().stream()
                .filter(e -> StringUtils.isBlank(e.getId()))
                .collect(Collectors.toList());
            List<SampleBackDetailDTO.UpdateDTO> updateList = addOrUpdateDTO.getDetailList().stream()
                .filter(e -> StringUtils.isNotBlank(e.getId()))
                .collect(Collectors.toList());

            // 处理新增明细
            List<SampleBackDetailEntity> addEntityList = new ArrayList<>();
            if (CollUtil.isNotEmpty(addList)) {
                for (SampleBackDetailDTO.UpdateDTO detailDTO : addList) {
                    SampleBackDetailEntity detailEntity = new SampleBackDetailEntity();
                    BeanMapperUtils.copy(detailDTO, detailEntity);
                    detailEntity.setMainId(addOrUpdateDTO.getId());
                    
                    // 根据归属人、使用方和SKU查询sample_ledger_id
                    String sampleLedgerId = getSampleLedgerId(sampleBackInfoEntity.getUserId(), detailEntity.getUseUserId(), detailEntity.getSkuId());
                    detailEntity.setSampleLedgerId(sampleLedgerId);
                    
                    addEntityList.add(detailEntity);
                }
                
                boolean saveResult = sampleBackDetailService.saveBatch(addEntityList);
                if (!saveResult) {
                    throw new ServiceException("样品退回单明细保存失败");
                }
                
                // 批量添加新增日志
                List<Pair<String, String>> addPairList = addEntityList.stream()
                    .map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo()))
                    .collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("添加SKU【%s】", ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), addPairList, "编辑操作");
            }

            // 处理更新明细
            if (CollUtil.isNotEmpty(updateList)) {
                for (SampleBackDetailDTO.UpdateDTO detailDTO : updateList) {
                    SampleBackDetailEntity detailEntity = new SampleBackDetailEntity();
                    BeanMapperUtils.copy(detailDTO, detailEntity);
                    detailEntity.setMainId(addOrUpdateDTO.getId());
                    
                    // 根据归属人、使用方和SKU查询sample_ledger_id
                    String sampleLedgerId = getSampleLedgerId(sampleBackInfoEntity.getUserId(), detailEntity.getUseUserId(), detailEntity.getSkuId());
                    detailEntity.setSampleLedgerId(sampleLedgerId);
                    
                    // 查找原有明细用于日志对比
                    SampleBackDetailEntity oldDetail = existingDetails.stream()
                        .filter(e -> Objects.equals(e.getId(), detailDTO.getId()))
                        .findFirst()
                        .orElse(null);
                    
                    boolean detailUpdate = sampleBackDetailService.updateById(detailEntity);
                    if (!detailUpdate) {
                        throw new ServiceException("样品退回单明细更新失败");
                    }
                    
                    // 添加更新日志
                    if (oldDetail != null) {
                        operateLogService.addModuleOperateLogByObj(oldDetail, detailEntity, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), addOrUpdateDTO.getId(), String.format("编辑SKU【%s】", oldDetail.getSkuNo()));
                    }
                }
            }
        }

        // 记录主单操作日志
            log.info("编辑 开始记录样品退回单日志数据，单号：【{}】", sampleBackInfoEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleBackInfoEntity.getCode(), "样品退回单");
        operateLogService.addModuleOperateLogByObj(old, sampleBackInfoEntity, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), sampleBackInfoEntity.getId(), msg);

        // 处理附件
        updateAttachment(addOrUpdateDTO, old);

        return Boolean.TRUE;
    }

    /**
     * 更新附件信息
     * @param addOrUpdateDTO 包含附件URL和名称列表的更新数据传输对象
     * @param old 旧的样品退回信息实体，用于获取业务ID
     */
    private void updateAttachment(SampleBackInfoDTO.UpdateDTO addOrUpdateDTO, SampleBackInfoEntity old) {
        List<String> attachmentUrlList = addOrUpdateDTO.getAttachmentUrlList();
        List<String> attachmentNameList = addOrUpdateDTO.getAttachmentNameList();
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()){
            List<WmsAttachmentDTO.UpdateDTO> oldAttachmentList = attachmentService.getByBusinessIds(Arrays.asList(old.getId()));
            if(CollUtil.isNotEmpty(oldAttachmentList)){
                // 处理删除的数据
                List<WmsAttachmentDTO.UpdateDTO> remove = oldAttachmentList.stream()
                        .filter(oldAttachment -> !attachmentUrlList.contains(oldAttachment.getAttachUrl()))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(remove)){
                    attachmentService.deleteByUrlList(remove.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
                }
            }

            //处理需要新增的数据
            List<String> oldUrlList = oldAttachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            List<String> add = attachmentUrlList.stream()
                    .filter(url -> !oldUrlList.contains(url))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(add)){
                Class<SampleBackInfoEntity> credentialClass = SampleBackInfoEntity.class;
                TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
                //获取到表名
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
                    attachmentService.saveBatch(batchAttachmentList);
                }
            }
        }
    }

    @Override
    public PagingVO<SampleBackInfoDTO.ListDTO> paging(PagingDTO<SampleBackInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleBackInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SampleBackInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        SampleBackInfoDTO.PagingParamDTO searchParam = new SampleBackInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SampleBackInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SampleBackInfoDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new SampleBackInfoDTO.TabListDTO(status, "", 0));
        }
        });

        list.forEach(e ->{
            e.setTabFlagName(ApproveStatusEnum.getName(e.getTabFlag()));
        });
        
        // 按照指定顺序排序
        List<String> orderList = Arrays.asList("waitSubmit", "approveIng", "approved", "rejected");
        list.sort((a, b) -> {
            int indexA = orderList.indexOf(a.getTabFlag());
            int indexB = orderList.indexOf(b.getTabFlag());
            if (indexA == -1) indexA = Integer.MAX_VALUE;
            if (indexB == -1) indexB = Integer.MAX_VALUE;
            return Integer.compare(indexA, indexB);
        });
        
        list.add(0,new SampleBackInfoDTO.TabListDTO("all", "全部", list.stream().mapToInt(SampleBackInfoDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public SampleBackInfoDTO.ViewDTO view(String id) {
        SampleBackInfoDTO.ViewDTO viewDTO = new SampleBackInfoDTO.ViewDTO();
        SampleBackInfoEntity entity = super.getById(id);
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "样品退回单");
        }
        BeanMapperUtils.copy(entity, viewDTO);

        viewDTO.setApproveStatus(entity.getApproveStatus().getCode());
        // 设置明细列表到ViewDTO中
        List<SampleBackDetailEntity> detailEntities = sampleBackDetailService.list(new LambdaQueryWrapper<SampleBackDetailEntity>().eq(SampleBackDetailEntity::getMainId, id));
        
        // 构建用户ID到用户名的映射
        Map<String, String> userIdToNameMap = userList.stream()
            .collect(Collectors.toMap(
                FindUserDTO::getUserId, 
                FindUserDTO::getUserName, 
                (existing, replacement) -> existing
            ));
        
        // 批量查询可退回数量
        Map<String, SampleLedgerDTO.SkuAvailableQtyDTO> availableQtyMap = new HashMap<>();
        if (CollUtil.isNotEmpty(detailEntities)) {
            // 收集所有需要查询的条件
            Set<String> skuIds = detailEntities.stream()
                .map(SampleBackDetailEntity::getSkuId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
            
            if (CollUtil.isNotEmpty(skuIds)) {
                // 一次性查询所有SKU的可退回数量
                SampleLedgerDTO.SearchDTO searchDTO = new SampleLedgerDTO.SearchDTO();
                searchDTO.setUserId(entity.getUserId());
                searchDTO.setSkuIds(new ArrayList<>(skuIds));
                searchDTO.setType(SampleLedgerTypeEnum.BACK.getCode());
                List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(searchDTO);
                
                // 构建SKU可用数量映射，key为userId+useUserId+skuId的组合
                if (CollUtil.isNotEmpty(skuAvailableQtyDTOS)) {
                    for (SampleLedgerDTO.SkuAvailableQtyDTO dto : skuAvailableQtyDTOS) {
                        String key = dto.getUserId() + "_" + dto.getUseUserId() + "_" + dto.getSkuId();
                        availableQtyMap.put(key, dto);
                    }
                }
            }
        }
        
        // 构建最终结果
        List<SampleBackDetailDTO.ViewDTO> detailList = detailEntities.stream()
            .map(detail -> {
                SampleBackDetailDTO.ViewDTO detailDTO = new SampleBackDetailDTO.ViewDTO();
                BeanMapperUtils.copy(detail, detailDTO);
                
                // 填充使用方名称
                if (StrUtil.isNotBlank(detail.getUseUserId())) {
                    String useUserName = userIdToNameMap.get(detail.getUseUserId());
                    if (StrUtil.isNotBlank(useUserName)) {
                        detailDTO.setUseUserName(useUserName);
                    }
                }
                
                // 从映射中获取可退回数量
                String key = entity.getUserId() + "_" + detail.getUseUserId() + "_" + detail.getSkuId();
                SampleLedgerDTO.SkuAvailableQtyDTO skuAvailableQtyDTO = availableQtyMap.get(key);
                if (!Objects.isNull(skuAvailableQtyDTO)){
                    detailDTO.setAvailableQty(skuAvailableQtyDTO.getAvailableQty());
                    detailDTO.setSampleLedgerId(skuAvailableQtyDTO.getSampleLedgerId());
                }else {
                    detailDTO.setAvailableQty(0);
                }
                return detailDTO;
            })
            .collect(Collectors.toList());
        viewDTO.setDetailList(detailList);
        // 查询相关的附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(Arrays.asList(id));
        if(CollUtil.isNotEmpty(attachmentList)){
            // 分别提取附件名称和URL列表设置到返回对象中
            viewDTO.setAttachmentNameList(attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList()));
            viewDTO.setAttachmentUrlList(attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
        }


        return viewDTO;
    }

    @Override
    public void exportList(SampleBackInfoDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("样品退回单导出", EXPORT_WMS_SAMPLE_BACK_INFO_REPORT.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        return this.submit(id,ClientTypeEnum.WEB);
    }
    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(SampleBackInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SAMPLE_BACK_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SampleBackInfoDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SampleBackInfoDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        return approve(dto,ClientTypeEnum.WEB);
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SampleBackInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(SourceTypeEnum.SAMPLE_BACK_INFO.getCode());
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        return disApprove(id,ClientTypeEnum.WEB);
    }

    private Boolean validateDisApprove(SampleBackInfoEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        return delete(id,ClientTypeEnum.WEB);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        return this.invalid(id,remark,ClientTypeEnum.WEB);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        return this.cancelProcess(id,ClientTypeEnum.WEB);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SampleBackInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        
        // 审核通过后自动生成其他入库单
        if (ApproveStatusEnum.APPROVE.getStatus().equals(approveStatus.getStatus())) {
            try {
                generateOtherInboundOrder(entity);
                log.info("样品退回单审核通过，自动生成其他入库单成功，单据编号：{}", entity.getCode());
            } catch (Exception e) {
                log.error("样品退回单审核通过，自动生成其他入库单失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
                throw new ServiceException("样品退回单审核通过，自动生成其他入库单失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
            }
        }
        
        // todo 明细数据处理 上下游数据处理

        // 只有审核通过和反审核才记录台账流水
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (ApproveTypeEnum.PASS.equals(approveType) || ApproveTypeEnum.DIS_APPROVE.equals(approveType)) {
            // 记录台账流水
            try {
                SampleLedgerFlowDTO.AddFlowDTO flowDTO = buildFlow(entity.getId(), entity.getCode(), approveType);
                if (flowDTO != null) {
                    sampleLedgerFlowService.addSampleLedgerFlow(flowDTO);
                    log.info("样品退回单台账流水记录成功，单据编号：{}，审核类型：{}", entity.getCode(), approveType.getName());
                }
            } catch (Exception e) {
                log.error("样品退回单台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
                throw new ServiceException("样品退回单台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
            }
        }

        return Boolean.TRUE;
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        // 检查单据是否已作废
        SampleBackInfoEntity entity = super.getById(id);
        if (entity != null && InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException("已作废的样品退回单不支持审核操作");
        }
        
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SampleBackInfoEntity::getId, id)
            .set(SampleBackInfoEntity::getApproveUserId, userInfo.getUid())
            .set(SampleBackInfoEntity::getApproveUserName, userInfo.getUserName())
            .set(SampleBackInfoEntity::getApproveStatus, approveStatus)
            .set(SampleBackInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new SampleBackInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        // 检查单据是否已作废
        SampleBackInfoEntity entity = super.getById(id);
        if (entity != null && InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException("已作废的样品退回单不支持反审核操作");
        }
        
        this.lambdaUpdate().eq(SampleBackInfoEntity::getId, id)
            .set(SampleBackInfoEntity::getApproveUserId, "")
            .set(SampleBackInfoEntity::getApproveUserName, "")
            .set(SampleBackInfoEntity::getApproveStatus, approveStatus)
            .set(SampleBackInfoEntity::getApproveTime, null)
            .update(new SampleBackInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        // 检查单据是否已作废
        SampleBackInfoEntity entity = super.getById(id);
        if (entity != null && InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException("已作废的样品退回单不支持状态更新操作");
        }
        
        lambdaUpdate().eq(SampleBackInfoEntity::getId, id)
        .set(SampleBackInfoEntity::getApproveStatus, approveStatus)
        .update(new SampleBackInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SampleBackInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SAMPLE_BACK_INFO.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }

        // 获取所有仓库ID
        List<String> warehouseIds = list.stream()
                .map(SampleBackInfoDTO.ListDTO::getWarehouseId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询仓库信息
        Map<String, String> warehouseNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(warehouseIds)) {
            try {
                List<WarehouseEntity> warehouseList = warehouseService.lambdaQuery()
                        .in(WarehouseEntity::getId, warehouseIds)
                        .list();
                if (CollUtil.isNotEmpty(warehouseList)) {
                    warehouseNameMap = warehouseList.stream()
                            .collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));
                }
            } catch (Exception e) {
                log.warn("获取仓库信息失败，错误：{}", e.getMessage());
            }
        }

        // 属性赋值
        for(SampleBackInfoDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            
            // 设置仓库名称
            if (StrUtil.isNotBlank(data.getWarehouseId())) {
                String warehouseName = warehouseNameMap.get(data.getWarehouseId());
                data.setWarehouseName(warehouseName != null ? warehouseName : "");
            }

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                data.setApproveUserName(curApprove);
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SampleBackInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        
        // 校验明细SKU退回数量是否小于等于台账数量
        validateSampleBackQty(entity);
    }
    
    /**
     * 校验样品退回数量
     * @param entity 样品退回单实体
     */
    private void validateSampleBackQty(SampleBackInfoEntity entity) {
        // 获取样品退回单明细
        List<SampleBackDetailEntity> detailList = sampleBackDetailService.list(
            new LambdaQueryWrapper<SampleBackDetailEntity>()
                .eq(SampleBackDetailEntity::getMainId, entity.getId())
        );
        
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        
        // 查询台账可用数量
        SampleLedgerDTO.SearchDTO searchDTO = new SampleLedgerDTO.SearchDTO();
        searchDTO.setUserId(entity.getUserId());
        searchDTO.setType(SampleLedgerTypeEnum.BACK.getCode());
        searchDTO.setChildId(entity.getId()); // 排除当前单据
        searchDTO.setSkuIds(detailList.stream().map(SampleBackDetailEntity::getSkuId).collect(Collectors.toList()));
        
        List<SampleLedgerDTO.SkuAvailableQtyDTO> ledgerList = sampleLedgerService.listLedgerByUserId(searchDTO);
        
        // 构建SKU台账数量映射
        Map<String, Integer> skuLedgerQtyMap = ledgerList.stream()
            .collect(Collectors.toMap(
                SampleLedgerDTO.SkuAvailableQtyDTO::getSkuId,
                dto -> dto.getAvailableQty() != null ? dto.getAvailableQty() : 0,
                (existing, replacement) -> existing + replacement // 如果有重复SKU，累加数量
            ));
        
        // 校验每个明细的退回数量
        for (SampleBackDetailEntity detail : detailList) {
            String skuId = detail.getSkuId();
            Integer backQty = detail.getQty();
            Integer ledgerQty = skuLedgerQtyMap.getOrDefault(skuId, 0);
            
            if (backQty != null && backQty > ledgerQty) {
                throw new ServiceException(StrUtil.format("SKU【{}】退回数量不能大于台账数量", detail.getSkuNo()));
            }
        }
    }

    /**
     * 查询并回填部门名称和仓库名称
     */
    private void fillDeptAndWarehouseNames(SampleBackInfoEntity entity) {
        if (entity == null) {
            return;
        }

        // 查询部门名称
        if (StrUtil.isNotBlank(entity.getDeptId())) {
            try {
                SysDepartmentDTO department = sysUserFeign.getUserDeptById(entity.getDeptId());
                if (department != null && StrUtil.isNotBlank(department.getName())) {
                    entity.setDeptName(department.getName());
                }
            } catch (Exception e) {
                log.warn("查询部门名称失败，部门ID：{}，错误：{}", entity.getDeptId(), e.getMessage());
            }
        }

        // 查询仓库名称
        if (StrUtil.isNotBlank(entity.getWarehouseId())) {
            try {
                WarehouseEntity warehouse = warehouseService.getById(entity.getWarehouseId());
                if (warehouse != null && StrUtil.isNotBlank(warehouse.getName())) {
                    entity.setWarehouseName(warehouse.getName());
                }
            } catch (Exception e) {
                log.warn("查询仓库名称失败，仓库ID：{}，错误：{}", entity.getWarehouseId(), e.getMessage());
            }
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleBackInfoEntity sampleBackInfoEntity) {
        // 处理用户名
        if (StrUtil.isNotBlank(sampleBackInfoEntity.getUserId())) {
            try {
                List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Collections.singletonList(sampleBackInfoEntity.getUserId()));
                if (CollUtil.isNotEmpty(userList)) {
                    String userName = userList.stream()
                            .filter(user -> user.getUserId().equals(sampleBackInfoEntity.getUserId()))
                            .map(FindUserDTO::getUserName)
                            .findFirst()
                            .orElse("");
                    sampleBackInfoEntity.setUserName(userName);
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败，用户ID：{}，错误：{}", sampleBackInfoEntity.getUserId(), e.getMessage());
            }
        }

        // 处理组织名称
        if (StrUtil.isNotBlank(sampleBackInfoEntity.getOrgId())) {
            try {
                List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(sampleBackInfoEntity.getOrgId()));
                if (CollUtil.isNotEmpty(orgList)) {
                    String orgName = orgList.stream()
                            .filter(org -> org.getId().equals(sampleBackInfoEntity.getOrgId()))
                            .map(BaseIdDTO.CodeDTO::getName)
                            .findFirst()
                            .orElse("");
                    sampleBackInfoEntity.setOrgName(orgName);
                }
            } catch (Exception e) {
                log.warn("获取组织信息失败，组织ID：{}，错误：{}", sampleBackInfoEntity.getOrgId(), e.getMessage());
            }
        }
    }

    // ==================== 台账流水构建器实现 ====================

    @Override
    public String getSupportedSourceType() {
        return SourceTypeEnum.SAMPLE_BACK_INFO.getCode();
    }

    @Override
    public SampleLedgerFlowDTO.AddFlowDTO buildFlow(String sourceId, String sourceCode, ApproveTypeEnum approveType) {
        try {
            // 获取样品退回单主表信息
            SampleBackInfoEntity entity = this.getById(sourceId);
            if (entity == null) {
                log.error("获取样品退回单失败，sourceId：{}", sourceId);
                return null;
            }

            // 获取样品退回单明细
            List<SampleBackDetailEntity> detailList = sampleBackDetailService.list(new LambdaQueryWrapper<SampleBackDetailEntity>().eq(SampleBackDetailEntity::getMainId, sourceId));

            if (detailList.isEmpty()) {
                log.warn("样品退回单明细为空，sourceId：{}", sourceId);
                return null;
            }

            // 构建流水明细
            List<SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO> flowDetails = new ArrayList<>();
            for (SampleBackDetailEntity detail : detailList) {
                // 计算数量：审核为-X，反审核为+X
                Integer qty = calculateQty(detail.getQty(), approveType);
                
                // 通过sku_no查询sku_id

                SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO flowDetail = new SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO();
                flowDetail.setSourceDetailId(detail.getId());
                flowDetail.setSkuNo(detail.getSkuNo());
                flowDetail.setSkuId(detail.getSkuId());
                flowDetail.setProductName(detail.getProductName());
                flowDetail.setQty(qty);
                // 设置样品台账ID（如果有的话）
                // flowDetail.setSampleLedgerId(detail.getSampleLedgerId());
                flowDetails.add(flowDetail);
            }

            // 构建流水主表数据
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = new SampleLedgerFlowDTO.AddFlowDTO();
            flowDTO.setSourceType(getSupportedSourceType());
            flowDTO.setApproveType(approveType.getStatus());
            flowDTO.setOperateTime(LocalDateTime.now());
            flowDTO.setBillDate(entity.getBackDate());
            flowDTO.setSourceName("样品退回单");
            flowDTO.setSourceCode(sourceCode);
            flowDTO.setSourceId(sourceId);
            flowDTO.setUseUserId(entity.getUserId());
            flowDTO.setUseUserName(entity.getUserName());
            flowDTO.setUserId(entity.getUserId());
            flowDTO.setUserName(entity.getUserName());
            flowDTO.setDeptId(entity.getDeptId());
            // 根据部门ID查询部门名称
            flowDTO.setDeptName(getDeptNameById(entity.getDeptId()));
            flowDTO.setDetailList(flowDetails);

            return flowDTO;
        } catch (Exception e) {
            log.error("构建样品退回单台账流水失败，sourceId：{}，错误：{}", sourceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 计算数量：审核为-X，反审核为+X
     */
    @Override
    public Integer calculateQty(Integer originalQty, ApproveTypeEnum approveType) {
        if (originalQty == null) {
            return 0;
        }
        
        if (ApproveTypeEnum.PASS.equals(approveType)) {
            return -originalQty; // 审核：-X（减少库存）
        } else if (ApproveTypeEnum.DIS_APPROVE.equals(approveType)) {
            return originalQty; // 反审核：+X（增加库存）
        }
        
        return 0;
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

    /**
     * 根据样品退回单生成其他入库单
     * @param sampleBackInfo 样品退回单
     */
    private void generateOtherInboundOrder(SampleBackInfoEntity sampleBackInfo) {
        // 查询样品退回单明细
        List<SampleBackDetailEntity> detailList = sampleBackDetailService.listByMainId(sampleBackInfo.getId());
        if (CollUtil.isEmpty(detailList)) {
            log.warn("样品退回单明细为空，无法生成其他入库单，单据编号：{}", sampleBackInfo.getCode());
            return;
        }

        // 构建其他入库单明细列表
        List<OtherInstockDetailDTO.AddDTO> detailAddDTOList = new ArrayList<>();
        for (SampleBackDetailEntity detail : detailList) {
            OtherInstockDetailDTO.AddDTO detailAddDTO = new OtherInstockDetailDTO.AddDTO();
            detailAddDTO.setSkuId(detail.getSkuId());
            detailAddDTO.setSkuNo(detail.getSkuNo());
            detailAddDTO.setActualQty(detail.getQty());
            detailAddDTO.setRemark(detail.getRemark());
            detailAddDTO.setSourceDetailId(detail.getId());
            detailAddDTOList.add(detailAddDTO);
        }

        // 构建其他入库单主表DTO
        OtherInstockDTO.AddDTO addDTO = new OtherInstockDTO.AddDTO();
        addDTO.setBillDate(sampleBackInfo.getBackDate());
        addDTO.setInventoryDirection(InventoryDirectionEnum.ORDINARY.getCode());
        addDTO.setWarehouseId(sampleBackInfo.getWarehouseId());
        addDTO.setDeptId(sampleBackInfo.getDeptId());
        addDTO.setType(InstockTypeEnum.SAMPLE_BACK.getCode());
        addDTO.setRemark("样品退回单自动生成：" + sampleBackInfo.getCode());
        addDTO.setSourceType(SourceTypeEnum.SAMPLE_BACK_INFO.getCode());
        addDTO.setSourceId(sampleBackInfo.getId());
        addDTO.setSourceCode(sampleBackInfo.getCode());
        addDTO.setDetailList(detailAddDTOList);

        // 调用其他入库单服务的新增方法
        String otherInstockId = otherInstockService.add(addDTO);
        if (StrUtil.isBlank(otherInstockId)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        
        if (StrUtil.isNotBlank(otherInstockId)) {
            // 设置来源字段到其他入库单主表
            OtherInstockEntity otherInstock = otherInstockService.getById(otherInstockId);
            // 提交其他入库单
            otherInstockService.submit(otherInstockId, Boolean.FALSE);
            
            // 审核通过其他入库单
            BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
            baseApproveParamDTO.setIds(Collections.singletonList(otherInstockId));
            baseApproveParamDTO.setType(ApproveTypeEnum.PASS.getStatus());
            baseApproveParamDTO.setComment("样品退回单自动审核通过");
            otherInstockService.approve(otherInstockId, baseApproveParamDTO.getType(), baseApproveParamDTO.getComment(),false);

            log.info("样品退回单生成其他入库单并审核通过成功，样品退回单号：{}，其他入库单号：{}", 
                    sampleBackInfo.getCode(), otherInstock.getCode());
        }
    }

    /**
     * 同步反审核并删除关联的其他入库单
     * @param entity 样品退回单实体
     */
    private void handleAssociatedOtherInboundOrder(SampleBackInfoEntity entity) {
        // 查询关联的其他入库单（通过来源字段匹配）
        List<OtherInstockEntity> otherInstocks = otherInstockService.list(new LambdaQueryWrapper<OtherInstockEntity>()
                .eq(OtherInstockEntity::getSourceType, SourceTypeEnum.SAMPLE_BACK_INFO.getCode())
                .eq(OtherInstockEntity::getSourceId, entity.getId())
                .eq(OtherInstockEntity::getSourceCode, entity.getCode())
                .eq(OtherInstockEntity::getInvalidStatus, false));

        if (CollUtil.isNotEmpty(otherInstocks)) {
            log.info("找到关联的其他入库单，数量：{}，样品退回单号：{}", otherInstocks.size(), entity.getCode());
            
            // 先同步反审核其他入库单
            for (OtherInstockEntity otherInstock : otherInstocks) {
                try {
                    // 如果其他入库单是已审核状态，需要先反审核
                    if (ApproveStatusEnum.APPROVE.getStatus().equals(otherInstock.getApproveStatus())) {
                        log.info("开始反审核其他入库单，单号：{}，样品退回单号：{}", otherInstock.getCode(), entity.getCode());
                        
                        // 调用其他入库单的反审核方法
                        otherInstockService.disApprove(otherInstock.getId(),false);
                        
                        log.info("其他入库单反审核成功，单号：{}，样品退回单号：{}", otherInstock.getCode(), entity.getCode());
                    } else {
                        log.info("其他入库单状态为：{}，无需反审核，单号：{}，样品退回单号：{}", 
                                otherInstock.getApproveStatus(), otherInstock.getCode(), entity.getCode());
                    }
                } catch (Exception e) {
                    log.error("其他入库单反审核失败，单号：{}，样品退回单号：{}，错误：{}", 
                            otherInstock.getCode(), entity.getCode(), e.getMessage(), e);
                    throw new ServiceException("其他入库单反审核失败，单号：{}，样品退回单号：{}，错误：{}", 
                            otherInstock.getCode(), entity.getCode(), e.getMessage());
                }
            }
            
            // 反审核完成后，删除其他入库单主表
            List<String> otherInstockIds = otherInstocks.stream()
                    .map(OtherInstockEntity::getId)
                    .collect(Collectors.toList());
            otherInstockService.removeByIds(otherInstockIds);

            // 删除其他入库单明细
            List<String> otherInstockDetailIds = otherInstocks.stream()
                    .flatMap(otherInstock -> otherInstockDetailService.list(new LambdaQueryWrapper<OtherInstockDetailEntity>()
                            .eq(OtherInstockDetailEntity::getMainId, otherInstock.getId())).stream()
                            .map(OtherInstockDetailEntity::getId))
                    .collect(Collectors.toList());
            
            if (CollUtil.isNotEmpty(otherInstockDetailIds)) {
                otherInstockDetailService.removeByIds(otherInstockDetailIds);
            }
            
            log.info("成功删除关联的其他入库单，主表ID：{}，明细ID：{}，样品退回单号：{}", 
                    otherInstockIds, otherInstockDetailIds, entity.getCode());
        } else {
            log.info("未找到关联的其他入库单，样品退回单号：{}", entity.getCode());
        }
    }

    /**
     * 异步导入
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return
     */
    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        try {
            dto.setUserId(UserContext.getDefaultLoginUser().getUid());
            // 创建异步导入任务
            downloadTaskFeign.saveImportTask("样品退回单导入", IMPORT_WMS_SAMPLE_BACK_INFO.getCode(), dto);
            return true;
        } catch (Exception e) {
            log.error("创建样品退回单导入任务失败", e);
            return false;
        }
    }

    /**
     * 下载模板
     * @author wuhaotian
     * @date: 2025-08-21
     * @param response
     * @return
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/sampleBackInfoTemplate.xlsx";
        String standardExcelName = "sampleBackInfoTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
    }

    /**
     * 导入样品退回单
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importSampleBackInfo(BaseDTO.ImportDTO dto) {
        // SKU信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> map = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, e -> e, (o1, o2) -> o1));
        // 用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        // 部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        //设置操作人
        FindUserDTO findUserDTO = userList.stream().filter(e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if(Objects.nonNull(findUserDTO)){
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }
        SampleBackInfoExcelListener excelListenerUtil = new SampleBackInfoExcelListener(dto.getTaskId(), dto.getImportType(), dto.getImportCount(), deptList, map, userList);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), SampleBackInfoImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<SampleBackInfoImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "样品退回单错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SampleBackInfoImportExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    @Override
    public void handleImportSuccessList(List<SampleBackInfoImportExcelDTO> successList, List<String> errorNoList, List<SampleBackInfoImportExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        if (StringUtils.isBlank(importType)){
            //给个默认值
            importType=ImportTypeEnum.ADD.getCode();
        }

        if (CollUtil.isNotEmpty(errorNoList)) {
            successList = successList.stream().filter(e -> StringUtils.isNotBlank(e.getNo()) && !errorNoList.contains(e.getNo())).collect(Collectors.toList());

            // 全部返回到错误列表
            List<SampleBackInfoImportExcelDTO> collect = successList.stream().filter(e -> StringUtils.isBlank(e.getNo()) || errorNoList.contains(e.getNo())).collect(Collectors.toList());
            errorList2.addAll(collect);
        }

        SampleBackInfoServiceImpl bean = ApplicationContextUtils.getBean(SampleBackInfoServiceImpl.class);

        // 按序号分组
        Map<String, List<SampleBackInfoImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(SampleBackInfoImportExcelDTO::getNo));
        for (Map.Entry<String, List<SampleBackInfoImportExcelDTO>> entry : collect.entrySet()) {
            List<SampleBackInfoImportExcelDTO> value = entry.getValue();
            SampleBackInfoImportExcelDTO importMainDTO = value.get(0);
            SampleBackInfoDTO.AddDTO addDTO = new SampleBackInfoDTO.AddDTO();
            BeanMapperUtils.copy(importMainDTO, addDTO);
            List<SampleBackDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (SampleBackInfoImportExcelDTO importDTO : value) {
                SampleBackDetailDTO.AddDTO detailDTO = new SampleBackDetailDTO.AddDTO();
                BeanMapperUtils.copy(importDTO, detailDTO);
                // 明细备注
                detailDTO.setRemark(importDTO.getDetailRemark());
                detailList.add(detailDTO);
            }
            addDTO.setDetailList(detailList);

            if (ImportTypeEnum.ADD.getCode().equals(importType)) {
                bean.add(addDTO);
            }
        }
    }

    /**
     * 获取样品退回单分页数据（用于异步导出）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return
     */
    @Override
    public PagingVO<SampleBackInfoDTO.ListDTO> getSampleBackInfoPageData(PagingDTO<SampleBackInfoDTO.ExportDTO> dto) {
        Page<SampleBackInfoDTO.ExportDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SampleBackInfoDTO.ListDTO> pageData = this.baseMapper.listExport(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    // ========== APP端专用方法实现 ==========

    @Override
    public List<SampleBackInfoDTO.TabListDTO> tabListApp(PermissionsDTO dto) {
        SampleBackInfoDTO.PagingParamDTO searchParam = new SampleBackInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(dto.getPermissionSql());
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        // 使用一个SQL查询获取所有状态的统计数量
        List<SampleBackInfoDTO.TabListDTO> list = baseMapper.getAllStatusCounts(dto.getPermissionSql());

        // 移动端特殊处理：合并待提交和不通过
        List<SampleBackInfoDTO.TabListDTO> appList = new ArrayList<>();
        
        // 计算待提交/不通过的总数
        int waitSubmitCount = 0;
        int rejectCount = 0;
        SampleBackInfoDTO.TabListDTO waitSubmitItem = null;
        SampleBackInfoDTO.TabListDTO rejectItem = null;
        
        for (SampleBackInfoDTO.TabListDTO item : list) {
            if ("waitSubmit".equals(item.getTabFlag())) {
                waitSubmitCount = item.getCount();
                waitSubmitItem = item;
            } else if ("reject".equals(item.getTabFlag())) {
                rejectCount = item.getCount();
                rejectItem = item;
            }
        }
        
        // 创建合并后的待提交/不通过标签
        if (waitSubmitItem != null || rejectItem != null) {
            SampleBackInfoDTO.TabListDTO mergedItem = new SampleBackInfoDTO.TabListDTO();
            mergedItem.setTabFlag("waitSubmitOrReject");
            mergedItem.setTabFlagName("待提交/不通过");
            mergedItem.setCount(waitSubmitCount + rejectCount);
            appList.add(mergedItem);
        }
        
        // 添加其他标签（审核中、已审核）
        for (SampleBackInfoDTO.TabListDTO item : list) {
            if (!"waitSubmit".equals(item.getTabFlag()) && !"reject".equals(item.getTabFlag())) {
                // 设置正确的标签名称
                if ("approveIng".equals(item.getTabFlag())) {
                    item.setTabFlagName("审核中");
                } else if ("approve".equals(item.getTabFlag())) {
                    item.setTabFlagName("已审核");
                } else {
                    item.setTabFlagName(ApproveStatusEnum.getName(item.getTabFlag()));
                }
                appList.add(item);
            }
        }

        // 按照移动端指定顺序排序：待提交/不通过、审核中、已审核
        List<String> orderList = Arrays.asList("waitSubmitOrReject", "approveIng", "approve");
        appList.sort((a, b) -> {
            int indexA = orderList.indexOf(a.getTabFlag());
            int indexB = orderList.indexOf(b.getTabFlag());
            if (indexA == -1) indexA = Integer.MAX_VALUE;
            if (indexB == -1) indexB = Integer.MAX_VALUE;
            return Integer.compare(indexA, indexB);
        });

        return appList;
    }

    @Override
    public PagingVO<SampleBackInfoDTO.ListDTO> pagingApp(PagingDTO<SampleBackInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleBackInfoDTO.ListDTO> pageData = this.baseMapper.pagingApp(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(String id, ClientTypeEnum clientType) {
        SampleBackInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到样品退回单数据");
        }

        // 检查单据是否已作废
        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException("已作废的样品退回单不支持提交操作");
        }

        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改样品退回单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动样品退回单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录样品退回单日志数据，id：【{}】", id);
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto, ClientTypeEnum clientType) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SampleBackInfoEntity entity = getById(dto.getId());

        // 检查单据是否已作废
        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException("已作废的样品退回单不支持审核操作");
        }

        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(String id, ClientTypeEnum clientType) {
        SampleBackInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品退回单单数据"));

        // 检查单据是否已作废
        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException("已作废的样品退回单不支持反审核操作");
        }

        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 同步反审核并删除关联的其他入库单
        try {
            handleAssociatedOtherInboundOrder(entity);
            log.info("样品退回单反审核，同步处理关联其他入库单成功，单据编号：{}", entity.getCode());
        } catch (Exception e) {
            log.error("样品退回单反审核，同步处理关联其他入库单失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
            throw new ServiceException("样品退回单反审核，同步处理关联其他入库单失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
        }

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 记录台账流水（反审核）
        try {
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = buildFlow(entity.getId(), entity.getCode(), ApproveTypeEnum.DIS_APPROVE);
            if (flowDTO != null) {
                sampleLedgerFlowService.addSampleLedgerFlow(flowDTO);
                log.info("样品退回单反审核台账流水记录成功，单据编号：{}", entity.getCode());
            }
        } catch (Exception e) {
            log.error("样品退回单反审核台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
            throw new ServiceException("样品退回单反审核台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
        }

        // 操作日志
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id, ClientTypeEnum clientType) {
        SampleBackInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品退回单数据"));

        // 检查单据是否已作废
        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException("已作废的样品退回单不支持删除操作");
        }

        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）
//删除附件
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(Arrays.asList(id));
        if(CollUtil.isNotEmpty(attachmentList)){
            List<String> urlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            attachmentService.deleteByUrlList(urlList);
        }
        // 删除主单数据
        log.info("删除 开始删除样品退回单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除样品退回单日志数据，id：【{}】", id);
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getCode(), "删除样品退回单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO invalid(String id, String remark, ClientTypeEnum clientType) {
        SampleBackInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品退回单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus().getStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改样品退回单状态数据，id：【{}】", id);
        lambdaUpdate().eq(SampleBackInfoEntity::getId, id)
                .set(SampleBackInfoEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(SampleBackInfoEntity::getInvalidRemark, remark)
                .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(String id, ClientTypeEnum clientType) {
        SampleBackInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品退回单数据"));

        // 检查单据是否已作废
        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException("已作废的样品退回单不支持撤销操作");
        }

        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改样品退回单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SAMPLE_BACK_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    /**
     * 根据归属人、使用方和SKU查询sample_ledger_id
     * @param userId 归属人ID
     * @param useUserId 使用方ID
     * @param skuId SKU ID
     * @return sample_ledger_id
     */
    private String getSampleLedgerId(String userId, String useUserId, String skuId) {
        try {
            if (StrUtil.isBlank(userId) || StrUtil.isBlank(useUserId) || StrUtil.isBlank(skuId)) {
                return null;
            }
            
            // 直接查询样品台账表，根据归属人、使用方和SKU查询sample_ledger_id
            List<SampleLedgerEntity> sampleLedgerList = sampleLedgerService.lambdaQuery()
                .eq(SampleLedgerEntity::getUserId, userId)
                .eq(SampleLedgerEntity::getUseUserId, useUserId)
                .eq(SampleLedgerEntity::getSkuId, skuId)
                .list();
            
            if (CollUtil.isNotEmpty(sampleLedgerList)) {
                // 如果存在多条记录，返回第一个（或者可以根据业务需求选择特定的记录）
                SampleLedgerEntity sampleLedger = sampleLedgerList.get(0);
                log.debug("找到{}条样品台账记录，返回第一条，userId：{}，useUserId：{}，skuId：{}", 
                    sampleLedgerList.size(), userId, useUserId, skuId);
                return sampleLedger.getId();
            }
            
            log.warn("未找到对应的样品台账，userId：{}，useUserId：{}，skuId：{}", userId, useUserId, skuId);
            return null;
        } catch (Exception e) {
            log.error("查询样品台账ID失败，userId：{}，useUserId：{}，skuId：{}，错误：{}", userId, useUserId, skuId, e.getMessage(), e);
            return null;
        }
    }

}
