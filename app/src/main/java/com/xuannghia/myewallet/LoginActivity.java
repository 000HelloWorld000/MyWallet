package com.xuannghia.myewallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    EditText edtEmails,edtPwds;
    Button btnLogin, btnSignUp;
    ProgressBar progressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        edtEmails = findViewById(R.id.editEmail);
        edtPwds = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progress_login);
        progressBar.setVisibility(View.INVISIBLE);
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mAuthStateListener == null) {
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                    if (mFirebaseUser != null) {
                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    }
                    else {
                        Toast.makeText(LoginActivity.this,"Login failed, try again",Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmails.getText().toString();
                String password = edtPwds.getText().toString();
                Toast.makeText(LoginActivity.this,email,Toast.LENGTH_SHORT).show();
                if (email.isEmpty()) {
                    edtEmails.setError("You must type email.");
                    edtEmails.requestFocus();
                }
                else if (password.isEmpty()) {
                    edtPwds.setError("You must type password.");
                    edtPwds.requestFocus();
                } else if(!(email.isEmpty() && password.isEmpty())) {
                    progressBar.setVisibility(View.VISIBLE);
                    mFirebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(
                            LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Login failed, try again!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            }
                    );
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignInActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onDestroy() {
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        super.onDestroy();
    }
}
