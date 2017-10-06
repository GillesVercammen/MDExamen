package com.example.gilles.voorbeeldexamen;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;

public class SaverActivity extends AppCompatActivity {

    private static final String LOCATION = "LOCATION";
    private static final String BESCHRIJVING = "BESCHRIJVING";

    private TextView mTextView;
    private EditText beschrijving;
    private Button saveButton;
    private MySqlLiteHelper helper;

    final String TABLE_NAME = "locations";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context currentContext = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saver);

        mTextView = (TextView) findViewById(R.id.textView3);
        beschrijving = (EditText) findViewById(R.id.editText3);
        saveButton = (Button) findViewById(R.id.button3);

        helper = new MySqlLiteHelper(this);

        saveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                try{
                    String mBeschrijving = beschrijving.getText().toString();
                    if(!mBeschrijving.isEmpty()) {
                        final GeoPoint location = (GeoPoint) getIntent().getSerializableExtra("LOCATION");
                        Double lat = location.getLatitude();
                        Double lon = location.getLongitude();

                        helper.addLocation(lat, lon, mBeschrijving);
                        Toast.makeText(getApplicationContext(),
                                "Locatie opgeslagen",
                                Toast.LENGTH_SHORT)
                                .show();
                        Intent mainScreenIntent = new Intent(currentContext, MainActivity.class);
                        mainScreenIntent.putExtra(LOCATION, (Serializable) (GeoPoint) getIntent().getSerializableExtra("LOCATION"));
                        mainScreenIntent.putExtra(BESCHRIJVING, beschrijving.getText().toString());
                        startActivity(mainScreenIntent);
                    }else {
                        Toast.makeText(getApplicationContext(),
                                "Vul aub een beschrijving in",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                catch(Exception ex){
                    Log.e("SQLITE", ex.getMessage());
                }


            }
        });

    }

}
