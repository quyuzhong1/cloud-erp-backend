package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.sys.mapper.KingdeePostMapper;
import com.erp.server.sys.service.KingdeeDepartmentService;
import com.erp.server.sys.service.KingdeePostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import com.erp.server.sys.service.SysAccountingCompanyService;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.K;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.KingdeePostDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
    private CommonService commonService;


    @Autowired
    private SysAccountingCompanyService sysAccountingCompanyService;

    @Autowired
    private KingdeeDepartmentService kingdeeDepartmentService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KingdeePostDTO.AddDTO addDTO) {
        KingdeePostEntity kingdeePostEntity = new KingdeePostEntity();
        BeanMapperUtils.copy(addDTO, kingdeePostEntity);

        // 数据处理
        handleData(kingdeePostEntity);

        boolean save = super.save(kingdeePostEntity);
        if(!save) {
            throw new ServiceException("金蝶岗位单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "金蝶岗位单" , kingdeePostEntity.getCode());


        return new BaseResultDTO.AddDTO(kingdeePostEntity.getId(), "");
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeePostDTO.UpdateDTO updateDTO) {
        KingdeePostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶岗位单"));
        KingdeePostEntity kingdeePostEntity =  BeanMapperUtils.map(KingdeePostEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeePostEntity);
        log.info("编辑 开始修改金蝶岗位单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kingdeePostEntity);
        if(!save) {
            throw new ServiceException("金蝶岗位单保存失败");
        }

        return Boolean.TRUE;
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
        while (dataSign){
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
            if(Objects.isNull(dbEntity)){
                KingdeePostEntity  addEntity = new KingdeePostEntity();
                addEntity.setKingdeeId(kingdeeId);
                addEntity.setCode(code);
                addEntity.setName(name);
                addEntity.setUseOrgId(orgInfo.getId());
                addEntity.setUseOrgName(orgInfo.getName());
                addEntity.setKingdeeDeptId(kingdeeDeptId);
                saveOrUpdateList.add(dbEntity);
            }else{
                if(!dbEntity.getCode().equals(code) || !dbEntity.getName().equals(name) ||
                        dbEntity.getUseOrgId().equals(orgInfo.getId()) ||
                        dbEntity.getKingdeeDeptId().equals(kingdeeDeptId)){

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


    /**
    * 新增修改处理数据
    */
    private void handleData(KingdeePostEntity kingdeePostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
