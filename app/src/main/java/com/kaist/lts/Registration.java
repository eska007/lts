package com.kaist.lts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Registration extends AppCompatActivity {

    private EditText idEt;
    private EditText passwordEt;
    private EditText repasswordEt;
    private EditText nameEt;
    private EditText surnameEt;
    private Button manButton;
    private Button womanButton;
    private EditText emailEt;
    private EditText phoneEt;
    private EditText countryEt;
    private EditText addressEt;
    private Button registrationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        idEt = (EditText) findViewById(R.id.et_id);
        passwordEt = (EditText) findViewById(R.id.et_password);
        repasswordEt = (EditText) findViewById(R.id.et_repassword);
        nameEt = (EditText) findViewById(R.id.et_names);
        surnameEt = (EditText) findViewById(R.id.et_surname);
        manButton = (Button) findViewById(R.id.btn_man);
        womanButton = (Button) findViewById(R.id.btn_woman);
        emailEt = (EditText) findViewById(R.id.et_email);
        phoneEt = (EditText) findViewById(R.id.et_phone);
        countryEt = (EditText) findViewById(R.id.et_country);
        addressEt = (EditText) findViewById(R.id.et_address);
        registrationButton = (Button) findViewById(R.id.btn_registeration);

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idEt.getText().toString();
                String password = passwordEt.getText().toString();
                String repassord = repasswordEt.getText().toString();
                String name = nameEt.getText().toString();
                String surname = surnameEt.getText().toString();
                String phone = phoneEt.getText().toString();
                String email = emailEt.getText().toString();
                String country = countryEt.getText().toString();
                String address = addressEt.getText().toString();

                // requestRegistration(id, password,repassord, name, surname, phone, email, country, address);
            }
        });
    }
}
