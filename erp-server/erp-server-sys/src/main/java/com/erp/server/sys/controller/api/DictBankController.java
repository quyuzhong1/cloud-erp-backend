package com.erp.server.sys.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.validator.ValidList;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.BankDTO;
import com.erp.server.sys.service.DictBankService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 银行 字典表 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@RestController
@LogSystemModule("系统管理通用")
@RequestMapping("bank")
public class DictBankController extends BaseController {

    @Resource
    private DictBankService bankService;

    @LogAction(value = LogActionEnum.INSERT, desc = "新增或更新银行")
    @PostMapping("/saveOrUpdate")
    public ApiResult add(@RequestBody @Valid ValidList<BankDTO.AddOrUpdateDTO> bankList) {
        Boolean result = bankService.saveOrUpdateBatchBank(bankList);
        return result == true ? success() : failure();
    }


    /**
     * 删除银行
     *
     * @param ids
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-21 16:29
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除银行")
    @PostMapping("/delete")
    public ApiResult add(@RequestBody @Validated List<String> ids) {
        Boolean result = bankService.removeByIds(ids);
        return result == true ? success() : failure();
    }


    /**
     * 获取银行列表
     *
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-21 16:29
     */
    @GetMapping("/list")
    public ApiResult<List<BankDTO.ViewDTO>> list() {
        List<BankDTO.ViewDTO> list = bankService.getList();
        return success(list);
    }

}
