<div id="box" class="pdfDom" style="width: 100mm;height:100mm;page-break-before:always;border:1px solid #ddd">
  <div style="height:20mm;border-bottom:2px solid #000;display:flex; align-items: center;
  justify-content: space-between;font-size:14px">
    <div style="width:70mm">
      <!-- 这里放条码 -->
      <img ref="a" id="barcode" />
    </div>
    <div>
      <label>打印时间&nbsp;</label>
      <span>${printTime}</span>
    </div>
  </div>
  <div style="height:18mm;border-bottom:1px solid #000;font-size:14px;display:flex;flex-wrap: wrap;">

    <!-- 写循环即可 -->
    <div style="width:50%;">
      <div style="display:flex;align-items: center;">
        <b>店铺&nbsp;</b>
        <span>${shopName}</span>
      </div>
    </div>
    <div style="width:50%;">
      <div style="display:flex;align-items: center;">
        <b>买家id&nbsp;</b>
        <span>${customerId}</span>
      </div>
    </div>
    <div style="width:50%;">
      <div style="display:flex;align-items: center;">
        <b>金额&nbsp;</b>
        <span>${amount}</span>
      </div>
    </div>
    <div style="width:50%;">
      <div style="display:flex;align-items: center;">
        <b>实重&nbsp;</b>
        <span>${weight}</span>
      </div>
    </div>
    <div style="width:50%;">
      <div style="display:flex;align-items: center;">
        <b>渠道&nbsp;</b>
        <span>${channelName}</span>
      </div>
    </div>
    <div style="width:50%;">
      <div style="display:flex;align-items: center;">
        <b>运单号&nbsp;</b>
        <span>${transportNo}</span>
      </div>
    </div>
  </div>
  <div style="margin-bottom: 2mm;">
      <span style="font-size: 16px;"><b>备注:&nbsp;</b><span
                style="font-size: 20px;">${remark}</span></span>
  </div>
  <!-- 此处是希望转成pdf的部分的内容，用一个大div盒子包起来 -->
  <div style="line-height: 30px;font-size: 14px;">
    <table align="center" style="width: 100%; border-collapse: collapse;line-height:20px">
      <tbody>
      <tr>
        <th style=" border: 1px solid #c6c6c6;color: black;border-color: black;" align="center" valign="middle">图片
        </th>
        <th style=" border: 1px solid #c6c6c6;color: black;border-color: black;" align="center" valign="middle">SKU
        </th>
        <th style=" border: 1px solid #c6c6c6;color: black;border-color: black;" align="center" valign="middle">名称
        </th>
        <th style=" border: 1px solid #c6c6c6;color: black;border-color: black;" align="center" valign="middle">仓位
        </th>
        <th style=" border: 1px solid #c6c6c6;color: black;border-color: black;" align="center" valign="middle">数量
        </th>
        <th style=" border: 1px solid #c6c6c6;color: black;border-color: black;" align="center" valign="middle">多属性
        </th>
      </tr>

      <#list detailList as detail>
        <tr>

          <!-- 写循环即可 -->
          <td style="border: 1px solid #c6c6c6;padding-left:2px;color:black;border-color: black;" align="center"
              valign="middle">
            ${detail.skuImagesUrl}
          </td>
          <td style="border: 1px solid #c6c6c6;padding-left:2px;color:black;border-color: black;" align="center"
              valign="middle">
            ${detail.skuNo}
          </td>
          <td style="border: 1px solid #c6c6c6;padding-left:2px;color:black;border-color: black;" align="center"
              valign="middle">
            ${detail.productName}
          </td>
          <td style="border: 1px solid #c6c6c6;padding-left:2px;color:black;border-color: black;" align="center"
              valign="middle">
            ${detail.warehouseLocation}
          </td>
          <td style="border: 1px solid #c6c6c6;padding-left:2px;color:black;border-color: black;" align="center"
              valign="middle">
            ${detail.qty}
          </td>
          <td style="border: 1px solid #c6c6c6;padding-left:2px;color:black;border-color: black;" align="center"
              valign="middle">
            ${detail.variantProperty}
          </td>
        </tr>
      </#list>
      </tbody>
      <tfoot style="display:table-footer-group;">
      <tr>
        <td style=" border: 1px solid #c6c6c6;color: black;border-color: black;height:25px" colspan="10"
            align="right">
          <span><b>1</b>&nbsp;个商品种类&nbsp;&nbsp;<b>3</b>&nbsp;件商品</span>
        </td>
      </tr>
      </tfoot>
    </table>
  </div>
</div>