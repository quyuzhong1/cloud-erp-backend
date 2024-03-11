package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.model.scm.dto.KingdeePaymentConditionDTO;
import com.erp.model.scm.entity.KingdeePaymentConditionEntity;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.sys.mapper.KingdeeDepartmentMapper;
import com.erp.server.sys.service.CommonService;
import com.erp.server.sys.service.KingdeeDepartmentService;
import com.erp.server.sys.service.SysAccountingCompanyService;
import com.erp.server.sys.service.SysDepartmentService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private CommonService commonService;

    @Autowired
    private SysAccountingCompanyService sysAccountingCompanyService;

    @Autowired
    private SysDepartmentService sysDepartmentService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KingdeeDepartmentDTO.AddDTO addDTO) {
        KingdeeDepartmentEntity kingdeeDepartmentEntity = new KingdeeDepartmentEntity();
        BeanMapperUtils.copy(addDTO, kingdeeDepartmentEntity);

        // 数据处理
        handleData(kingdeeDepartmentEntity);

        log.info("开始新增");
        boolean save = super.save(kingdeeDepartmentEntity);
        if (!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "", kingdeeDepartmentEntity.getId());

        return new BaseResultDTO.AddDTO(kingdeeDepartmentEntity.getId(), kingdeeDepartmentEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeeDepartmentDTO.UpdateDTO updateDTO) {
        KingdeeDepartmentEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        KingdeeDepartmentEntity kingdeeDepartmentEntity = BeanMapperUtils.map(KingdeeDepartmentEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeeDepartmentEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(kingdeeDepartmentEntity);
        if (!save) {
            throw new ServiceException("保存失败");
        }

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

    private KingdeeDepartmentEntity getParentDeptByKingdeeCode(String parentKingdeeCode) {
        return this.lambdaQuery().eq(KingdeeDepartmentEntity::getKingdeeDeptCode, parentKingdeeCode).last("LIMIT 1").one();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(KingdeeDepartmentEntity kingdeeDepartmentEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
