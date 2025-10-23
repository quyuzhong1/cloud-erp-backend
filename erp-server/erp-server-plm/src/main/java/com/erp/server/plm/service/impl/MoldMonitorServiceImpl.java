package com.erp.server.plm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MoldMonitorDTO;
import com.erp.model.plm.dto.MoldMonitorDTO;
import com.erp.model.plm.enums.MoldMonitorLifeStatusEnum;
import com.erp.model.plm.enums.MoldMonitorReturnStatusEnum;
import com.erp.model.plm.enums.MoldMonitorStatusEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.MoldMonitorEntity;
import com.erp.server.plm.mapper.MoldMonitorMapper;
import com.erp.server.plm.service.MoldMonitorService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.erp.server.plm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.MoldMonitorDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 模具监控 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-22
 */
@Slf4j
@Service
public class MoldMonitorServiceImpl extends SuperServiceImpl<MoldMonitorMapper, MoldMonitorEntity> implements MoldMonitorService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(MoldMonitorDTO.AddDTO addDTO) {
        MoldMonitorEntity moldMonitorEntity = new MoldMonitorEntity();
        BeanMapperUtils.copy(addDTO, moldMonitorEntity);

        // 数据处理
        handleData(moldMonitorEntity);

        log.info("开始新增模具监控");
        boolean save = super.save(moldMonitorEntity);
        if(!save) {
            throw new ServiceException("模具监控保存失败");
        }
        return new BaseResultDTO.AddDTO(moldMonitorEntity.getId(), moldMonitorEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(MoldMonitorDTO.UpdateDTO addOrUpdateDTO) {
        MoldMonitorEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "模具监控"));
        MoldMonitorEntity moldMonitorEntity =  BeanMapperUtils.map(MoldMonitorEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(moldMonitorEntity);
        log.info("编辑 开始修改模具监控数据，id：【{}】", old.getId());
        boolean save = super.updateById(moldMonitorEntity);
        if(!save) {
            throw new ServiceException("模具监控保存失败");
        }
        return Boolean.TRUE;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(MoldMonitorEntity moldMonitorEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<MoldMonitorDTO.TabListDTO> tabList(MoldMonitorDTO.TabDTO param) {
        MoldMonitorDTO.PagingParamDTO searchParam = new MoldMonitorDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        searchParam.setSourceType(param.getSourceType());
        List<MoldMonitorDTO.TabListDTO> list = baseMapper.tabList(searchParam);

        List<MoldMonitorDTO.TabListDTO> result = new ArrayList<>();
        result.add(new MoldMonitorDTO.TabListDTO("all","全部", 0));

        // 预警状态
        if(Objects.equals(param.getSourceType(), SourceTypeEnum.CFG_MOLD_ALERT_RULE.getCode())){
            List<String> statusList = MoldMonitorLifeStatusEnum.getStatusList();
            for (String status : statusList) {
                MoldMonitorDTO.TabListDTO tabListDTO = list.stream().filter(e -> e.getTabFlag().equals(status)).findFirst().orElse(new MoldMonitorDTO.TabListDTO(status, "", 0));
                tabListDTO.setTabFlagName(MoldMonitorLifeStatusEnum.getName(status));
                result.add(tabListDTO);
            }
        }else {
            // 返还状态
            List<String> statusList = MoldMonitorReturnStatusEnum.getStatusList();
            for (String status : statusList) {
                MoldMonitorDTO.TabListDTO tabListDTO = list.stream().filter(e -> e.getTabFlag().equals(status)).findFirst().orElse(new MoldMonitorDTO.TabListDTO(status, "", 0));
                tabListDTO.setTabFlagName(MoldMonitorReturnStatusEnum.getName(status));
                result.add(tabListDTO);
            }
        }
        return result;
    }

    @Override
    public PagingVO<MoldMonitorDTO.ListDTO> paging(PagingDTO<MoldMonitorDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<MoldMonitorDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<MoldMonitorDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        for (MoldMonitorDTO.ListDTO data : list) {
            data.setStatusName(MoldMonitorStatusEnum.getName(data.getStatus()));
            data.setReturnStatusName(MoldMonitorReturnStatusEnum.getName(data.getReturnStatus()));
            data.setLifeStatusName(MoldMonitorLifeStatusEnum.getName(data.getLifeStatus()));
        }
    }



}
