package com.erp.server.oms.controller.api;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.BankAccountDTO;
import com.erp.model.oms.dto.CustomerB2CDTO;
import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.server.oms.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 银行账号控制器
 *
 * @CreateTime: 2023-07-04  15:27
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping(value = "/bankAccount")
public class BankAccountController extends BaseController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping("/select")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> select(@RequestParam(value = "orgId") String orgId) {
        List<BankAccountEntity> list = bankAccountService.findByOrgId(orgId);
        List<BaseDropDownDTO.DisabledDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.DisabledDTO(x.getBankAccountNo(), x.getBankAccountNo()+"      "+x.getAccountName(),x.getDisabled()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BankAccountDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<BankAccountDTO.PagingParamDTO> dto) {
        PagingVO<BankAccountDTO.PagingViewDTO> pagingVO = bankAccountService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 添加
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated BankAccountDTO.AddDTO dto){
        Boolean result = bankAccountService.add(dto);
        return result ? success() : failure();
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/view")
    public ApiResult<BankAccountDTO.ViewDTO> view(@RequestParam(value = "id") String id){
        BankAccountDTO.ViewDTO viewDTO = bankAccountService.view(id);
        return success(viewDTO);
    }

}
