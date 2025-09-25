package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.*;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.validator.ValidList;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SampleLedgerTypeEnum;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.wms.mapper.SampleReturnInfoMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.SampleReturnInfoDTO;
import com.erp.model.wms.entity.SampleReturnDetailEntity;
import com.erp.model.wms.entity.SampleReturnInfoEntity;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SampleLedgerFlowBuilder;
import com.erp.server.wms.service.SampleReturnDetailService;
import com.erp.server.wms.service.SampleReturnInfoService;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_RETURN_INFO;

/**
 * <p>
 * 样品归还单主表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SampleReturnInfoServiceImpl extends SuperServiceImpl<SampleReturnInfoMapper, SampleReturnInfoEntity> implements SampleReturnInfoService , SampleLedgerFlowBuilder {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private SampleReturnDetailService sampleReturnDetailService;
    @Autowired
    private SampleBorrowInfoService sampleBorrowInfoService;
    @Autowired
    private SampleLedgerFlowService sampleLedgerFlowService;
    @Autowired
    private WmsAttachmentService attachmentService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private FileFeign fileFeign;
    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private CfgQueryOptionFeign cfgQueryOptionFeign;
    @Autowired
    private SampleBorrowDetailService sampleBorrowDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleReturnInfoDTO.AddDTO addDTO) {
        SampleReturnInfoEntity sampleReturnInfoEntity = new SampleReturnInfoEntity();
        BeanMapperUtils.copy(addDTO, sampleReturnInfoEntity);

        // 数据处理
        handleData(sampleReturnInfoEntity);

        log.info("开始新增样品归还单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YPGH);
        sampleReturnInfoEntity.setCode(code);
        boolean save = super.save(sampleReturnInfoEntity);
        if(!save) {
            throw new ServiceException("样品归还单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format(addDTO.getClientType().getName()+"用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品归还单" , sampleReturnInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg,  ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), sampleReturnInfoEntity.getId(), "新增操作");
        //明细
        List<SampleReturnDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        //不允许重复添加
        long sourceDetailIdCount = detailList.stream().map(SampleReturnDetailDTO.AddDTO::getSourceDetailId).distinct().count();
        if(sourceDetailIdCount != detailList.size()){
            throw new ServiceException(ApiError.ERROR_REPEAT_SKU);
        }
        List<SampleReturnDetailEntity> sampleReturnDetailEntities = BeanMapperUtils.copyList(SampleReturnDetailEntity.class, detailList);
        List<String> skuIds = sampleReturnDetailEntities.stream().map(SampleReturnDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //sku信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));
        for (SampleReturnDetailEntity sampleReturnDetailEntity : sampleReturnDetailEntities) {
            sampleReturnDetailEntity.setMainId(sampleReturnInfoEntity.getId());

            SkuVO skuVO = skuMap.getOrDefault(sampleReturnDetailEntity.getSkuId(), null);
            if(Objects.nonNull(skuVO)){
                sampleReturnDetailEntity.setSkuNo(skuVO.getSkuNo());
                sampleReturnDetailEntity.setProductName(skuVO.getSkuName());
            }
        }
        checkDetailQty("",sampleReturnDetailEntities);

        sampleReturnDetailService.saveBatch(sampleReturnDetailEntities);

        //附件
        addAttachment(addDTO, sampleReturnInfoEntity);

        return new BaseResultDTO.AddDTO(sampleReturnInfoEntity.getId(), code);
    }

    private void checkDetailQty(String id,List<SampleReturnDetailEntity> sampleReturnDetailEntities) {
        List<String> sourceDetailIds = sampleReturnDetailEntities.stream().map(SampleReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SampleBorrowInfoDTO.SampleReturnView> sampleReturnViews = sampleBorrowInfoService.listSampleReturnView(id,sourceDetailIds);
        Map<String, SampleBorrowInfoDTO.SampleReturnView> viewMap = sampleReturnViews.stream().collect(Collectors.toMap(SampleBorrowInfoDTO.SampleReturnView::getSourceDetailId, Function.identity(), (o1, o2) -> o1));
        for (SampleReturnDetailEntity sampleReturnDetailEntity : sampleReturnDetailEntities) {
            String sourceDetailId = sampleReturnDetailEntity.getSourceDetailId();
            SampleBorrowInfoDTO.SampleReturnView sampleReturnView = viewMap.getOrDefault(sourceDetailId, null);
            if(Objects.isNull(sampleReturnView)){
                throw new ServiceException(ApiError.ERROR_SAMPLE_RETURN_QTY_NOT_EXIST,sampleReturnDetailEntity.getSkuNo());
            }

            Integer returnQty = Objects.isNull(sampleReturnDetailEntity.getReturnQty()) ? 0 :sampleReturnDetailEntity.getReturnQty();
            Integer canReturnQty =Objects.isNull(sampleReturnView.getCanReturnQty()) ? 0 :sampleReturnView.getCanReturnQty();
            if(canReturnQty < returnQty){
                throw new ServiceException(ApiError.ERROR_SAMPLE_RETURN_QTY_NOT_ENOUGH,sampleReturnDetailEntity.getSkuNo(),returnQty,canReturnQty);
            }
        }
    }

    /**
     * 添加附件信息
     * @param addDTO 包含附件URL和名称列表的数据传输对象
     * @param sampleReturnInfoEntity
     */
    private void addAttachment(SampleReturnInfoDTO.AddDTO addDTO, SampleReturnInfoEntity sampleReturnInfoEntity) {
        //附件集合
        List<String> attachmentUrlList = addDTO.getAttachmentUrlList();
        //附件名
        List<String> attachmentNameList = addDTO.getAttachmentNameList();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<SampleReturnInfoEntity> credentialClass = SampleReturnInfoEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                attachment.setAttachUrl(attachmentUrlList.get(i));
                attachment.setAttachName(attachmentNameList.get(i));
                attachment.setBusinessId(sampleReturnInfoEntity.getId());
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
    public Boolean update(SampleReturnInfoDTO.UpdateDTO addOrUpdateDTO) {
        SampleReturnInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品归还单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SampleReturnInfoEntity sampleReturnInfoEntity =  BeanMapperUtils.map(SampleReturnInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleReturnInfoEntity);
        log.info("编辑 开始修改样品归还单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(sampleReturnInfoEntity);
        if(!save) {
            throw new ServiceException("样品归还单保存失败");
        }

        // 记录操作日志
        log.info("编辑 开始记录样品归还单日志数据，单号：【{}】", old.getCode());
        String msg = StrUtil.format(addOrUpdateDTO.getClientType().getName()+"用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "样品归还单");
        operateLogService.addModuleOperateLogByObj(old, sampleReturnInfoEntity, ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), sampleReturnInfoEntity.getId(), msg);

        //明细
        updateDetail(addOrUpdateDTO, sampleReturnInfoEntity);

        //附件
        updateAttachment(addOrUpdateDTO, old);

        return Boolean.TRUE;
    }



    private void updateDetail(SampleReturnInfoDTO.UpdateDTO addOrUpdateDTO, SampleReturnInfoEntity sampleReturnInfoEntity) {
        List<SampleReturnDetailDTO.UpdateDTO> detailList = addOrUpdateDTO.getDetailList();
        //不允许重复添加
        long sourceDetailIdCount = detailList.stream().map(SampleReturnDetailDTO.UpdateDTO::getSourceDetailId).distinct().count();
        if(sourceDetailIdCount != detailList.size()){
            throw new ServiceException(ApiError.ERROR_REPEAT_SKU);
        }
        List<SampleReturnDetailEntity> oldList = sampleReturnDetailService.listByMainId(sampleReturnInfoEntity.getId());

        List<SampleReturnDetailEntity> sampleReturnDetailEntities = BeanMapperUtils.copyList(SampleReturnDetailEntity.class, detailList);
        // 提取所有SKU编号，用于后续查询可用数量
        List<String> skuIds = sampleReturnDetailEntities.stream().map(SampleReturnDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //sku信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));
        for (SampleReturnDetailEntity sampleReturnDetailEntity : sampleReturnDetailEntities) {
            sampleReturnDetailEntity.setMainId(sampleReturnInfoEntity.getId());

            SkuVO skuVO = skuMap.getOrDefault(sampleReturnDetailEntity.getSkuId(), null);
            if(Objects.nonNull(skuVO)){
                sampleReturnDetailEntity.setSkuNo(skuVO.getSkuNo());
                sampleReturnDetailEntity.setProductName(skuVO.getSkuName());
            }
        }

        checkDetailQty(sampleReturnInfoEntity.getId(),sampleReturnDetailEntities);

        if(CollUtil.isNotEmpty(oldList)){
            List<String> detailIds = detailList.stream().map(SampleReturnDetailDTO.UpdateDTO::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            // 处理删除的数据
            List<SampleReturnDetailEntity> remove = oldList.stream()
                    .filter(oldEntity -> !detailIds.contains(oldEntity.getId()))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(remove)){
                sampleReturnDetailService.removeByIds(remove.stream().map(SampleReturnDetailEntity::getId).collect(Collectors.toList()));
                //添加日志
                List<Pair<String, String>> removePairList = remove.stream().map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog(addOrUpdateDTO.getClientType().getName()+"删除SKU【%s】", ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), removePairList, "编辑操作");
            }
        }
        //处理需要新增的数据
        List<SampleReturnDetailEntity> addList = sampleReturnDetailEntities.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(addList)){
            sampleReturnDetailService.saveBatch(addList);

            //添加日志
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(addOrUpdateDTO.getClientType().getName()+"添加SKU【%s】", ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), addPairList, "编辑操作");
        }
        //处理需要更新的数据
        List<SampleReturnDetailEntity> updateList = sampleReturnDetailEntities.stream().filter(e -> StringUtils.isNotBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(updateList)){
            sampleReturnDetailService.updateBatchById(updateList);
            //添加日志
            for (SampleReturnDetailEntity sampleReturnDetailEntity : updateList) {
                SampleReturnDetailEntity oldDetail = oldList.stream().filter(e -> Objects.equals(e.getId(), sampleReturnDetailEntity.getId())).findFirst().orElse(null);
                if(Objects.nonNull(oldDetail)){
                    operateLogService.addModuleOperateLogByObj(oldDetail, sampleReturnDetailEntity, ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), sampleReturnInfoEntity.getId(), String.format(addOrUpdateDTO.getClientType().getName()+"编辑SKU【%s】",oldDetail.getSkuNo()));
                }
            }
        }
    }

    private void updateAttachment(SampleReturnInfoDTO.UpdateDTO addOrUpdateDTO, SampleReturnInfoEntity old) {
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
                Class<SampleReturnInfoEntity> credentialClass = SampleReturnInfoEntity.class;
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
    public PagingVO<SampleReturnInfoDTO.ListDTO> paging(PagingDTO<SampleReturnInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleReturnInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SampleReturnInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        SampleReturnInfoDTO.PagingParamDTO searchParam = new SampleReturnInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SampleReturnInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SampleReturnInfoDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new SampleReturnInfoDTO.TabListDTO(status,ApproveStatusEnum.getName(status), 0));
            }
        });
        list.stream().forEach(e ->{
            e.setTabFlagName(ApproveStatusEnum.getName(e.getTabFlag()));
        });
        // 修改为按照 ApproveStatusEnum 枚举声明顺序排序
        list.sort(Comparator.comparingInt(tabDto -> {
            ApproveStatusEnum statusEnum = ApproveStatusEnum.getByStatus(tabDto.getTabFlag());
            return statusEnum != null ? statusEnum.ordinal() : Integer.MAX_VALUE;
        }));
        return list;
    }

    @Override
    public List<SampleReturnInfoDTO.TabListDTO> tabListApp(PermissionsDTO param) {
        List<SampleReturnInfoDTO.TabListDTO> tabListDTOS = tabList(param);

        Map<String, Integer> map = tabListDTOS.stream().collect(Collectors.toMap(SampleReturnInfoDTO.TabListDTO::getTabFlag, SampleReturnInfoDTO.TabListDTO::getCount));

        List<SampleReturnInfoDTO.TabListDTO> list = new ArrayList<>();
        list.add(new SampleReturnInfoDTO.TabListDTO(ApproveStatusEnum.WAIT_SUBMIT.getCode()+"/"+ApproveStatusEnum.REJECT.getCode(), "待提交/不通过" ,map.get(ApproveStatusEnum.WAIT_SUBMIT.getCode()) + map.get(ApproveStatusEnum.REJECT.getCode()) ));
        list.add(new SampleReturnInfoDTO.TabListDTO(ApproveStatusEnum.APPROVE_ING.getCode(), "审核中" , map.get(ApproveStatusEnum.APPROVE_ING.getCode())));
        list.add(new SampleReturnInfoDTO.TabListDTO(ApproveStatusEnum.APPROVE.getCode(), "待归还" , map.get(ApproveStatusEnum.APPROVE.getCode())));
        return list;
    }

    @Override
    public PagingVO<SampleReturnInfoDTO.ListDTO> pagingApp(PagingDTO<SampleReturnInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleReturnInfoDTO.ListDTO> pageData = this.baseMapper.pagingApp(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(SampleReturnInfoDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("样品归还单导出", EXPORT_WMS_SAMPLE_RETURN_INFO.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id, ClientTypeEnum clientType) {
        SampleReturnInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到样品归还单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改样品归还单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动样品归还单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品归还单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SampleReturnInfoDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId(),dto.getClientType());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SampleReturnInfoDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId(),dto.getClientType());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto, ClientTypeEnum clientType) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SampleReturnInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品归还单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SampleReturnInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SAMPLE_RETURN_INFO.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
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
    public BatchResultDTO disApprove(String id, ClientTypeEnum clientType) {
        SampleReturnInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品归还单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());


        //回写借用单sku明细的待归还数量
        writeBackSampleBorrowInfo(entity.getId(),ApproveStatusEnum.WAIT_SUBMIT);

        // 记录台账流水（反审核）- 归还人
        try {
            SampleLedgerFlowDTO.AddFlowDTO returnFlowDTO = buildFlow(entity.getId(), entity.getCode(), ApproveTypeEnum.DIS_APPROVE);
            if (returnFlowDTO != null) {
                sampleLedgerFlowService.addSampleLedgerFlow(returnFlowDTO);
                log.info("样品归还单反审核归还人台账流水记录成功，单据编号：{}", entity.getCode());
            }
        } catch (Exception e) {
            log.error("样品归还单反审核归还人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
            throw new ServiceException("样品归还单反审核归还人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
        }
        
        // 记录台账流水（反审核）- 接收人
        try {
            SampleLedgerFlowDTO.AddFlowDTO receiverFlowDTO = buildReceiverFlow(entity.getId(), entity.getCode(), ApproveTypeEnum.DIS_APPROVE);
            if (receiverFlowDTO != null) {
                sampleLedgerFlowService.addSampleLedgerFlow(receiverFlowDTO);
                log.info("样品归还单反审核接收人台账流水记录成功，单据编号：{}", entity.getCode());
            }
        } catch (Exception e) {
            log.error("样品归还单反审核接收人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
            throw new ServiceException("样品归还单反审核接收人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
        }

        // 操作日志
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品归还单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SampleReturnInfoEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id, ClientTypeEnum clientType) {
        SampleReturnInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品归还单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除数据
        log.info("删除 开始删除样品归还单数据，id：【{}】", id);
        super.removeById(id);

        //删除明细
        sampleReturnDetailService.lambdaUpdate()
                .set(SampleReturnDetailEntity::getIsDeleted, Boolean.TRUE)
                .eq(SampleReturnDetailEntity::getMainId, id)
                .update();
        //删除附件
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(Arrays.asList(id));
        if(CollUtil.isNotEmpty(attachmentList)){
            List<String> urlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            attachmentService.deleteByUrlList(urlList);
        }
        // 删除日志数据
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品归还单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), entity.getId(), "删除样品归还单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id,String remark, ClientTypeEnum clientType) {
        SampleReturnInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品归还单数据"));
        // 只有待提交、审核不通过数据允许作废
        if (!(Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus()) || Objects.equals(ApproveStatusEnum.REJECT, entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        //已作废不支持作废
        if(entity.getInvalidStatus()){
            throw new ServiceException(ApiError.ERROR_98012);
        }

        log.info("作废 开始作废样品归还单数据，id：【{}】", id);
        lambdaUpdate().set(SampleReturnInfoEntity::getInvalidStatus,  Boolean.TRUE)
                .set(SampleReturnInfoEntity::getInvalidRemark, remark)
                .eq(SampleReturnInfoEntity::getId, id)
                .update();
        // 日志数据
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据作废操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品归还单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), entity.getId(),  OperationTypeEnum.INVALID.getName());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id, ClientTypeEnum clientType) {
        SampleReturnInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品归还单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改样品归还单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format(clientType.getName()+"用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品归还单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SAMPLE_RETURN_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SampleReturnInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        if(Objects.equals(approveStatus,ApproveStatusEnum.APPROVE)){
            //回写借用单sku明细的待归还数量
            writeBackSampleBorrowInfo(entity.getId(),approveStatus);
        }

        // 只有审核通过和反审核才记录台账流水
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (ApproveTypeEnum.PASS.equals(approveType) || ApproveTypeEnum.DIS_APPROVE.equals(approveType)) {
            // 记录台账流水 - 归还人
            try {
                SampleLedgerFlowDTO.AddFlowDTO returnFlowDTO = buildFlow(entity.getId(), entity.getCode(), approveType);
                if (returnFlowDTO != null) {
                    sampleLedgerFlowService.addSampleLedgerFlow(returnFlowDTO);
                    log.info("样品归还单归还人台账流水记录成功，单据编号：{}，审核类型：{}", entity.getCode(), approveType.getName());
                }
            } catch (Exception e) {
                log.error("样品归还单归还人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
                throw new ServiceException("样品归还单归还人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
            }
            
            // 记录台账流水 - 接收人
            try {
                SampleLedgerFlowDTO.AddFlowDTO receiverFlowDTO = buildReceiverFlow(entity.getId(), entity.getCode(), approveType);
                if (receiverFlowDTO != null) {
                    sampleLedgerFlowService.addSampleLedgerFlow(receiverFlowDTO);
                    log.info("样品归还单接收人台账流水记录成功，单据编号：{}，审核类型：{}", entity.getCode(), approveType.getName());
                }
            } catch (Exception e) {
                log.error("样品归还单接收人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
                throw new ServiceException("样品归还单接收人台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
            }
        }
        return Boolean.TRUE;
    }

    private void writeBackSampleBorrowInfo(String id,ApproveStatusEnum approveStatus) {
        List<SampleReturnDetailEntity> sampleReturnDetailEntities = sampleReturnDetailService.listByMainId(id);
        Map<String, Integer> returnQtySumBySourceDetailId = sampleReturnDetailEntities.stream()
                .collect(Collectors.groupingBy(
                        SampleReturnDetailEntity::getSourceDetailId,
                        Collectors.summingInt(detail -> Optional.ofNullable(detail.getReturnQty()).orElse(0))
                ));

        List<String> sourceDetailIds = sampleReturnDetailEntities.stream().map(SampleReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());

        List<SampleBorrowDetailEntity> sampleBorrowDetailEntities = sampleBorrowDetailService.lambdaQuery().in(SampleBorrowDetailEntity::getId, sourceDetailIds).list();

        for (SampleBorrowDetailEntity sampleBorrowDetailEntity : sampleBorrowDetailEntities) {
            Integer returnQty = returnQtySumBySourceDetailId.getOrDefault(sampleBorrowDetailEntity.getId(), 0);
            int waitReturnQty = Objects.isNull(sampleBorrowDetailEntity.getWaitReturnQty()) ? 0 : sampleBorrowDetailEntity.getWaitReturnQty();
            if(Objects.equals(approveStatus,ApproveStatusEnum.APPROVE)){
                sampleBorrowDetailEntity.setWaitReturnQty(waitReturnQty - returnQty);
            }else {
                sampleBorrowDetailEntity.setWaitReturnQty(waitReturnQty + returnQty);
            }
        }
        sampleBorrowDetailService.updateBatchById(sampleBorrowDetailEntities);
    }

    @Override
    public SampleReturnInfoDTO.ViewDTO view(String id) {
        SampleReturnInfoEntity sampleReturnInfoEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到样品归还单数据"));
        SampleReturnInfoDTO.ViewDTO data = BeanMapperUtils.map(SampleReturnInfoDTO.ViewDTO.class, sampleReturnInfoEntity);
        data.setApproveStatus(sampleReturnInfoEntity.getApproveStatus().getStatus());
        // 数据填充处理
        fillOne(data);
        // 获取归还单明细列表，并查询对应的借用信息
        List<SampleReturnDetailEntity> sampleReturnDetailEntities = sampleReturnDetailService.listByMainId(id);
        List<String> sourceDetailIds = sampleReturnDetailEntities.stream().map(SampleReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SampleBorrowInfoDTO.SampleReturnView> sampleReturnViews = sampleBorrowInfoService.listSampleReturnView("",sourceDetailIds);
        Map<String, SampleBorrowInfoDTO.SampleReturnView> viewMap = sampleReturnViews.stream().collect(Collectors.toMap(SampleBorrowInfoDTO.SampleReturnView::getSourceDetailId, Function.identity(), (o1, o2) -> o1));
        // 转换明细数据并填充借用相关信息
        List<SampleReturnDetailDTO.ViewDTO> viewDTOS = BeanMapperUtils.copyList(SampleReturnDetailDTO.ViewDTO.class, sampleReturnDetailEntities);
        for (SampleReturnDetailDTO.ViewDTO viewDTO : viewDTOS) {
            SampleBorrowInfoDTO.SampleReturnView sampleReturnView = viewMap.getOrDefault(viewDTO.getSourceDetailId(), null);
            if(Objects.nonNull(sampleReturnView)){
                viewDTO.setWaitReturnQty(sampleReturnView.getWaitReturnQty());
                viewDTO.setReturnedQty(sampleReturnView.getReturnedQty());
                viewDTO.setCanReturnQty(sampleReturnView.getCanReturnQty());
                viewDTO.setUseUserId(sampleReturnView.getUseUserId());
                viewDTO.setUseUserName(sampleReturnView.getUseUserName());
            }
        }
        data.setDetailList(viewDTOS);
        // 查询相关的附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(Arrays.asList(id));
        if(CollUtil.isNotEmpty(attachmentList)){
            // 分别提取附件名称和URL列表设置到返回对象中
            data.setAttachmentNameList(attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList()));
            data.setAttachmentUrlList(attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
        }
        return data;
    }

    @Override
    public List<SampleReturnInfoDTO.ListDTO> listReturnBySourceId(String sourceId) {
        List<SampleReturnInfoDTO.ListDTO> listDTOS = baseMapper.listReturnBySourceId(sourceId);
        listDTOS.forEach(e -> {
            e.setApproveStatusName(ApproveStatusEnum.getName(e.getApproveStatus()));
            e.setInvalidStatusName(InvalidStatusEnum.getName(e.getInvalidStatus()));
        });
        return listDTOS;
    }


    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(SampleReturnInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SAMPLE_RETURN_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    /**
     * 根据样品归还信息实体获取变量映射表
     *
     * @param entity 样品借用信息实体对象，用于提取业务变量数据
     * @return 返回根据业务键获取的变量映射表，包含业务相关的配置变量
     */
    private Map<String,Object> getVariablesMap(SampleReturnInfoEntity entity){
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.SAMPLE_RETURN_INFO.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> map = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);
        return map;
    }

    private void fillOne(SampleReturnInfoDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SampleReturnInfoEntity::getId, id)
            .set(SampleReturnInfoEntity::getApproveUserId, userInfo.getUid())
            .set(SampleReturnInfoEntity::getApproveUserName, userInfo.getUserName())
            .set(SampleReturnInfoEntity::getApproveStatus, approveStatus)
            .set(SampleReturnInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new SampleReturnInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SampleReturnInfoEntity::getId, id)
            .set(SampleReturnInfoEntity::getApproveUserId, "")
            .set(SampleReturnInfoEntity::getApproveUserName, "")
            .set(SampleReturnInfoEntity::getApproveStatus, approveStatus)
            .set(SampleReturnInfoEntity::getApproveTime, null)
            .update(new SampleReturnInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SampleReturnInfoEntity::getId, id)
                .set(SampleReturnInfoEntity::getApproveUserId, "")
                .set(SampleReturnInfoEntity::getApproveUserName, "")
                .set(SampleReturnInfoEntity::getApproveStatus, approveStatus)
                .set(SampleReturnInfoEntity::getApproveTime, null)
        .update(new SampleReturnInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SampleReturnInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SAMPLE_RETURN_INFO.getCode(), obj.getId()));
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
        for(SampleReturnInfoDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));

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
    private void validateSubmit(SampleReturnInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus()) || entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleReturnInfoEntity sampleReturnInfoEntity) {
        //来源借用单
        sampleReturnInfoEntity.setSourceType(SampleLedgerTypeEnum.BORROW.getCode());

        String returnUserId = sampleReturnInfoEntity.getReturnUserId();
        String receiverUserId = sampleReturnInfoEntity.getReceiverUserId();
        if(Objects.equals(returnUserId,receiverUserId)){
            throw new ServiceException(ApiError.ERROR_SAMPLE_RETURN_USER_SAME);
        }
        List<FindUserDTO> users = sysUserFeign.getUserListByUserIds(Arrays.asList(returnUserId, receiverUserId));
        if(CollUtil.isEmpty(users)){
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }
        FindUserDTO returnUser = users.stream().filter(e -> Objects.equals(e.getUserId(), returnUserId)).findFirst().orElse(null);
        if(Objects.isNull(returnUser)){
            throw new ServiceException(ApiError.NOT_EXIST,"归还人");
        }
        FindUserDTO receiverUser = users.stream().filter(e -> Objects.equals(e.getUserId(), receiverUserId)).findFirst().orElse(null);
        if(Objects.isNull(receiverUser)){
            throw new ServiceException(ApiError.NOT_EXIST,"接收人");
        }

        String returnDeptId = sampleReturnInfoEntity.getReturnDeptId();
        String receiverDeptId = sampleReturnInfoEntity.getReceiverDeptId();
        List<SysDepartmentEntity> sysDepartmentEntities = sysUserFeign.listDeptByIds(Arrays.asList(returnDeptId, receiverDeptId));
        if(CollUtil.isEmpty(sysDepartmentEntities)){
            throw new ServiceException(ApiError.ERROR_9029);
        }

        SysDepartmentEntity returnDept = sysDepartmentEntities.stream().filter(e -> Objects.equals(e.getId(), returnDeptId)).findFirst().orElse(null);
        if(Objects.isNull(returnDept)){
            throw new ServiceException(ApiError.NOT_EXIST,"归还部门");
        }
        SysDepartmentEntity receiverDept = sysDepartmentEntities.stream().filter(e -> Objects.equals(e.getId(), receiverDeptId)).findFirst().orElse(null);
        if(Objects.isNull(receiverDept)){
            throw new ServiceException(ApiError.NOT_EXIST,"接收部门");
        }
        //赋值
        sampleReturnInfoEntity.setReturnUserName(returnUser.getUserName());
        sampleReturnInfoEntity.setReceiverUserName(receiverUser.getUserName());
        sampleReturnInfoEntity.setReturnDeptName(returnDept.getName());
        sampleReturnInfoEntity.setReceiverDeptName(receiverDept.getName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> generateSampleReturn(ValidList<SampleBorrowInfoDTO.SampleReturnView> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        SampleReturnInfoServiceImpl bean = ApplicationContextUtils.getBean(SampleReturnInfoServiceImpl.class);

        // 按 sourceId 分组
        Map<String, List<SampleBorrowInfoDTO.SampleReturnView>> sourceGroupMap = list.stream()
                .collect(Collectors.groupingBy(SampleBorrowInfoDTO.SampleReturnView::getSourceId));

        List<BatchResultDTO> resultDTOS = new ArrayList<>();

        for (Map.Entry<String, List<SampleBorrowInfoDTO.SampleReturnView>> sourceEntry : sourceGroupMap.entrySet()) {
            List<SampleBorrowInfoDTO.SampleReturnView> bySourceIdList = sourceEntry.getValue();

            // 在同一 sourceId 下，再按 returnDate 分组
            Map<LocalDate, List<SampleBorrowInfoDTO.SampleReturnView>> dateGroupMap = bySourceIdList.stream()
                    .collect(Collectors.groupingBy(SampleBorrowInfoDTO.SampleReturnView::getReturnDate));

            for (Map.Entry<LocalDate, List<SampleBorrowInfoDTO.SampleReturnView>> dateEntry : dateGroupMap.entrySet()) {
                List<SampleBorrowInfoDTO.SampleReturnView> byReturnDateList = dateEntry.getValue();

                if (CollUtil.isEmpty(byReturnDateList)) {
                    continue;
                }

                // 构造归还单主表数据
                SampleBorrowInfoDTO.SampleReturnView firstItem = byReturnDateList.get(0);
                SampleReturnInfoDTO.AddDTO addDTO = new SampleReturnInfoDTO.AddDTO();
                BeanMapperUtils.copy(firstItem, addDTO);

                // 构造归还单明细数据
                List<SampleReturnDetailDTO.AddDTO> detailList = BeanMapperUtils.copyList(SampleReturnDetailDTO.AddDTO.class, byReturnDateList);
                addDTO.setDetailList(detailList);

                BatchResultDTO result;
                try {
                    // 调用服务保存归还单
                    BaseResultDTO.AddDTO add = bean.add(addDTO);
                    result = BatchResultDTO.success(add.getId(), add.getCode(), OperationTypeEnum.ADD);

                    // 记录下推日志
                    String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据下推样品归还单【{}】", UserContext.getDefaultLoginUser().getUserName(), addDTO.getSourceCode(), "样品借用单",add.getCode());
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), addDTO.getSourceId(), "下推操作");
                } catch (Exception e) {
                    log.error("系统异常：sourceId={}, sourceCode={}", firstItem.getSourceId(), firstItem.getSourceCode(), e);
                    result = BatchResultDTO.fail(firstItem.getSourceId(), firstItem.getSourceCode(), e.getMessage());
                }
                resultDTOS.add(result);
            }
        }
        return resultDTOS;
    }

    // ==================== 台账流水构建器实现 ====================

    @Override
    public String getSupportedSourceType() {
        return SourceTypeEnum.SAMPLE_RETURN_INFO.getCode();
    }

    @Override
    public SampleLedgerFlowDTO.AddFlowDTO buildFlow(String sourceId, String sourceCode, ApproveTypeEnum approveType) {
        try {
            // 获取样品归还单主表信息
            SampleReturnInfoEntity entity = this.getById(sourceId);
            if (entity == null) {
                log.error("获取样品归还单失败，sourceId：{}", sourceId);
                return null;
            }

            // 获取样品归还单明细
            List<SampleReturnDetailEntity> detailList = sampleReturnDetailService.list(new LambdaQueryWrapper<SampleReturnDetailEntity>().eq(SampleReturnDetailEntity::getMainId,sourceId));

            if (detailList.isEmpty()) {
                log.warn("样品归还单明细为空，sourceId：{}", sourceId);
                return null;
            }

            // 构建流水明细
            List<SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO> flowDetails = new ArrayList<>();
            for (SampleReturnDetailEntity detail : detailList) {
                // 计算数量：审核为-X，反审核为+X
                Integer qty = calculateQty(detail.getReturnQty(), approveType);

                SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO flowDetail = new SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO();
                flowDetail.setSourceDetailId(detail.getId());
                flowDetail.setSkuNo(detail.getSkuNo());
                flowDetail.setSkuId(detail.getSkuId());
                flowDetail.setProductName(detail.getProductName());
                flowDetail.setQty(qty);
                // 设置样品台账ID，用于查询使用方信息
                flowDetail.setSampleLedgerId(detail.getSampleLedgerId());
                flowDetails.add(flowDetail);
            }

            // 构建流水主表数据
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = new SampleLedgerFlowDTO.AddFlowDTO();
            flowDTO.setSourceType(getSupportedSourceType());
            flowDTO.setApproveType(approveType.getStatus());
            flowDTO.setOperateTime(LocalDateTime.now());
            flowDTO.setBillDate(entity.getReturnDate());
            flowDTO.setSourceName("样品归还单");
            flowDTO.setSourceCode(sourceCode);
            flowDTO.setSourceId(sourceId);
            // 使用方信息将通过每个明细的sampleLedgerId在SampleLedgerFlowServiceImpl中查询获取
            // flowDTO.setUseUserId(entity.getReturnUserId());
            // flowDTO.setUseUserName(entity.getReturnUserName());
            flowDTO.setUserId(entity.getReturnUserId());
            flowDTO.setUserName(entity.getReturnUserName());
            flowDTO.setDeptId(entity.getReturnDeptId());
            // 根据部门ID查询部门名称
            flowDTO.setDeptName(getDeptNameById(entity.getReturnDeptId()));
            flowDTO.setDetailList(flowDetails);

            return flowDTO;
        } catch (Exception e) {
            log.error("构建样品归还单台账流水失败，sourceId：{}，错误：{}", sourceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建接收人的台账流水
     */
    private SampleLedgerFlowDTO.AddFlowDTO buildReceiverFlow(String sourceId, String sourceCode, ApproveTypeEnum approveType) {
        try {
            // 获取样品归还单主表信息
            SampleReturnInfoEntity entity = this.getById(sourceId);
            if (entity == null) {
                log.error("获取样品归还单失败，sourceId：{}", sourceId);
                return null;
            }

            // 获取样品归还单明细
            List<SampleReturnDetailEntity> detailList = sampleReturnDetailService.list(new LambdaQueryWrapper<SampleReturnDetailEntity>().eq(SampleReturnDetailEntity::getMainId,sourceId));

            if (detailList.isEmpty()) {
                log.warn("样品归还单明细为空，sourceId：{}", sourceId);
                return null;
            }

            // 构建流水明细
            List<SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO> flowDetails = new ArrayList<>();
            for (SampleReturnDetailEntity detail : detailList) {
                // 计算数量：审核为+X，反审核为-X（接收人视角，接收是增加库存）
                Integer qty = calculateReceiverQty(detail.getReturnQty(), approveType);

                SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO flowDetail = new SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO();
                flowDetail.setSourceDetailId(detail.getId());
                flowDetail.setSkuNo(detail.getSkuNo());
                flowDetail.setSkuId(detail.getSkuId());
                flowDetail.setProductName(detail.getProductName());
                flowDetail.setQty(qty);
                // 设置样品台账ID，用于查询使用方信息
                flowDetail.setSampleLedgerId(detail.getSampleLedgerId());
                flowDetails.add(flowDetail);
            }

            // 构建流水主表数据 - 接收人台账
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = new SampleLedgerFlowDTO.AddFlowDTO();
            flowDTO.setSourceType(getSupportedSourceType());
            flowDTO.setApproveType(approveType.getStatus());
            flowDTO.setOperateTime(LocalDateTime.now());
            flowDTO.setBillDate(entity.getReturnDate());
            flowDTO.setSourceName("样品归还单");
            flowDTO.setSourceCode(sourceCode);
            flowDTO.setSourceId(sourceId);
            flowDTO.setUseUserId(entity.getReceiverUserId());
            flowDTO.setUseUserName(entity.getReceiverUserName());
            flowDTO.setUserId(entity.getReceiverUserId());
            flowDTO.setUserName(entity.getReceiverUserName());
            flowDTO.setDeptId(entity.getReceiverDeptId());
            // 根据部门ID查询部门名称
            flowDTO.setDeptName(getDeptNameById(entity.getReceiverDeptId()));
            flowDTO.setDetailList(flowDetails);

            return flowDTO;
        } catch (Exception e) {
            log.error("构建样品归还单接收人台账流水失败，sourceId：{}，错误：{}", sourceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 计算接收人数量：审核为+X，反审核为-X（接收人视角，接收是增加库存）
     */
    private Integer calculateReceiverQty(Integer originalQty, ApproveTypeEnum approveType) {
        if (originalQty == null) {
            return 0;
        }

        if (ApproveTypeEnum.PASS.equals(approveType)) {
            return originalQty; // 审核：+X（接收人增加库存）
        } else if (ApproveTypeEnum.DIS_APPROVE.equals(approveType)) {
            return -originalQty; // 反审核：-X（接收人减少库存）
        }

        return 0;
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

}
