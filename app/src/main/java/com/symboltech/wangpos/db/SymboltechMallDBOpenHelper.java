package com.symboltech.wangpos.db;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.symboltech.wangpos.log.LogUtil;

/**
 * Database SymboltechDBOpenHelper simple introduction
 *
 * <p>
 * detailed comment
 * 
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月28日
 * @see
 * @since 1.0
 */
public class SymboltechMallDBOpenHelper {

	private static final String TAG = "SymboltechDBOpenHelper";

	/** context */
	private final Context mCtx;
	/** DatabaseHelper */
	private DatabaseHelper mDbHelper;
	/** SQLiteDatabase */
	private SQLiteDatabase mDb;

	/** Database Name */
	private static final String DATABASE_NAME = "sysboltech_mall_pos_database";

	/** Database Version */
	private static final int DATABASE_VERSION = 1;

	/** Table Name */
	private static final String DATABASE_TABLE_LOGIN_USER_NAME = "login_user_name";
	public static final String LOGIN_USERNAME = "username";	// 用户名 
	public static final String LOGIN_CREATTIME = "creattime";// 时间戳 
	/** table creat for user lgoin check */
	private static final String CREAT_LOGIN_USER_TABLE = "create table " + DATABASE_TABLE_LOGIN_USER_NAME
			+ "(_id integer primary key autoincrement, " + LOGIN_USERNAME + " varchar(20), " + LOGIN_CREATTIME
			+ " varchar(20))";
	
	/** Table Name */
	private static final String DATABASE_TABLE_LOGIN = "login";
	public static final String LOGIN_USE_ID = "personid";	// 用户名ID
	public static final String LOGIN_USE_NAME = "rydm";	// 登录ID
	public static final String LOGIN_PASSWORLD = "password";// 密码
	public static final String LOGIN_PERSONNAME = "personname";// 收款员姓名
	/** table creat for user lgoin check */
	private static final String CREAT_LOGIN_TABLE = "create table " + DATABASE_TABLE_LOGIN
			+ "(_id integer primary key autoincrement, " + LOGIN_USE_ID + " varchar(20) unique, " + LOGIN_USE_NAME + " varchar(20), " 
			+LOGIN_PERSONNAME + " varchar(20), "+ LOGIN_PASSWORLD + " varchar(20))";
	
	private static final String DATABASE_TABLE_PAYTYPE = "pay_type";//支付方式表名
	private static final String RECORD_NUM = "billid";//记录编号
	private static final String PAY_TYPE = "type";//收款方式
	private static final String PAY_ID = "id"; // 收款ID
	private static final String PAY_MONEY = "money";//收款金额
	private static final String YY_MONEY = "overage";//溢余金额
	private static final String PAY_NAME = "name";//名称
	private static final String SKY = "personid";//收款员
	private static final String SKRQ = "skrq";//收款日期
	/** table creat for paytype */
	private static final String CREAT_PAYTYPE_TABLE = "create table " + DATABASE_TABLE_PAYTYPE
			+ "(_id integer primary key autoincrement, " + RECORD_NUM + " varchar(10) not null references user_order(billid) on delete cascade, " + PAY_ID + " varchar(5) not null, "
			+ PAY_TYPE + " varchar(5) not null, " + PAY_MONEY+ " decimal(14,2) not null, "+ YY_MONEY +" decimal(14,2) default 0.0, "
			+ SKY +" varchar(10) not null, "+ SKRQ +" timespace not null, "+PAY_NAME+" varchar(10) not null)";
	
	private static final String DATABASE_TABLE_GOODS ="goods_info";//商品信息表
	private static final String GOODS_ID = "id";//商品ID
	private static final String GOODS_NAME = "goodsname";
	private static final String GOODS_USEDPOINT = "usedpoint";
	private static final String GOODS_BARCODE = "barcode";//商品条形码
	private static final String GOODS_CODE = "code";//商品码
	private static final String GOODS_PRICE = "price";//零售单价
	private static final String GOODS_COUNT = "salecount";//销售数量
	private static final String GOODS_INX = "inx";//顺序号
	private static final String GOODS_DISCMONEY ="discmoney";//折扣金额
	private static final String GOODS_YHJE = "preferentialmoney";//优惠金额
	private static final String GOODS_UNIT = "unit";//单位
	private static final String GOODS_SALEAMT = "saleamt";//销售金额
	/** table creat for goods */
	private static final String CREAT_GOODS_TABLE = "create table " + DATABASE_TABLE_GOODS
			+ "(_id integer primary key autoincrement, " + RECORD_NUM + " varchar(10) not null references user_order(billid) on delete cascade, " + GOODS_ID + " varchar(10) not null, "
			+ GOODS_NAME + " varchar(13) not null, " + GOODS_USEDPOINT +" decimal(14,2) not null, "+ GOODS_CODE+" varchar(13) not null, "
			+ GOODS_BARCODE + " varchar(13) not null, " + GOODS_PRICE +" decimal(14,2) not null, "+ GOODS_COUNT +" integer not null, "
			+ GOODS_INX + " varchar(5) not null, "+ GOODS_DISCMONEY + " decimal(14,2) default 0.0, "+ GOODS_YHJE + " decimal(14,2) default 0.0, "
			+ GOODS_UNIT + " varchar(5), " + GOODS_SALEAMT + " decimal(14,2) not null)";

	/** Table Name orderID */
	private static final String DATABASE_TABLE_ORDER = "user_order";
	private static final String ORDER_PERSONID = "personid";//收款员id
	private static final String ORDER_CASHIER = "cashier";//销售员id
	private static final String ORDER_CASHIERNAME = "cashiername";//销售员名称
	private static final String ORDER_OLDBILLID = "oldbillid";//原销售id
	private static final String ORDER_OLDPOSNO = "oldposno";//原款台号
	private static final String ORDER_SALETYPE = "saletype";//交易类型
	private static final String ORDER_TOTALMONEY = "totalmoney";//交易金额
	private static final String ORDER_BACKREASON = "backreason";//退货原因
	private static final String ORDER_CHANGEMONEY = "changemoney";//找零金额
	private static final String ORDER_STATUS = "status";//交易状态
	private static final String ORDER_BJ_OFFLINE = "offline";//是否上传
	private static final String ORDER_SALEGOODSTIME = "time";//提交商品时间
	private static final String ORDER_SALETIME = "saletime";//交易时间
	/** table creat for user order print check */
	private static final String CREAT_ORDERID_TABLE = "create table " + DATABASE_TABLE_ORDER
			+ "(" + RECORD_NUM + " varchar(10) primary key, " 
			+ ORDER_PERSONID + " varchar(5) not null, " + ORDER_CASHIER +" varchar(5) not null, "
			+ ORDER_CASHIERNAME + " varchar(10) not null, "
			+ ORDER_OLDBILLID+" varchar(10), " + ORDER_OLDPOSNO + " varchar(10), " 
			+ ORDER_SALETYPE+ " varchar(5) not null, " + ORDER_TOTALMONEY +" decimal(14,2) not null, "
			+ ORDER_BACKREASON+" varchar(30), "+ ORDER_CHANGEMONEY + " decimal(14,2) default 0.0, "
			+ ORDER_SALEGOODSTIME +" timespace not null, "+ ORDER_STATUS +" varchar(5) not null default 0, "
			+ ORDER_BJ_OFFLINE + " varchar(5) not null default 0, "+ORDER_SALETIME+" timespace)";

	/** Table Name */
	private static final String DATABASE_TABLE_BANK_INFO = "bank_info";
	public static final String BANK_PERSON_ID = "posno";	// 款台号
	public static final String BANK_BILLID = "billid";	// 小票号
	public static final String BANK_TRANSTYPE = "transtype";// 交易类型(1消费,2当日撤销,3隔日退货)
	public static final String BANK_CARDNO = "cardno";// 卡号(支付宝账号、微信账号)
	public static final String BANK_BANKCODE = "bankcode";	// 银行代码
	public static final String BANK_BATCHNO = "batchno";	// 批次号
	public static final String BANK_REFNO = "refno";// 参考号
	public static final String BANK_TRADENO = "tradeno";// 流水号
	public static final String BANK_AMOUNT = "amount";// 金额
	public static final String BANK_BJ_OFFLINE = "offline";//是否上传
	public static final String BANK_SKFSID = "skfsid";//是否上传
	public static final String BANK_DECMONEY = "decmoney";// 扣减金额(预留，如果银行有扣减活动，则记录)
	public static final String BANK_SKRQ = "skrq";//收款日期
	/** table creat for bank_info */
	private static final String CREAT_BANK_INFO_TABLE = "create table if not exists " + DATABASE_TABLE_BANK_INFO
			+ "(_id integer primary key autoincrement, " + BANK_BILLID + " varchar(20) not null, " + BANK_PERSON_ID + " varchar(20) not null, "
			+ BANK_TRANSTYPE + " varchar(10) not null, "+ BANK_CARDNO + " varchar(20) not null, " + BANK_BANKCODE + " varchar(20), "
			+ BANK_BATCHNO + " varchar(20), " + BANK_REFNO + " varchar(20), " + BANK_TRADENO + " varchar(20) not null, " + BANK_AMOUNT + " decimal(14,2) not null, "
			+ BANK_SKFSID + " varchar(20), " + BANK_SKRQ +" timespace not null, " + BANK_BJ_OFFLINE + " varchar(5) not null default 0, " + BANK_DECMONEY + " decimal(14,2))";

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public SymboltechMallDBOpenHelper(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * 
	 * @author CWI-APST emial:26873204@qq.com
	 * @Description: TODO(getDatabaseHelper)
	 * @return
	 * @throws SQLException
	 */
	public DatabaseHelper getDatabaseHelper() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		return mDbHelper;
	}

	/**
	 * Inner private class. Database Helper class for creating and updating
	 * database.
	 */
	public static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * onCreate method is called for the 1st time when database doesn't
		 * exists.
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("pragma foreign_keys=on");
			db.execSQL(CREAT_LOGIN_TABLE);
			db.execSQL(CREAT_LOGIN_USER_TABLE);
			db.execSQL(CREAT_ORDERID_TABLE);
			db.execSQL(CREAT_PAYTYPE_TABLE);
			db.execSQL(CREAT_GOODS_TABLE);
			db.execSQL(CREAT_BANK_INFO_TABLE);
		}

		/**
		 * onUpgrade method is called when database version changes.
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			LogUtil.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
			if(newVersion > oldVersion){
				onCreate(db); //创建新的表
			}
		}
	}

}
