package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 小票格式返回信息
 * simple Stringroduction
 *
 * <p>detailed comment
 * @author zmm
 * @see
 * @since 1.0
 */
public class TicketFormatInfo implements Serializable{
	/** TODO*/
	private static final long serialVersionUID = 1L;
	
	/**
     * ticktype : {"id":1,"name":"销售小票"}
     * tickbegin : 欢迎光临步步高［换行］店铺：［店铺名称］ 订单号：［订单号］［换行］ 收款台号：［收款台号］［换行］ 收款员：［收款员姓名］［换行］ 销售：［销售员姓名］［换行］ 日期：［交易日期］［交易时间］［换行］
     * tickend : 网址：［网址］［换行］服务热线：［服务热线］
     * goods : 商品  数量  金额  消耗积分［换行］［分割行］ ［商品名称］［换行］ ［商品代码］［数量］［金额］［消耗积分］［换行］ ［分割行］
     * moneys : 合计[总数量］［总金额］［总消耗积分］［换行］[代金券名称］ [代金券金额］［换行］ [积分抵扣名称］ [积分抵扣金额］［换行］ 应付[应付金额］［换行］ [分割行］ 实付 [实付金额］［换行］ [收款方式名称］ [收款方式金额］［换行］ 找零 [找零金额］［换行］ [分割行］
     * vip : 会员卡号    ［会员卡号］［换行］新增积分 ［新增积分］［换行］ 消耗积分 ［消耗积分］［换行］ 累计积分 ［累计积分］［换行］ ［分割行］
     * bank : null
     * sendcoupon : 新增券［换行］［优惠券名称］［优惠券金额］［换行］
     * usecoupon : 使用券［换行］［优惠券名称］［优惠券金额］［换行］
     * owncoupon : 拥有券［换行］［优惠券名称］［优惠券金额］［换行］ ［分割行］
     * report :
     * tickbasic : {"conditionindex":[{"id":32,"conditionid":1,"yxj":1},{"id":32,"conditionid":3,"yxj":2},{"id":32,"conditionid":4,"yxj":3},{"id":32,"conditionid":5,"yxj":4},{"id":32,"conditionid":8,"yxj":5},{"id":32,"conditionid":7,"yxj":6},{"id":32,"conditionid":9,"yxj":7},{"id":32,"conditionid":2,"yxj":8},{"id":32,"conditionid":6,"yxj":9}],"id":32,"paperwidth":50,"topmargin":5,"bottommargin":5,"leftmargin":2,"rightmargin":2,"rowspace":0,"lineformat":"---------------------------------------------------","dateformat":"yyyy.mm.dd","timeformat":"hh:mm:ss","website":"www.bbkmall.com","hotline":"4000-856-857","alignment":0,"cutway":0}
     */

    private List<Tickdatas> tickdatas;

    public List<Tickdatas> getTickdatas() {
		return tickdatas;
	}

	public void setTickdatas(List<Tickdatas> tickdatas) {
		this.tickdatas = tickdatas;
	}

	public static class Tickdatas implements Serializable{
        /**
         * id : 1
         * name : 销售小票
         */
		private static final long serialVersionUID = 1L;
        private TicktypeEntity ticktype;
        private String tickbegin;
        private String tickend;
        private String goods;
        private String moneys;
        private String vip;
        private String bank;
        private String sendcoupon;
        private String usecoupon;
        private String owncoupon;
        private String report;
        private String park;
        private String printcount;
        /**
         * conditionindex : [{"id":32,"conditionid":1,"yxj":1},{"id":32,"conditionid":3,"yxj":2},{"id":32,"conditionid":4,"yxj":3},{"id":32,"conditionid":5,"yxj":4},{"id":32,"conditionid":8,"yxj":5},{"id":32,"conditionid":7,"yxj":6},{"id":32,"conditionid":9,"yxj":7},{"id":32,"conditionid":2,"yxj":8},{"id":32,"conditionid":6,"yxj":9}]
         * id : 32
         * paperwidth : 50
         * topmargin : 5
         * bottommargin : 5
         * leftmargin : 2
         * rightmargin : 2
         * rowspace : 0
         * lineformat : ---------------------------------------------------
         * dateformat : yyyy.mm.dd
         * timeformat : hh:mm:ss
         * website : www.bbkmall.com
         * hotline : 4000-856-857
         * alignment : 0
         * cutway : 0
         */

        private TickbasicEntity tickbasic;

        public String getPrintcount() {
            return printcount;
        }

        public void setPrintcount(String printcount) {
            this.printcount = printcount;
        }

        public void setTicktype(TicktypeEntity ticktype) {
            this.ticktype = ticktype;
        }

        public void setTickbegin(String tickbegin) {
            this.tickbegin = tickbegin;
        }

        public void setTickend(String tickend) {
            this.tickend = tickend;
        }

        public void setGoods(String goods) {
            this.goods = goods;
        }

        public void setMoneys(String moneys) {
            this.moneys = moneys;
        }

        public void setVip(String vip) {
            this.vip = vip;
        }

        public void setBank(String bank) {
            this.bank = bank;
        }

        public void setSendcoupon(String sendcoupon) {
            this.sendcoupon = sendcoupon;
        }

        public void setUsecoupon(String usecoupon) {
            this.usecoupon = usecoupon;
        }

        public void setOwncoupon(String owncoupon) {
            this.owncoupon = owncoupon;
        }

        public void setReport(String report) {
            this.report = report;
        }

        public void setTickbasic(TickbasicEntity tickbasic) {
            this.tickbasic = tickbasic;
        }

        public TicktypeEntity getTicktype() {
            return ticktype;
        }

        public String getTickbegin() {
            return tickbegin;
        }

        public String getTickend() {
            return tickend;
        }

        public String getGoods() {
            return goods;
        }

        public String getMoneys() {
            return moneys;
        }

        public String getVip() {
            return vip;
        }

        public String getBank() {
            return bank;
        }

        public String getSendcoupon() {
            return sendcoupon;
        }

        public String getUsecoupon() {
            return usecoupon;
        }

        public String getOwncoupon() {
            return owncoupon;
        }

        public String getReport() {
            return report;
        }

        
        public String getPark() {
			return park;
		}

		public void setPark(String park) {
			this.park = park;
		}

		public TickbasicEntity getTickbasic() {
            return tickbasic;
        }

        public static class TicktypeEntity implements Serializable{
        	
        	private static final long serialVersionUID = 1L;
        	private String id;
            private String name;

            public void setId(String id) {
                this.id = id;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getId() {
                return id;
            }

            public String getName() {
                return name;
            }
        }

        public static class TickbasicEntity implements Serializable{
        	
        	private static final long serialVersionUID = 1L;
        	private String id;
            private String paperwidth;
            private String topmargin;
            private String bottommargin;
            private String leftmargin;
            private String rightmargin;
            private String rowspace;
            private String lineformat;
            private String lineformat_again;
            private String dateformat;
            private String timeformat;
            private String website;
            private String hotline;
            private String alignment;
            private String cutway;
            /**
             * id : 32
             * conditionid : 1
             * yxj : 1
             */

            private List<ConditionindexEntity> conditionindex;

            public void setId(String id) {
                this.id = id;
            }

            public void setPaperwidth(String paperwidth) {
                this.paperwidth = paperwidth;
            }

            public void setTopmargin(String topmargin) {
                this.topmargin = topmargin;
            }

            public void setBottommargin(String bottommargin) {
                this.bottommargin = bottommargin;
            }

            public void setLeftmargin(String leftmargin) {
                this.leftmargin = leftmargin;
            }

            public void setRightmargin(String rightmargin) {
                this.rightmargin = rightmargin;
            }

            public void setRowspace(String rowspace) {
                this.rowspace = rowspace;
            }

            public void setLineformat(String lineformat) {
                this.lineformat = lineformat;
            }

            public void setDateformat(String dateformat) {
                this.dateformat = dateformat;
            }

            public void setTimeformat(String timeformat) {
                this.timeformat = timeformat;
            }

            public void setWebsite(String website) {
                this.website = website;
            }

            public void setHotline(String hotline) {
                this.hotline = hotline;
            }

            public void setAlignment(String alignment) {
                this.alignment = alignment;
            }

            public void setCutway(String cutway) {
                this.cutway = cutway;
            }

            public void setConditionindex(List<ConditionindexEntity> conditionindex) {
                this.conditionindex = conditionindex;
            }

            public String getLineformat_again() {
				return lineformat_again;
			}

			public void setLineformat_again(String lineformat_again) {
				this.lineformat_again = lineformat_again;
			}

			public String getId() {
                return id;
            }

            public String getPaperwidth() {
                return paperwidth;
            }

            public String getTopmargin() {
                return topmargin;
            }

            public String getBottommargin() {
                return bottommargin;
            }

            public String getLeftmargin() {
                return leftmargin;
            }

            public String getRightmargin() {
                return rightmargin;
            }

            public String getRowspace() {
                return rowspace;
            }

            public String getLineformat() {
                return lineformat;
            }

            public String getDateformat() {
                return dateformat;
            }

            public String getTimeformat() {
                return timeformat;
            }

            public String getWebsite() {
                return website;
            }

            public String getHotline() {
                return hotline;
            }

            public String getAlignment() {
                return alignment;
            }

            public String getCutway() {
                return cutway;
            }

            public List<ConditionindexEntity> getConditionindex() {
                return conditionindex;
            }

            public static class ConditionindexEntity implements Serializable{
            	
            	private static final long serialVersionUID = 1L;
            	private String id;
                private String conditionid;
                private String yxj;

                public void setId(String id) {
                    this.id = id;
                }

                public void setConditionid(String conditionid) {
                    this.conditionid = conditionid;
                }

                public void setYxj(String yxj) {
                    this.yxj = yxj;
                }

                public String getId() {
                    return id;
                }

                public String getConditionid() {
                    return conditionid;
                }

                public String getYxj() {
                    return yxj;
                }
            }
        }
    }
	
}
