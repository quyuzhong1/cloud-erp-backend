package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.SoChangeMapper;
import com.erp.server.oms.service.SoChangeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 销售订单变更 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoChangeServiceImpl extends SuperServiceImpl<SoChangeMapper, SoChangeEntity> implements SoChangeService {


    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 添加销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-18 11:54
     */
    @Override
    public String add(SoChangeDTO.AddDTO dto) {
        String id = IdWorker.getIdStr();
        SoChangeEntity soChange = new SoChangeEntity();
        BeanMapper.copy(dto, soChange);
        String useId = dto.getUseId();
        String deptId = dto.getDeptId();
        String useName = "";
        String deptName = "";
        if (StringUtils.isEmpty(useId)) {
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(useId);
            if (userInfo != null) {
                useName = userInfo.getUserName();
            }
        }
        if (StringUtils.isNotBlank(deptId)) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(deptId);
            if (dept != null) {
                deptName = dept.getName();
            }
        }
        soChange.setDeptName(deptName);
        soChange.setUserName(useName);
        soChange.setId(id);
        String code= sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSBG, BusinessNoTypeEnum.CODE_XSD.getCode()));

        Boolean addResult = this.save(soChange);
        if(addResult){

        }


        return null;
    }
}
