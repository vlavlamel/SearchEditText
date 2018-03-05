package com.example.searchableedittext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SearchEditText.OnSearchTextListener {

	TextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SearchEditText searchEditText = findViewById(R.id.search);
		textView = findViewById(R.id.result);
		searchEditText.setOnSearchTextListener(this);
	}

	@Override
	public void onSearchTextSubmit(String s) {
		Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT)
			.show();
		//textView.setText(s);
	}

	@Override
	public void onSearchTextChange(String s) {
		textView.setText(s);
	}
}
