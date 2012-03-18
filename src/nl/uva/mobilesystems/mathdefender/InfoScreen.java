package nl.uva.mobilesystems.mathdefender;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;


public class InfoScreen extends Activity implements OnClickListener {

	private String mode;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infoscreen);
		
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			mode = extras.getString("mode");
		}
		
		TableLayout tableLayout = (TableLayout)findViewById(R.id.tableLayout);
		TextView infoScreenTitle = (TextView)findViewById(R.id.infoScreenTitle); 
		TextView infoTextView = (TextView)findViewById(R.id.infoTextView); 		

		if(mode.equals("supermarket")){
			infoScreenTitle.setText(R.string.supermarket);
			infoTextView.setText(R.string.supermarket_instructions);
			tableLayout.setBackgroundResource(R.drawable.background_supermarket);
		}
		else if(mode.equals("zen")){
			infoScreenTitle.setText(R.string.zen);
			infoTextView.setText(R.string.zen_instructions);			
			tableLayout.setBackgroundResource(R.drawable.background_hsc);
		}
		
		Button button;
		button = (Button)findViewById(R.id.button_continue);
		button.setOnClickListener(this);

	}

	public void onClick(View v) {
		Intent intent;
		if(v == (Button)findViewById(R.id.button_continue)) {

			intent = new Intent(this, InitialActivity.class);
			intent.putExtra("mode",mode);
			
			startActivity(intent);
		}
		
	}
}