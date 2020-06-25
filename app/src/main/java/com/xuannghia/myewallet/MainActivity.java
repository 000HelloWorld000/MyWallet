package com.xuannghia.myewallet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;
import android.widget.Toolbar;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

public class MainActivity extends AppCompatActivity {
    private Web3j web3j;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/307a5c4d2cd14c4fa9c9299570ae7493"));
        try {
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().sendAsync().get();
            if (!clientVersion.hasError()) {
                //Connected
                Toast.makeText(this, "Connect success", Toast.LENGTH_SHORT).show();
            } else {
                //Show Error
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
