package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.excel.SupplierVisitImportExcelDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.tms.dto.excel.DictHsCodeExcelDTO;
import com.erp.model.tms.entity.DictHsCodeEntity;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.dto.SampleScrapDetailDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.dto.excel.SampleScrapImportExcelDTO;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.erp.model.wms.entity.SampleScrapDetailEntity;
import com.erp.model.wms.entity.SampleScrapInfoEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.wms.listener.SampleScrapExcelListener;
import com.erp.server.wms.mapper.SampleScrapInfoMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import jodd.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_SCRAP_INFO_REPORT;

/**
 * <p>
 * 样品报废单主表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SampleScrapInfoServiceImpl extends SuperServiceImpl<SampleScrapInfoMapper, SampleScrapInfoEntity> implements SampleScrapInfoService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Resource
    private SampleScrapDetailService sampleScrapDetailService;
    @Resource
    private WmsAttachmentService attachmentService;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SampleLedgerService sampleLedgerService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleScrapInfoDTO.AddDTO addDTO) {
        SampleScrapInfoEntity sampleScrapInfoEntity = new SampleScrapInfoEntity();
        BeanMapperUtils.copy(addDTO, sampleScrapInfoEntity);

        log.info("开始新增样品报废单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YPZF);
        sampleScrapInfoEntity.setCode(code);
        boolean save = super.save(sampleScrapInfoEntity);
        if(!save) {
            throw new ServiceException("样品报废单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品报废单" , sampleScrapInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_SCRAP_INFO.getCode(), sampleScrapInfoEntity.getId(), "新增操作");

        // 明细
        List<SampleScrapDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        detailList.forEach(detail -> detail.setMainId(sampleScrapInfoEntity.getId()));
        List<SampleScrapDetailEntity> sampleScrapDetailEntities = BeanMapperUtils.copyList(SampleScrapDetailEntity.class, detailList);

        // 提取所有SKU编号，用于后续查询可用数量
        List<String> skuNos = sampleScrapDetailEntities.stream().map(SampleScrapDetailEntity::getSkuNo).distinct().collect(Collectors.toList());

        String scrapUserId = sampleScrapInfoEntity.getScrapUserId();
        //校验可用数量是否足够
        checkDetailQty("" , scrapUserId, skuNos, sampleScrapDetailEntities);

        sampleScrapDetailService.saveBatch(sampleScrapDetailEntities);

        //附件
        addAttachment(addDTO, sampleScrapInfoEntity);
        return new BaseResultDTO.AddDTO(sampleScrapInfoEntity.getId(), code);
    }

    private void checkDetailQty(String id , String scrapUserId, List<String> skuNos, List<SampleScrapDetailEntity> sampleScrapDetailEntities) {
        // 构造查询条件：根据用户ID和SKU列表查询样品台账中的可用数量
        SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
        dto.setUserId(scrapUserId);
        dto.setSkuNos(skuNos);
        List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(dto);
        Map<String, Integer> sampleLedgerMap = skuAvailableQtyDTOS.stream().collect(Collectors.toMap(SampleLedgerDTO.SkuAvailableQtyDTO::getId, SampleLedgerDTO.SkuAvailableQtyDTO::getQty));

        // 查询这些SKU已经被报废的数量
        Map<String, Integer> sampleScrapMap = sampleScrapDetailService.listBySku(id,skuNos);

        // 计算每个明细项中SKU的实际可报废数量（台账数量 - 已报废数量）
        sampleScrapDetailEntities.forEach(detailDTO -> {
            String sampleLedgerId = detailDTO.getSampleLedgerId();
            Integer ledgerQty = sampleLedgerMap.getOrDefault(sampleLedgerId, 0);
            if(detailDTO.getScrapQty().compareTo(ledgerQty) > 0){
                throw new ServiceException(ApiError.ERROR_SAMPLE_AVAILABLE_QTY,detailDTO.getSkuNo(),"报废");
            }

            //防止明细里还有重复SKU
            sampleLedgerMap.put(sampleLedgerId,ledgerQty - detailDTO.getScrapQty());
        });
    }

    /**
     * 添加附件信息
     * @param addDTO 包含附件URL和名称列表的数据传输对象
     * @param sampleScrapInfoEntity 样品报废信息实体对象
     */
    private void addAttachment(SampleScrapInfoDTO.AddDTO addDTO, SampleScrapInfoEntity sampleScrapInfoEntity) {
        //附件集合
        List<String> attachmentUrlList = addDTO.getAttachmentUrlList();
        //附件名
        List<String> attachmentNameList = addDTO.getAttachmentNameList();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<SampleScrapDetailEntity> credentialClass = SampleScrapDetailEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                attachment.setAttachUrl(attachmentUrlList.get(i));
                attachment.setAttachName(attachmentNameList.get(i));
                attachment.setBusinessId(sampleScrapInfoEntity.getId());
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
    public Boolean update(SampleScrapInfoDTO.UpdateDTO addOrUpdateDTO) {
        SampleScrapInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品报废单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SampleScrapInfoEntity sampleScrapInfoEntity =  BeanMapperUtils.map(SampleScrapInfoEntity.class, addOrUpdateDTO);

        log.info("编辑 开始修改样品报废单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(sampleScrapInfoEntity);
        if(!save) {
            throw new ServiceException("样品报废单保存失败");
        }
        // 记录操作日志
        log.info("编辑 开始记录样品报废单日志数据，单号：【{}】", sampleScrapInfoEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleScrapInfoEntity.getCode(), "样品报废单");
        operateLogService.addModuleOperateLogByObj(old, sampleScrapInfoEntity, ModuleTypeEnum.SAMPLE_SCRAP_INFO.getCode(), sampleScrapInfoEntity.getId(), msg);

        //明细
        updateDetail(addOrUpdateDTO, sampleScrapInfoEntity);
        //附件
        updateAttachment(addOrUpdateDTO, old);
        return Boolean.TRUE;
    }

    /**
     * 更新样品报废信息的明细数据
     * <p>
     * 该方法根据传入的更新DTO对象，对样品报废信息的明细进行增删改操作，并记录相应的操作日志。
     * 具体包括：
     * - 删除旧明细中存在但新数据中不存在的记录；
     * - 新增新数据中ID为空的明细记录；
     * - 更新新数据中ID不为空的明细记录；
     * 同时为上述操作添加对应的操作日志。
     *
     * @param addOrUpdateDTO        包含待更新明细数据的DTO对象
     * @param sampleScrapInfoEntity 当前更新后的样品报废主表实体对象
     */
    private void updateDetail(SampleScrapInfoDTO.UpdateDTO addOrUpdateDTO,  SampleScrapInfoEntity sampleScrapInfoEntity) {
        List<SampleScrapDetailDTO.UpdateDTO> detailList = addOrUpdateDTO.getDetailList();
        List<SampleScrapDetailEntity> oldList = sampleScrapDetailService.listByMainId(sampleScrapInfoEntity.getId());
        detailList.forEach(detail -> detail.setMainId(sampleScrapInfoEntity.getId()));
        List<SampleScrapDetailEntity> sampleScrapDetailEntities = BeanMapperUtils.copyList(SampleScrapDetailEntity.class, detailList);

        // 提取所有SKU编号，用于后续查询可用数量
        List<String> skuNos = sampleScrapDetailEntities.stream().map(SampleScrapDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
        String scrapUserId = sampleScrapInfoEntity.getScrapUserId();
        //校验可用数量是否足够
        checkDetailQty(sampleScrapInfoEntity.getId(),scrapUserId, skuNos, sampleScrapDetailEntities);

        if(CollUtil.isNotEmpty(oldList)){
            // 处理删除的数据
            List<SampleScrapDetailEntity> remove = oldList.stream()
                    .filter(oldEntity -> !detailList.contains(oldEntity.getId()))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(remove)){
                sampleScrapDetailService.removeByIds(remove.stream().map(SampleScrapDetailEntity::getId).collect(Collectors.toList()));
                //添加日志
                List<Pair<String, String>> removePairList = remove.stream().map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除SKU【%s】", ModuleTypeEnum.SAMPLE_SCRAP_INFO.getCode(), removePairList, "编辑操作");
            }
        }
        //处理需要新增的数据
        List<SampleScrapDetailEntity> addList = sampleScrapDetailEntities.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(addList)){
            sampleScrapDetailService.saveBatch(addList);

            //添加日志
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加SKU【%s】", ModuleTypeEnum.SAMPLE_SCRAP_INFO.getCode(), addPairList, "编辑操作");
        }
        //处理需要更新的数据
        List<SampleScrapDetailEntity> updateList = sampleScrapDetailEntities.stream().filter(e -> StringUtils.isNotBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(updateList)){
            sampleScrapDetailService.updateBatchById(updateList);
            //添加日志
            String msg = "编辑SKU【%s】";
            for (SampleScrapDetailEntity sampleScrapDetailEntity : updateList) {
                SampleScrapDetailEntity oldDetail = oldList.stream().filter(e -> Objects.equals(e.getId(), sampleScrapDetailEntity.getId())).findFirst().orElse(null);
                if(Objects.nonNull(oldDetail)){
                    operateLogService.addModuleOperateLogByObj(oldDetail, sampleScrapDetailEntity, ModuleTypeEnum.SAMPLE_SCRAP_INFO.getCode(), sampleScrapInfoEntity.getId(), msg);
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
     * @param old            旧的样品报废信息实体，用于获取业务ID
     */
    private void updateAttachment(SampleScrapInfoDTO.UpdateDTO addOrUpdateDTO, SampleScrapInfoEntity old) {
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
                Class<SampleScrapInfoEntity> credentialClass = SampleScrapInfoEntity.class;
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
    public PagingVO<SampleScrapInfoDTO.ListDTO> paging(PagingDTO<SampleScrapInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleScrapInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SampleScrapInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        SampleScrapInfoDTO.PagingParamDTO searchParam = new SampleScrapInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SampleScrapInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SampleScrapInfoDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new SampleScrapInfoDTO.TabListDTO(status,ApproveStatusEnum.getName(status), 0));
        }
        });
        list.sort(Comparator.comparing(SampleScrapInfoDTO.TabListDTO::getTabFlag));
        list.add(0,new SampleScrapInfoDTO.TabListDTO("all","全部", 0));
        return list;
    }

    @Override
    public void exportList(SampleScrapInfoDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("样品报废单导出", EXPORT_WMS_SAMPLE_SCRAP_INFO_REPORT.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        SampleScrapInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到样品报废单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改样品报废单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动样品报废单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录样品报废单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品报废单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_SCRAP_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SampleScrapInfoDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SampleScrapInfoDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SampleScrapInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品报废单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_SCRAP_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SampleScrapInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SAMPLE_SCRAP_INFO.getCode());
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
    public BatchResultDTO disApprove(String id) {
        SampleScrapInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品报废单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 记录台账流水

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品报废单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_SCRAP_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SampleScrapInfoEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SampleScrapInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品报废单数据"));
        // 只有待提交、审核不通过数据允许删除
        if (!(Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus()) || Objects.equals(ApproveStatusEnum.REJECT, entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_98032);
        }

        //删除明细
        sampleScrapDetailService.lambdaUpdate()
                .set(SampleScrapDetailEntity::getIsDeleted, Boolean.TRUE)
                .eq(SampleScrapDetailEntity::getMainId, id)
                .update();
        // 删除数据
        log.info("删除 开始删除样品报废单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除样品报废单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品报废单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_SCRAP_INFO.getCode(), entity.getCode(), "删除样品报废单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id) {
        SampleScrapInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品报废单数据"));
        // 只有待提交、审核不通过数据允许作废
        if (!(Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus()) || Objects.equals(ApproveStatusEnum.REJECT, entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        //已作废不支持作废
        if(entity.getInvalidStatus()){
            throw new ServiceException(ApiError.ERROR_98012);
        }
        // 删除数据
        log.info("作废 开始作废样品报废单数据，id：【{}】", id);
        lambdaUpdate()
                .set(SampleScrapInfoEntity::getInvalidStatus, Boolean.TRUE)
                .eq(SampleScrapInfoEntity::getId, id)
                .update();

        // 日志
        log.info("作废 开始作废样品报废单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品报废单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_SCRAP_INFO.getCode(), entity.getCode(), "作废样品报废单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    @Override
    public void batchImportVisit(List<SampleScrapInfoDTO.AddDTO> addList) {

    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SampleScrapInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品报废单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改样品报废单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品报废单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_SCRAP_INFO.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SAMPLE_SCRAP_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SampleScrapInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 记录台账流水

        return Boolean.TRUE;
    }



    /**
     * 根据样品报废单ID查询详细信息，包括主表信息、明细列表、附件信息，并计算每个SKU的可报废数量。
     *
     * @param id 样品报废单ID，用于查询主表和关联数据
     * @return SampleScrapInfoDTO.ViewDTO 包含完整报废单视图数据的数据传输对象
     * @throws ServiceException 当未找到对应ID的样品报废单时抛出异常
     */
    @Override
    public SampleScrapInfoDTO.ViewDTO view(String id) {
        // 查询主表信息，若不存在则抛出异常
        SampleScrapInfoEntity sampleScrapInfoEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到样品报废单数据"));

        // 将实体映射为ViewDTO对象
        SampleScrapInfoDTO.ViewDTO data = BeanMapperUtils.map(SampleScrapInfoDTO.ViewDTO.class, sampleScrapInfoEntity);

        // 填充额外展示所需的数据
        fillOne(data);

        // 查询报废明细列表并转换为DTO
        List<SampleScrapDetailEntity> sampleScrapDetailEntities = sampleScrapDetailService.listByMainId(id);
        List<SampleScrapDetailDTO.ViewDTO> detailDTOList = BeanMapperUtils.copyList(SampleScrapDetailDTO.ViewDTO.class,sampleScrapDetailEntities);

        // 提取所有SKU编号，用于后续查询可用数量
        List<String> skuNos = sampleScrapDetailEntities.stream().map(SampleScrapDetailEntity::getSkuNo).distinct().collect(Collectors.toList());

        // 构造查询条件：根据用户ID和SKU列表查询样品台账中的可用数量
        SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
        dto.setUserId(sampleScrapInfoEntity.getScrapUserId());
        dto.setSkuNos(skuNos);
        List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(dto);
        Map<String, Integer> sampleLedgerMap = skuAvailableQtyDTOS.stream().collect(Collectors.toMap(SampleLedgerDTO.SkuAvailableQtyDTO::getId, SampleLedgerDTO.SkuAvailableQtyDTO::getQty));

        // 计算每个明细项中SKU的实际可报废数量（台账数量 - 已报废数量）
        detailDTOList.forEach(detailDTO -> {
            String sampleLedgerId = detailDTO.getSampleLedgerId();
            Integer ledgerQty = sampleLedgerMap.getOrDefault(sampleLedgerId, 0);
            detailDTO.setAvailableScrapQty(ledgerQty);
        });

        // 设置明细列表到主数据对象中
        data.setDetailList(detailDTOList);

        // 查询与该报废单相关的附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(Arrays.asList(id));
        if(CollUtil.isNotEmpty(attachmentList)){
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

    public void startProcess(SampleScrapInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SAMPLE_SCRAP_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * 根据样本报废信息实体获取变量映射表
     *
     * @param entity 样本报废信息实体对象，用于转换为变量参数
     * @return 返回根据业务键获取的变量映射表，包含配置查询选项的相关变量信息
     */
    private Map<String,Object> getVariablesMap(SampleScrapInfoEntity entity){
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.SAMPLE_RETURN_INFO.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> map = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);
        return map;
    }


    private void fillOne(SampleScrapInfoDTO.ViewDTO data) {
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
        this.lambdaUpdate().eq(SampleScrapInfoEntity::getId, id)
            .set(SampleScrapInfoEntity::getApproveUserId, userInfo.getUid())
            .set(SampleScrapInfoEntity::getApproveUserName, userInfo.getUserName())
            .set(SampleScrapInfoEntity::getApproveStatus, approveStatus)
            .set(SampleScrapInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new SampleScrapInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SampleScrapInfoEntity::getId, id)
            .set(SampleScrapInfoEntity::getApproveUserId, "")
            .set(SampleScrapInfoEntity::getApproveUserName, "")
            .set(SampleScrapInfoEntity::getApproveStatus, approveStatus)
            .set(SampleScrapInfoEntity::getApproveTime, null)
            .update(new SampleScrapInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SampleScrapInfoEntity::getId, id)
        .set(SampleScrapInfoEntity::getApproveStatus, approveStatus)
        .update(new SampleScrapInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SampleScrapInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 属性赋值
        for(SampleScrapInfoDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SampleScrapInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }


    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        //sku信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> map = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, e -> e,(o1,o2)->o1));
        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        SampleScrapExcelListener excelListenerUtil = new SampleScrapExcelListener(deptList, map, userList);
        try {
            EasyExcel.read(excelFile.getInputStream(), SampleScrapImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入样品报废单错误！", e);
            return Boolean.FALSE;
        }

        List<SampleScrapImportExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<SampleScrapInfoDTO.ImportDTO> successList = excelListenerUtil.getSuccessList();
        if(CollUtil.isNotEmpty(successList)){
            if(CollUtil.isNotEmpty(errorList)){
                List<String> errorNoList = errorList.stream().map(SampleScrapImportExcelDTO::getNo).distinct().collect(Collectors.toList());
                successList = successList.stream().filter(e -> StringUtils.isNotBlank(e.getNo()) && !errorNoList.contains(e.getNo())).collect(Collectors.toList());

                //全部返回到错误列表
                List<SampleScrapInfoDTO.ImportDTO> collect = successList.stream().filter(e -> StringUtils.isBlank(e.getNo()) || errorNoList.contains(e.getNo())).collect(Collectors.toList());
                List<SampleScrapImportExcelDTO> sampleScrapImportExcelDTOS = BeanMapperUtils.copyList(SampleScrapImportExcelDTO.class, collect);
                errorList.addAll(sampleScrapImportExcelDTOS);
            }

            //按序号分组
            Map<String, List<SampleScrapInfoDTO.ImportDTO>> collect = successList.stream().collect(Collectors.groupingBy(SampleScrapInfoDTO.ImportDTO::getNo));
            for (Map.Entry<String, List<SampleScrapInfoDTO.ImportDTO>> entry : collect.entrySet()) {
                List<SampleScrapInfoDTO.ImportDTO> value = entry.getValue();
                SampleScrapInfoDTO.AddDTO addDTO = new SampleScrapInfoDTO.AddDTO();
                BeanMapperUtils.copy(value.get(0), addDTO);
                List<SampleScrapDetailDTO.AddDTO> detailList = new ArrayList<>();
                for (SampleScrapInfoDTO.ImportDTO importDTO : value) {
                    SampleScrapDetailDTO.AddDTO detailDTO = new SampleScrapDetailDTO.AddDTO();
                    BeanMapperUtils.copy(importDTO, detailDTO);
                    detailList.add(detailDTO);
                }
                addDTO.setDetailList(detailList);
            }
        }

        if (errorList.size() > 0) {
            // 按照no字段的数值大小进行排序
            errorList.sort(Comparator.comparingInt(dto -> Integer.parseInt(dto.getNo())));

            String fileName = "样品报废单错误信息";
            ExcelUtil.export(fileName, "sampleScrapError", errorList, SupplierVisitImportExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


}
