package com.example.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText etName, etMark1, etMark2, etMark3, etMark4;
    Button btnAdd, btnUpdate, btnDelete, btnView;
    TextView tvResults;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etMark1 = findViewById(R.id.etMark1);
        etMark2 = findViewById(R.id.etMark2);
        etMark3 = findViewById(R.id.etMark3);
        etMark4 = findViewById(R.id.etMark4);
        btnAdd = findViewById(R.id.btnAdd);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnView = findViewById(R.id.btnView);
        tvResults = findViewById(R.id.tvResults);

        // Create or Open SQLite Database
        db = openOrCreateDatabase("StudentDB", MODE_PRIVATE, null);
        // Create the table with correct column types
        db.execSQL("CREATE TABLE IF NOT EXISTS students(name TEXT, mark1 INTEGER, mark2 INTEGER, mark3 INTEGER, mark4 INTEGER);");

        // Add Student
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                if (!name.isEmpty() && validateMarks()) {
                    int mark1 = Integer.parseInt(etMark1.getText().toString());
                    int mark2 = Integer.parseInt(etMark2.getText().toString());
                    int mark3 = Integer.parseInt(etMark3.getText().toString());
                    int mark4 = Integer.parseInt(etMark4.getText().toString());

                    try {
                        db.execSQL("INSERT INTO students (name, mark1, mark2, mark3, mark4) VALUES (?, ?, ?, ?, ?);",
                                new Object[]{name, mark1, mark2, mark3, mark4});
                        Toast.makeText(MainActivity.this, "Student Added", Toast.LENGTH_SHORT).show();
                        clearFields();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Enter valid data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Update Student
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                if (!name.isEmpty() && validateMarks()) {
                    int mark1 = Integer.parseInt(etMark1.getText().toString());
                    int mark2 = Integer.parseInt(etMark2.getText().toString());
                    int mark3 = Integer.parseInt(etMark3.getText().toString());
                    int mark4 = Integer.parseInt(etMark4.getText().toString());

                    // Check if student exists before updating
                    Cursor cursor = db.rawQuery("SELECT * FROM students WHERE name = ? COLLATE NOCASE", new String[]{name});
                    if (cursor.getCount() > 0) {
                        cursor.close();

                        // Use ContentValues to update the record and get the affected rows
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("mark1", mark1);
                        contentValues.put("mark2", mark2);
                        contentValues.put("mark3", mark3);
                        contentValues.put("mark4", mark4);
                        int rowsAffected = db.update("students", contentValues, "name = ? COLLATE NOCASE", new String[]{name});

                        if (rowsAffected > 0) {
                            Toast.makeText(MainActivity.this, "Student Updated", Toast.LENGTH_SHORT).show();
                            clearFields();
                        } else {
                            Toast.makeText(MainActivity.this, "Error: Student not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cursor.close();
                        Toast.makeText(MainActivity.this, "Student not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Enter valid data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Delete Student
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                if (!name.isEmpty()) {
                    try {
                        int rowsAffected = db.delete("students", "name = ? COLLATE NOCASE", new String[]{name});
                        if (rowsAffected > 0) {
                            Toast.makeText(MainActivity.this, "Student Deleted", Toast.LENGTH_SHORT).show();
                            clearFields();
                        } else {
                            Toast.makeText(MainActivity.this, "Error: Student not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Enter the student name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // View Students and Calculate Sum and Average
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = db.rawQuery("SELECT * FROM students", null);
                StringBuilder result = new StringBuilder();

                // Ensure the cursor has data
                if (cursor.getCount() == 0) {
                    tvResults.setText("No students found in the database.");
                    cursor.close();
                    return;
                }

                // Iterate through the records
                while (cursor.moveToNext()) {
                    String name = cursor.getString(0);
                    int mark1 = cursor.getInt(1);
                    int mark2 = cursor.getInt(2);
                    int mark3 = cursor.getInt(3);
                    int mark4 = cursor.getInt(4);

                    // Calculate total and average marks for each student
                    int totalMarks = mark1 + mark2 + mark3 + mark4;
                    double averageMarks = totalMarks / 4.0;

                    // Append student data with total and average
                    result.append("Name: ").append(name).append("\n")
                            .append("Marks: ").append(mark1).append(", ").append(mark2)
                            .append(", ").append(mark3).append(", ").append(mark4).append("\n")
                            .append("Total Marks: ").append(totalMarks).append("\n")
                            .append("Average Marks: ").append(String.format("%.2f", averageMarks)).append("\n")
                            .append("--------------------------------\n");
                }

                // Update the TextView with the results
                tvResults.setText(result.toString());
                cursor.close();
            }
        });
    }

    private boolean validateMarks() {
        return !etMark1.getText().toString().isEmpty() &&
                !etMark2.getText().toString().isEmpty() &&
                !etMark3.getText().toString().isEmpty() &&
                !etMark4.getText().toString().isEmpty();
    }

    private void clearFields() {
        etName.setText("");
        etMark1.setText("");
        etMark2.setText("");
        etMark3.setText("");
        etMark4.setText("");
    }
}
