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
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysPostEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.sys.mapper.KingdeePostMapper;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeePostService;
import com.erp.server.sys.service.KingdeeDepartmentService;
import com.erp.server.sys.service.KingdeePostService;
import com.erp.server.sys.service.SysAccountingCompanyService;
import com.erp.server.sys.service.SysPostService;
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
 * 金蝶岗位表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@Service
public class KingdeePostServiceImpl extends SuperServiceImpl<KingdeePostMapper, KingdeePostEntity> implements KingdeePostService {




    @Autowired
    private SysAccountingCompanyService sysAccountingCompanyService;

    @Autowired
    private KingdeeDepartmentService kingdeeDepartmentService;

    @Autowired
    private SysPostService sysPostService;

    @Autowired
    private SyncKingdeePostService syncKingdeePostService;

    @Autowired
    private DmpMqFeign dmpMqFeign;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(KingdeePostDTO.AddDTO addDTO) {
        KingdeePostEntity kingdeePostEntity = new KingdeePostEntity();
        BeanMapperUtils.copy(addDTO, kingdeePostEntity);
        // 数据处理
        handleData(kingdeePostEntity);
        boolean save = super.save(kingdeePostEntity);
        if (!save) {
            throw new ServiceException("金蝶岗位单保存失败");
        }
        DmpPushTaskEntity pushTaskEntity = syncKingdeePostService.syncDataToKingdee(kingdeePostEntity, SyncOperateEnum.OPERATE_ADD.getCode());
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
    public Boolean update(KingdeePostDTO.UpdateDTO updateDTO) {
        KingdeePostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶岗位"));
        String oldOrgId = old.getUseOrgId();
        String newOrgId = updateDTO.getUseOrgId();
        if (!oldOrgId.equals(newOrgId)) {
            throw new ServiceException("组织不能修改");
        }

        KingdeePostEntity kingdeePostEntity = BeanMapperUtils.map(KingdeePostEntity.class, updateDTO);
        kingdeePostEntity.setKingdeeId(old.getKingdeeId());
        kingdeePostEntity.setCode(old.getCode());
        // 数据处理
        handleData(kingdeePostEntity);
        boolean save = super.updateById(kingdeePostEntity);
        if (!save) {
            throw new ServiceException("金蝶岗位单保存失败");
        }
        //金蝶推送
        DmpPushTaskEntity pushTaskEntity = syncKingdeePostService.syncDataToKingdee(kingdeePostEntity, SyncOperateEnum.OPERATE_UPDATE.getCode());
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
            }
        });
        return save;

    }

    @Override
    public Boolean init() {
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.HR_ORG_HRPOST.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        //审核状态
        queryFilters.add(StrUtil.format(" FDocumentStatus = {}", "'C'"));
        //禁用状态
        queryFilters.add(StrUtil.format(" FFORBIDSTATUS = {}", "'A'"));
        //查询
        String fieldKeys = "FPOSTID,FNumber,FName,FUseOrgId.FNumber,FDept.FNumber";
        String filterStr = String.join(" and ", queryFilters);
        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取10ing条
        Integer pageSize = 1000;
        List<KingdeePostDTO.KingdeeDTO> postList = new ArrayList<>(20);
        Boolean dataSign = true;
        while (dataSign) {
            List<Map<String, Object>> result = apiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            List<KingdeePostDTO.KingdeeDTO> entityList = result.stream().map(obj ->
                    BeanUtil.toBean(obj, KingdeePostDTO.KingdeeDTO.class)).collect(Collectors.toList());
            postList.addAll(entityList);
            pageIndex++;
        }
        //数据库存在的
        List<KingdeePostEntity> dbList = this.list();
        List<BaseIdDTO.CodeDTO> orgList = sysAccountingCompanyService.getByIds(Collections.emptyList());
        List<KingdeePostEntity> saveOrUpdateList = new ArrayList<>(20);
        //部门列表
        List<KingdeeDepartmentEntity> deptList = kingdeeDepartmentService.list();
        for (KingdeePostDTO.KingdeeDTO item : postList) {
            String kingdeeId = item.getKingdeeId();
            //金蝶部门code
            String deptCode = item.getDeptCode();
            String name = item.getName();
            String code = item.getCode();
            String useOrgCode = item.getUseOrgCode();
            BaseIdDTO.CodeDTO orgInfo = orgList.stream().filter(org -> org.getCode().equals(useOrgCode)).
                    findFirst().orElse(null);
            if (Objects.isNull(orgInfo)) {
                continue;
            }
            //部门id
            String kingdeeDeptId = deptList.stream().filter(entity -> entity.getKingdeeDeptCode().equals(deptCode)).
                    findFirst().map(KingdeeDepartmentEntity::getId).orElse("");
            //等于空 继续
            if (StringUtils.isBlank(kingdeeDeptId)) {
                continue;
            }
            KingdeePostEntity dbEntity = dbList.stream().filter(entity -> entity.getKingdeeId().equals(kingdeeId)).
                    findFirst().orElse(null);
            //表示没有
            if (Objects.isNull(dbEntity)) {
                KingdeePostEntity addEntity = new KingdeePostEntity();
                addEntity.setKingdeeId(kingdeeId);
                addEntity.setCode(code);
                addEntity.setName(name);
                addEntity.setUseOrgId(orgInfo.getId());
                addEntity.setUseOrgName(orgInfo.getName());
                addEntity.setKingdeeDeptId(kingdeeDeptId);
                saveOrUpdateList.add(addEntity);
            } else {
                if (!dbEntity.getCode().equals(code) || !dbEntity.getName().equals(name) ||
                        !dbEntity.getUseOrgId().equals(orgInfo.getId()) ||
                        !dbEntity.getKingdeeDeptId().equals(kingdeeDeptId)) {

                    dbEntity.setCode(code);
                    dbEntity.setName(name);
                    dbEntity.setUseOrgId(orgInfo.getId());
                    dbEntity.setUseOrgName(orgInfo.getName());
                    dbEntity.setKingdeeDeptId(kingdeeDeptId);
                    saveOrUpdateList.add(dbEntity);
                }
            }
        }

        return this.saveOrUpdateBatch(saveOrUpdateList);
    }

    @Override
    public KingdeePostDTO.ViewDTO view(String id) {
        KingdeePostEntity postEntity = this.getById(id);
        if (Objects.isNull(postEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶岗位不存在");
        }
        KingdeePostDTO.ViewDTO viewDTO = new KingdeePostDTO.ViewDTO();
        BeanUtil.copyProperties(postEntity, viewDTO);
        String kingdeeDeptId = postEntity.getKingdeeDeptId();
        KingdeeDepartmentEntity kingdeeDept = kingdeeDepartmentService.getById(kingdeeDeptId);
        if (Objects.nonNull(kingdeeDept)) {
            viewDTO.setKingdeeDeptName(kingdeeDept.getKingdeeDeptName());
        }
        return viewDTO;
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId, String syncKingdeeCode) {
        return this.lambdaUpdate()
                .eq(KingdeePostEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), KingdeePostEntity::getKingdeeId, syncKingdeeId)
                .set(StringUtils.isNotBlank(syncKingdeeCode), KingdeePostEntity::getCode, syncKingdeeCode)
                .update();
    }

    @Override
    public BatchResultDTO delete(String id) {
        KingdeePostEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶岗位不存在");
        }
        Boolean result = this.removeById(id);

        if (result) {
            //金蝶推送
            DmpPushTaskEntity pushTaskEntity = syncKingdeePostService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_DELETE.getCode());
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
                }
            });
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);

    }

    @Override
    public PagingVO<KingdeePostDTO.PagingViewDTO> paging(PagingDTO<KingdeePostDTO.PagingParamDTO> dto) {
        KingdeePostDTO.PagingParamDTO paramDTO = dto.getParams();
        paramDTO.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<KingdeePostDTO.PagingViewDTO> pageData = this.baseMapper.paging(query, paramDTO);
        return new PagingVO(pageData);
    }

    @Override
    public List<KingdeePostEntity> listByOrgId(String orgId) {
        if (StringUtils.isBlank(orgId)) {
            List<BaseIdDTO.CodeDTO> orgList = sysAccountingCompanyService.listByCodes(Arrays.asList("100"));
            if (CollectionUtils.isNotEmpty(orgList)) {
                orgId = orgList.get(0).getId();
            }
        }
        return this.lambdaQuery().eq(KingdeePostEntity::getUseOrgId, orgId).list();
    }

    @Override
    public List<KingdeePostEntity> listByKingDeptId(String deptId) {
        return this.lambdaQuery().eq(KingdeePostEntity::getKingdeeDeptId,deptId).list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(KingdeePostEntity entity) {
        String id = entity.getId();
        String kingdeeDeptId = entity.getKingdeeDeptId();
        String useOrgId = entity.getUseOrgId();
        SysAccountingCompanyEntity orgInfo = sysAccountingCompanyService.getById(useOrgId);
        if (Objects.isNull(orgInfo)) {
            throw new ServiceException("组织信息不存在");
        }
        String erpPostId = entity.getErpPostId();
        if(StringUtils.isNotBlank(erpPostId)){
            SysPostEntity sysPost = sysPostService.getById(erpPostId);
            if(Objects.isNull(sysPost)){
                throw new ServiceException("ERP岗位不存在");
            }
            entity.setName(sysPost.getPostName());
        }

        entity.setUseOrgName(orgInfo.getCompanyName());
        KingdeeDepartmentEntity kingdeeDept = kingdeeDepartmentService.getById(kingdeeDeptId);
        if (Objects.isNull(kingdeeDept)) {
            throw new ServiceException("金蝶部门不存在");
        }
        String kingdeeDeptCode = kingdeeDept.getKingdeeDeptCode();
        String kingdeeDeptName = kingdeeDept.getKingdeeDeptName();
        if (StringUtils.isBlank(kingdeeDeptCode)) {
            throw new ServiceException(kingdeeDeptName + "未同步金蝶,请先同步金蝶");
        }
        entity.setKingdeeDeptCode(kingdeeDeptCode);
        entity.setUseOrgCode(orgInfo.getCode());
        int orgNameCount=this.lambdaQuery().eq(KingdeePostEntity::getUseOrgId,useOrgId).
                eq(KingdeePostEntity::getKingdeeDeptId,kingdeeDeptId).
                eq(KingdeePostEntity::getName,entity.getName()).
                ne(StringUtils.isNotBlank(id),KingdeePostEntity::getId,id).
                count();
        if (orgNameCount > 0) {
            throw new ServiceException( orgInfo.getCompanyName() + "下"+entity.getName()+"岗位已存在");
        }
    }
}
