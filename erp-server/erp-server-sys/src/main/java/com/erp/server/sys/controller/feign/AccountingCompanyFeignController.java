package com.erp.server.sys.controller.feign;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.controller.BaseController;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.server.sys.service.SysAccountingCompanyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lambda
 * @Classname AccountingCompanyFeignController

 * @Date 2023-03-22 15:28
 * @Created by yl
 */
@RestController
@RequestMapping("feign/accountingCompany")
public class AccountingCompanyFeignController extends BaseController {

    @Resource
    private SysAccountingCompanyService sysAccountingCompanyService;

    @PostMapping("/getByIds")
    public List<BaseIdDTO.CodeDTO> getByIds(@RequestBody List<String> ids) {
        List<BaseIdDTO.CodeDTO> list = sysAccountingCompanyService.getByIds(ids);
        return list;
    }

    @PostMapping("/listByCodes")
    public List<BaseIdDTO.CodeDTO> listByCodes(@RequestBody List<String> codes) {
        List<BaseIdDTO.CodeDTO> list = sysAccountingCompanyService.listByCodes(codes);
        return list;
    }


    /**
     * @return List<BaseIdDTO>
     * @description: 查询所有已启用组织
     * @author Will
     * @date: 2023/3/22 16:37
     */
    @GetMapping("/list")
    public List<BaseIdDTO> listAccountingCompany() {
        List<BaseIdDTO> list = sysAccountingCompanyService.listAccountingCompany();
        return list;
    }

    /**
     * 根据主键id查询组织信息
     *
     * @param id id:组织id
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     * @Author Luo_WG
     * @Date 2023/4/13 12:19
     **/
    @PostMapping("/getCompanyById")
    public SysAccountingCompanyEntity getCompanyById(@RequestBody String id) {
        if (StringUtils.isBlank(id)) {
            return new SysAccountingCompanyEntity();
        } else {
            SysAccountingCompanyEntity sysAccountingCompanyEntity = sysAccountingCompanyService.getById(id);
            return sysAccountingCompanyEntity;
        }

    }

    /**
     * 根据主键id查询组织信息
     *
     * @param id id:组织id
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     * @Author Luo_WG
     * @Date 2023/4/13 12:19
     **/
    @PostMapping("/getCompanyByKindgeeId")
    public SysAccountingCompanyEntity getCompanyByKindgeeId(@RequestBody String KindgeeId) {
        if (StringUtils.isBlank(KindgeeId)) {
            return new SysAccountingCompanyEntity();
        } else {
            SysAccountingCompanyEntity sysAccountingCompanyEntity = sysAccountingCompanyService.getCompanyByKindgeeId(KindgeeId);
            return sysAccountingCompanyEntity;
        }
    }

    /**
     * 根据公司名称查询公司信息
     *
     * @param orgName 公司名称
     * @return SysAccountingCompanyEntity
     * @Author Luo_WG
     * @Date 2023/4/13 12:19
     **/
    @PostMapping("/getCompanyByName")
    public SysAccountingCompanyEntity getCompanyByName(@RequestBody String orgName) {
        if (StringUtils.isBlank(orgName)) {
            return new SysAccountingCompanyEntity();
        } else {
            SysAccountingCompanyEntity sysAccountingCompanyEntity = sysAccountingCompanyService.getCompanyByName(orgName);
            return sysAccountingCompanyEntity;
        }

    }

    @PostMapping("/listCompanyById")
    public List<SysAccountingCompanyEntity> listCompanyById(@RequestBody List<String> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return new ArrayList<>();
        } else {
            return sysAccountingCompanyService.listByIds(ids);
        }
    }
}
