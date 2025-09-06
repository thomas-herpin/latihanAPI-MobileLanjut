package edu.uph.m23si2.pertamaapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import edu.uph.m23si2.pertamaapp.api.ApiResponse;
import edu.uph.m23si2.pertamaapp.api.ApiService;
import edu.uph.m23si2.pertamaapp.api.KotaResponse;
import edu.uph.m23si2.pertamaapp.model.Kota;
import edu.uph.m23si2.pertamaapp.model.Provinsi;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    Button btnLogin;
    EditText edtNama, edtPassword;
    Spinner sprProvinsi, sprKota;
    List<Provinsi> provinsiList = new ArrayList<>();
    List<String> namaProvinsi = new ArrayList<>();
    ArrayAdapter<String> provinsiAdapter;

    List<String> namaKota = new ArrayList<>();
    ArrayAdapter<String> kotaAdapter;

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("default.realm")
                .schemaVersion(1)
                .allowWritesOnUiThread(true)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        sprProvinsi = findViewById(R.id.sprProvinsi);
        sprKota = findViewById(R.id.sprKota);

        provinsiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namaProvinsi);
        provinsiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        sprProvinsi.setAdapter(provinsiAdapter);

        kotaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namaKota);
        kotaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        sprKota.setAdapter(kotaAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wilayah.id/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Panggil API Provinsi
        apiService.getProvinsi().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    provinsiList = response.body().getData();
                    namaProvinsi.clear();
                    for (Provinsi p : provinsiList) {
                        if (p.getName() != null) {
                            namaProvinsi.add(p.getName());
                        }
                    }
                    provinsiAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Gagal ambil provinsi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        sprProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Provinsi selected = provinsiList.get(position);
                String kodeProv = selected.getCode();
                Log.d("Provinsi", selected.getCode() + " - " + selected.getName());

                // Panggil API Kota
                apiService.getKota(kodeProv).enqueue(new Callback<KotaResponse>() {
                    @Override
                    public void onResponse(Call<KotaResponse> call, Response<KotaResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (Kota k : response.body().getData()) {
                                Log.d("Kota", k.getCode() + " - " + k.getName());
                                namaKota.add(k.getName());
                            }
                            kotaAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("Kota", "Response gagal: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<KotaResponse> call, Throwable t) {
                        Log.e("Kota", "Error: " + t.getMessage(), t);
                        Toast.makeText(LoginActivity.this, "Gagal ambil kota: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnLogin = findViewById(R.id.btnLogin);
        edtNama = findViewById(R.id.edtNama);
        edtPassword = findViewById(R.id.edtPassword);

        btnLogin.setOnClickListener(v -> toDashboard());
    }

    public void toDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }
}