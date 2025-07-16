package com.erp.server.scm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.SupplierCredentialStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.scm.mapper.SupplierCredentialMapper;
import com.erp.server.scm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_CREDENTIAL_REPORT;

/**
 * <p>
 * 供应商资质表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierCredentialServiceImpl extends SuperServiceImpl<SupplierCredentialMapper, SupplierCredentialEntity> implements SupplierCredentialService {


    @Resource
    private AttachmentService attachmentService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SupplierCredentialService self;

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 保存 供应商资质信息
     *
     * @param credentialList
     * @return void
     * @author yl
     * @date 2023-03-17 16:14
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBatchCredential(List<SupplierCredentialDTO.AddDTO> credentialList) {
        if (CollectionUtils.isEmpty(credentialList)) {
            return;
        }
        for (SupplierCredentialDTO.AddDTO item : credentialList) {
            self.add(item);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SupplierCredentialDTO.AddDTO dto) {
        if(StringUtils.isBlank(dto.getSupplierId())){
            throw new ServiceException("供应商不能为空");
        }
        SupplierEntity supplierEntity = supplierService.getByIdOpt(dto.getSupplierId()).orElseThrow(()->new ServiceException("未找到供应商数据"));

        Integer count = self.lambdaQuery()
                .eq(SupplierCredentialEntity::getSupplierId, dto.getSupplierId())
                .eq(SupplierCredentialEntity::getCode, dto.getCode())
                .count();
        if(count > 0){
            throw new ServiceException("【"+supplierEntity.getName()+"】【"+dto.getName()+"】资质已存在");
        }

        SupplierCredentialEntity addEntity = new SupplierCredentialEntity();
        String id = IdWorker.getIdStr();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(id);
        //校验有效期
        self.checkDate(addEntity);
        //获取状态
        self.getCredentialStatuses(addEntity);
        boolean save = self.save(addEntity);
        if(!save){
            throw new ServiceException("供应商证照新增失败");
        }

        //附件集合
        List<String> attachmentUrlList = dto.getAttachmentUrlList();
        //附件名
        List<String> attachmentNameList = dto.getAttachmentNameList();
        List<AttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<SupplierCredentialEntity> credentialClass = SupplierCredentialEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                AttachmentEntity attachment = new AttachmentEntity();
                attachment.setAttachUrl(attachmentUrlList.get(i));
                attachment.setAttachName(attachmentNameList.get(i));
                attachment.setBusinessId(id);
                attachment.setType(type);
                batchAttachmentList.add(attachment);
            }
            if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                attachmentService.saveBatch(batchAttachmentList);
            }
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】供应商证照【{}】", UserContext.getDefaultLoginUser().getUserName(), supplierEntity.getName() , dto.getName());
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUPPLIER.getCode(), supplierEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(id, id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void getCredentialStatuses(SupplierCredentialEntity addEntity) {
        // 判断当前时间是否在资质有效期之间
        LocalDate effectiveDate = addEntity.getEffectiveDate();
        LocalDate expireDate = addEntity.getExpireDate();
        //有效日期不为空
        if(Objects.nonNull(effectiveDate) && Objects.nonNull(expireDate)){
            SupplierCredentialStatusEnum status = SupplierCredentialStatusEnum.EXPIRED;
            LocalDate now = LocalDate.now();
            if(now.compareTo(effectiveDate) < 0){
                status = SupplierCredentialStatusEnum.NOT_EFFECTIVE;
            }else if(now.compareTo(effectiveDate) >= 0 && now.compareTo(expireDate) <= 0){
                status = SupplierCredentialStatusEnum.EFFECTIVE;
            }
            addEntity.setStatus(status.getCode());
        }else {
            addEntity.setStatus("");
        }
    }

    /**
     * 保存 供应商资质信息
     *
     * @param credentialList
     * @return void
     * @author yl
     * @date 2023-03-17 16:14
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateBatchCredential(List<SupplierCredentialDTO.UpdateDTO> credentialList) {
        if (CollectionUtils.isEmpty(credentialList)) {
            return;
        }
        for (SupplierCredentialDTO.UpdateDTO item : credentialList) {
            self.update(item);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SupplierCredentialDTO.UpdateDTO dto) {
        if(StringUtils.isBlank(dto.getId())){
            throw new ServiceException("主键id不能为空");
        }
        if(StringUtils.isBlank(dto.getSupplierId())){
            throw new ServiceException("供应商id不能为空");
        }
        SupplierCredentialEntity old = super.getByIdOpt(dto.getId()).orElseThrow(()->new ServiceException("未找到供应商证照数据"));

        SupplierEntity supplierEntity = supplierService.getByIdOpt(dto.getSupplierId()).orElseThrow(()->new ServiceException("未找到供应商数据"));

        Integer count = self.lambdaQuery()
                .eq(SupplierCredentialEntity::getSupplierId, dto.getSupplierId())
                .eq(SupplierCredentialEntity::getCode, dto.getCode())
                .ne(SupplierCredentialEntity::getId,dto.getId())
                .count();
        if(count > 0){
            throw new ServiceException("【"+supplierEntity.getName()+"】【"+dto.getName()+"】资质已存在");
        }

        SupplierCredentialEntity entity = new SupplierCredentialEntity();
        BeanMapper.copy(dto, entity);
        //校验有效期
        checkDate(entity);
        //获取状态
        self.getCredentialStatuses(entity);
        boolean save = self.updateById(entity);
        if(!save){
            throw new ServiceException("供应商证照更新失败");
        }

        //附件集合
        List<String> attachmentUrlList = dto.getAttachmentUrlList();
        List<String> attachmentNameList = dto.getAttachmentNameList();
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()){
            List<AttachmentDTO.UpdateDTO> oldAttachmentList = attachmentService.getByBusinessId(entity.getId());
            if(CollUtil.isNotEmpty(oldAttachmentList)){
                // 处理删除的数据
                List<AttachmentDTO.UpdateDTO> remove = oldAttachmentList.stream()
                        .filter(oldAttachment -> !attachmentUrlList.contains(oldAttachment.getAttachUrl()))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(remove)){
                    attachmentService.deleteByUrlList(remove.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
                }
            }

            //处理需要新增的数据
            List<String> oldUrlList = oldAttachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            List<String> add = attachmentUrlList.stream()
                    .filter(url -> !oldUrlList.contains(url))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(add)){
                Class<SupplierCredentialEntity> credentialClass = SupplierCredentialEntity.class;
                TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
                //获取到表名
                String type = tableName.value();
                List<AttachmentEntity> batchAttachmentList = new ArrayList<>(10);
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    if(!add.contains(attachmentUrlList.get(i))){
                        continue;
                    }
                    AttachmentEntity addAttachment = new AttachmentEntity();
                    addAttachment.setAttachUrl(attachmentUrlList.get(i));
                    addAttachment.setAttachName(attachmentNameList.get(i));
                    addAttachment.setBusinessId(entity.getId());
                    addAttachment.setType(type);
                    batchAttachmentList.add(addAttachment);
                }

                if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                    attachmentService.saveBatch(batchAttachmentList);
                }
            }
        }else {
            throw new ServiceException("附件不能为空");
        }
        //操作日志
        String msg = StrUtil.format("用户【{}】更新【{}】供应商证照", UserContext.getDefaultLoginUser().getUserName(), supplierEntity.getName());
        moduleOperateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.SUPPLIER.getCode(), dto.getSupplierId(), "", msg);
        return Boolean.TRUE;
    }

    /**
     * 根据供应商ｉｄ　获取资质信息
     *
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SupplierCredentialDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-20 10:19
     */
    @Override
    public List<SupplierCredentialDTO.UpdateDTO> getBySupplierId(String supplierId) {
        List<SupplierCredentialEntity> list = this.getList(supplierId);
        List<SupplierCredentialDTO.UpdateDTO> resultList = BeanMapper.copyList(list, SupplierCredentialDTO.UpdateDTO.class);
        //获取到业务表id
        List<String> businessIds = resultList.stream().map(SupplierCredentialDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(businessIds);
        for (SupplierCredentialDTO.UpdateDTO item : resultList) {
            // 判断当前时间是否在资质有效期之间
            LocalDate effectiveDate = item.getEffectiveDate();
            LocalDate expireDate = item.getExpireDate();
            String status = updateStatusByDate(item.getId(), item.getStatus(), effectiveDate, expireDate);
            item.setStatus(status);

            item.setStatusName(SupplierCredentialStatusEnum.getName(item.getStatus()));

            List<String> attachmentUrlList = attachmentList.stream().
                    filter(a -> a.getBusinessId().equals(item.getId())).
                    map(AttachmentDTO.UpdateDTO::getAttachUrl).
                    collect(Collectors.toList());
            List<String> attachmentNameList = attachmentList.stream().
                    filter(a -> a.getBusinessId().equals(item.getId())).
                    map(AttachmentDTO.UpdateDTO::getAttachName).
                    collect(Collectors.toList());
            item.setAttachmentUrlList(attachmentUrlList);
            item.setAttachmentNameList(attachmentNameList);
        }
        return resultList;
    }


    /**
     * 修改供应商资质信息
     *
     * @param credentialList
     * @param supplierId
     * @return void
     * @author yl
     * @date 2023-03-20 11:37
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCredential(List<SupplierCredentialDTO.UpdateDTO> credentialList, String supplierId) {
        // 根据 code 来校验是否有重复
        Map<String, List<SupplierCredentialDTO.UpdateDTO>> collect = credentialList.stream()
                .collect(Collectors.groupingBy(SupplierCredentialDTO.UpdateDTO::getCode));
        // 判断是否有重复的 code
        for (Map.Entry<String, List<SupplierCredentialDTO.UpdateDTO>> entry : collect.entrySet()) {
            SupplierCredentialDTO.UpdateDTO updateDTO = entry.getValue().get(0);

            if (entry.getValue().size() > 1) {
                throw new ServiceException("存在重复的资质编码：" + updateDTO.getName());
            }

            if(StringUtils.isNotBlank(updateDTO.getId())){
                Integer count = self.lambdaQuery()
                        .eq(SupplierCredentialEntity::getSupplierId, supplierId)
                        .eq(SupplierCredentialEntity::getCode, entry.getKey())
                        .ne(SupplierCredentialEntity::getId,updateDTO.getId())
                        .count();
                if(count > 0){
                    throw new ServiceException("【"+updateDTO.getName()+"】资质已存在");
                }
            }
        }

        //这是要添加的
        List<SupplierCredentialDTO.UpdateDTO> addList = credentialList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        List<SupplierCredentialEntity> saveOrUpdateList = new ArrayList<>(credentialList.size());
        Class<SupplierCredentialEntity> credentialClass = SupplierCredentialEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        List<AttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        List<SupplierCredentialEntity> dbList = this.getList(supplierId);


        List<String> deleteIdList = getDeleteIds(credentialList, dbList);
        //这是要删除的
        List<SupplierCredentialEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        for (SupplierCredentialDTO.UpdateDTO item : credentialList) {
            SupplierCredentialEntity entity = new SupplierCredentialEntity();
            BeanMapper.copy(item, entity);
            entity.setSupplierId(supplierId);
            //校验有效期
            self.checkDate(entity);
            //获取状态
            self.getCredentialStatuses(entity);
            if(StringUtils.isNotBlank(item.getId())){//编辑
                saveOrUpdateList.add(entity);
            }else {//新增
                entity.setId(IdWorker.getIdStr());
                self.save(entity);
            }

            //附件集合
            List<String> attachmentUrlList = item.getAttachmentUrlList();
            List<String> attachmentNameList = item.getAttachmentNameList();
            if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {

                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    AttachmentEntity addAttachment = new AttachmentEntity();
                    addAttachment.setAttachUrl(attachmentUrlList.get(i));
                    addAttachment.setAttachName(attachmentNameList.get(i));
                    addAttachment.setBusinessId(entity.getId());
                    addAttachment.setType(type);
                    batchAttachmentList.add(addAttachment);
                }

            }
        }
        //这是修改的
        List<SupplierCredentialEntity> updateList = saveOrUpdateList.stream().filter(s -> StringUtils.isNotBlank(s.getId())).collect(Collectors.toList());

        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(supplierId, obj.getName())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("删除了一个资质名称【%s】", ModuleTypeEnum.SUPPLIER.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(supplierId, obj.getName())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("添加了一个资质名称【%s】", ModuleTypeEnum.SUPPLIER.getCode(), addPairList, "编辑操作");

        //修改的
        for (SupplierCredentialEntity update : updateList) {
            String id = update.getId();
            SupplierCredentialEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                moduleOperateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.SUPPLIER.getCode(), supplierId, "", "");
            }
        }
        if(CollectionUtils.isNotEmpty(saveOrUpdateList)){
            this.saveOrUpdateBatch(saveOrUpdateList);
        }
        if(CollectionUtils.isNotEmpty(batchAttachmentList)){
            attachmentService.saveBatch(batchAttachmentList);
        }


    }


    /**
     * 获取要删除的
     *
     * @param updateList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-31 15:54
     */
    private List<String> getDeleteIds(List<SupplierCredentialDTO.UpdateDTO> updateList, List<SupplierCredentialEntity> dbList) {
        List<String> ids = updateList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SupplierCredentialDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SupplierCredentialEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    /**
     * 根据供应商id 集合删除资质信息
     *
     * @param supplierIds
     * @return void
     * @author yl
     * @date 2023-03-20 18:56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBySupplierIds(List<String> supplierIds) {
        if (CollectionUtils.isEmpty(supplierIds)) {
            return;
        }
        List<SupplierCredentialEntity> allList = this.getList(supplierIds);
        List<String> idList = allList.stream().map(SupplierCredentialEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(idList)) {
            LambdaQueryWrapper<SupplierCredentialEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SupplierCredentialEntity::getSupplierId, idList);
            this.remove(queryWrapper);
            attachmentService.deleteByBusinessIds(idList);
        }
    }


    /**
     * 检查资质日期
     *
     * @param credentialList
     * @return void
     * @author yl
     * @date 2023-03-29 15:48
     */
    @Override
    public void checkListDate(List<SupplierCredentialDTO.AddDTO> credentialList) {
        if (CollectionUtils.isNotEmpty(credentialList)) {
            List<SupplierCredentialDTO.AddDTO> list = credentialList.stream().filter(c -> c.getEffectiveDate() != null && c.getExpireDate() != null).collect(Collectors.toList());
            long count = list.stream().filter(c -> c.getExpireDate().compareTo(c.getEffectiveDate()) < 0).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_98037);
            }
        }
    }

    /**
     * 检查资质日期
     * @param
     * @return void
     * @author jack
     * @date 2025-06-23
     */
    @Override
    public void checkDate(SupplierCredentialEntity supplierCredentialEntity) {
        if (Objects.nonNull(supplierCredentialEntity)) {
            LocalDate effectiveDate = supplierCredentialEntity.getEffectiveDate();
            LocalDate expireDate = supplierCredentialEntity.getExpireDate();
            if(Objects.nonNull(effectiveDate) && Objects.nonNull(expireDate) && expireDate.compareTo(effectiveDate) < 0){
                throw new ServiceException(ApiError.ERROR_98037);
            }
        }
    }



    /**
     * 转化 导入的数据
     *
     * @param supplierId
     * @param credentialList
     * @return java.util.List<com.erp.model.scm.entity.SupplierAccountEntity>
     * @author yl
     * @date 2023-03-31 9:11
     */
    @Override
    public List<SupplierCredentialEntity> transform(String supplierId, List<SupplierCredentialDTO.ImportAddDTO> credentialList) {
        if (CollectionUtils.isEmpty(credentialList)) {
            return Collections.emptyList();
        }
        List<SupplierCredentialEntity> addList = new ArrayList<>(credentialList.size());
        for (SupplierCredentialDTO.ImportAddDTO item : credentialList) {
            SupplierCredentialEntity credential = new SupplierCredentialEntity();
            BeanMapper.copy(item, credential);
            credential.setSupplierId(supplierId);
            addList.add(credential);
        }
        return addList;
    }

    private List<SupplierCredentialEntity> getList(String supplierId) {
        LambdaQueryWrapper<SupplierCredentialEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SupplierCredentialEntity::getSupplierId, supplierId);
        return list(queryWrapper);
    }


    private List<SupplierCredentialEntity> getList(List<String> supplierIdS) {
        if (CollectionUtils.isEmpty(supplierIdS)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SupplierCredentialEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SupplierCredentialEntity::getSupplierId, supplierIdS);
        return list(queryWrapper);
    }



    @Override
    public List<SupplierCredentialDTO.TabListDTO> tabList(PermissionsDTO param) {
        SupplierCredentialDTO.PagingParamDTO searchParam = new SupplierCredentialDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SupplierCredentialDTO.TabListDTO> list = baseMapper.tabList(searchParam);

        Map<String, SupplierCredentialDTO.TabListDTO> map = list.stream().collect(Collectors.toMap(SupplierCredentialDTO.TabListDTO::getTabFlag, t -> t));
        List<SupplierCredentialDTO.TabListDTO> result = new ArrayList<>();
        result.add(new SupplierCredentialDTO.TabListDTO( SupplierCredentialStatusEnum.NOT_EFFECTIVE.getCode(), SupplierCredentialStatusEnum.NOT_EFFECTIVE.getName() , map.containsKey(SupplierCredentialStatusEnum.NOT_EFFECTIVE.getCode()) ? map.get(SupplierCredentialStatusEnum.NOT_EFFECTIVE.getCode()).getCount() : 0   ));
        result.add(new SupplierCredentialDTO.TabListDTO( SupplierCredentialStatusEnum.EFFECTIVE.getCode(), SupplierCredentialStatusEnum.EFFECTIVE.getName() , map.containsKey(SupplierCredentialStatusEnum.EFFECTIVE.getCode()) ? map.get(SupplierCredentialStatusEnum.EFFECTIVE.getCode()).getCount() : 0   ));
        result.add(new SupplierCredentialDTO.TabListDTO( SupplierCredentialStatusEnum.EXPIRED.getCode(), SupplierCredentialStatusEnum.EXPIRED.getName() , map.containsKey(SupplierCredentialStatusEnum.EXPIRED.getCode()) ? map.get(SupplierCredentialStatusEnum.EXPIRED.getCode()).getCount() : 0   ));
        return result;
    }

    @Override
    public PagingVO<SupplierCredentialDTO.ListDTO> paging(PagingDTO<SupplierCredentialDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SupplierCredentialDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<SupplierCredentialDTO.ListDTO> records) {
        List<String> ids = records.stream().map(SupplierCredentialDTO.ListDTO::getId).distinct().collect(Collectors.toList());

        Class<SupplierCredentialEntity> credentialClass = SupplierCredentialEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        //附件信息
        Map<String, List<AttachmentDTO.UpdateDTO>> attachmentMap = attachmentService.getByBusinessIdAndType(ids, type).stream().collect(Collectors.groupingBy(AttachmentDTO.UpdateDTO::getBusinessId));

        for (SupplierCredentialDTO.ListDTO record : records) {
            //更新状态
            // 判断当前时间是否在资质有效期之间
            LocalDate effectiveDate = record.getEffectiveDate();
            LocalDate expireDate = record.getExpireDate();
            String status = updateStatusByDate(record.getId(), record.getStatus(), effectiveDate, expireDate);
            record.setStatus(status);
            record.setStatusName(SupplierCredentialStatusEnum.getName(record.getStatus()));
            record.setSupplierStatusName(ApproveStatusEnum.getName(record.getSupplierStatus()));

            List<AttachmentDTO.UpdateDTO> attachmentList = attachmentMap.get(record.getId());
            if(CollUtil.isNotEmpty(attachmentList)){
                List<String> attachmentUrlList = attachmentList.stream().
                        filter(a -> a.getBusinessId().equals(record.getId())).
                        map(AttachmentDTO.UpdateDTO::getAttachUrl).
                        collect(Collectors.toList());
                List<String> attachmentNameList = attachmentList.stream().
                        filter(a -> a.getBusinessId().equals(record.getId())).
                        map(AttachmentDTO.UpdateDTO::getAttachName).
                        collect(Collectors.toList());
                record.setAttachmentUrlList(attachmentUrlList);
                record.setAttachmentNameList(attachmentNameList);
            }
        }
    }

    private String updateStatusByDate(String id ,String oldStatus,LocalDate effectiveDate, LocalDate expireDate) {
        //有效日期不为空
        if(Objects.nonNull(effectiveDate) && Objects.nonNull(expireDate)){
            SupplierCredentialStatusEnum status = SupplierCredentialStatusEnum.EXPIRED;
            LocalDate now = LocalDate.now();
            if(now.compareTo(effectiveDate) < 0){
                status = SupplierCredentialStatusEnum.NOT_EFFECTIVE;
            }else if(now.compareTo(effectiveDate) >= 0 && now.compareTo(expireDate) <= 0){
                status = SupplierCredentialStatusEnum.EFFECTIVE;
            }
            if(!Objects.equals(oldStatus,status.getCode())){
                //更新状态值
                lambdaUpdate()
                        .set(SupplierCredentialEntity::getStatus,status.getCode())
                        .eq(SupplierCredentialEntity::getId, id)
                        .update();

                return status.getCode();

            }else {
                return oldStatus;
            }
        }else {
            //更新状态值
            lambdaUpdate()
                    .set(SupplierCredentialEntity::getStatus,"")
                    .eq(SupplierCredentialEntity::getId, id )
                    .update();

            return "";
        }
    }

    @Override
    public List<SupplierCredentialDTO.ViewDTO> view(List<String> ids) {
        List<SupplierCredentialEntity> list = this.listByIds(ids);
        List<SupplierCredentialDTO.ViewDTO> resultList = BeanMapper.copyList(list, SupplierCredentialDTO.ViewDTO.class);

        // 数据填充处理
        fillOne(resultList);
        return resultList;
    }

    private void fillOne(List<SupplierCredentialDTO.ViewDTO> resultList) {
        if(CollUtil.isEmpty(resultList)){
            return ;
        }
        List<String> ids = resultList.stream().map(SupplierCredentialDTO.ViewDTO::getId).distinct().collect(Collectors.toList());

        Class<SupplierCredentialEntity> credentialClass = SupplierCredentialEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        //附件信息
        Map<String, List<AttachmentDTO.UpdateDTO>> attachmentMap = attachmentService.getByBusinessIdAndType(ids, type).stream().collect(Collectors.groupingBy(AttachmentDTO.UpdateDTO::getBusinessId));

        for (SupplierCredentialDTO.ViewDTO record : resultList) {
            List<AttachmentDTO.UpdateDTO> attachmentList = attachmentMap.get(record.getId());
            if(CollUtil.isNotEmpty(attachmentList)){
                List<String> attachmentUrlList = attachmentList.stream().
                        filter(a -> a.getBusinessId().equals(record.getId())).
                        map(AttachmentDTO.UpdateDTO::getAttachUrl).
                        collect(Collectors.toList());
                List<String> attachmentNameList = attachmentList.stream().
                        filter(a -> a.getBusinessId().equals(record.getId())).
                        map(AttachmentDTO.UpdateDTO::getAttachName).
                        collect(Collectors.toList());
                record.setAttachmentUrlList(attachmentUrlList);
                record.setAttachmentNameList(attachmentNameList);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SupplierCredentialEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到供应商证照数据"));

        String supplierId = entity.getSupplierId();
        SupplierEntity supplierEntity = supplierService.getByIdOpt(supplierId).orElseThrow(()->new ServiceException("未找到供应商数据"));

        if(Objects.equals(supplierEntity.getApproveStatus() , ApproveStatusEnum.APPROVE) ){
            throw new ServiceException("已审核供应商-不支持删除");
        }

        // 删除主单数据
        super.removeById(id);

        // 删除日志数据
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作", UserContext.getDefaultLoginUser().getUserName(), supplierEntity.getCode(), "供应商证照管理");
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUPPLIER.getCode(), supplierEntity.getId(), "删除供应商证照数据");
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DELETE);
    }

    @Override
    public void exportList(SupplierCredentialDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("供应商证照管理导出", EXPORT_SCM_SUPPLIER_CREDENTIAL_REPORT.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO updateStatus(String id) {
        SupplierCredentialEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到供应商证照数据"));

        String supplierId = entity.getSupplierId();
        SupplierEntity supplierEntity = supplierService.getByIdOpt(supplierId).orElseThrow(()->new ServiceException("未找到供应商数据"));
        //校验有效期
        self.checkDate(entity);
        // 判断当前时间是否在资质有效期之间
        LocalDate effectiveDate = entity.getEffectiveDate();
        LocalDate expireDate = entity.getExpireDate();
        SupplierCredentialStatusEnum status = SupplierCredentialStatusEnum.EXPIRED;
        LocalDate now = LocalDate.now();
        if(now.isBefore(effectiveDate)){
            status = SupplierCredentialStatusEnum.NOT_EFFECTIVE;
        }else if(now.isAfter(effectiveDate) && now.isBefore(expireDate)){
            status = SupplierCredentialStatusEnum.EFFECTIVE;
        }

        lambdaUpdate()
                .eq(SupplierCredentialEntity::getId , id)
                .set(SupplierCredentialEntity::getStatus, status.getCode())
                .update();

        // 日志数据
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据更新生效状态由{}为{}", UserContext.getDefaultLoginUser().getUserName(), supplierEntity.getCode(), "供应商证照管理",SupplierCredentialStatusEnum.getName(entity.getStatus()),status.getName());
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUPPLIER.getCode(), supplierEntity.getId(), "更新供应商证照数据");

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public DictBasicDTO addDictCredential(String credentialName) {
        if(StringUtils.isBlank(credentialName)){
            return null;
        }

        //校验名称是否已存在
        Integer count = dictBasicService.lambdaQuery()
                .eq(DictBasicEntity::getName, credentialName)
                .eq(DictBasicEntity::getType, DictBasicEnum.CREDENTIAL_TYPE.getType())
                .count();
        if(count > 0){
            throw new ServiceException(ApiError.ERROR_98124);
        }

        DictBasicEntity dictBasicEntity = new DictBasicEntity();
        dictBasicEntity.setValue("CT_"+IdWorker.getIdStr());
        dictBasicEntity.setName(credentialName);
        dictBasicEntity.setType(DictBasicEnum.CREDENTIAL_TYPE.getType());
        dictBasicEntity.setTypeName(DictBasicEnum.CREDENTIAL_TYPE.getDesc());
        dictBasicEntity.setStatus(Boolean.TRUE);
        boolean save = dictBasicService.save(dictBasicEntity);
        if(!save){
            throw new ServiceException("自定义证照新增失败");
        }
        DictBasicDTO dto = new DictBasicDTO();
        BeanMapper.copy(dictBasicEntity, dto);
        return dto;
    }


}
