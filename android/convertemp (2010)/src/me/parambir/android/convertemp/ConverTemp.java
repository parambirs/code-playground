package me.parambir.android.convertemp;

import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class ConverTemp extends Activity {
    /** Called when the activity is first created. */
	private EditText c;
	private EditText f;
	private EditText k;
	
	private double celsius = 37;
	private double fahrenheit = 98.6;
	private double kelvin = celsius + 273.15;
	
	private DecimalFormat df = new DecimalFormat("#.###");
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        c = (EditText) findViewById(R.id.celsius);
        f = (EditText) findViewById(R.id.fahrenheit);
        k = (EditText) findViewById(R.id.kelvin);
     
        c.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void onTextChanged(CharSequence seq, int arg1, int arg2, int arg3)
				{
					try
					{
						String celsiusString = seq.toString().trim();
						if(celsiusString.equals(""))
						{
							clearEditText(f);
							clearEditText(k);
							return;
						}
						double newCelsius = Double.parseDouble(celsiusString);
						if(isSignificantDifference(newCelsius, celsius))
						{
							celsius = newCelsius;
							double fahrenheit = (9.0 * celsius) / 5.0  + 32.0;
							double kelvin = celsius + 273.15;
							if(isSignificantDifference(fahrenheit, ConverTemp.this.fahrenheit))
							{
								setEditText(f, fahrenheit);
							}
							if(isSignificantDifference(kelvin, ConverTemp.this.kelvin))
							{
								setEditText(k, kelvin);
							}
						}
						
					}
					catch(Exception e)
					{
						clearEditText(f);
						clearEditText(k);
					}
				}
				
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3){}
				
				@Override
				public void afterTextChanged(Editable arg0){}
			});
        
        f.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					try
					{
						String fahrenheitString = s.toString().trim();
						if(fahrenheitString.equals(""))
						{
							clearEditText(c);
							clearEditText(k);
							return;
						}
						double newFahrenheit = Double.parseDouble(fahrenheitString);
						if(isSignificantDifference(newFahrenheit, fahrenheit))
						{
							fahrenheit = newFahrenheit;
							double celsius = 5.0 * (fahrenheit - 32.0) / 9.0;
							double kelvin = celsius + 273.15;
							
							if(isSignificantDifference(celsius, ConverTemp.this.celsius))
							{
								setEditText(c, celsius);
							}
							
							if(isSignificantDifference(kelvin, ConverTemp.this.kelvin))
							{
								setEditText(k, kelvin);
							}
						}
						
					}
					catch(Exception e)
					{
						clearEditText(c);
						clearEditText(k);
					}
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				
				@Override
				public void afterTextChanged(Editable s) {}
			});
        
        k.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				try
				{
					String kelvinString = s.toString().trim();
					if(kelvinString.equals(""))
					{
						clearEditText(c);
						clearEditText(f);
						return;
					}
					double newKelvin = Double.parseDouble(kelvinString);
					if(isSignificantDifference(newKelvin, kelvin))
					{
						kelvin = newKelvin;
						double celsius = kelvin - 273.15;
						double fahrenheit = (9.0 * celsius) / 5.0  + 32.0;
						
						if(isSignificantDifference(celsius, ConverTemp.this.celsius))
						{
							setEditText(c, celsius);
						}
						
						if(isSignificantDifference(fahrenheit, ConverTemp.this.fahrenheit))
						{
							setEditText(f, fahrenheit);
						}
					}
					
				}
				catch(Exception e)
				{
					clearEditText(c);
					clearEditText(f);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
    }
    
    private boolean isSignificantDifference(double v1, double v2)
    {
    	return Math.abs(v1 - v2) > 0.000001;
    }
    
    private void setEditText(EditText e, double d)
    {
    	if(!e.isFocused())
    	{
    		e.setText(df.format(d));
    	}
    	
    }
    
    private void clearEditText(EditText e)
    {
    	if(!e.getText().toString().trim().equals(""))
    	{
    		if(!e.isFocused())
    		{
    			e.setText("");
    		}
    	}
    }
}