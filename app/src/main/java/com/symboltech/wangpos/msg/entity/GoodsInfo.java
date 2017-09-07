package com.symboltech.wangpos.msg.entity;

import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.StringUtil;

import java.io.Serializable;


/**
 * 商品info
 * 
 * @author so
 * 
 */
public class GoodsInfo implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	private String id; // 商品id
	private String inx;// 商品顺序
	private String code;// 商品代码
	private String price;// 商品价格
	private String zkprice;// 商品折扣价格
	private String usedpoint = "0";// 消耗的总积分
	private String grantpoint = "0";//获得的积分
	private String saleamt;// 销售金额
	private String salecount = "1";// 商品数量
	private String discmoney = "0";// 折扣金额
	private String preferentialmoney = "0";// 优惠金额
	private String brandcode;// 品牌编码
	private String brandname;// 品牌名称
	private String barcode;// 条码
	private String goodsname;// 名称
	private String pic;// 图片
	private String unit;// 单位
	private String point = "0";// 单品消耗积分
	private String addprice; // 积分换购价格
	private String sptype;// 商品类型 0 品种商品 1 积分商品 2部分积分 3大类商品
	private String usedpointtemp = "0";// 消耗的积分临时变量
	private String spmode = ""; //0:定价商品，不许修改单价

	public String getSpmode() {
		return spmode;
	}

	public void setSpmode(String spmode) {
		this.spmode = spmode;
	}

	public String getGrantpoint() {
		return grantpoint;
	}

	public void setGrantpoint(String grantpoint) {
		this.grantpoint = grantpoint;
	}

	public String getUsedpointtemp() {
		return usedpointtemp;
	}

	public void setUsedpointtemp(String usedpointtemp) {
		this.usedpointtemp = usedpointtemp;
	}

	public String getSptype() {
		return sptype;
	}

	public void setSptype(String sptype) {
		this.sptype = sptype;
	}

	public String getAddprice() {
		return addprice;
	}

	public void setAddprice(String addprice) {
		this.addprice = addprice;
		updatepriceandointegral();
	}

	public String getZkprice() {
		return zkprice;
	}

	public void setZkprice(String zkprice) {
		this.zkprice = zkprice;
		updatepriceandointegral();
	}

	public String getPoint() {
		return point;
	}

	public void setPoint(String point) {
		this.point = point;
		updatepriceandointegral();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInx() {
		return inx;
	}

	public void setInx(String inx) {
		this.inx = inx;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
		updatepriceandointegral();
	}

	public String getUsedpoint() {
		return usedpoint;
	}

	public void setUsedpoint(String usedpoint) {
		this.usedpoint = usedpoint;
	}

	public String getSaleamt() {
		return saleamt;
	}

	public void setSaleamt(String saleamt) {
		this.saleamt = saleamt;
	}

	public String getSalecount() {
		return salecount;
	}

	public void setSalecount(String salecount) {
		this.salecount = salecount;
		updatepriceandointegral();
	}

	public String getDiscmoney() {
		return discmoney;
	}

	public void setDiscmoney(String discmoney) {
		this.discmoney = discmoney;
	}

	public String getPreferentialmoney() {
		return preferentialmoney;
	}

	public void setPreferentialmoney(String preferentialmoney) {
		this.preferentialmoney = preferentialmoney;
	}

	public String getBrandcode() {
		return brandcode;
	}

	public void setBrandcode(String brandcode) {
		this.brandcode = brandcode;
	}

	public String getBrandname() {
		return brandname;
	}

	public void setBrandname(String brandname) {
		this.brandname = brandname;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getGoodsname() {
		return goodsname;
	}

	public void setGoodsname(String goodsname) {
		this.goodsname = goodsname;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * 更新总价格和积分详情
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 */
	private void updatepriceandointegral() {
		// TODO Auto-generated method stub
		try {
			if (!StringUtil.isEmpty(this.point) && !StringUtil.isEmpty(this.salecount)) {
				this.usedpointtemp =  MoneyAccuracyUtils.getmoneybytwo(ArithDouble.parseInt(this.salecount) * ArithDouble.parseDouble(this.point));
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if (!StringUtil.isEmpty(sptype) && !StringUtil.isEmpty(this.salecount)
					&& (!StringUtil.isEmpty(this.price) || !StringUtil.isEmpty(this.addprice))) {
				if (ConstantData.GOODS_SOURCE_BY_BRAND.equals(sptype) || ConstantData.GOODS_SOURCE_BY_BINTEGRAL.equals(sptype)) {
					this.saleamt = MoneyAccuracyUtils.getmoneybytwo(ArithDouble.parseInt(this.salecount) * ArithDouble.sub(ArithDouble.parseDouble(this.price), ArithDouble.parseDouble(this.zkprice)));
				} else if (ConstantData.GOODS_SOURCE_BY_INTEGRAL.equals(sptype)|| ConstantData.GOODS_SOURCE_BY_SINTEGRAL.equals(sptype)) {
					this.saleamt = MoneyAccuracyUtils.getmoneybytwo(ArithDouble.parseInt(this.salecount) * ArithDouble.parseDouble(this.addprice));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
