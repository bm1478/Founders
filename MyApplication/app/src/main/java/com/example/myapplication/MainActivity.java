package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.samsung.android.sdk.blockchain.CoinType;
import com.samsung.android.sdk.blockchain.ListenableFutureTask;
import com.samsung.android.sdk.blockchain.SBlockchain;
import com.samsung.android.sdk.blockchain.account.Account;
import com.samsung.android.sdk.blockchain.account.ethereum.EthereumAccount;
import com.samsung.android.sdk.blockchain.coinservice.CoinNetworkInfo;
import com.samsung.android.sdk.blockchain.coinservice.CoinServiceFactory;
import com.samsung.android.sdk.blockchain.coinservice.ethereum.EthereumService;
import com.samsung.android.sdk.blockchain.exception.SsdkUnsupportedException;
import com.samsung.android.sdk.blockchain.network.EthereumNetworkType;
import com.samsung.android.sdk.blockchain.ui.CucumberWebView;
import com.samsung.android.sdk.blockchain.ui.OnSendTransactionListener;
import com.samsung.android.sdk.blockchain.wallet.HardwareWallet;
import com.samsung.android.sdk.blockchain.wallet.HardwareWalletType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnSendTransactionListener {

    Button connectBtn;
    Button generateAccountBtn;
    Button getAccountsBtn;
    Button paymentsheetBtn;
    Button sendSmartContractBtn;
    Button webViewInitBtn;
    private SBlockchain sBlockchain;
    private HardwareWallet wallet;
    private Account generatedAccount;
    private List<Account> accounts;
    private CucumberWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sBlockchain = new SBlockchain();
        try {
            sBlockchain.initialize(this);
        } catch (SsdkUnsupportedException e) {
            e.printStackTrace();
        }

        connectBtn = findViewById(R.id.connect);
        generateAccountBtn = findViewById(R.id.generateAccount);
        getAccountsBtn = findViewById(R.id.getAccounts);
        paymentsheetBtn = findViewById(R.id.paymentsheet);
        sendSmartContractBtn = findViewById(R.id.sendSmartContract);
        webViewInitBtn = findViewById(R.id.webViewInit);
        webView = findViewById(R.id.cucumberWebView);


        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        generateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generate();
            }
        });

        getAccountsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAccounts();
            }
        });

        paymentsheetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentSheet();
            }
        });

        sendSmartContractBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });

        webViewInitBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                webViewInit();
            }
        });

    }

    private void connect() {
        sBlockchain.getHardwareWalletManager()
                .connect(HardwareWalletType.SAMSUNG, true)
                .setCallback(new ListenableFutureTask.Callback<HardwareWallet>() {  //비동기 asynchronous
                    @Override
                    public void onSuccess(HardwareWallet hardwareWallet) {
                        wallet = hardwareWallet;
                    }

                    @Override
                    public void onFailure(ExecutionException e) {

                    }

                    @Override
                    public void onCancelled(InterruptedException e) {

                    }
                });
    }

    private void generate() {
        CoinNetworkInfo coinNetworkInfo = new CoinNetworkInfo(
                CoinType.ETH,
                EthereumNetworkType.ROPSTEN,
                "https://ropsten.infura.io/v3/70ddb1f89ca9421885b6268e847a459d"
        );

        sBlockchain.getAccountManager()
                .generateNewAccount(wallet, coinNetworkInfo)
                .setCallback(new ListenableFutureTask.Callback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        generatedAccount = account;
                        Log.d("MyApp", account.toString());
                    }

                    @Override
                    public void onFailure(@NotNull ExecutionException e) {
                        Log.d("MyApp", e.toString());
                    }

                    @Override
                    public void onCancelled(@NotNull InterruptedException e) {

                    }
                });
    }

    private void getAccounts() {
        List<Account> accountsList = sBlockchain.getAccountManager().getAccounts(
                wallet.getWalletId(),
                CoinType.ETH,
                EthereumNetworkType.ROPSTEN);
        Log.d("MyApp", Arrays.toString(new List[]{accountsList}));
    }

    private void paymentSheet() {
        CoinNetworkInfo coinNetworkInfo = new CoinNetworkInfo(
                CoinType.ETH,
                EthereumNetworkType.ROPSTEN,
                "https://ropsten.infura.io/v3/70ddb1f89ca9421885b6268e847a459d"
        );

        List<Account> accounts = sBlockchain.getAccountManager()
                .getAccounts(
                        wallet.getWalletId(),
                        CoinType.ETH,
                        EthereumNetworkType.ROPSTEN
                );

        EthereumService service= (EthereumService) CoinServiceFactory.getCoinService(this, coinNetworkInfo);
        Intent intent = service
                .createEthereumPaymentSheetActivityIntent(
                    this,
                    wallet,
                    (EthereumAccount) accounts.get(0),
                    "0x6E8B9ab63937c8fF33D0Fa6e03bB0cbFE17b18f3",
                    new BigInteger("1000000000000"),
                    null,
                    null
            );

        startActivityForResult(intent, 0);
    }

    private void webViewInit() {
        CoinNetworkInfo coinNetworkInfo = new CoinNetworkInfo(
                CoinType.ETH,
                EthereumNetworkType.ROPSTEN,
                "https://ropsten.infura.io/v3/70ddb1f89ca9421885b6268e847a459d"
        );

        List<Account> accounts = sBlockchain.getAccountManager()
                .getAccounts(
                        wallet.getWalletId(),
                        CoinType.ETH,
                        EthereumNetworkType.ROPSTEN
                );

        EthereumService service= (EthereumService) CoinServiceFactory.getCoinService(this, coinNetworkInfo);

        webView.init(service, accounts.get(0), this);
        webView.loadUrl("https://faucet.metamask.io");
    }

    @Override
    public void onSendTransaction(@NotNull String requestId, @NotNull EthereumAccount fromAccount, @NotNull String toAddress, @Nullable BigInteger value, @Nullable String data, @Nullable BigInteger nonce) {
        HardwareWallet wallet = sBlockchain.getHardwareWalletManager().getConnectedHardwareWallet();
        Intent intent = webView
                .createEthereumPaymentSheetActivityIntent(
                        this,
                        requestId,
                        wallet,
                        toAddress,
                        value,
                        data,
                        nonce
                );
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != 0) {
            return;
        }

        webView.onActivityResult(requestCode, resultCode, data);
    }
}
