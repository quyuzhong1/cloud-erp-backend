package com.erp.server.msg.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname: FeiShuSendSingleParam
 * @Description: TODO
 * @CreateTime: 2023-04-20  15:03
 * @Author: zhangchunlin
 */
@Data
public class FeiShuSendMultiParam extends FeiShuSendBaseParam implements Serializable {

   @JSONField(name = "union_ids")
   private List<String> unionIds;

}