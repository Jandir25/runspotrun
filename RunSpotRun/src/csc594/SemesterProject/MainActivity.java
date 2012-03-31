package csc594.SemesterProject;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import csc594.SemesterProject.MyGeoPoint.MyPointType;

public class MainActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
    }
    
    public void doGoToMap(View view)
    {    	
    	Intent launchMap = new Intent(this, RunMapActivity.class);
		
    	//Debug test hardcoded route
    	ArrayList<MyGeoPoint> route = new ArrayList<MyGeoPoint>();
		route.add(new MyGeoPoint(39312718,	-84281230, MyPointType.Start));
		route.add(new MyGeoPoint(39313623,	-84282732));
		route.add(new MyGeoPoint(39313847,	-84283859));
		route.add(new MyGeoPoint(39314694,	-84284137));
		route.add(new MyGeoPoint(39315831,	-84281509));
		route.add(new MyGeoPoint(39318919,	-84279041));
		route.add(new MyGeoPoint(39321500,	-84273033));
		route.add(new MyGeoPoint(39319724,	-84271874));
		route.add(new MyGeoPoint(39314188,	-84277571));
		route.add(new MyGeoPoint(39312594,	-84280490, MyPointType.Stop));
		
    	launchMap.putExtra("Route", route);
		startActivity(launchMap);
    }
    
	@Override
	protected void onActivityResult(int requestCode,
							int resultCode, Intent data) 
	{
		if (resultCode == RESULT_OK) 
		{
			//do nothing
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
