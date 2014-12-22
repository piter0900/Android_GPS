package ca.uwaterloo.Lab3_204_18;

import ca.uwaterloo.sensortoy.LineGraphView;

public class MainActivity extends Activity implements OnClickListener {
	
	Date last;
	SensorManager sensorManager;
	TextView steps;
	TextView northSteps;
	TextView eastSteps;
	LineGraphView graph;
	int currentDirection;
	int count;
	double countNorth;
	double countEast;
	TextView direction;
	float[] acValues = new float[3];  
	float[] magValues = new float[3];  

	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        LinearLayout l = (LinearLayout)findViewById(R.id.Layout);
        l.setOrientation(LinearLayout.VERTICAL);
        
        //set title
        TextView tv1 = (TextView) findViewById(R.id.label1);
        tv1.setText("Simple Pedometer");
        
        // Set buttons
        Button button1 = (Button) findViewById(R.id.button1);    
        button1.setOnClickListener(this);
        Button button2 = (Button) findViewById(R.id.button2); 
        button2.setOnClickListener(this);

        //add graph
        graph = new LineGraphView(getApplicationContext(),100,
        		Arrays.asList("x", "y", "z"));
        l.addView(graph);
        graph.setVisibility(View.VISIBLE);
       
        //get data from linear acceleration sensor
       
        TextView tv3 = new TextView(getApplicationContext());
        tv3.setText("The Number of Steps are:");
        l.addView(tv3);
        count = 0; 
        steps = new TextView(getApplicationContext());
        l.addView(steps);
        
        TextView tv5 = new TextView(getApplicationContext());
        tv5.setText("North:");
        l.addView(tv5);
        countNorth = 0; 
        northSteps = new TextView(getApplicationContext());
        l.addView(northSteps);
        
        TextView tv4 = new TextView(getApplicationContext());
        tv4.setText("East:");
        l.addView(tv4);
        countEast = 0; 
        eastSteps = new TextView(getApplicationContext());
        l.addView(eastSteps);
        
        direction = new TextView(getApplicationContext());
        l.addView(direction);
	}
        

    public void onClick(View view) { 
        switch (view.getId()) { 
        case R.id.button1: 
        	sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); 
            Sensor accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(Listener, accelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
            Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(Listener, magneticSensor,SensorManager.SENSOR_DELAY_FASTEST);
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(Listener, accelerometerSensor,SensorManager.SENSOR_DELAY_FASTEST);
        break; 
        
        case R.id.button2: 
        	count = 0;
        	countNorth = 0;
        	countEast = 0;
        break; 

       } 
    }     

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    	
    final SensorEventListener Listener = new SensorEventListener() {
    	float smoothedAccelx = 0.0f;
		float smoothedAccely = 0.0f;
		float smoothedAccelz = 0.0f;
		float smoothedMagx = 0.0f;
		float smoothedMagy = 0.0f;
		float smoothedMagz = 0.0f;
		float smoothedAcx = 0.0f;
		float smoothedAcy = 0.0f;
		float smoothedAcz = 0.0f;
		boolean flag = true;
		float lastPoint;
		
    	public void onAccuracyChanged(Sensor s, int i) {}
    	public void onSensorChanged(SensorEvent se) {
    		if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
    			//use low pass to filter the noise
    			smoothedMagx = lowPass(se.values[0],smoothedMagx);
    			smoothedMagy = lowPass(se.values[1],smoothedMagy);
    			smoothedMagz = lowPass(se.values[2],smoothedMagz);
    			magValues[0]= smoothedMagx;
    			magValues[1]= smoothedMagy;
    			magValues[2]= smoothedMagz;
    			
    		}
    		if(se.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
    			//use low pass to filter the noise
    			smoothedAcx = lowPass(se.values[0],smoothedAcx);	
    			smoothedAcy = lowPass(se.values[1],smoothedAcy);
    			smoothedAcz = lowPass(se.values[2],smoothedAcz);
    			acValues[0]= smoothedAcx;
    			acValues[1]= smoothedAcy;
    			acValues[2]= smoothedAcz;
    		}
    		if(se.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
    			smoothedAccelx = lowPass(se.values[0],smoothedAccelx);	
    			smoothedAccely = lowPass(se.values[1],smoothedAccely);
    			smoothedAccelz = lowPass(se.values[2],smoothedAccelz);
    		    float array[] = {smoothedAccelx, smoothedAccely, smoothedAccelz};
    		    graph.addPoint(array);//add values to the graph
    		    
    		    //calculate orientation
    			float[] RotMatrix = new float[9];//must be [9] or greater length
    	        float[] dir = new float[3];//store the direction
    	        SensorManager.getRotationMatrix(RotMatrix, null, acValues, magValues);  
    	        SensorManager.getOrientation(RotMatrix, dir);
    	        String s = String.format("%f",dir[0]);
    	        direction.setText(s);

    	        //count steps
    		    if (flag) { 
    		        lastPoint = smoothedAccelz;
    		        last = new Date(System.currentTimeMillis());
    		    	flag = false; 
    		    } 

    		    if ((Math.abs(smoothedAccelz - lastPoint) > 0.9f)) { 
    		    	if(System.currentTimeMillis() - last.getTime() > 300){
    		    		lastPoint = smoothedAccelz;
        		    	
        		    	if(lastPoint< 0.0f){
        		    	   count++;
                     	   countNorth = countNorth + Math.cos(dir[0]);
        		    	   countEast = countEast + Math.sin(dir[0]);
        		    	}
        		    last.setTime(System.currentTimeMillis());
    		    	}	
    		    } 
    		    steps.setText(Integer.toString(count));
    		    northSteps.setText(Double.toString(countNorth));
    		    eastSteps.setText(Double.toString(countEast));
    		    
    		}
    	}
    };
    //method used to filter the raw data
    public float lowPass(float value, float smoothedvalue){
    	float alpha = 0.05f;
    	float result;
    	result = alpha * value + (1 - alpha) * smoothedvalue;
    	return result;
    }
}
	