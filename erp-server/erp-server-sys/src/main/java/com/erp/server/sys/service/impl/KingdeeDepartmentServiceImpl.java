package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.sys.mapper.KingdeeDepartmentMapper;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysDeptService;
import com.erp.server.sys.service.KingdeeDepartmentService;
import com.erp.server.sys.service.KingdeePostService;
import com.erp.server.sys.service.SysAccountingCompanyService;
import com.erp.server.sys.service.SysDepartmentService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@Service
public class KingdeeDepartmentServiceImpl extends SuperServiceImpl<KingdeeDepartmentMapper, KingdeeDepartmentEntity> implements KingdeeDepartmentService {



    @Autowired
    private SysAccountingCompanyService sysAccountingCompanyService;

    @Autowired
    private SysDepartmentService sysDepartmentService;

    @Autowired
    private SyncKingdeeSysDeptService syncKingdeeSysDeptService;

    @Autowired
    private KingdeePostService kingdeePostService;

    @Autowired
    private DmpMqFeign dmpMqFeign;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(KingdeeDepartmentDTO.AddDTO addDTO) {
        KingdeeDepartmentEntity kingdeeDepartmentEntity = new KingdeeDepartmentEntity();
        BeanMapperUtils.copy(addDTO, kingdeeDepartmentEntity);
        // 数据处理
        handleData(kingdeeDepartmentEntity);
        boolean save = super.save(kingdeeDepartmentEntity);
        if (!save) {
            throw new ServiceException("保存失败");
        }
        //金蝶推送
        DmpPushTaskEntity pushTaskEntity = syncKingdeeSysDeptService.syncDataToKingdee(kingdeeDepartmentEntity, SyncOperateEnum.OPERATE_ADD.getCode());
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
            }
        });
        return save;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeeDepartmentDTO.UpdateDTO updateDTO) {
        KingdeeDepartmentEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶部门不存在"));
        String oldOrgId = old.getUseOrgId();
        String newOrgId = updateDTO.getUseOrgId();
        if (!oldOrgId.equals(newOrgId)) {
            throw new ServiceException("组织不能修改");
        }

        KingdeeDepartmentEntity kingdeeDepartmentEntity = BeanMapperUtils.map(KingdeeDepartmentEntity.class, updateDTO);
        kingdeeDepartmentEntity.setKingdeeId(old.getKingdeeId());
        kingdeeDepartmentEntity.setKingdeeDeptCode(old.getKingdeeDeptCode());
        // 数据处理
        handleData(kingdeeDepartmentEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(kingdeeDepartmentEntity);
        if (!save) {
            throw new ServiceException("保存失败");
        }
       //金蝶推送
        DmpPushTaskEntity pushTaskEntity = syncKingdeeSysDeptService.syncDataToKingdee(kingdeeDepartmentEntity, SyncOperateEnum.OPERATE_UPDATE.getCode());
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
            }
        });
        return Boolean.TRUE;
    }

    @Override
    public Boolean init() {
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_DEPARTMENT.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        //审核状态
        queryFilters.add(StrUtil.format(" FDocumentStatus = {}", "'C'"));
        //禁用状态
        queryFilters.add(StrUtil.format(" FFORBIDSTATUS = {}", "'A'"));
        //查询
        String fieldKeys = "FDEPTID,FNumber,FName,FUseOrgId.FNumber,FParentID.FNumber";
        String filterStr = String.join(" and ", queryFilters);
        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取100条
        Integer pageSize = 1000;
        List<KingdeeDepartmentDTO.KingdeeDTO> deptList = new ArrayList<>(20);
        Boolean dataSign = true;
        while (dataSign) {
            List<Map<String, Object>> result = apiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            List<KingdeeDepartmentDTO.KingdeeDTO> entityList = result.stream().map(obj ->
                    BeanUtil.toBean(obj, KingdeeDepartmentDTO.KingdeeDTO.class)).collect(Collectors.toList());
            deptList.addAll(entityList);
            pageIndex++;
        }
        //数据库存在的
        List<KingdeeDepartmentEntity> dbList = this.list();
        List<BaseIdDTO.CodeDTO> orgList = sysAccountingCompanyService.getByIds(Collections.emptyList());
        List<KingdeeDepartmentEntity> saveOrUpdateList = new ArrayList<>(20);
        //erp系统部门列表
        List<SysDepartmentEntity> erpDeptList = sysDepartmentService.list();

        for (KingdeeDepartmentDTO.KingdeeDTO item : deptList) {
            String kingdeeId = item.getKingdeeId();
            String kingdeeDeptName = item.getKingdeeDeptName();
            String kingdeeDeptCode = item.getKingdeeDeptCode();
            String useOrgCode = item.getUseOrgCode();
            String parentKingdeeCode = item.getParentKingdeeCode();
            if ("null".equals(parentKingdeeCode)) {
                parentKingdeeCode = "0";
            }
            BaseIdDTO.CodeDTO orgInfo = orgList.stream().filter(org -> org.getCode().equals(useOrgCode)).
                    findFirst().orElse(null);
            if (Objects.isNull(orgInfo)) {
                continue;
            }
            KingdeeDepartmentEntity dbEntity = dbList.stream().filter(entity -> entity.getKingdeeId().equals(kingdeeId)).
                    findFirst().orElse(null);
            if (Objects.isNull(dbEntity)) {
                KingdeeDepartmentEntity addEntity = new KingdeeDepartmentEntity();
                addEntity.setKingdeeId(kingdeeId);
                addEntity.setKingdeeDeptCode(kingdeeDeptCode);
                addEntity.setKingdeeDeptName(kingdeeDeptName);
                addEntity.setUseOrgId(orgInfo.getId());
                addEntity.setUseOrgName(orgInfo.getName());
                addEntity.setParentKingdeeCode(parentKingdeeCode);
                String erpDeptId = erpDeptList.stream().filter(d -> d.getName().equals(kingdeeDeptName)).
                        map(SysDepartmentEntity::getId).findFirst().orElse("");
                addEntity.setErpDeptId(erpDeptId);
                saveOrUpdateList.add(addEntity);
            } else {
                //表示有
                if (!dbEntity.getKingdeeDeptCode().equals(kingdeeDeptCode) || !dbEntity.getKingdeeDeptName().equals(kingdeeDeptName) ||
                        !dbEntity.getUseOrgId().equals(orgInfo.getId()) || !dbEntity.getParentKingdeeCode().
                        equals(parentKingdeeCode)) {
                    dbEntity.setKingdeeDeptCode(kingdeeDeptCode);
                    dbEntity.setKingdeeDeptName(kingdeeDeptName);
                    dbEntity.setUseOrgId(orgInfo.getId());
                    dbEntity.setUseOrgName(orgInfo.getName());
                    dbEntity.setParentKingdeeCode(parentKingdeeCode);
                    saveOrUpdateList.add(dbEntity);
                }
            }
        }
        return this.saveOrUpdateBatch(saveOrUpdateList);
    }

    @Override
    public KingdeeDepartmentDTO.ViewDTO view(String id) {
        KingdeeDepartmentEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("金蝶部门不存在");
        }
        KingdeeDepartmentDTO.ViewDTO viewDTO = new KingdeeDepartmentDTO.ViewDTO();
        BeanUtil.copyProperties(entity, viewDTO);
        //父级别金蝶code
        String parentKingdeeCode = entity.getParentKingdeeCode();
        //erp 系统id
        String erpDeptId = entity.getErpDeptId();
        SysDepartmentEntity sysDepartmentEntity = sysDepartmentService.getById(erpDeptId);
        if (Objects.nonNull(sysDepartmentEntity)) {
            viewDTO.setErpDeptName(sysDepartmentEntity.getName());
        }
        KingdeeDepartmentEntity parentEntity = this.getParentDeptByKingdeeCode(parentKingdeeCode);
        if (Objects.nonNull(parentEntity)) {
            viewDTO.setParentDeptName(parentEntity.getKingdeeDeptName());
            viewDTO.setParentId(parentEntity.getId());
        } else {
            viewDTO.setParentDeptName("");
            viewDTO.setParentId("0");
        }
        return viewDTO;
    }

    @Override
    public PagingVO<KingdeeDepartmentDTO.PagingViewDTO> paging(PagingDTO<KingdeeDepartmentDTO.PagingParamDTO> dto) {
        KingdeeDepartmentDTO.PagingParamDTO paramDTO = dto.getParams();
        paramDTO.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<KingdeeDepartmentDTO.PagingViewDTO> pageData = this.baseMapper.paging(query, paramDTO);
        List<KingdeeDepartmentDTO.PagingViewDTO> list=pageData.getRecords();
        fillList(list);
        return new PagingVO(pageData);
    }

    private void fillList(List<KingdeeDepartmentDTO.PagingViewDTO> list) {
        List<String> erpDeptIdList = list.stream().map(KingdeeDepartmentDTO.PagingViewDTO::getErpDeptId).
                distinct().collect(Collectors.toList());

        List<String> parentKingdeeCodeList = list.stream().map(KingdeeDepartmentDTO.PagingViewDTO::getParentKingdeeCode).
                distinct().collect(Collectors.toList());
        List<SysDepartmentEntity> erpDeptList = CollectionUtils.isNotEmpty(erpDeptIdList) ? sysDepartmentService.listByIds(erpDeptIdList) : Collections.emptyList();

        List<KingdeeDepartmentEntity> kingdeeDepartmentList = this.listByParentKingdeeCodeList(parentKingdeeCodeList);
        for(KingdeeDepartmentDTO.PagingViewDTO item:list){
            String erpDeptId = item.getErpDeptId();
            String erpDeptName = erpDeptList.stream().filter(x->x.getId().equals(erpDeptId)).findFirst().
                    map(SysDepartmentEntity::getName).orElse("");
            item.setErpDeptName(erpDeptName);
            String parentKingdeeCode = item.getParentKingdeeCode();
            KingdeeDepartmentEntity parentDepartment = kingdeeDepartmentList.stream().filter(k -> k.getKingdeeDeptCode().
                    equals(parentKingdeeCode)).findFirst().orElse(null);
            if (Objects.nonNull(parentDepartment)) {
                item.setParentDeptName(parentDepartment.getKingdeeDeptName());
                item.setParentId(parentDepartment.getId());
            }
        }

    }

    private List<KingdeeDepartmentEntity> listByParentKingdeeCodeList(List<String> parentKingdeeCodeList) {
        if (CollectionUtils.isEmpty(parentKingdeeCodeList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(KingdeeDepartmentEntity::getKingdeeDeptCode,parentKingdeeCodeList).list();
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId, String syncKingdeeCode) {
         return this.lambdaUpdate()
                 .eq(KingdeeDepartmentEntity::getId, id)
                 .set(StringUtils.isNotBlank(syncKingdeeId), KingdeeDepartmentEntity::getKingdeeId, syncKingdeeId)
                 .set(StringUtils.isNotBlank(syncKingdeeCode), KingdeeDepartmentEntity::getKingdeeDeptCode, syncKingdeeCode)
                 .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO delete(String id) {
        KingdeeDepartmentEntity entity = super.getById(id);
        List<KingdeePostEntity> postList = kingdeePostService.listByKingDeptId(id);
        if (CollectionUtils.isNotEmpty(postList)) {
            throw new ServiceException("该部门下存在任岗信息,无法删除");
        }
        String kingdeeDeptCode = entity.getKingdeeDeptCode();
        int count = this.lambdaQuery().eq(KingdeeDepartmentEntity::getParentKingdeeCode, kingdeeDeptCode).count();
        if (count > 0) {
            throw new ServiceException("该部门下存在子部门,无法删除");
        }

        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶部门不存在"));
        Boolean result = this.removeById(id);
        if (result) {
            //金蝶推送
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSysDeptService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_DELETE.getCode());
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
                }
            });
        }
        return BatchResultDTO.success(entity.getId(), entity.getKingdeeDeptCode(), OperationTypeEnum.DELETE);

    }

    @Override
    public List<KingdeeDepartmentEntity> listByOrgId(String orgId) {
        return this.lambdaQuery().eq(KingdeeDepartmentEntity::getUseOrgId, orgId).list();
    }

    @Override
    public List<KingdeeDepartmentDTO.TreeViewDTO> tree(String orgId) {
        List<KingdeeDepartmentEntity> allList=this.listByOrgId(orgId);
        List<KingdeeDepartmentDTO.TreeViewDTO> resultList = new ArrayList<>(20);
        List<KingdeeDepartmentEntity> pList=allList.stream().filter(d->d.getParentKingdeeCode().equals("0")).collect(Collectors.toList());
        for (KingdeeDepartmentEntity item : pList) {
            KingdeeDepartmentDTO.TreeViewDTO treeView=new KingdeeDepartmentDTO.TreeViewDTO();
            treeView.setId(item.getId());
            treeView.setName(item.getKingdeeDeptName());
            treeView.setParentId("0");
            treeView.setParentName("");
            treeView.setChildrenList(getChildrenList(item,allList));
            resultList.add(treeView);
        }
        return resultList;
    }

    @Override
    public KingdeeDepartmentEntity getInfo(DeptKingdeeDTO.FindDeptKingdeeDTO dto) {
        String erpDeptId = dto.getDeptId();
        String orgId = dto.getOrgId();
        KingdeeDepartmentEntity entity = this.lambdaQuery().eq(KingdeeDepartmentEntity::getUseOrgId, orgId).
                eq(KingdeeDepartmentEntity::getErpDeptId, erpDeptId).last("LIMIT 1").one();
        return entity;
    }

    private List<KingdeeDepartmentDTO.TreeViewDTO> getChildrenList(KingdeeDepartmentEntity item, List<KingdeeDepartmentEntity> allList) {
        List<KingdeeDepartmentDTO.TreeViewDTO> resultList=new ArrayList<>(10);
        List<KingdeeDepartmentEntity> list=allList.stream().filter(d->d.getParentKingdeeCode().equals(item.getKingdeeDeptCode())).collect(Collectors.toList());
        for (KingdeeDepartmentEntity entity : list) {
            KingdeeDepartmentDTO.TreeViewDTO treeView=new KingdeeDepartmentDTO.TreeViewDTO();
            treeView.setId(entity.getId());
            treeView.setName(entity.getKingdeeDeptName());
            treeView.setParentId(item.getId());
            treeView.setParentName(item.getKingdeeDeptName());
            treeView.setChildrenList(getChildrenList(entity,allList));
            resultList.add(treeView);
        }
        return CollectionUtils.isEmpty(resultList) ? null : resultList;

    }

    private KingdeeDepartmentEntity getParentDeptByKingdeeCode(String parentKingdeeCode) {
        return this.lambdaQuery().eq(KingdeeDepartmentEntity::getKingdeeDeptCode, parentKingdeeCode).last("LIMIT 1").one();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(KingdeeDepartmentEntity kingdeeDepartmentEntity) {
        String useOrgId = kingdeeDepartmentEntity.getUseOrgId();
        SysAccountingCompanyEntity orgInfo= sysAccountingCompanyService.getById(useOrgId);
        if(Objects.isNull(orgInfo)){
            throw new ServiceException("组织信息不存在");
        }
        kingdeeDepartmentEntity.setUseOrgName(orgInfo.getCompanyName());
        String deptName = kingdeeDepartmentEntity.getKingdeeDeptName();
        String id = kingdeeDepartmentEntity.getId();
        String erpDeptId = kingdeeDepartmentEntity.getErpDeptId();
        //检查名称
        long nameCount=this.lambdaQuery().eq(KingdeeDepartmentEntity::getUseOrgId,useOrgId).
                eq(KingdeeDepartmentEntity::getKingdeeDeptName,deptName)
                .ne(StringUtils.isNotBlank(id),KingdeeDepartmentEntity::getId,id).count();
        if (nameCount > 0) {
            throw new ServiceException("同组织下部门名称不能重复");
        }
        //检查组织下绑定的 erp 部門id
        long erpDeptIdCount = this.lambdaQuery().eq(KingdeeDepartmentEntity::getUseOrgId, useOrgId).
                eq(KingdeeDepartmentEntity::getErpDeptId, erpDeptId).
                ne(StringUtils.isNotBlank(id), KingdeeDepartmentEntity::getId, id).count();
        if (erpDeptIdCount > 0) {
            throw new ServiceException("同组织下绑定的ERP部门不能存在多个");
        }

        //父级部门
        String parentId =kingdeeDepartmentEntity.getParentId();
        if (StringUtils.isNotBlank(parentId)) {
            KingdeeDepartmentEntity parentDept = this.getById(parentId);
            if (Objects.nonNull(parentDept)) {
                kingdeeDepartmentEntity.setParentKingdeeCode(parentDept.getKingdeeDeptCode());
            }
        }else{
            kingdeeDepartmentEntity.setParentId("0");
            kingdeeDepartmentEntity.setParentKingdeeCode("0");
        }
        kingdeeDepartmentEntity.setUseOrgCode(orgInfo.getCode());

    }
}
