package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.tms.dto.RemotePostcodeDTO;
import com.erp.model.tms.dto.RemotePostcodeDetailDTO;
import com.erp.model.tms.entity.RemotePostcodeEntity;
import com.erp.model.tms.enums.RemotePostcodeDetailMatchTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.mapper.RemotePostcodeMapper;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.RemotePostcodeDetailService;
import com.erp.server.tms.service.RemotePostcodeService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 偏远邮编组 服务实现类
 * </p>
 *
 * @author jack
 * @since 2024-11-29
 */
@Slf4j
@Service
public class RemotePostcodeServiceImpl extends SuperServiceImpl<RemotePostcodeMapper, RemotePostcodeEntity> implements RemotePostcodeService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private RemotePostcodeDetailService remotePostcodeDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RemotePostcodeDTO.AddDTO addDTO) {
        RemotePostcodeEntity remotePostcodeEntity = new RemotePostcodeEntity();
        BeanMapperUtils.copy(addDTO, remotePostcodeEntity);

        // 数据处理
        log.info("开始新增偏远邮编组");
        boolean save = super.save(remotePostcodeEntity);
        if(!save) {
            throw new ServiceException("偏远邮编组保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("创建偏远地区邮编组【{}】成功", remotePostcodeEntity.getName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REMOTE_POSTCODE.getCode(), remotePostcodeEntity.getId(), "新增操作");
        //添加明细
        remotePostcodeDetailService.add(addDTO, remotePostcodeEntity.getId());
        return new BaseResultDTO.AddDTO(remotePostcodeEntity.getId(), remotePostcodeEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RemotePostcodeDTO.UpdateDTO updateDTO) {
        RemotePostcodeEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "偏远邮编组"));
        RemotePostcodeEntity remotePostcodeEntity =  BeanMapperUtils.map(RemotePostcodeEntity.class, updateDTO);

        // 数据处理
        boolean save = super.updateById(remotePostcodeEntity);
        if(!save) {
            throw new ServiceException("偏远邮编组保存失败");
        }
        // 记录主单操作日志
        operateLogService.addModuleOperateLogByObj(old, remotePostcodeEntity, ModuleTypeEnum.REMOTE_POSTCODE.getCode(), remotePostcodeEntity.getId(), "","");

        //更新明细
        remotePostcodeDetailService.update(updateDTO,remotePostcodeEntity.getId());
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<RemotePostcodeDTO.ListDTO> paging(PagingDTO<RemotePostcodeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<RemotePostcodeDTO.ListDTO> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<RemotePostcodeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        for (RemotePostcodeDTO.ListDTO record : pageData.getRecords()) {
            //是否禁用
            record.setDisabledName(Boolean.TRUE.equals(record.getDisabled()) ? "停用" : "启用");
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<RemotePostcodeDTO.TabListDTO> tabList(PermissionsDTO param) {
        RemotePostcodeDTO.PagingParamDTO searchParam = new RemotePostcodeDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<RemotePostcodeDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(RemotePostcodeDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new RemotePostcodeDTO.TabListDTO(status, 0));
        }
        });
        list.add(new RemotePostcodeDTO.TabListDTO("all", list.stream().mapToInt(RemotePostcodeDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public Boolean exportList(RemotePostcodeDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("偏远邮编", FileTaskEventEnum.EXPORT_WMS_REMOTE_POSTCODE.getCode() ,param);
        return true;
    }

    @Override
    public PagingVO<RemotePostcodeDTO.ExportListDTO> listExport(PagingDTO<RemotePostcodeDTO.PagingParamDTO> pagingParamDTO) {
        Page<RemotePostcodeDTO.ExportListDTO> pageData = this.baseMapper.listExport(new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize()), pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        List<String> citys = pageData.getRecords().stream().map(RemotePostcodeDTO.ExportListDTO::getCity).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<DictCityEntity> dictCityEntities = sysUserFeign.listCityByIds(citys);
        // 将城市信息转换为 Map，减少多次流式查找
        Map<String, String> cityNameMap = dictCityEntities.stream()
                .collect(Collectors.toMap(DictCityEntity::getId, DictCityEntity::getName));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (RemotePostcodeDTO.ExportListDTO record : pageData.getRecords()) {
            //是否禁用
            record.setDisabledName(Boolean.TRUE.equals(record.getDisabled()) ? "停用" : "启用");
            //城市名称
            record.setCityName(cityNameMap.getOrDefault(record.getCity(),""));
            // 匹配类型名称
            record.setMatchTypeName(RemotePostcodeDetailMatchTypeEnum.getName(record.getMatchType()));
            record.setUpdateTimeStr(record.getUpdateTime().format(dateTimeFormatter));
        }
        return new PagingVO(pageData);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        RemotePostcodeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到偏远邮编组数据"));
        // 删除主单数据
        super.removeById(id);
        remotePostcodeDetailService.removeByMainIds(Collections.singletonList(id));
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "偏远邮编组");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REMOTE_POSTCODE.getCode(), entity.getId(), "删除偏远邮编组数据");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }

    @Override
    public RemotePostcodeDTO.ViewDTO view(String id) {
        RemotePostcodeEntity remotePostcodeEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到偏远邮编组数据"));
        RemotePostcodeDTO.ViewDTO data = BeanMapperUtils.map(RemotePostcodeDTO.ViewDTO.class, remotePostcodeEntity);
        // 数据填充处理
        List<RemotePostcodeDetailDTO.ViewDTO> details = remotePostcodeDetailService.listByMainIds(Collections.singletonList(id));

        List<String> citys = details.stream().map(RemotePostcodeDetailDTO.ViewDTO::getCity).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<DictCityEntity> dictCityEntities = sysUserFeign.listCityByIds(citys);
        // 将城市信息转换为 Map，减少多次流式查找
        Map<String, String> cityNameMap = dictCityEntities.stream()
                .collect(Collectors.toMap(DictCityEntity::getId, DictCityEntity::getName));

        details.forEach(detail -> {
            // 匹配类型名称
            detail.setMatchTypeName(RemotePostcodeDetailMatchTypeEnum.getName(detail.getMatchType()));

            // 设置城市名称
            String cityName = cityNameMap.getOrDefault(detail.getCity(), "");
            detail.setCityName(cityName);
        });
        data.setDetails(details);
        return data;
    }
}
