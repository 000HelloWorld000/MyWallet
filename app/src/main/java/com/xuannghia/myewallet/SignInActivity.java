package com.xuannghia.myewallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;
    EditText edtEmail, edtPwd, edtConfirmPass;
    Button btnSignIn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);
        edtEmail = findViewById(R.id.editTextEmail);
        edtPwd = findViewById(R.id.editTextPassword);
        edtConfirmPass = findViewById(R.id.editConfirmPass);
        btnSignIn = findViewById(R.id.btnSignIn);
        mFirebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progress_sign_up);
        progressBar.setVisibility(View.INVISIBLE);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();
                Toast.makeText(SignInActivity.this,email,Toast.LENGTH_SHORT).show();
                String password = edtPwd.getText().toString();
                String confirmPassword = edtConfirmPass.getText().toString();
                if (email.isEmpty()) {
                    edtEmail.setError("You must type email.");
                    edtEmail.requestFocus();
                } else if (password.isEmpty()) {
                    edtPwd.setError("You must type password.");
                    edtPwd.requestFocus();
                } else if (confirmPassword.isEmpty()) {
                    edtConfirmPass.setError("You must type confirm password.");
                    edtConfirmPass.requestFocus();
                }  else if (!confirmPassword.equalsIgnoreCase(password)) {
                    edtConfirmPass.setError("Confirm password dont like your password");
                    edtConfirmPass.requestFocus();
                }
                else if (!(email.isEmpty() && password.isEmpty() && confirmPassword.isEmpty())) {
                    progressBar.setVisibility(View.VISIBLE);
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password).
                            addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SignInActivity.this, "Sign in failed, try again!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            }
                            );
                }
            }
        });
    }
}
