package com.erp.server.sys.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.sys.dto.SysLogRecordFieldDTO;
import com.erp.model.sys.dto.SysLogRecordFieldListDTO;
import com.erp.model.sys.entity.SysLogRecordFieldEntity;
import com.erp.server.sys.mapper.SysLogRecordFieldMapper;
import com.erp.server.sys.service.SysLogRecordFieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * SYS系统日志字段保存配置表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-08-29
 */
@Slf4j
@Service
public class SysLogRecordFieldServiceImpl extends SuperServiceImpl<SysLogRecordFieldMapper, SysLogRecordFieldEntity> implements SysLogRecordFieldService {

    @Override
    public List<SysLogRecordFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<SysLogRecordFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysLogRecordFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public List<SysLogRecordFieldListDTO> listByDto(SysLogRecordFieldDTO.ListDTO dto) {
        List<SysLogRecordFieldEntity> list = this.listByClassPaths(dto.getClassPathList());
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            SysLogRecordFieldListDTO infoDTO = new SysLogRecordFieldListDTO();
            BeanUtils.copyProperties(e, infoDTO);
            return infoDTO;
        }).collect(Collectors.toList());

    }
}
