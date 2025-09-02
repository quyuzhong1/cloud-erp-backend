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
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.dto.KingdeeUserRefPostDTO;
import com.erp.model.sys.entity.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.sys.mapper.KingdeeUserRefPostMapper;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeUserPostService;
import com.erp.server.sys.service.*;
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
 * 金蝶员工任岗表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@Service
public class KingdeeUserRefPostServiceImpl extends SuperServiceImpl<KingdeeUserRefPostMapper, KingdeeUserRefPostEntity> implements KingdeeUserRefPostService {


    @Autowired
    private SysAccountingCompanyService sysAccountingCompanyService;

    @Autowired
    private KingdeeDepartmentService kingdeeDepartmentService;

    @Autowired
    private SysUserInfoService sysUserInfoService;

    @Autowired
    private KingdeePostService kingdeePostService;

    @Autowired
    private SyncKingdeeUserPostService syncKingdeeUserPostService;

    @Autowired
    private DmpMqFeign dmpMqFeign;



    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(KingdeeUserRefPostDTO.AddDTO addDTO) {
        KingdeeUserRefPostEntity kingdeeUserRefPostEntity = new KingdeeUserRefPostEntity();
        BeanMapperUtils.copy(addDTO, kingdeeUserRefPostEntity);
        // 数据处理
        handleData(kingdeeUserRefPostEntity);
        boolean save = super.save(kingdeeUserRefPostEntity);
        if (!save) {
            throw new ServiceException("金蝶员工任岗单保存失败");
        }
        DmpPushTaskEntity pushTaskEntity = syncKingdeeUserPostService.syncDataToKingdee(kingdeeUserRefPostEntity, SyncOperateEnum.OPERATE_ADD.getCode());
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
    public Boolean update(KingdeeUserRefPostDTO.UpdateDTO updateDTO) {
        KingdeeUserRefPostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶员工任岗单"));
        String oldOrgId = old.getUseOrgId();
        String newOrgId = updateDTO.getUseOrgId();
        if (!oldOrgId.equals(newOrgId)) {
            throw new ServiceException("组织不能修改");
        }

        KingdeeUserRefPostEntity kingdeeUserRefPostEntity = BeanMapperUtils.map(KingdeeUserRefPostEntity.class, updateDTO);
        kingdeeUserRefPostEntity.setKingdeeId(old.getKingdeeId());
        kingdeeUserRefPostEntity.setCode(old.getCode());
        // 数据处理
        handleData(kingdeeUserRefPostEntity);
        boolean save = super.updateById(kingdeeUserRefPostEntity);
        if (!save) {
            throw new ServiceException("金蝶员工任岗单保存失败");
        }
        //金蝶推送
        DmpPushTaskEntity pushTaskEntity = syncKingdeeUserPostService.syncDataToKingdee(kingdeeUserRefPostEntity, SyncOperateEnum.OPERATE_UPDATE.getCode());
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
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_NEWSTAFF.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        //审核状态
        queryFilters.add(StrUtil.format(" FDocumentStatus = {}", "'C'"));
        //禁用状态
        queryFilters.add(StrUtil.format(" FFORBIDSTATUS = {}", "'A'"));
        //查询
        String fieldKeys = "FSTAFFID,FStaffNumber,FName,FUseOrgId.FNumber,FDept.FNumber,FPosition.FNumber";
        String filterStr = String.join(" and ", queryFilters);
        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取10ing条
        Integer pageSize = 1000;
        List<KingdeeUserRefPostDTO.KingdeeDTO> userPostList = new ArrayList<>(20);
        Boolean dataSign = true;
        while (dataSign) {
            List<Map<String, Object>> result = apiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            List<KingdeeUserRefPostDTO.KingdeeDTO> entityList = result.stream().map(obj ->
                    BeanUtil.toBean(obj, KingdeeUserRefPostDTO.KingdeeDTO.class)).collect(Collectors.toList());
            userPostList.addAll(entityList);
            pageIndex++;
        }
        //数据库存在的
        List<KingdeeUserRefPostEntity> dbList = this.list();
        List<BaseIdDTO.CodeDTO> orgList = sysAccountingCompanyService.getByIds(Collections.emptyList());
        //岗位列表
        List<KingdeePostEntity> postList = kingdeePostService.list();


        List<KingdeeUserRefPostEntity> saveOrUpdateList = new ArrayList<>(20);
        List<SysUserInfoEntity> userList = sysUserInfoService.listErpUser();
        for (KingdeeUserRefPostDTO.KingdeeDTO item : userPostList) {
            String kingdeeId = item.getKingdeeId();
            //员工任岗code
            String userPostCode = item.getCode();
            //使用组织code
            String useOrgCode = item.getUseOrgCode();
            //岗位code
            String postCode = item.getKingdeePostCode();

            //用户名
            String userName = item.getUserName();
            String userId = userList.stream().filter(u -> userName.equals(u.getRealName())).
                    findFirst().map(SysUserInfoEntity::getUid).orElse("");
            if (StringUtils.isBlank(userId)) {
                continue;
            }
            KingdeePostEntity postEntity = postList.stream().filter(p -> p.getCode().equals(postCode)).
                    findFirst().orElse(null);
            if (Objects.isNull(postEntity)) {
                continue;
            }
            BaseIdDTO.CodeDTO orgInfo = orgList.stream().filter(org -> org.getCode().equals(useOrgCode)).
                    findFirst().orElse(null);
            if (Objects.isNull(orgInfo)) {
                continue;
            }
            String useOrgId = orgInfo.getId();
            //组织名
            String useOrgName = orgInfo.getName();
            String kingdeePostId = postEntity.getId();
            String kingdeeDeptId = postEntity.getKingdeeDeptId();
            KingdeeUserRefPostEntity dbEntity = dbList.stream().filter(entity -> entity.getKingdeeId().equals(kingdeeId)).
                    findFirst().orElse(null);
            //表示没有
            if (Objects.isNull(dbEntity)) {
                KingdeeUserRefPostEntity addEntity = new KingdeeUserRefPostEntity();
                addEntity.setKingdeeId(kingdeeId);
                addEntity.setCode(userPostCode);
                addEntity.setUseOrgId(useOrgId);
                addEntity.setErpUserId(userId);
                addEntity.setUseOrgName(useOrgName);
                addEntity.setKingdeePostId(kingdeePostId);
                addEntity.setKingdeeDepartmentId(kingdeeDeptId);
                saveOrUpdateList.add(addEntity);
            } else {
                if (!dbEntity.getCode().equals(userPostCode) ||
                        !dbEntity.getUseOrgId().equals(orgInfo.getId()) ||
                        !dbEntity.getErpUserId().equals(userId) ||
                        !dbEntity.getKingdeePostId().equals(kingdeePostId)) {

                    dbEntity.setCode(userPostCode);
                    dbEntity.setUseOrgId(useOrgId);
                    dbEntity.setUseOrgName(useOrgName);
                    dbEntity.setKingdeePostId(kingdeeDeptId);
                    dbEntity.setErpUserId(userId);
                    dbEntity.setKingdeeDepartmentId(kingdeeDeptId);
                    saveOrUpdateList.add(dbEntity);
                }
            }

        }

        return this.saveOrUpdateBatch(saveOrUpdateList);

    }

    @Override
    public PagingVO<KingdeeUserRefPostDTO.PagingUserViewDTO> paging(PagingDTO<KingdeeUserRefPostDTO.PagingParamDTO> dto) {
        KingdeeUserRefPostDTO.PagingParamDTO paramDTO = dto.getParams();
        paramDTO.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<KingdeeUserRefPostDTO.PagingUserViewDTO> pageData = this.baseMapper.paging(query, paramDTO);
        return new PagingVO(pageData);
    }

    @Override
    public PagingVO<KingdeeUserRefPostDTO.DetailPagingViewDTO> detailPaging(PagingDTO<KingdeeUserRefPostDTO.DetailPagingParamDTO> dto) {
        KingdeeUserRefPostDTO.DetailPagingParamDTO paramDTO = dto.getParams();
        paramDTO.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<KingdeeUserRefPostDTO.DetailPagingViewDTO> pageData = this.baseMapper.detailPaging(query, paramDTO);
        return new PagingVO(pageData);

    }

    @Override
    public KingdeeUserRefPostDTO.UserPostViewDTO view(String id) {
        SysUserInfoEntity userInfo = sysUserInfoService.getById(id);
        if (Objects.isNull(userInfo)) {
            throw new ServiceException("员工不存在");
        }
        KingdeeUserRefPostDTO.UserPostViewDTO viewDTO = new KingdeeUserRefPostDTO.UserPostViewDTO();
        viewDTO.setId(id);
        viewDTO.setMobile(userInfo.getMobile());
        viewDTO.setUserName(userInfo.getUserName());
        viewDTO.setRealName(userInfo.getRealName());
        List<KingdeeUserRefPostEntity> userPostList = this.listByUserId(id);
        List<KingdeeUserRefPostDTO.ViewDTO> userPostViewList = new ArrayList<>(userPostList.size());
        List<String> kingdeePostIdList=userPostList.stream().map(KingdeeUserRefPostEntity::getKingdeePostId).
                collect(Collectors.toList());

        List<String> kingdeeDeptIdList=userPostList.stream().map(KingdeeUserRefPostEntity::getKingdeeDepartmentId).
                collect(Collectors.toList());
        //任岗信息
        Boolean postIsNotEmpty=CollectionUtils.isNotEmpty(kingdeePostIdList);

        //部门
        Boolean deptIsNotEmpty=CollectionUtils.isNotEmpty(kingdeeDeptIdList);

        //任岗信息
        List<KingdeePostEntity> kingdeePostList = postIsNotEmpty ? kingdeePostService.listByIds(kingdeePostIdList) : Collections.emptyList();

        //部门信息
        List<KingdeeDepartmentEntity> kingdeeDeptList = deptIsNotEmpty ? kingdeeDepartmentService.listByIds(kingdeeDeptIdList) : Collections.emptyList();

        for (KingdeeUserRefPostEntity item : userPostList) {
            KingdeeUserRefPostDTO.ViewDTO itemView = new KingdeeUserRefPostDTO.ViewDTO();
            itemView.setId(item.getId());
            itemView.setKingdeePostId(item.getKingdeePostId());
            itemView.setKingdeeDeptId(item.getKingdeeDepartmentId());
            String postName=kingdeePostList.stream().filter(k->k.getId().equals(item.getKingdeePostId())).map(KingdeePostEntity::getName).
                    findFirst().orElse("");
            itemView.setPostName(postName);
            String deptName=kingdeeDeptList.stream().filter(k->k.getId().equals(item.getKingdeeDepartmentId())).map(KingdeeDepartmentEntity::getKingdeeDeptName).
                    findFirst().orElse("");
            itemView.setKingdeeDeptName(deptName);
            itemView.setUseOrgId(item.getUseOrgId());
            itemView.setUseOrgName(item.getUseOrgName());
            userPostViewList.add(itemView);
        }
        viewDTO.setUserPostList(userPostViewList);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        KingdeeUserRefPostEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶员工任岗"));
        Boolean result = this.removeById(id);
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        if (result) {
            //金蝶推送
            DmpPushTaskEntity pushTaskEntity = syncKingdeeUserPostService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_DELETE.getCode());
            resultList.add(pushTaskEntity);
        }
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);

    }

    /**
     * 根据user is查询
     *
     * @param userId
     * @return
     */
    private List<KingdeeUserRefPostEntity> listByUserId(String userId) {
        return this.lambdaQuery().eq(KingdeeUserRefPostEntity::getErpUserId, userId).list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(KingdeeUserRefPostEntity entity) {
        String id = entity.getId();
        String useOrgId = entity.getUseOrgId();
        SysAccountingCompanyEntity orgInfo = sysAccountingCompanyService.getById(useOrgId);
        if (Objects.isNull(orgInfo)) {
            throw new ServiceException("组织信息不存在");
        }
        String kingdeePostId = entity.getKingdeePostId();
        String erpUserId = entity.getErpUserId();
        int count = this.lambdaQuery().ne(StringUtils.isNotBlank(id), KingdeeUserRefPostEntity::getId, id).
                eq(KingdeeUserRefPostEntity::getKingdeePostId, kingdeePostId).
                eq(KingdeeUserRefPostEntity::getErpUserId, erpUserId).
                eq(KingdeeUserRefPostEntity::getUseOrgId, useOrgId).count();
        if (count > 0) {
              throw new ServiceException("该员工该岗位已存在");
        }


        entity.setUseOrgName(orgInfo.getCompanyName());
        entity.setUseOrgCode(orgInfo.getCode());
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId, String syncKingdeeCode) {
        return this.lambdaUpdate()
                .eq(KingdeeUserRefPostEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), KingdeeUserRefPostEntity::getKingdeeId, syncKingdeeId)
                .set(StringUtils.isNotBlank(syncKingdeeCode), KingdeeUserRefPostEntity::getCode, syncKingdeeCode)
                .update();
    }

    @Override
    public KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePost(KingdeePostDTO.FindUserKingdeePostInfoDTO dto) {
        String userId = dto.getUserId();
        String orgCode = dto.getOrgCode();
        List<BaseIdDTO.CodeDTO> orgList = sysAccountingCompanyService.listByCodes(Arrays.asList(orgCode));
        if (CollectionUtils.isEmpty(orgList)) {
            return null;
        }
        String orgId = orgList.get(0).getId();
        KingdeePostDTO.UserKingdeePostInfoDTO result = baseMapper.getKingdeeUserPost(userId, orgId);
        if(Objects.nonNull(result)){
            result.setUseOrgName(orgList.get(0).getName());
            result.setUseOrgCode(orgCode);
        }
        SysUserInfoEntity  userInfo=  sysUserInfoService.getById(userId);
        if(Objects.nonNull(userInfo)){
            result.setUserId(userId);
            result.setUserName(userInfo.getUserName());
            result.setKingdeeUserCode(userInfo.getCode());
        }
        return result;
    }

    @Override
    public KingdeeUserRefPostEntity getDeptByUserId(String userId) {
        List<KingdeeUserRefPostEntity> list = lambdaQuery().eq(KingdeeUserRefPostEntity::getErpUserId, userId).list();
        if (CollectionUtils.isNotEmpty(list)){
            return list.get(0);
        }else {
            return null;
        }
    }

}
