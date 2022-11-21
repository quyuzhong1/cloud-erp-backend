package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.entity.SysCodeEntity;
import com.erp.server.sys.mapper.SysCodeMapper;
import com.erp.server.sys.service.SysCodeService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/21 11:35
 */
@Service
public class SysCodeServiceImpl extends ServiceImpl<SysCodeMapper, SysCodeEntity>  implements SysCodeService {


    @Override
    public boolean saveSysCode(SysCodeDTO dto) {
        return false;
    }


    @Override
    public String getSysCode(SysCodeDTO dto) {

        return null;
    }
}
