package com.xuannghia.myewallet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;
import java.math.BigDecimal;
import java.security.Provider;
import java.security.Security;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuthentication;
    private TextView txtShowEmail, txtShowAddressWallet;
    private Button btnLogout, btnTransaction;
    private ImageView imgQR;
    private ProgressBar progressBar;
    private Button btnCreateWallet;
    private Web3j web3j;
    private DatabaseReference mDatabase;
    String currentEmail = "";
    String walletPath;
    File walletDirs;
    String fileName;
    String addressWallet;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBouncyCastle();

        txtShowEmail = findViewById(R.id.txtShowEmail);
        btnCreateWallet = findViewById(R.id.btnCreateWallet);
        txtShowAddressWallet = findViewById(R.id.txtAddressWallet);
        btnLogout = findViewById(R.id.btnLogout);
        btnTransaction = findViewById(R.id.btnTransaction);
        imgQR = findViewById(R.id.qrCode);

        mAuthentication = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        currentEmail = mAuthentication.getCurrentUser().getEmail();

        walletPath = getFilesDir().getAbsolutePath();
        walletDirs = new File(walletPath);

        web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/307a5c4d2cd14c4fa9c9299570ae7493"));
        try {
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().sendAsync().get();
            if (!clientVersion.hasError()) {
                //Connected
                Toast.makeText(this, "Connect success !!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, clientVersion.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        getDataRemoteDatabase(mAuthentication.getUid());

        if (user == null) {
            btnCreateWallet.isEnabled();
        } else {
            btnCreateWallet.setEnabled(false);
            txtShowAddressWallet.setText(user.getAddress());
        }

        txtShowEmail.setText(currentEmail);
        createWallet(MainActivity.this, currentEmail, walletDirs);
        btnCreateWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addressWallet = getAddress(MainActivity.this, currentEmail, walletDirs);
                writeDatabaseRemote(addressWallet, currentEmail, mAuthentication.getUid());
                getDataRemoteDatabase(mAuthentication.getUid());
            }
        });

        btnTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTransaction(currentEmail, walletDirs, web3j);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuthentication.signOut();
                finish();
            }
        });
    }

    // Workaround for bug with ECDA signatures.
    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    private void createWallet(Context context, String currentEmail, File walletDir) {
        try {
            fileName = WalletUtils.generateLightNewWalletFile(currentEmail, walletDir);
            walletDirs = new File(walletPath + "/" + fileName);
            Toast.makeText(context, "New Wallet is created", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String getAddress(Context context, String currentEmail, File walletDir) {
        String address = "";
        try {
            Credentials credentials = WalletUtils.loadCredentials(currentEmail, walletDir);
            address = credentials.getAddress();
            Toast.makeText(context, credentials.getAddress().toString(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return address;
    }

    public void sendTransaction(String currentEmail, File walletDirs, Web3j web3j) {
        try {
            Credentials credentials = WalletUtils.loadCredentials(currentEmail, walletDirs);
            TransactionReceipt receipt = Transfer.sendFunds(web3j, credentials, "0x742193ba2df7c1badbde1b2f9b0dc3bb90a3ea57", new BigDecimal(1), Convert.Unit.ETHER).sendAsync().get();
            Toast.makeText(this, "Success transaction" + receipt.getTransactionHash(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void writeDatabaseRemote(String address, String currentEmail, String uuid) {
        User user = new User(currentEmail, uuid, address);

        mDatabase.child("USER").child(uuid).setValue(user);

    }

    public void getDataRemoteDatabase(String uuid) {
        Query queryUser = mDatabase.child("USER");
        queryUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.child(uuid).getValue(User.class);
                txtShowAddressWallet.setText(user.getAddress());
                QRGEncoder qrgEncoder = new QRGEncoder(user.getAddress(), null, QRGContents.Type.TEXT, 10);
                Bitmap qrBitma = qrgEncoder.getBitmap();
                imgQR.setImageBitmap(qrBitma);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
