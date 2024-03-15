package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.dto.KingdeeUserRefPostDTO;
import com.erp.model.sys.entity.KingdeeOperatorRefPostEntity;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.sys.mapper.KingdeeOperatorRefPostMapper;
import com.erp.server.sys.service.CommonService;
import com.erp.server.sys.service.KingdeeOperatorRefPostService;
import com.erp.server.sys.service.KingdeeUserRefPostService;
import com.erp.server.sys.service.SysAccountingCompanyService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(KingdeeOperatorRefPostDTO.AddDTO addDTO) {
        KingdeeOperatorRefPostEntity kingdeeOperatorRefPostEntity = new KingdeeOperatorRefPostEntity();
        BeanMapperUtils.copy(addDTO, kingdeeOperatorRefPostEntity);

        // 数据处理
        handleData(kingdeeOperatorRefPostEntity);

        log.info("开始新增金蝶业务员单");
        boolean save = super.save(kingdeeOperatorRefPostEntity);
        if(!save) {
            throw new ServiceException("金蝶业务员单保存失败");
        }
        return save;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeeOperatorRefPostDTO.UpdateDTO updateDTO) {
        KingdeeOperatorRefPostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶业务员单"));
        KingdeeOperatorRefPostEntity kingdeeOperatorRefPostEntity =  BeanMapperUtils.map(KingdeeOperatorRefPostEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeeOperatorRefPostEntity);
        log.info("编辑 开始修改金蝶业务员单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kingdeeOperatorRefPostEntity);
        if(!save) {
            throw new ServiceException("金蝶业务员单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）


        return Boolean.TRUE;
    }

    @Override
    public Boolean init() {
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_OPERATOR.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        //禁用状态
        queryFilters.add(StrUtil.format(" FForbiddenStatus = {}", "'0'"));
        //查询
        String fieldKeys = "FOperatorType,FBizOrgId.FNumber,FNumber,FStaffId.FStaffNumber";
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
        List<KingdeeUserRefPostEntity> userPostList=  kingdeeUserRefPostService.list();

        List<KingdeeOperatorRefPostEntity> saveOrUpdateList = new ArrayList<>(20);
        for (KingdeeOperatorRefPostDTO.KingdeeDTO item : operatorList) {
            //code
            String code = item.getCode();
            //类型code
            String typeCode=item.getTypeCode();

            //使用组织code
            String useOrgCode = item.getUseOrgCode();

            //员工任岗code
            String userPostCode=item.getUserPostCode();
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
            KingdeeOperatorRefPostEntity dbEntity = dbList.stream().filter(entity -> entity.getCode().equals(code)).
                    findFirst().orElse(null);
            //表示没有
            if(Objects.isNull(dbEntity)){
                KingdeeOperatorRefPostEntity addEntity = new KingdeeOperatorRefPostEntity();
                addEntity.setCode(code);
                addEntity.setUseOrgId(useOrgId);
                addEntity.setUseOrgName(useOrgName);
                addEntity.setUseOrgName(useOrgName);
                addEntity.setTypeCode(typeCode);
                addEntity.setUserPostId(userPostId);
                saveOrUpdateList.add(addEntity);
            }else{
                if(!dbEntity.getCode().equals(code) ||
                        !dbEntity.getUseOrgId().equals(orgInfo.getId()) ||
                        !dbEntity.getTypeCode().equals(typeCode) ||
                        !dbEntity.getUserPostId().equals(userPostId)
                ){
                    dbEntity.setCode(code);
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
    public BatchResultDTO delete(String id) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KingdeeOperatorRefPostEntity kingdeeOperatorRefPostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
