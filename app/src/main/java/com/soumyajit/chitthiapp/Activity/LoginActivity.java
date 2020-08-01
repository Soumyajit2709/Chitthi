package com.soumyajit.chitthiapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.soumyajit.chitthiapp.R;
import com.soumyajit.chitthiapp.Util.ConnectionManager;

public class LoginActivity extends AppCompatActivity {

    MaterialEditText emailETLogin,passETLogin;
    Button loginBtn;
    TextView forgotPass,signUp;

    FirebaseAuth auth;
    FirebaseUser firebaseUser;

    ProgressDialog progressDialog;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null){
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailETLogin = findViewById(R.id.email);
        passETLogin = findViewById(R.id.password);
        loginBtn = findViewById(R.id.btnLogin);
        forgotPass = findViewById(R.id.forgotPass);
        signUp = findViewById(R.id.signUp);

        getSupportActionBar().setTitle("Login");

        if (new ConnectionManager().checkConnectivity(LoginActivity.this)) {

            auth = FirebaseAuth.getInstance();

            forgotPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                    startActivity(i);
                }
            });

            signUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent j = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(j);
                }
            });

            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email_text = emailETLogin.getText().toString();
                    String pass_text = passETLogin.getText().toString();

                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("Logging in...");
                    progressDialog.show();

                    if (TextUtils.isEmpty(email_text) || TextUtils.isEmpty(pass_text)) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "All Fields are Required", Toast.LENGTH_SHORT).show();
                    } else {
                        auth.signInWithEmailAndPassword(email_text, pass_text)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (task.isSuccessful()) {
                                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            progressDialog.dismiss();
                                            startActivity(i);
                                            finish();
                                            Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            });
        } else {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Error!!!");
            alertDialog.setMessage("No Internet Connection").setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(i);
                    finish();
                }
            })
                    .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.create();
            alertDialog.show();
        }
    }
}