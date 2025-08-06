package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.dto.UserInfoDTO;
import com.erp.model.sys.entity.KingdeeOperatorRefPostEntity;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.sys.mapper.KingdeeOperatorRefPostMapper;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeOperatorService;
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

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 金蝶业务员表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@Service
public class KingdeeOperatorRefPostServiceImpl extends SuperServiceImpl<KingdeeOperatorRefPostMapper, KingdeeOperatorRefPostEntity> implements KingdeeOperatorRefPostService {


    @Autowired
    private SysAccountingCompanyService sysAccountingCompanyService;

    @Autowired
    private KingdeeUserRefPostService kingdeeUserRefPostService;


    @Autowired
    private SyncKingdeeOperatorService syncKingdeeOperatorService;

    @Autowired
    private SysUserInfoService sysUserInfoService;


    @Resource
    private CommonService commonService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO add(String typeCode, String userPostId) {
        KingdeeUserRefPostEntity userPost = kingdeeUserRefPostService.getById(userPostId);
        if (Objects.isNull(userPost) || StringUtils.isBlank(userPost.getCode())) {
            throw new ServiceException("用户岗位不存在");
        }
        KingdeeOperatorRefPostEntity kingdeeOperator = this.getByTypeAndUserPost(typeCode, userPostId);
        if (Objects.nonNull(kingdeeOperator)) {
            throw new ServiceException("该业务类型已存在");
        }

        KingdeeOperatorRefPostEntity addEntity = new KingdeeOperatorRefPostEntity();
        addEntity.setUserPostId(userPostId);
        addEntity.setTypeCode(typeCode);
        addEntity.setUseOrgId(userPost.getUseOrgId());
        addEntity.setUseOrgName(userPost.getUseOrgName());
        addEntity.setCode(userPost.getCode());
//        handleDb(addEntity);
        Boolean result = this.save(addEntity);
        if (result) {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeOperatorService.syncDataToKingdee(addEntity, SyncOperateEnum.OPERATE_ADD.getCode());
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
                }
            });
        }
        return BatchResultDTO.success(addEntity.getId(), addEntity.getId(), OperationTypeEnum.ADD);
    }

    private void handleDb(KingdeeOperatorRefPostEntity entity) {
        //类型
        String typeCode = entity.getTypeCode();
        //使用组织
        String useOrgId = entity.getUseOrgId();

        //使用组织
        String useOrgName = entity.getUseOrgName();
        //用户岗位
        String userPostId = entity.getUserPostId();
        List<KingdeeOperatorRefPostEntity> list = this.lambdaQuery().
                eq(KingdeeOperatorRefPostEntity::getTypeCode, typeCode).eq(KingdeeOperatorRefPostEntity::getUseOrgId, useOrgId).list();
        List<String> userPostIdList = list.stream().map(KingdeeOperatorRefPostEntity::getUserPostId).distinct().collect(Collectors.toList());
        userPostIdList.add(userPostId);
        List<KingdeeUserRefPostEntity> userRefPostList = CollectionUtils.isNotEmpty(userPostIdList) ?
                kingdeeUserRefPostService.listByIds(userPostIdList) : Collections.emptyList();

        Map<String, List<KingdeeUserRefPostEntity>> map = userRefPostList.stream().collect(Collectors.groupingBy(KingdeeUserRefPostEntity::getErpUserId));
        for (Map.Entry<String, List<KingdeeUserRefPostEntity>> item : map.entrySet()) {
            if (item.getValue().size() > 1) {
                throw new ServiceException("该用戶在" + useOrgName + " 组织下存在多个岗位");
            }

        }
    }

    private KingdeeOperatorRefPostEntity getByTypeAndUserPost(String typeCode, String userPostId) {
        return this.lambdaQuery().eq(KingdeeOperatorRefPostEntity::getTypeCode, typeCode).
                eq(KingdeeOperatorRefPostEntity::getUserPostId, userPostId).last("LIMIT 1").one();
    }


    @Override
    public Boolean init() {
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_OPERATOR.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        //禁用状态
        queryFilters.add(StrUtil.format(" FForbiddenStatus = {}", "'0'"));
        //查询
        String fieldKeys = "FEntity_FEntryId,FOperatorType,FBizOrgId.FNumber,FNumber,FStaffId.FStaffNumber";
        String filterStr = String.join(" and ", queryFilters);

        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取10ing条
        Integer pageSize = 1000;
        List<KingdeeOperatorRefPostDTO.KingdeeDTO> operatorList = new ArrayList<>(20);
        Boolean dataSign = true;
        while (dataSign) {
            List<Map<String, Object>> result = apiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            List<KingdeeOperatorRefPostDTO.KingdeeDTO> entityList = result.stream().map(obj ->
                    BeanUtil.toBean(obj, KingdeeOperatorRefPostDTO.KingdeeDTO.class)).collect(Collectors.toList());
            operatorList.addAll(entityList);
            pageIndex++;
        }
        //数据库存在的
        List<KingdeeOperatorRefPostEntity> dbList = this.list();
        List<BaseIdDTO.CodeDTO> orgList = sysAccountingCompanyService.getByIds(Collections.emptyList());
        //员工任岗信息
        List<KingdeeUserRefPostEntity> userPostList = kingdeeUserRefPostService.list();

        List<KingdeeOperatorRefPostEntity> saveOrUpdateList = new ArrayList<>(20);
        for (KingdeeOperatorRefPostDTO.KingdeeDTO item : operatorList) {
            //code
            String code = item.getCode();

            //code
            String kingdeeId = item.getKingdeeId();
            //类型code
            String typeCode = item.getTypeCode();

            //使用组织code
            String useOrgCode = item.getUseOrgCode();

            //员工任岗code
            String userPostCode = item.getUserPostCode();
            KingdeeUserRefPostEntity userPostEntity = userPostList.stream().filter(p -> p.getCode().equals(userPostCode)).
                    findFirst().orElse(null);
            if (Objects.isNull(userPostEntity)) {
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
            String userPostId = userPostEntity.getId();
            KingdeeOperatorRefPostEntity dbEntity = dbList.stream().filter(entity -> entity.getKingdeeId().equals(kingdeeId)).
                    findFirst().orElse(null);
            //表示没有
            if (Objects.isNull(dbEntity)) {
                KingdeeOperatorRefPostEntity addEntity = new KingdeeOperatorRefPostEntity();
                addEntity.setUseOrgId(useOrgId);
                addEntity.setUseOrgName(useOrgName);
                addEntity.setUseOrgName(useOrgName);
                addEntity.setTypeCode(typeCode);
                addEntity.setUserPostId(userPostId);
                addEntity.setKingdeeId(kingdeeId);
                saveOrUpdateList.add(addEntity);
            } else {
                if (!dbEntity.getUseOrgId().equals(orgInfo.getId()) ||
                        !dbEntity.getTypeCode().equals(typeCode) ||
                        !dbEntity.getUserPostId().equals(userPostId)
                ) {
                    dbEntity.setUseOrgId(useOrgId);
                    dbEntity.setUseOrgName(useOrgName);
                    dbEntity.setTypeCode(typeCode);
                    dbEntity.setUserPostId(userPostId);
                    saveOrUpdateList.add(dbEntity);
                }
            }
        }
        return this.saveOrUpdateBatch(saveOrUpdateList);
    }


    @Override
    public PagingVO<KingdeeOperatorRefPostDTO.PagingViewDTO> paging(PagingDTO<KingdeeOperatorRefPostDTO.PagingParamDTO> dto) {
        KingdeeOperatorRefPostDTO.PagingParamDTO paramDTO = dto.getParams();
        paramDTO.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<KingdeeOperatorRefPostDTO.PagingViewDTO> pageData = this.baseMapper.paging(query, paramDTO);
        return new PagingVO(pageData);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        KingdeeOperatorRefPostEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶业务员"));
        String kingdeeId = entity.getKingdeeId();
        Boolean result = this.removeById(id);
        if (result && StringUtils.isNotBlank(kingdeeId)) {
            //金蝶推送
            DmpPushTaskEntity pushTaskEntity = syncKingdeeOperatorService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_DELETE.getCode());
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
                }
            });
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }


    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(KingdeeOperatorRefPostEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), KingdeeOperatorRefPostEntity::getKingdeeId, syncKingdeeId)
                .update();
    }

    @Override
    public KingdeeOperatorRefPostDTO.OperatorDTO find(KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO dto) {
        KingdeeOperatorRefPostDTO.OperatorDTO result = baseMapper.find(dto);
        if (Objects.nonNull(result)) {
            SysUserInfoEntity userInfo = sysUserInfoService.getById(dto.getUserId());
            if (Objects.nonNull(userInfo)) {
                result.setUserName(userInfo.getUserName());
            }
        }
        return result;
    }

    @Override
    public List<UserInfoDTO.BusinessOperationUserDTO> listInfo(KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO dto) {
        List<UserInfoDTO.BusinessOperationUserDTO> dbList = baseMapper.listInfo(dto);
        if (CollUtil.isEmpty(dbList)){
            return Collections.emptyList();
        }
        String userId = UserContext.getDefaultLoginUser().getUid();
        dbList.forEach(item -> {
            if (CharSequenceUtil.isNotBlank(item.getUserId()) && item.getUserId().equals(userId)){
                item.setIsMyState(1);
            }else {
                item.setIsMyState(0);
            }
            Integer deleteState = item.getDeleteState();
            Integer userState = item.getUserState();
            if (MathUtil.ZERO.equals(deleteState) || MathUtil.ZERO.equals(userState)) {
                item.setDisabled(Boolean.TRUE);
            } else {
                item.setDisabled(Boolean.FALSE);
            }
            //部门为空时设置为时效
            if (CharSequenceUtil.isBlank(item.getDepartmentId()) || CharSequenceUtil.isBlank(item.getDepartmentName())){
                item.setDisabled(Boolean.TRUE);
            }
        });
        return dbList;
    }

    @Override
    public List<KingdeeOperatorRefPostDTO.OperatorDTO> listOperatorByUserIdList(List<String> userIdList) {
        List<KingdeeOperatorRefPostDTO.OperatorDTO> resultList = baseMapper.listOperatorByUserIdList(userIdList);
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(KingdeeBusinessOperatorDTO.BatchUpdateDTO dto) {
        boolean result = lambdaUpdate().in(KingdeeOperatorRefPostEntity::getId, dto.getIds())
                .set(KingdeeOperatorRefPostEntity::getDisabled, dto.getDisabled())
                .update();

        if (!result) {
            throw new ServiceException("业务员状态更新失败");
        }
        log.warn("业务员状态更新成功, ids: {}, disabled: {}", dto.getIds(), dto.getDisabled());
    }

    @Override
    public List<UserInfoDTO.BusinessOperationUserDTO> listUser(KingdeeBusinessOperatorDTO.ListBusinessOperatorUserDTO dto) {
        List<UserInfoDTO.BusinessOperationUserDTO> dbList = baseMapper.listUser(dto);
        if (CollUtil.isEmpty(dbList)){
            return Collections.emptyList();
        }
        String userId = UserContext.getDefaultLoginUser().getUid();
        dbList.forEach(item -> {
            if (CharSequenceUtil.isNotBlank(item.getUserId()) && item.getUserId().equals(userId)){
                item.setIsMyState(1);
            }else {
                item.setIsMyState(0);
            }
            Integer deleteState = item.getDeleteState();
            Integer userState = item.getUserState();
            if (MathUtil.ZERO.equals(deleteState) || MathUtil.ZERO.equals(userState)) {
                item.setDisabled(Boolean.TRUE);
            } else {
                item.setDisabled(Boolean.FALSE);
            }
            //部门为空时设置为时效
            if (CharSequenceUtil.isBlank(item.getDepartmentId()) || CharSequenceUtil.isBlank(item.getDepartmentName())){
                item.setDisabled(Boolean.TRUE);
            }
        });
        return dbList;
    }

}
