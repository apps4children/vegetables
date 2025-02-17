package com.iitk.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.iitk.database.Student;
import com.iitk.database.TestAdapter;
import com.iitk.vegetables.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

public class DisplayRecords extends Activity{

	FileWriter writer;

	ArrayList<Student> student = null;
	ArrayList<String> studentByGroup = null;
	ArrayList<Student> studentByName = null;
	ArrayList<Student> studentDataCsv = null;

	TestAdapter mDbHelper;
	ListView listView;
	CustomAdapter customAdapter;
	Animation animAlpha;
	ImageView saveRecord, exit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.displayreport);
		mDbHelper = new TestAdapter(this);
		displayRecords();
		customAdapter = new CustomAdapter(student, this);

		listView = (ListView) findViewById(R.id.lvDisplayReport);
		listView.setAdapter(customAdapter);
		// click animation
		animAlpha = AnimationUtils.loadAnimation(this, R.anim.animbump);

		saveRecord = (ImageView) findViewById(R.id.bSaveFromDispReport);
		exit = (ImageView) findViewById(R.id.bBackToHomeFromDispReport);
		saveRecord.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				alert();
			}
		});

		exit.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void onClick(View v) {
		ImageView button = (ImageView) v;
		Student row = (Student) button.getTag();
		mDbHelper.createDatabase();
		mDbHelper.open();
		mDbHelper.deleteRecord(row.getId());
		mDbHelper.close();
		customAdapter.deleteRow(row);
		customAdapter.notifyDataSetChanged();
	}

	public void displayRecords() {
		student = new ArrayList<Student>();
		mDbHelper.createDatabase();
		mDbHelper.open();
		student = mDbHelper.browseAllStudent();
		mDbHelper.close();
	}

	// generate csv file
	public void createCSV(ArrayList<Student> studentdata, String name) {
		studentDataCsv = new ArrayList<Student>();
		studentDataCsv = studentdata;
		String[] headers = new String[] { "Name", "Date", "Name to vegetable",
				"Vegetable to name", "Vegetable to vegetable", "Total Mistakes" };
		try {
			String path = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/AppsReport/Vegetables";
			File dir = new File(path);
			if (!dir.exists())
				dir.mkdirs();

			File file = new File(dir, name + ".csv");
			if (file.exists())
				file.delete();
			writer = new FileWriter(file);

			writeCsvHeader(headers[0], headers[1], headers[2], headers[3],
					headers[4], headers[5]);

			for (int i = 0; i < studentDataCsv.size(); i++) {
				int totalMistake = 0;
				StringBuilder level1 = new StringBuilder();
				StringBuilder level2 = new StringBuilder();
				StringBuilder level3 = new StringBuilder();
				
				StringBuilder mistake = new StringBuilder();
				Student st = studentDataCsv.get(i);

				if (!(st.getLevel1().equalsIgnoreCase("No mistakes")
						|| st.getLevel1() == null || st.getLevel1().equals(""))) {
					totalMistake = totalMistake + countMistake(st.getLevel1());

					level1.append("\"");
					level1.append(st.getLevel1());
					level1.append("\"");
				}

				if (!(st.getLevel2().equalsIgnoreCase("No mistakes")
						|| st.getLevel2() == null || st.getLevel2().equals(""))) {
					totalMistake = totalMistake + countMistake(st.getLevel2());

					level2.append("\"");
					level2.append(st.getLevel2());
					level2.append("\"");
				}

				if (!(st.getLevel3().equalsIgnoreCase("No mistakes")
						|| st.getLevel3() == null || st.getLevel3().equals(""))) {
					totalMistake = totalMistake + countMistake(st.getLevel3());

					level3.append("\"");
					level3.append(st.getLevel3());
					level3.append("\"");
				}
				mistake.append(String.valueOf(totalMistake));
				mistake.append("\n");// adding new line after record

				writeCsvData(st.getName(), st.getDate(), level1.toString(),
						level2.toString(), level3.toString(),mistake.toString());
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// end of method create

	private void writeCsvHeader(String h1, String h2, String h3, String h4,
			String h5, String h6)
			throws IOException {
		String line = String.format("%s,%s,%s,%s,%s,%s\n", h1, h2, h3,
				h4, h5, h6);
		writer.write(line);
	}

	private void writeCsvData(String name, String date, String level1,
			String level2, String level3, String totalMistake) throws IOException {
		String line = String.format("%s,%s,%s,%s,%s,%s",
				name.toUpperCase(), date, level1, level2, level3,totalMistake);
		writer.write(line);
	}

	public int countMistake(String str) {
		int mistakes = 0;
	
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ',')
				mistakes = mistakes + 1;
		}
		return mistakes;

	}

	// alert dialog
	public void alert() {
		studentByGroup = new ArrayList<String>();
		mDbHelper.createDatabase();
		mDbHelper.open();
		studentByGroup = mDbHelper.browseStudentByGroup();
		mDbHelper.close();

		final String[] items = studentByGroup.toArray(new String[studentByGroup
				.size()]);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Student Name");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				String name = items[item];
				if (name.equalsIgnoreCase("All")) {
					createCSV(student, name);

					sdCardPath(name);// showing alert for path
				} else {
					studentByName = new ArrayList<Student>();
					mDbHelper.createDatabase();
					mDbHelper.open();
					studentByName = mDbHelper.browseStudentByName(name);
					mDbHelper.close();
					createCSV(studentByName, name);

					sdCardPath(name);// showing alert for path
				}
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void sdCardPath(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("File Path : sdcard/AppsReport/Vegetables \n");
		sb.append("File Name : " + name + ".csv");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Saved File Location");
		builder.setMessage(sb.toString()).setCancelable(false)
				.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
