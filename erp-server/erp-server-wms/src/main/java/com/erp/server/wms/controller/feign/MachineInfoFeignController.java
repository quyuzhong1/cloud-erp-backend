package com.erp.server.wms.controller.feign;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.server.wms.service.MachineInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 加工单
 *
 * @author will
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/feign/machineInfo")
public class MachineInfoFeignController extends BaseController {

    @Resource
    private MachineInfoService machineInfoService;


    @PostMapping("/listBySku")
    public List<MachineInfoDTO.ListDTO> listBySku(@RequestBody MachineInfoDTO.FindInfoBySkuDTO dto) {
        return machineInfoService.listBySku(dto);
    }

    /**
     * 新增加工单
     * @author Will
     * @date: 2023/12/6 14:16
     * @param dto
     * @return String
     */
    @PostMapping("/addMachineInfo")
    public String addMachineInfo(@RequestBody MachineInfoDTO.AddDTO dto) {
        String id = machineInfoService.add(dto);
        return id;
    }

}
