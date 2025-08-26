package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.SampleBorrowImportExcelDTO;
import com.erp.model.wms.dto.excel.SampleScrapImportExcelDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.listener.SampleBorrowExcelListener;
import com.erp.server.wms.mapper.SampleBorrowInfoMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 样品借用单 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SampleBorrowInfoServiceImpl extends SuperServiceImpl<SampleBorrowInfoMapper, SampleBorrowInfoEntity> implements SampleBorrowInfoService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Resource
    private SampleBorrowDetailService sampleBorrowDetailService;
    @Resource
    private SampleLedgerService sampleLedgerService;
    @Resource
    private WmsAttachmentService attachmentService;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private FileFeign fileFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleBorrowInfoDTO.AddDTO addDTO) {
        SampleBorrowInfoEntity sampleBorrowInfoEntity = new SampleBorrowInfoEntity();
        BeanMapperUtils.copy(addDTO, sampleBorrowInfoEntity);

        // 数据处理
        handleData(sampleBorrowInfoEntity);

        log.info("开始新增样品借用单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YPJY);
        sampleBorrowInfoEntity.setCode(code);
        boolean save = super.save(sampleBorrowInfoEntity);
        if(!save) {
            throw new ServiceException("样品借用单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品借用单" , sampleBorrowInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), sampleBorrowInfoEntity.getId(), "新增操作");
        // 明细
        List<SampleBorrowDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        List<SampleBorrowDetailEntity> sampleBorrowDetailEntities = BeanMapperUtils.copyList(SampleBorrowDetailEntity.class, detailList);
        List<String> skuIds = sampleBorrowDetailEntities.stream().map(SampleBorrowDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //sku信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));
        for (SampleBorrowDetailEntity sampleBorrowDetailEntity : sampleBorrowDetailEntities) {
            sampleBorrowDetailEntity.setMainId(sampleBorrowInfoEntity.getId());

            SkuVO skuVO = skuMap.getOrDefault(sampleBorrowDetailEntity.getSkuId(), null);
            if(Objects.nonNull(skuVO)){
                sampleBorrowDetailEntity.setSkuNo(skuVO.getSkuNo());
                sampleBorrowDetailEntity.setProductName(skuVO.getSkuName());
            }
        }

        // 提取所有SKU编号，用于后续查询可用数量
        List<String> skuNos = sampleBorrowDetailEntities.stream().map(SampleBorrowDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
        String lendUserId = sampleBorrowInfoEntity.getLendUserId();
        //校验可用数量是否足够
        checkDetailQty("" , lendUserId, skuNos, sampleBorrowDetailEntities);


        sampleBorrowDetailEntities.forEach(e -> e.setWaitReturnQty(e.getBorrowQty()));
        sampleBorrowDetailService.saveBatch(sampleBorrowDetailEntities);
        //附件
        addAttachment(addDTO, sampleBorrowInfoEntity);
        return new BaseResultDTO.AddDTO(sampleBorrowInfoEntity.getId(), code);
    }

    private void checkDetailQty(String id , String lendUserId, List<String> skuNos, List<SampleBorrowDetailEntity> sampleBorrowDetailEntities) {
        // 构造查询条件：根据用户ID和SKU列表查询样品台账中的可用数量
        SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
        dto.setUserId(lendUserId);
        dto.setSkuNos(skuNos);
        dto.setType("borrow");
        dto.setChildId(id);
        List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(dto);
        Map<String, SampleLedgerDTO.SkuAvailableQtyDTO> sampleLedgerMap = skuAvailableQtyDTOS.stream().collect(Collectors.toMap(SampleLedgerDTO.SkuAvailableQtyDTO::getSampleLedgerId, Function.identity(),(o1,o2)-> o1));

        // 计算每个明细项中SKU的实际可报废数量（台账数量 - 已报废数量）
        sampleBorrowDetailEntities.forEach(detailDTO -> {
            String sampleLedgerId = detailDTO.getSampleLedgerId();
            SampleLedgerDTO.SkuAvailableQtyDTO sampleLedger = sampleLedgerMap.getOrDefault(sampleLedgerId, null);
            if(Objects.nonNull(sampleLedger)){
                Integer availableQty = Objects.isNull(sampleLedger.getAvailableQty()) ? 0 : sampleLedger.getAvailableQty() ;
                Integer borrowedQty  = Objects.isNull(detailDTO.getBorrowQty()) ? 0 : detailDTO.getBorrowQty() ;
                if(borrowedQty.compareTo(availableQty) > 0){
                    throw new ServiceException(ApiError.ERROR_SAMPLE_AVAILABLE_QTY,detailDTO.getSkuNo(),"借用");
                }

                //防止明细里还有重复
                sampleLedger.setAvailableQty(availableQty - borrowedQty);
                sampleLedgerMap.put(sampleLedgerId,sampleLedger);
            }else {
                throw new ServiceException(ApiError.ERROR_SAMPLE_AVAILABLE_QTY,detailDTO.getSkuNo(),"借用");
            }
        });
    }

    /**
     * 添加附件信息
     * @param addDTO 包含附件URL和名称列表的数据传输对象
     * @param sampleBorrowInfoEntity
     */
    private void addAttachment(SampleBorrowInfoDTO.AddDTO addDTO, SampleBorrowInfoEntity sampleBorrowInfoEntity) {
        //附件集合
        List<String> attachmentUrlList = addDTO.getAttachmentUrlList();
        //附件名
        List<String> attachmentNameList = addDTO.getAttachmentNameList();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<SampleBorrowDetailEntity> credentialClass = SampleBorrowDetailEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                attachment.setAttachUrl(attachmentUrlList.get(i));
                attachment.setAttachName(attachmentNameList.get(i));
                attachment.setBusinessId(sampleBorrowInfoEntity.getId());
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
    public Boolean update(SampleBorrowInfoDTO.UpdateDTO addOrUpdateDTO) {
        SampleBorrowInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品借用单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SampleBorrowInfoEntity sampleBorrowInfoEntity =  BeanMapperUtils.map(SampleBorrowInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleBorrowInfoEntity);
        log.info("编辑 开始修改样品借用单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(sampleBorrowInfoEntity);
        if(!save) {
            throw new ServiceException("样品借用单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录样品借用单日志数据，单号：【{}】", old.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleBorrowInfoEntity.getCode(), "样品借用单");
        operateLogService.addModuleOperateLogByObj(old, sampleBorrowInfoEntity,  ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), sampleBorrowInfoEntity.getId(), msg);

        //明细
        updateDetail(addOrUpdateDTO,sampleBorrowInfoEntity);

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
     * @param sampleBorrowInfoEntity 主表实体对象
     */
    private void updateDetail(SampleBorrowInfoDTO.UpdateDTO addOrUpdateDTO,  SampleBorrowInfoEntity sampleBorrowInfoEntity) {
        List<SampleBorrowDetailDTO.UpdateDTO> detailList = addOrUpdateDTO.getDetailList();
        List<SampleBorrowDetailEntity> oldList = sampleBorrowDetailService.listByMainId(sampleBorrowInfoEntity.getId());

        List<SampleBorrowDetailEntity> sampleBorrowDetailEntities = BeanMapperUtils.copyList(SampleBorrowDetailEntity.class, detailList);
        // 提取所有SKU编号，用于后续查询可用数量
        List<String> skuIds = sampleBorrowDetailEntities.stream().map(SampleBorrowDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //sku信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));
        for (SampleBorrowDetailEntity sampleBorrowDetailEntity : sampleBorrowDetailEntities) {
            sampleBorrowDetailEntity.setMainId(sampleBorrowInfoEntity.getId());

            SkuVO skuVO = skuMap.getOrDefault(sampleBorrowDetailEntity.getSkuId(), null);
            if(Objects.nonNull(skuVO)){
                sampleBorrowDetailEntity.setSkuNo(skuVO.getSkuNo());
                sampleBorrowDetailEntity.setProductName(skuVO.getSkuName());
            }
        }

        // 提取所有SKU编号，用于后续查询可用数量
        List<String> skuNos = sampleBorrowDetailEntities.stream().map(SampleBorrowDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
        String BorrowUserId = sampleBorrowInfoEntity.getBorrowUserId();
        //校验可用数量是否足够
        checkDetailQty(sampleBorrowInfoEntity.getId(),BorrowUserId, skuNos, sampleBorrowDetailEntities);

        sampleBorrowDetailEntities.forEach(e -> e.setWaitReturnQty(e.getBorrowQty()));

        if(CollUtil.isNotEmpty(oldList)){
            List<String> detailIds = detailList.stream().map(SampleBorrowDetailDTO.UpdateDTO::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            // 处理删除的数据
            List<SampleBorrowDetailEntity> remove = oldList.stream()
                    .filter(oldEntity -> !detailIds.contains(oldEntity.getId()))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(remove)){
                sampleBorrowDetailService.removeByIds(remove.stream().map(SampleBorrowDetailEntity::getId).collect(Collectors.toList()));
                //添加日志
                List<Pair<String, String>> removePairList = remove.stream().map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除SKU【%s】", ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), removePairList, "编辑操作");
            }
        }
        //处理需要新增的数据
        List<SampleBorrowDetailEntity> addList = sampleBorrowDetailEntities.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(addList)){
            sampleBorrowDetailService.saveBatch(addList);

            //添加日志
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加SKU【%s】", ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), addPairList, "编辑操作");
        }
        //处理需要更新的数据
        List<SampleBorrowDetailEntity> updateList = sampleBorrowDetailEntities.stream().filter(e -> StringUtils.isNotBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(updateList)){
            sampleBorrowDetailService.updateBatchById(updateList);
            //添加日志
            String msg = "编辑SKU【%s】";
            for (SampleBorrowDetailEntity sampleBorrowDetailEntity : updateList) {
                SampleBorrowDetailEntity oldDetail = oldList.stream().filter(e -> Objects.equals(e.getId(), sampleBorrowDetailEntity.getId())).findFirst().orElse(null);
                if(Objects.nonNull(oldDetail)){
                    operateLogService.addModuleOperateLogByObj(oldDetail, sampleBorrowDetailEntity, ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), sampleBorrowInfoEntity.getId(), msg);
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
     * @param old            获取业务ID
     */
    private void updateAttachment(SampleBorrowInfoDTO.UpdateDTO addOrUpdateDTO, SampleBorrowInfoEntity old) {
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
                Class<SampleBorrowInfoEntity> credentialClass = SampleBorrowInfoEntity.class;
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
    public PagingVO<SampleBorrowInfoDTO.ListDTO> paging(PagingDTO<SampleBorrowInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleBorrowInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SampleBorrowInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        SampleBorrowInfoDTO.PagingParamDTO searchParam = new SampleBorrowInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SampleBorrowInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SampleBorrowInfoDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new SampleBorrowInfoDTO.TabListDTO(status, "", 0));
            }
        });

        list.stream().forEach(e ->{
            if(Objects.equals(ApproveStatusEnum.APPROVE.getCode(), e.getTabFlag())){
                e.setTabFlagName("待归还");
            }else {
                e.setTabFlagName(ApproveStatusEnum.getName(e.getTabFlag()));
            }
        });
        list.sort(Comparator.comparing(SampleBorrowInfoDTO.TabListDTO::getTabFlag));
        return list;
    }

    @Override
    public void exportList(SampleBorrowInfoDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("样品借用单导出", EXPORT_WMS_SAMPLE_BORROW_INFO.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        SampleBorrowInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到样品借用单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改样品借用单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动样品借用单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录样品借用单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品借用单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SampleBorrowInfoDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SampleBorrowInfoDTO.UpdateDTO dto) {
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
        SampleBorrowInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品借用单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SampleBorrowInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SAMPLE_BORROW_INFO.getCode());
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
        SampleBorrowInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品借用单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品借用单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SampleBorrowInfoEntity entity) {
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
        SampleBorrowInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品借用单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException("只有待提交数据支持删除");
        }
        //删除明细
        sampleBorrowDetailService.lambdaUpdate()
                .set(SampleBorrowDetailEntity::getIsDeleted, Boolean.TRUE)
                .eq(SampleBorrowDetailEntity::getMainId, id)
                .update();
        //删除附件
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(Arrays.asList(id));
        if(CollUtil.isNotEmpty(attachmentList)){
            List<String> urlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            attachmentService.deleteByUrlList(urlList);
        }

        // 删除主单数据
        log.info("删除 开始删除样品借用单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除样品借用单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品借用单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), entity.getCode(), "删除样品借用单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id) {
        SampleBorrowInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品借用单数据"));
        // 只有待提交、审核不通过数据允许作废
        if (!(Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus()) || Objects.equals(ApproveStatusEnum.REJECT, entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        //已作废不支持作废
        if(entity.getInvalidStatus()){
            throw new ServiceException(ApiError.ERROR_98012);
        }
        // 删除主单数据
        log.info("作废 开始作废样品借用单主单数据，id：【{}】", id);
        lambdaUpdate()
                .set(SampleBorrowInfoEntity::getInvalidStatus, Boolean.TRUE)
                .eq(SampleBorrowInfoEntity::getId, id)
                .update();
        // 日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品借用单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), entity.getCode(), "作废样品借用单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SampleBorrowInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品借用单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改样品借用单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品借用单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BORROW_INFO.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SAMPLE_BORROW_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SampleBorrowInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }



    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        downloadTaskFeign.saveImportTask("导入样品借用单", IMPORT_WMS_SAMPLE_BORROW_INFO.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importSampleBorrow(BaseDTO.ImportDTO dto) {
        //sku信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> map = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, e -> e,(o1,o2)->o1));
        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        SampleBorrowExcelListener excelListenerUtil = new SampleBorrowExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),deptList, map, userList);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), SampleBorrowImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<SampleBorrowImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "样品借用单错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SampleBorrowImportExcelDTO.class);
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
    public void handleImportSuccessList(List<SampleBorrowImportExcelDTO> successList, List<String> errorNoList, List<SampleBorrowImportExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        if(CollUtil.isNotEmpty(errorNoList)){
            successList = successList.stream().filter(e -> StringUtils.isNotBlank(e.getNo()) && !errorNoList.contains(e.getNo())).collect(Collectors.toList());

            //全部返回到错误列表
            List<SampleBorrowImportExcelDTO> collect = successList.stream().filter(e -> StringUtils.isBlank(e.getNo()) || errorNoList.contains(e.getNo())).collect(Collectors.toList());
            errorList2.addAll(collect);
        }

        SampleBorrowInfoServiceImpl bean = ApplicationContextUtils.getBean(SampleBorrowInfoServiceImpl.class);

        //按序号分组
        Map<String, List<SampleBorrowImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(SampleBorrowImportExcelDTO::getNo));
        for (Map.Entry<String, List<SampleBorrowImportExcelDTO>> entry : collect.entrySet()) {
            List<SampleBorrowImportExcelDTO> value = entry.getValue();
            SampleBorrowImportExcelDTO importMainDTO = value.get(0);
            SampleBorrowInfoDTO.AddDTO addDTO = new SampleBorrowInfoDTO.AddDTO();
            BeanMapperUtils.copy(importMainDTO, addDTO);
            List<SampleBorrowDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (SampleBorrowImportExcelDTO importDTO : value) {
                SampleBorrowDetailDTO.AddDTO detailDTO = new SampleBorrowDetailDTO.AddDTO();
                BeanMapperUtils.copy(importDTO, detailDTO);
                //明细备注
                detailDTO.setRemark(importDTO.getDetailRemark());
                detailList.add(detailDTO);
            }
            addDTO.setDetailList(detailList);

            if (ImportTypeEnum.ADD.getCode().equals(importType)){
                bean.add(addDTO);
            }
        }
    }

    @Override
    public SampleBorrowInfoDTO.ViewDTO view(String id) {
        SampleBorrowInfoEntity sampleBorrowInfoEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到样品借用单数据"));
        SampleBorrowInfoDTO.ViewDTO data = BeanMapperUtils.map(SampleBorrowInfoDTO.ViewDTO.class, sampleBorrowInfoEntity);
        // 数据填充处理
        fillOne(data);

        List<SampleBorrowDetailEntity> sampleBorrowDetailEntities = sampleBorrowDetailService.listByMainId(id);
        List<SampleBorrowDetailDTO.ViewDTO> detailDTOList = BeanMapperUtils.copyList(SampleBorrowDetailDTO.ViewDTO.class, sampleBorrowDetailEntities);

        // 提取所有SKU编号，用于后续查询可用数量
        List<String> skuNos = sampleBorrowDetailEntities.stream().map(SampleBorrowDetailEntity::getSkuNo).distinct().collect(Collectors.toList());

        // 构造查询条件：根据用户ID和SKU列表查询样品台账中的可用数量
        SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
        dto.setUserId(sampleBorrowInfoEntity.getBorrowUserId());
        dto.setSkuNos(skuNos);
        dto.setType("borrow");
        List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(dto);
        Map<String, SampleLedgerDTO.SkuAvailableQtyDTO> sampleLedgerMap = skuAvailableQtyDTOS.stream().collect(Collectors.toMap(SampleLedgerDTO.SkuAvailableQtyDTO::getSampleLedgerId, Function.identity(),(o1, o2)-> o1));

        // 计算每个明细项中SKU的实际可用数量
        detailDTOList.forEach(detailDTO -> {
            String sampleLedgerId = detailDTO.getSampleLedgerId();
            SampleLedgerDTO.SkuAvailableQtyDTO sampleLedger = sampleLedgerMap.getOrDefault(sampleLedgerId, null);
            if(Objects.nonNull(sampleLedger)){
                detailDTO.setAvailableQty(sampleLedger.getAvailableQty());
                detailDTO.setUseUserId(sampleLedger.getUseUserId());
                detailDTO.setUseUserName(sampleLedger.getUseUserName());
            }
        });
        // 设置明细列表到主数据对象中
        data.setDetailList(detailDTOList);

        // 查询相关的附件信息
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

    public void startProcess(SampleBorrowInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SAMPLE_BORROW_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(SampleBorrowInfoDTO.ViewDTO data) {
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
        this.lambdaUpdate().eq(SampleBorrowInfoEntity::getId, id)
            .set(SampleBorrowInfoEntity::getApproveUserId, userInfo.getUid())
            .set(SampleBorrowInfoEntity::getApproveUserName, userInfo.getUserName())
            .set(SampleBorrowInfoEntity::getApproveStatus, approveStatus)
            .set(SampleBorrowInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new SampleBorrowInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SampleBorrowInfoEntity::getId, id)
            .set(SampleBorrowInfoEntity::getApproveUserId, "")
            .set(SampleBorrowInfoEntity::getApproveUserName, "")
            .set(SampleBorrowInfoEntity::getApproveStatus, approveStatus)
            .set(SampleBorrowInfoEntity::getApproveTime, null)
            .update(new SampleBorrowInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SampleBorrowInfoEntity::getId, id)
        .set(SampleBorrowInfoEntity::getApproveStatus, approveStatus)
        .update(new SampleBorrowInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SampleBorrowInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        LocalDate now = LocalDate.now();
        // 属性赋值
        for(SampleBorrowInfoDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));

            /*
            若“待归还数量 = 0“，则展示”完成归还“
            若“待归还数量＞0“，则根据N判断，N = 预计退回日期 - 当天日期：
            若N大于60，则显示2个月以上；
            若30＜N≤60，则显示2个月内；
            若0＜N≤30，则显示1个月内；
            若N = 0，则显示0天后超期，标红显示
            若N小于0，则显示已超期X天，标红显示，其中X取N的正数
            */
            String returnPeriod = "";
            Integer waitReturnQty = data.getWaitReturnQty();
            if(waitReturnQty == 0){
                returnPeriod = "完成归还";
            }else {
                //若“待归还数量＞0“，则根据N判断，N = 预计退回日期 - 当天日期：
                LocalDate estimatedReturnDate = data.getEstimatedReturnDate();
                if(Objects.nonNull(estimatedReturnDate)){
                    long between = ChronoUnit.DAYS.between(now, estimatedReturnDate);
                    if(between > 60){
                        returnPeriod = "2个月以上";
                    }else if(between > 30){
                        returnPeriod = "2个月内";
                    }else if(between > 0){
                        returnPeriod = "1个月内";
                    }else if(between == 0){
                        returnPeriod = "0天后超期";
                    }else {
                        returnPeriod = "已超期" + Math.abs(between) + "天";
                    }
                }
            }
            data.setReturnPeriod(returnPeriod);
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SampleBorrowInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleBorrowInfoEntity sampleBorrowInfoEntity) {
        //借用日期和预计退回日期比较，预计退回日期不能小于借用日期
        if(sampleBorrowInfoEntity.getBorrowDate().isAfter(sampleBorrowInfoEntity.getEstimatedReturnDate())){
            throw new ServiceException(ApiError.ERROR_SAMPLE_BORROW_DATA);
        }

        String borrowUserId = sampleBorrowInfoEntity.getBorrowUserId();
        String lendUserId = sampleBorrowInfoEntity.getLendUserId();
        List<FindUserDTO> users = sysUserFeign.getUserListByUserIds(Arrays.asList(borrowUserId, lendUserId));
        if(CollUtil.isEmpty(users) || users.size() != 2){
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }

        FindUserDTO borrowUser = users.stream().filter(e -> Objects.equals(e.getUserId(), borrowUserId)).findFirst().orElse(null);
        if(Objects.isNull(borrowUser)){
            throw new ServiceException(ApiError.NOT_EXIST,"借入人");
        }
        FindUserDTO lendUser = users.stream().filter(e -> Objects.equals(e.getUserId(), lendUserId)).findFirst().orElse(null);
        if(Objects.isNull(lendUser)){
            throw new ServiceException(ApiError.NOT_EXIST,"借出人");
        }

        String borrowDeptId = sampleBorrowInfoEntity.getBorrowDeptId();
        String lendDeptId = sampleBorrowInfoEntity.getLendDeptId();
        List<SysDepartmentEntity> sysDepartmentEntities = sysUserFeign.listDeptByIds(Arrays.asList(borrowDeptId, lendDeptId));
        if(CollUtil.isEmpty(sysDepartmentEntities) || sysDepartmentEntities.size() != 2){
            throw new ServiceException(ApiError.ERROR_9029);
        }

        SysDepartmentEntity borrowDept = sysDepartmentEntities.stream().filter(e -> Objects.equals(e.getId(), borrowDeptId)).findFirst().orElse(null);
        if(Objects.isNull(borrowDept)){
            throw new ServiceException(ApiError.NOT_EXIST,"借入部门");
        }
        SysDepartmentEntity lendDept = sysDepartmentEntities.stream().filter(e -> Objects.equals(e.getId(), lendDeptId)).findFirst().orElse(null);
        if(Objects.isNull(lendDept)){
            throw new ServiceException(ApiError.NOT_EXIST,"借入部门");
        }

        //赋值
        sampleBorrowInfoEntity.setBorrowUserName(borrowUser.getUserName());
        sampleBorrowInfoEntity.setLendUserName(lendUser.getUserName());
        sampleBorrowInfoEntity.setBorrowDeptName(borrowDept.getName());
        sampleBorrowInfoEntity.setLendDeptName(lendDept.getName());
    }
}
