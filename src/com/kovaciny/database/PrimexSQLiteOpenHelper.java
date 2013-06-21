package com.kovaciny.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.runners.ParentRunner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kovaciny.primexmodel.Product;
import com.kovaciny.primexmodel.ProductionLine;
import com.kovaciny.primexmodel.Roll;
import com.kovaciny.primexmodel.Sheet;
import com.kovaciny.primexmodel.SpeedValues;
import com.kovaciny.primexmodel.WorkOrder;

public class PrimexSQLiteOpenHelper extends SQLiteOpenHelper {
    
	public PrimexSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	// If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 57;
    public static final String DATABASE_NAME = "Primex.db";
    
	private static final String TEXT_TYPE = " TEXT";
	private static final String DOUBLE_TYPE = " DOUBLE";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	private static final String REAL_TYPE = " REAL";
		
	private static final String SQL_CREATE_PRODUCTION_LINES =
    	    "CREATE TABLE " + PrimexDatabaseSchema.ProductionLines.TABLE_NAME + " (" +
    	    PrimexDatabaseSchema.ProductionLines._ID + " INTEGER PRIMARY KEY," +
    	    PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LINE_NUMBER + INTEGER_TYPE + COMMA_SEP +
    	    PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LENGTH + INTEGER_TYPE + COMMA_SEP +
    	    PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_DIE_WIDTH + INTEGER_TYPE + COMMA_SEP +
    	    PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_SPEED_CONTROLLER_TYPE + TEXT_TYPE + COMMA_SEP +
    	    PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_SPEED_SETPOINT + DOUBLE_TYPE + COMMA_SEP +
    	    PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_DIFFERENTIAL_SPEED_SETPOINT + DOUBLE_TYPE + COMMA_SEP +
    	    PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_SPEED_FACTOR + DOUBLE_TYPE + COMMA_SEP +
    	    PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_TAKEOFF_EQUIPMENT_TYPE + TEXT_TYPE +
    	    " )";
    
    private static final String SQL_CREATE_WORK_ORDERS =
    	    "CREATE TABLE " + PrimexDatabaseSchema.WorkOrders.TABLE_NAME + " (" +
    	    PrimexDatabaseSchema.WorkOrders._ID + " INTEGER PRIMARY KEY," +
	    	PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER + INTEGER_TYPE + COMMA_SEP +
	    	PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_SELECTED_PRODUCT_ID + INTEGER_TYPE + COMMA_SEP +
	    	PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_TOTAL_PRODUCTS_ORDERED + DOUBLE_TYPE + COMMA_SEP +
	    	PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_NUMBER_OF_SKIDS + INTEGER_TYPE + COMMA_SEP +
	    	PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_SELECTED_SKID_ID + INTEGER_TYPE + COMMA_SEP +
	    	PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_MAXIMUM_STACK_HEIGHT + DOUBLE_TYPE + COMMA_SEP +
	    	" UNIQUE (" + PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER + ")" +
	    	" )";
    
    private static final String SQL_CREATE_LINE_WORK_ORDER_LINK = 
    		"CREATE TABLE " + PrimexDatabaseSchema.LineWorkOrderLink.TABLE_NAME + " (" +
    		PrimexDatabaseSchema.LineWorkOrderLink._ID + " INTEGER PRIMARY KEY, " + 
    		PrimexDatabaseSchema.LineWorkOrderLink.COLUMN_NAME_LINE_ID + INTEGER_TYPE + COMMA_SEP + 
    		PrimexDatabaseSchema.LineWorkOrderLink.COLUMN_NAME_WO_ID + INTEGER_TYPE + COMMA_SEP +
    		" UNIQUE (" + PrimexDatabaseSchema.LineWorkOrderLink.COLUMN_NAME_LINE_ID + ")" +
    		")";
    		

    private static final String SQL_CREATE_PRODUCTS = 
    		"CREATE TABLE " + PrimexDatabaseSchema.Products.TABLE_NAME + " (" +
    		PrimexDatabaseSchema.Products._ID + " INTEGER PRIMARY KEY," +
    		PrimexDatabaseSchema.Products.COLUMN_NAME_GAUGE + REAL_TYPE + COMMA_SEP +
    		PrimexDatabaseSchema.Products.COLUMN_NAME_WIDTH + REAL_TYPE + COMMA_SEP +
    		PrimexDatabaseSchema.Products.COLUMN_NAME_LENGTH + REAL_TYPE + COMMA_SEP +
    		PrimexDatabaseSchema.Products.COLUMN_NAME_TYPE + INTEGER_TYPE + COMMA_SEP +
    		PrimexDatabaseSchema.Products.COLUMN_NAME_WO_NUMBER + INTEGER_TYPE + COMMA_SEP +
    		" UNIQUE ("  + PrimexDatabaseSchema.Products.COLUMN_NAME_WO_NUMBER + ")" +
    		" )";
    
    private static final String SQL_CREATE_PRODUCT_TYPES = 
    		"CREATE TABLE " + PrimexDatabaseSchema.ProductTypes.TABLE_NAME + " (" +
    		PrimexDatabaseSchema.ProductTypes._ID + " INTEGER PRIMARY KEY," +
    		PrimexDatabaseSchema.ProductTypes.COLUMN_NAME_TYPES + TEXT_TYPE +
    		//I'm just gonna be careful cause I don't want to turn foreign keys on.
    		//"FOREIGN KEY(" + PrimexDatabaseSchema.ProductTypes.COLUMN_NAME_TYPES + ") REFERENCES +" +
    		//		PrimexDatabaseSchema.ProductTypes.COLUMN_NAME_TYPES +
    		" )";
    
    private static final String SQL_CREATE_MODEL_STATE = 
    		"CREATE TABLE " + PrimexDatabaseSchema.ModelState.TABLE_NAME + " (" +
    		PrimexDatabaseSchema.ModelState._ID + " INTEGER PRIMARY KEY," +
    		PrimexDatabaseSchema.ModelState.COLUMN_NAME_SELECTED_LINE + INTEGER_TYPE + COMMA_SEP +
    		PrimexDatabaseSchema.ModelState.COLUMN_NAME_SELECTED_WORK_ORDER + INTEGER_TYPE +
    		" )";
    
	//list "child" tables, which have a foreign key, before their parent, so drop table works
    private static final String TABLE_NAME_LINE_WORK_ORDER_LINK = PrimexDatabaseSchema.LineWorkOrderLink.TABLE_NAME;
    private static final String TABLE_NAME_PRODUCT_TYPES = PrimexDatabaseSchema.ProductTypes.TABLE_NAME;
    private static final String TABLE_NAME_PRODUCTS = PrimexDatabaseSchema.Products.TABLE_NAME;
    private static final String TABLE_NAME_WORK_ORDERS = PrimexDatabaseSchema.WorkOrders.TABLE_NAME;
    private static final String TABLE_NAME_PRODUCTION_LINES = PrimexDatabaseSchema.ProductionLines.TABLE_NAME;
    private static final String TABLE_NAME_MODEL_STATE = PrimexDatabaseSchema.ModelState.TABLE_NAME;

    private static final String SQL_DELETE_LINE_WORK_ORDER_LINK = 
    		"DROP TABLE IF EXISTS " + TABLE_NAME_LINE_WORK_ORDER_LINK;
    private static final String SQL_DELETE_PRODUCT_TYPES =
			"DROP TABLE IF EXISTS " + TABLE_NAME_PRODUCT_TYPES;
	private static final String SQL_DELETE_PRODUCTS =
			"DROP TABLE IF EXISTS " + TABLE_NAME_PRODUCTS;
	private static final String SQL_DELETE_PRODUCTION_LINES =
			"DROP TABLE IF EXISTS " + TABLE_NAME_PRODUCTION_LINES;
	private static final String SQL_DELETE_WORK_ORDERS =
			"DROP TABLE IF EXISTS " + TABLE_NAME_WORK_ORDERS;
	private static final String SQL_DELETE_MODEL_STATE =
			"DROP TABLE IF EXISTS " + TABLE_NAME_MODEL_STATE;
	
    public void onCreate(SQLiteDatabase db) {
    	final List<Integer> lineNumbers = Arrays.asList(1,6,7,9,10,  11,12,13,14,15,  16,17,18); //13 lines
    	db.execSQL(SQL_CREATE_PRODUCTION_LINES);
        //Batch insert to SQLite database on Android
        try {
        	List<Integer> linesWithGearedSpeedControl = Arrays.asList(6,9,12,16,17); //TODO remove this and speed controller type
        	List<Double> lengthsList = 
        			Arrays.asList(new Double[] {99.0d, 51.5d, 34.2d, 46.0d, 44.7d,  45.7d,56.7d,56.3d,64.3d,46.5d, 45.0d,61.9d, 71d});
        	Iterator<Double> lengthsIterator = lengthsList.iterator();
        	
        	List<Double> speedFactorsList = Arrays.asList(new Double[]{1d,.0769d,1d,.99d,1.015d,  1d,0.9917d,.98d,1d,1.01d, 1d,.0347d,.987d});        	
        	Iterator<Double> speedFactorsIterator = speedFactorsList.iterator();
	        db.beginTransaction();
	        for (Integer lineNum : lineNumbers) {
	        	ContentValues values = new ContentValues();
	        	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LINE_NUMBER, lineNum);
	        	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LENGTH, lengthsIterator.next());
	        	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_DIE_WIDTH, 0);
	        	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_SPEED_SETPOINT, 0);
	        	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_DIFFERENTIAL_SPEED_SETPOINT, 0);
	        	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_SPEED_FACTOR, speedFactorsIterator.next());
	        	if (linesWithGearedSpeedControl.contains(lineNum)) {
	        		values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_SPEED_CONTROLLER_TYPE, ProductionLine.SPEED_CONTROLLER_TYPE_GEARED);
	        	} else {
	        		values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_SPEED_CONTROLLER_TYPE, ProductionLine.SPEED_CONTROLLER_TYPE_DIRECT);
	        	}
	        	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_TAKEOFF_EQUIPMENT_TYPE, "Maxson");
	        	
	        	long rowId = db.insertOrThrow(
	        			PrimexDatabaseSchema.ProductionLines.TABLE_NAME, 
	        			null, 
	        			values);
	        	
	        }
	        db.setTransactionSuccessful();
        } finally {
          db.endTransaction();
        }
        
        db.execSQL(SQL_CREATE_WORK_ORDERS);
        try {
        	db.beginTransaction();
        	ContentValues values = new ContentValues();
        	values.put(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER,123);
        	values.put(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_TOTAL_PRODUCTS_ORDERED,69);
        	values.put(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_NUMBER_OF_SKIDS,1);
        	values.put(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_MAXIMUM_STACK_HEIGHT,0);
        	
        	db.insertOrThrow(PrimexDatabaseSchema.WorkOrders.TABLE_NAME, null, values);
        	db.setTransactionSuccessful();
        } finally {
        	db.endTransaction();
        }

        db.execSQL(SQL_CREATE_LINE_WORK_ORDER_LINK);
        try {
        	db.beginTransaction();
        	ContentValues values = new ContentValues();
        	values.put(PrimexDatabaseSchema.LineWorkOrderLink.COLUMN_NAME_LINE_ID, 7);
        	values.put(PrimexDatabaseSchema.LineWorkOrderLink.COLUMN_NAME_WO_ID, 1);
        	db.insertOrThrow(PrimexDatabaseSchema.LineWorkOrderLink.TABLE_NAME, null, values);
        	db.setTransactionSuccessful();
        } finally {
        	db.endTransaction();
        }
        db.execSQL(SQL_CREATE_PRODUCT_TYPES);
        try {
        	db.beginTransaction();
        	String[] types = {Product.SHEETS_TYPE, Product.ROLLS_TYPE};
        	for (int j = 0; j < 2; j++) {
	        	ContentValues ptvalues = new ContentValues();
	        	ptvalues.put(PrimexDatabaseSchema.ProductTypes.COLUMN_NAME_TYPES, types[j]);
	        	
	        	long rowId = db.insertOrThrow(PrimexDatabaseSchema.ProductTypes.TABLE_NAME, null, ptvalues);
        	}
	        db.setTransactionSuccessful();
        } finally {
        	db.endTransaction();
        }
        
        db.execSQL(SQL_CREATE_PRODUCTS);
        db.execSQL(SQL_CREATE_MODEL_STATE);
        try {
        	db.beginTransaction();
        	Integer lineNum = 12;
        	Integer woNum = 123;
        	ContentValues modvalues = new ContentValues();
        	modvalues.put(PrimexDatabaseSchema.ModelState.COLUMN_NAME_SELECTED_LINE, lineNum);
        	modvalues.put(PrimexDatabaseSchema.ModelState.COLUMN_NAME_SELECTED_WORK_ORDER, woNum);
        	
        	long rowId = db.insertOrThrow(PrimexDatabaseSchema.ModelState.TABLE_NAME, null, modvalues);
        	
	        db.setTransactionSuccessful();
        } finally {
        	db.endTransaction();
        }
        

    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_LINE_WORK_ORDER_LINK);
    	db.execSQL(SQL_DELETE_PRODUCTION_LINES);
        db.execSQL(SQL_DELETE_WORK_ORDERS);
        db.execSQL(SQL_DELETE_PRODUCT_TYPES);
        db.execSQL(SQL_DELETE_PRODUCTS);
        db.execSQL(SQL_DELETE_MODEL_STATE);
        onCreate(db);
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /*
     * CRUD methods go here, see http://pheide.com/page/11/tab/24#post13 if it gets to be too many
     * Just Ctrl+F for the name of the class
     */
    
    public void saveState() {
    	
    }
    
    public long addLine(ProductionLine newLine) {
    	SQLiteDatabase db = getWritableDatabase();
    	
    	ContentValues values = new ContentValues();
    	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LINE_NUMBER, newLine.getLineNumber());
    	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LENGTH, newLine.getLineLength());
    	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_DIE_WIDTH, newLine.getDieWidth());
    	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_SPEED_CONTROLLER_TYPE, newLine.getSpeedControllerType());
    	values.put(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_TAKEOFF_EQUIPMENT_TYPE, newLine.getTakeoffEquipmentType());
    	
    	long rowId = db.insertOrThrow(
    			PrimexDatabaseSchema.ProductionLines.TABLE_NAME, 
    			null, 
    			values);
    	
    	return rowId;
    }
    
    public ProductionLine getLine(int lineNumber){
    	SQLiteDatabase db = getReadableDatabase();
    	
    	Cursor resultCursor = db.query(
    			PrimexDatabaseSchema.ProductionLines.TABLE_NAME,
    			null, 
    			PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LINE_NUMBER + "=?",
    			new String[] {String.valueOf(lineNumber)},
    			null,
    			null,
    			null
    			);
    			
    	try {
    		resultCursor.moveToFirst();
	    	int ln_index = resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LINE_NUMBER);
	    	int ln = resultCursor.getInt(ln_index);
	    	int ll = resultCursor.getInt(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LENGTH));
	    	int dw = resultCursor.getInt(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_DIE_WIDTH));
	    	String sct = resultCursor.getString(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_SPEED_CONTROLLER_TYPE));
	    	String tet = resultCursor.getString(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_TAKEOFF_EQUIPMENT_TYPE));
	    	double sp = resultCursor.getDouble(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_SPEED_SETPOINT));
	    	double diff = resultCursor.getDouble(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_DIFFERENTIAL_SPEED_SETPOINT));
	    	double sf = resultCursor.getDouble(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_SPEED_FACTOR));
	    	
	    	ProductionLine newLine = new ProductionLine(ln,ll,dw,sct,tet);
	    	SpeedValues sv = new SpeedValues(sp,diff,sf);
	    	newLine.setSpeedValues(sv);
	    	return newLine;
	    } finally {
	    	if (resultCursor != null) resultCursor.close();
    	}    	
    }
    
    public List<Integer> getLineNumbers() {
    	SQLiteDatabase db = getReadableDatabase();
    	
    	Cursor c = db.query(
    		    PrimexDatabaseSchema.ProductionLines.TABLE_NAME,  // The table to query
    		    null,                               // The columns to return
    		    null,                                // The columns for the WHERE clause
    		    null,                            // The values for the WHERE clause
    		    null,                                     // don't group the rows
    		    null,                                     // don't filter by row groups
    		    null	                                 // The sort order
    		    );
    	
    	try {
    		List<Integer> lineNumbers = new ArrayList<Integer>();
    		while (c.moveToNext()) {
				int column = c.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LINE_NUMBER);
				lineNumbers.add(c.getInt(column));
    		}
	    	return lineNumbers;    		    		
    	} finally {
	    	if (c != null) c.close();
    	}
    }  








	
	
	
	
	
	
	
	
	
	public long insertOrReplaceWorkOrder(WorkOrder newWO) {
		SQLiteDatabase db = getWritableDatabase();
		
		/*String sql = "INSERT INTO " + PrimexDatabaseSchema.WorkOrders.TABLE_NAME + " (" +
				PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER + COMMA_SEP + 
				PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_SELECTED_PRODUCT_ID + " )"	
				"SELECT " + newWO.getWoNumber() + COMMA_SEP + 
				PrimexDatabaseSchema.Products._ID + 
				" WHERE " PrimexDatabaseSchema.Products.COLUMN_NAME_WO_ID " = " + newWO.getProduct()
				;*/
		if (newWO.hasProduct()) {
			insertOrReplaceProduct(newWO.getProduct(), newWO.getWoNumber());
			String query = "SELECT last_insert_rowid() FROM " + PrimexDatabaseSchema.Products.TABLE_NAME;
			Cursor c = db.rawQuery(query, null);
			int productId = 0;
			if (c != null && c.moveToFirst()) {
			    productId = c.getInt(0); //The 0 is the column index, we only have 1 column, so the index is 0
			}
			//debug TODO
			String sqll = "SELECT * FROM " + PrimexDatabaseSchema.Products.TABLE_NAME + " WHERE " + 
			PrimexDatabaseSchema.Products._ID + "=?";
			Cursor cc = db.rawQuery(sqll, new String[]{String.valueOf(productId)});
			if (cc != null && cc.moveToFirst()) {
			    String pproductId = cc.getString(1); //The 0 is the column index, we only have 1 column, so the index is 0
			}
//			values.put(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_SELECTED_SKID_ID, lastRowId);
		}
		
		ContentValues values = new ContentValues();
		values.put(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER, newWO.getWoNumber());
		double tpo = newWO.getTotalProductsOrdered();
		tpo = 69; //debug
		values.put(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_TOTAL_PRODUCTS_ORDERED, tpo);
		values.put(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_NUMBER_OF_SKIDS, newWO.getNumberOfSkids());
		values.put(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_MAXIMUM_STACK_HEIGHT, newWO.getMaximumStackHeight());
		if ( newWO.hasSelectedSkid()) {
			//TODO look up skid in skids table
			//values.put(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_SELECTED_SKID_ID, newWO.getSelectedSkid().getSkidNumber());	
		} else values.put(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_SELECTED_SKID_ID, 69); 
		
		long rowId = db.insertWithOnConflict(
				PrimexDatabaseSchema.WorkOrders.TABLE_NAME, 
				null, 
				values,
				SQLiteDatabase.CONFLICT_REPLACE);
		
		return rowId;
	}
	
	public WorkOrder getWorkOrder(int woNumber){
		SQLiteDatabase db = getReadableDatabase();

		Cursor resultCursor = db.rawQuery("SELECT * FROM " + PrimexDatabaseSchema.WorkOrders.TABLE_NAME + " WHERE " + 
				PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER + "=?", new String[]{String.valueOf(woNumber)});
		/*Cursor resultCursor = db.query(
				PrimexDatabaseSchema.WorkOrders.TABLE_NAME,
				null, 
				PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER + "=?",
				new String[] {String.valueOf(woNumber)},
				null,
				null,
				null
				);*/
		int wonum = -1;
		int prod_id = -1;
		double ordered = -1d;
		int skids = -1;
		int selected = -1;
		double height = -1d;
		try {
			if (resultCursor.moveToFirst()) {
		    	wonum = resultCursor.getInt(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER));
		    	prod_id = resultCursor.getInt(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_SELECTED_PRODUCT_ID));
		    	ordered = resultCursor.getDouble(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_TOTAL_PRODUCTS_ORDERED));
		    	skids = resultCursor.getInt(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_NUMBER_OF_SKIDS));
		    	selected = resultCursor.getInt(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_SELECTED_SKID_ID));
		    	height = resultCursor.getDouble(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_MAXIMUM_STACK_HEIGHT));
		    	
				WorkOrder wo = new WorkOrder(wonum);
				if (prod_id != -1) {
					wo.setProduct(getProduct(wonum));
				}
				if (ordered != -1) { //TODO this all seems like really bad error checking.
					wo.setTotalProductsOrdered(ordered);	
				}
				
				wo.setNumberOfSkids(skids);
				if (selected != -1) {
					//TODO wo.setSelectedSkid(selected);
				}
				wo.setMaximumStackHeight(height);
		    	return wo;
			} else return null;
	    } finally {
	    	if (resultCursor != null) resultCursor.close();
		}    	
	}
	
	public int getHighestWoNumber() {
		SQLiteDatabase db = getReadableDatabase();
		
		Cursor c = db.query(
			    PrimexDatabaseSchema.WorkOrders.TABLE_NAME,  // The table to query
			    new String[] {"MAX(WO_number)"}, // The columns to return
			    null,                                // The columns for the WHERE clause
			    null,                            // The values for the WHERE clause
			    null,                                     // don't group the rows
			    null,                                     // don't filter by row groups
			    null	                                 // The sort order
			    );
		
		try { 
			if (c.moveToFirst()) {
				return c.getInt(0);
			}
			else return 0; 
		} finally { c.close(); } 
	}
	
	public List<Integer> getWoNumbers() {
		SQLiteDatabase db = getReadableDatabase();
		
		List<Integer> workOrders = new ArrayList<Integer>();
		    	
		Cursor c = db.query(
			    PrimexDatabaseSchema.WorkOrders.TABLE_NAME,  // The table to query
			    null,                               // The columns to return
			    null,                                // The columns for the WHERE clause
			    null,                            // The values for the WHERE clause
			    null,                                     // don't group the rows
			    null,                                     // don't filter by row groups
			    null	                                 // The sort order
			    );
		
		try {
			while (c.moveToNext()) {
				workOrders.add( c.getInt( c.getColumnIndexOrThrow(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER) ) );
			}
		} finally {
	    	if (c != null) c.close();
		}
		
		return workOrders;
	}
	
	public void clearWorkOrders() {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(SQL_DELETE_LINE_WORK_ORDER_LINK);
		db.execSQL(SQL_CREATE_LINE_WORK_ORDER_LINK);
		db.execSQL(SQL_DELETE_WORK_ORDERS);
		db.execSQL(SQL_CREATE_WORK_ORDERS);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public int getProductType(int type){
		SQLiteDatabase db = getReadableDatabase();
		Cursor resultCursor = db.query(
				PrimexDatabaseSchema.ProductTypes.TABLE_NAME,
				null, 
				PrimexDatabaseSchema.ProductTypes.COLUMN_NAME_TYPES + "=?",
				new String[] {String.valueOf(type)},
				null,
				null,
				null
				);
		
		int thetype = 0;
		try {
			if (resultCursor.moveToFirst()) {
		    	thetype= resultCursor.getInt(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductTypes.COLUMN_NAME_TYPES));
			}
	    	return thetype;
	    } finally {
	    	if (resultCursor != null) resultCursor.close();
		}
	}
	
	public int getProductTypeId(String type){
		SQLiteDatabase db = getReadableDatabase();
		
		String sql = "SELECT * FROM " + PrimexDatabaseSchema.ProductTypes.TABLE_NAME + " WHERE " +
				PrimexDatabaseSchema.ProductTypes.COLUMN_NAME_TYPES + "=?";
		Cursor resultCursor = db.rawQuery(sql, new String[]{type});
		
		int thetype = 0;
		try {
			if (resultCursor.moveToFirst()) {
		    	thetype= resultCursor.getInt(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductTypes._ID));
			} else Log.e("error", "you didn't match any darn rows");
	    	return thetype;
	    } finally {
	    	if (resultCursor != null) resultCursor.close();
		}
	}
	
	public String getFieldAsString(String tableName, String columnName, String[] whereArgs){
		SQLiteDatabase db = getReadableDatabase();
		
		String sql = "SELECT " + columnName + " FROM " + tableName;
		if (whereArgs != null) {
			sql += " WHERE " + columnName + "=?";
		}
		Cursor resultCursor = db.rawQuery(sql, whereArgs);
		
		String value = null;
		try {
			if (resultCursor.moveToFirst()) {
		    	int index = resultCursor.getColumnIndexOrThrow(columnName);
				value = resultCursor.getString(index);
			} else Log.e("error", "you didn't match any darn rows");
	    	return value;
	    } finally {
	    	if (resultCursor != null) resultCursor.close();
		}
	}
	
	public int getIdOfValue (String tableName, String columnName, Object value) {
		SQLiteDatabase db = getReadableDatabase();
		String sql = "SELECT _id" +
				" FROM " + tableName + 
				" WHERE " + columnName + 
				"=?";
		Cursor resultsCursor = db.rawQuery(sql, new String[]{String.valueOf(value)});
		int columnId = -1;
		try {
			if (resultsCursor.getCount() > 1) {
				Log.e("ERROR", "shouldn't get more than one result");
			}
			if (resultsCursor.moveToFirst()){
				columnId = resultsCursor.getInt(0);
			}
			return columnId;
		} finally {
			if (resultsCursor != null) {
				resultsCursor.close();
			}
		}		
	}
	
	
	
	public long insertOrReplaceProduct(Product newProduct, int woNumber) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(PrimexDatabaseSchema.Products.COLUMN_NAME_GAUGE, newProduct.getGauge());
		values.put(PrimexDatabaseSchema.Products.COLUMN_NAME_WIDTH, newProduct.getWidth());
		values.put(PrimexDatabaseSchema.Products.COLUMN_NAME_LENGTH, newProduct.getLength());
		String type = newProduct.getType();
		int foreignKey = getProductTypeId(type);
		values.put(PrimexDatabaseSchema.Products.COLUMN_NAME_TYPE, foreignKey);
		int otherForeign = woNumber;
		values.put(PrimexDatabaseSchema.Products.COLUMN_NAME_WO_NUMBER, otherForeign);
		
		long rowId = db.insertWithOnConflict(
				PrimexDatabaseSchema.Products.TABLE_NAME, 
				null, 
				values,
				SQLiteDatabase.CONFLICT_REPLACE);
		
		if (rowId == -1) {
			Log.v("verbose", "insert error code -1");
		} else {
			Log.v("verbose", "insert row ID " + String.valueOf(rowId));
		}
		return rowId;
	}
	
	public Product getProduct(int woNumber) {
		SQLiteDatabase db = getReadableDatabase();
		
		String sql = "SELECT * FROM " +
				PrimexDatabaseSchema.Products.TABLE_NAME + 
				" JOIN " + PrimexDatabaseSchema.ProductTypes.TABLE_NAME + 
				" ON " + PrimexDatabaseSchema.ProductTypes.TABLE_NAME + "." + PrimexDatabaseSchema.ProductTypes._ID +  
				"=" + PrimexDatabaseSchema.Products.TABLE_NAME + "." + PrimexDatabaseSchema.Products.COLUMN_NAME_TYPE + 
				" JOIN " + PrimexDatabaseSchema.WorkOrders.TABLE_NAME + 
				" ON " + PrimexDatabaseSchema.WorkOrders.TABLE_NAME + "." + PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER +
				"=" + woNumber;
		//String[] whereargs = new String[]{String.valueOf(lineNumber)};
		Cursor resultCursor = db.rawQuery(sql, null);

		Product p = null;
		
		try {
			if (resultCursor.getCount() > 1) {
				Log.e("ERROR","You are not looking up a unique record for wo number " + String.valueOf(woNumber) +
						"and are going to get errors");
			}
			if (resultCursor.moveToFirst()) {
				int indexOfProductType = resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.ProductTypes.COLUMN_NAME_TYPES);
				String type = resultCursor.getString(indexOfProductType);
				if (type.equals(Product.SHEETS_TYPE)) {
					p = new Sheet(
						resultCursor.getDouble(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.Products.COLUMN_NAME_GAUGE)),
						resultCursor.getDouble(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.Products.COLUMN_NAME_WIDTH)),
						resultCursor.getDouble(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.Products.COLUMN_NAME_LENGTH))
						);
				} else if (type.equals(Product.ROLLS_TYPE)) {
					p = new Roll(
 							resultCursor.getDouble(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.Products.COLUMN_NAME_GAUGE)),
							resultCursor.getDouble(resultCursor.getColumnIndexOrThrow(PrimexDatabaseSchema.Products.COLUMN_NAME_WIDTH)),
							0
						);					
				} else throw new IllegalArgumentException("not a sheet or roll!");
			} else {
				Log.e("error", "getProduct database query returned no results");
			}
		} finally {
			if (resultCursor != null) {resultCursor.close();}
		}	
		return p;
	}
	
	public int updateColumn(String tableName, String columnName, String where, String[] whereArgs, String newValue){
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();

		values.put(columnName, newValue);
		
		int numAffectedRows = db.update(
				tableName,
				values, 
				where,
				whereArgs
				);
				
		return numAffectedRows;	    
	}
	
	public long updateLineWorkOrderLink(int lineNumber, int woNumber) {
		SQLiteDatabase db = getWritableDatabase();
		int lineId = getIdOfValue(
				PrimexDatabaseSchema.ProductionLines.TABLE_NAME, 
				PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LINE_NUMBER, 
				lineNumber);
		int woId = getIdOfValue(
				PrimexDatabaseSchema.WorkOrders.TABLE_NAME, 
				PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER,
				woNumber);
		ContentValues values = new ContentValues();
		values.put (PrimexDatabaseSchema.LineWorkOrderLink.COLUMN_NAME_LINE_ID, lineId);
		values.put (PrimexDatabaseSchema.LineWorkOrderLink.COLUMN_NAME_WO_ID, woId);
		long rowId = db.replace(PrimexDatabaseSchema.LineWorkOrderLink.TABLE_NAME, null, values);
		return rowId;	
	}
	
	/*
	 * Returns 0 if work order not found.
	 */
	public int getWoNumberByLine(int lineNumber) {
		SQLiteDatabase db = getReadableDatabase();
		String sql = "SELECT " + PrimexDatabaseSchema.WorkOrders.TABLE_NAME + "." + PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER + 
				" FROM " + PrimexDatabaseSchema.WorkOrders.TABLE_NAME +  
				" JOIN " + PrimexDatabaseSchema.LineWorkOrderLink.TABLE_NAME + 
				" ON " + PrimexDatabaseSchema.WorkOrders.TABLE_NAME + "." + PrimexDatabaseSchema.WorkOrders._ID + 
				"=" + PrimexDatabaseSchema.LineWorkOrderLink.TABLE_NAME + "." + PrimexDatabaseSchema.LineWorkOrderLink.COLUMN_NAME_WO_ID +
				" JOIN " + PrimexDatabaseSchema.ProductionLines.TABLE_NAME +
				" ON " + PrimexDatabaseSchema.ProductionLines.TABLE_NAME + "." + PrimexDatabaseSchema.ProductionLines._ID + 
				"=" + PrimexDatabaseSchema.LineWorkOrderLink.TABLE_NAME + "." + PrimexDatabaseSchema.LineWorkOrderLink.COLUMN_NAME_LINE_ID +
				" AND " + PrimexDatabaseSchema.ProductionLines.TABLE_NAME + "." + PrimexDatabaseSchema.ProductionLines.COLUMN_NAME_LINE_NUMBER + 
				"=" + lineNumber;
		
		Cursor resultCursor = db.rawQuery(sql, null);
		if (resultCursor.getCount() > 1) {
			Log.e("ERROR", "More than one work order for this query, you will get errors");
		}
		int woNumber = 0;
		try {
			if (resultCursor.moveToFirst()) {
				int columnIndex = resultCursor.getColumnIndex(PrimexDatabaseSchema.WorkOrders.COLUMN_NAME_WO_NUMBER);
				woNumber = resultCursor.getInt(columnIndex);
			}
			return woNumber;
		} finally {
			if (resultCursor != null) {
				resultCursor.close();
			}
		}
	}
}
