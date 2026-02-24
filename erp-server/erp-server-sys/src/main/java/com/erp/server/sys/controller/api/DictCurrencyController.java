package com.erp.server.sys.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.server.sys.service.DictCurrencyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 公共基础
 *
 * @author Lambda
 * @Classname DictCurrencyController

 * @Date 2023-03-21 17:14
 * @Created by yl
 */

@RestController
@RequestMapping("currency")
public class DictCurrencyController extends BaseController {

    @Resource
    private  DictCurrencyService dictCurrencyService;

    /**
     * 获取到货币列表
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<CurrencyDTO.ViewDTO>> getList() {
        List<CurrencyDTO.ViewDTO>  list=  dictCurrencyService.getList();
        return success(list);
    }
    /**
     * 货币分页查询-高级搜索
     *
     * @return ApiResult<PagingVO <CurrencyDTO.ViewDTO>>
     * @author zdy
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<CurrencyDTO.ViewDTO>> pagingSelect(@RequestBody @Validated PagingDTO<CurrencyDTO.SelectDTO> dto) {
        return success(dictCurrencyService.pagingSelect(dto));
    }
}
