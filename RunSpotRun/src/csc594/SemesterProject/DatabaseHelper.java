package csc594.SemesterProject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;

public class DatabaseHelper extends SQLiteOpenHelper 
{
	private static final String DATABASE_NAME="geobase";
	SimpleDateFormat fmt1 = new SimpleDateFormat("MM/dd/yyyy");
	SimpleDateFormat fmt2 = new SimpleDateFormat("hh:mm:ss a");
	Context c;
	
	public DatabaseHelper(Context context)
	{
		super(context, DATABASE_NAME, null, 1);
		c = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		try
		{
			db.execSQL("CREATE TABLE Route (_id INTEGER PRIMARY KEY AUTOINCREMENT, Name TEXT, Date TEXT, StartTime TEXT, EndTime TEXT, Distance TEXT);");
			db.execSQL("CREATE TABLE Point (_id INTEGER PRIMARY KEY AUTOINCREMENT, RouteID INTEGER, Latitude INTEGER, Longitude INTEGER, Time TEXT, Distance TEXT);");
		}
		catch (Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
	
	public static void ForceCreate(SQLiteDatabase db)
	{
		try
		{
			db.execSQL("CREATE TABLE Route (_id INTEGER PRIMARY KEY AUTOINCREMENT, Name TEXT, Date TEXT, StartTime TEXT, EndTime TEXT, Distance TEXT);");
            db.execSQL("CREATE TABLE Point (_id INTEGER PRIMARY KEY AUTOINCREMENT, RouteID INTEGER, Latitude INTEGER, Longitude INTEGER, Time TEXT, Distance TEXT);");
            System.out.print("onCreate Force DB");
		}
		catch (Exception ex)
		{
			//System.out.print("onCreate Force DB error ");
			System.out.println(ex.toString());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// TO DO Auto-generated method stub
	}
	
	ArrayList<RouteItem> GetRoutes()
	{
		//returns all routes
		
		ArrayList<RouteItem> routes = new ArrayList<RouteItem>();
		
		String SQL = "SELECT * FROM Route ORDER BY Date, StartTime ASC";

        Cursor cur = this.getReadableDatabase().rawQuery(SQL, null);
        
        if (cur.moveToFirst())
        {        
	        while (!cur.isAfterLast())
	        {	        	
	        	routes.add(SetRouteFromCursor(cur));
	        	
	        	cur.moveToNext();
	        }
        }	
        cur.close();
                
        Collections.sort(routes,new Comparator<RouteItem>() {
            @Override
			public int compare(RouteItem arg0, RouteItem arg1) {
            	Date date0, date1, time0, time1;
            	try 
            	{
            		date0 = fmt1.parse(arg0.getDate());
            		date1 = fmt1.parse(arg1.getDate());
            		
            		int dateCompare = date0.compareTo(date1);
            		
            		//check if the same day and return if different days
            		if(dateCompare != 0){ return dateCompare * -1;}//-1 so list is desc order
            		
            		//same day, now check times
            		
            		time0 = fmt2.parse(arg0.getTime());
            		time1 = fmt2.parse(arg1.getTime());
					return time0.compareTo(time1) * -1;
				} catch (ParseException e) 
				{
					return 0;
				}
			}
        });
            	
    	return routes;
	}

	RouteItem GetRoute(int routeKey)
	{
		//return a single route for given route key	
		
		String SQL = "SELECT * FROM Route WHERE _id = ?";
        Cursor cur = this.getReadableDatabase().rawQuery(SQL, new String[] { Integer.toString(routeKey) });
        
        if (cur.moveToFirst())
        {
        	//cur.close(); //don't close here since it is being passed..
        	//return SetRouteFromCursor(cur); //still error, was never getting close from this method
        	RouteItem route = SetRouteFromCursor(cur);
        	cur.close(); 
        	return route;
        }
        else
        {
        	cur.close();
        	return new RouteItem();
        }
    }	
	
	String GetRouteDuration(int routeKey)
	{
		//return the duration of the route for given route key			
		String SQL = "SELECT * FROM Route WHERE _id = ?";
		Cursor cur = this.getReadableDatabase().rawQuery(SQL, new String[] { Integer.toString(routeKey) });
		        
		if (cur.moveToFirst())
		{
			String startTime = cur.getString(cur.getColumnIndex("StartTime"));
			String endTime = cur.getString(cur.getColumnIndex("EndTime"));
			
			String duration = "undefined";
			
			try
			{
				duration = CalculateDuration(startTime, endTime);
			}
			catch (Exception ex)
			{
				
			}
			
		 	cur.close(); 
		 	return duration;
		}
		else
		{
			cur.close();
			return "0";
		}
	}

	ArrayList<MyGeoPoint> GetPoints(int routeKey)
	{
		//return all points for given route key
		//<first>start, normal, <last>stop
		
		ArrayList<MyGeoPoint> points = new ArrayList<MyGeoPoint>();
		
		String SQL = "SELECT * FROM Point WHERE RouteID = ?";
        Cursor cur = this.getReadableDatabase().rawQuery(SQL, new String[] { Integer.toString(routeKey) });
        int count = 0;
        
        if (cur.moveToFirst())
        {        
        	boolean cont;
        	do
	        {	        	
	        	points.add(SetPointFromCursor(cur, "Point" + Integer.toString(count)));
	        	
	        	cont = cur.moveToNext();
	        	count++;
	        }while(cont);
        }
        
        cur.close();
		return points;
	}

	long AddPoint(MyGeoPoint point, int routeKey)
	{
		String distFromLastPt = point.getDistance();
		String distFromStart = UpdateRoute(distFromLastPt, point.getTime(), routeKey);
		
		//adds a point for given route key
		ContentValues cv = new ContentValues();
		
		cv.put("RouteID", routeKey);
		cv.put("Latitude", point.getPoint().getLatitudeE6());
		cv.put("Longitude", point.getPoint().getLongitudeE6());
		cv.put("Time", point.getTime());
		cv.put("Distance", distFromStart);
		
		return this.getWritableDatabase().insert("Point", "RouteID", cv);
	}

	long AddRoute(RouteItem route)
	{
		//adds a new route, returns the key
		
		ContentValues cv = new ContentValues();
		
		cv.put("Name", route.getName());
		cv.put("Date", route.getDate());
		cv.put("StartTime", route.getTime());
		cv.putNull("EndTime");
		cv.put("Distance", 0);
		return this.getWritableDatabase().insert("Route", "Name", cv);
	}
	
	int DeleteRoute(int routeKey)
	{
		//deletes a single route for given route key
		
		return this.getWritableDatabase().delete("Route", "_id = ?", new String[] { Integer.toString(routeKey) });
	}
	
	
	private String UpdateRoute(String newDistance, String endTime, int routeKey)
	{
		RouteItem route = GetRoute(routeKey);
		double currentDistance = route.getDistance();
		
		double intermedDist = Double.parseDouble(newDistance) + currentDistance;
				
		ContentValues cv = new ContentValues();
	    cv.put("Distance", intermedDist);
	    cv.put("EndTime", endTime);
	    
	    this.getWritableDatabase().update("Route", cv, "_id = ?", new String[] { Integer.toString(routeKey) });
	    
	    //return intermedDist+""; 
	    return String.format("%.2f", intermedDist); 
	}
	
	private RouteItem SetRouteFromCursor(Cursor cur)
	{
		RouteItem route = new RouteItem();
		
		route.setKey((int)cur.getLong(cur.getColumnIndex("_id")));
    	route.setName(cur.getString(cur.getColumnIndex("Name")));
    	route.setDate(cur.getString(cur.getColumnIndex("Date")));
    	route.setTime(cur.getString(cur.getColumnIndex("StartTime")));
    	route.setDistance(Double.parseDouble(cur.getString(cur.getColumnIndex("Distance")))); 
    	
    	//cur.close(); //not close here since this same cursor is continue to be used in GetRoutes()
    	return route;
	}
	
	private MyGeoPoint SetPointFromCursor(Cursor cur, String name)
	{
		int latitude = (int)cur.getLong(cur.getColumnIndex("Latitude"));
		int longitude = (int)cur.getLong(cur.getColumnIndex("Longitude"));
		String time = cur.getString(cur.getColumnIndex("Time"));
		String distance = cur.getString(cur.getColumnIndex("Distance"));
		
		MyGeoPoint point;
				
		if(cur.isFirst())
		{
			point = new MyGeoPoint(latitude, longitude, time, distance, name, MyGeoPoint.MyPointType.Start);
		}
		else if(cur.isLast())
		{
			point = new MyGeoPoint(latitude, longitude, time, distance, name, MyGeoPoint.MyPointType.Stop);
		}
		else
		{
			point = new MyGeoPoint(latitude, longitude, time, distance, name, MyGeoPoint.MyPointType.Normal);
		}
    	
    	return point;
	}
	
	private String CalculateDuration(String start, String end) throws Exception
	{		
		try
		{
			Date startDate = fmt2.parse(start);
			Date endDate = fmt2.parse(end);
		
			long s = (endDate.getTime() - startDate.getTime()) / 1000;
			return String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
		}	
		catch (Exception ex)
		{
			throw ex;
		}
	}
}
