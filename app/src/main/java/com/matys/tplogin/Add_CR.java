package com.matys.tplogin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class Add_CR extends AppCompatActivity {

    EditText editTextDateVisite, editTextDateContreVisite, editTextMotifVisite, editTextRemarque, editTextPracticien, editTextProduit, editTextQuantite;
    CheckBox checkBoxRefus;
    Button buttonEnregistrer, buttonRetour;

    public class SharedPreferencesManager {
        private static final String SHARED_PREFS_NAME = "gsbcr_prefs";
        private static final String USER_ID_KEY = "user_id";
        private static final String TOKEN_KEY = "token";

        private SharedPreferences sharedPreferences;

        public SharedPreferencesManager(Context context) {
            this.sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        }

        public void saveUserId(String userId) {
            sharedPreferences.edit().putString(USER_ID_KEY, userId).apply();
        }

        public String getUserId() {
            return sharedPreferences.getString(USER_ID_KEY, null);
        }

        public void saveToken(String token) {
            sharedPreferences.edit().putString(TOKEN_KEY, token).apply();
        }

        public String getToken() {
            return sharedPreferences.getString(TOKEN_KEY, null);
        }
    }


    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cr);

        // Initialize EditText fields
        editTextDateVisite = findViewById(R.id.editTextDateVisite);
        editTextDateContreVisite = findViewById(R.id.editTextDateContreVisite);
        editTextMotifVisite = findViewById(R.id.editTextMotifVisite);
        editTextRemarque = findViewById(R.id.editTextRemarque);
        editTextPracticien = findViewById(R.id.editTextPracticien);
        editTextProduit = findViewById(R.id.editTextProduit);
        editTextQuantite = findViewById(R.id.editTextQuantite);
        checkBoxRefus = findViewById(R.id.checkBoxRefus);

        // Initialize Buttons
        buttonEnregistrer = findViewById(R.id.buttonEnregistrer);
        buttonRetour = findViewById(R.id.buttonRetour);

        // Set click listener for Enregistrer button
        buttonEnregistrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCompteRendu();
            }
        });

        // Set click listener for Retour button
        buttonRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Add_CR.this, UserInfoActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addCompteRendu() {
        SharedPreferencesManager prefsManager = new SharedPreferencesManager(getApplicationContext());
        final String userId = prefsManager.getUserId();

        final String dateVisite = editTextDateVisite.getText().toString().trim();
        final String dateContreVisite = editTextDateContreVisite.getText().toString().trim();
        final String motifVisite = editTextMotifVisite.getText().toString().trim();
        final String remarque = editTextRemarque.getText().toString().trim();
        final String practicien = editTextPracticien.getText().toString().trim();
        final String produit = editTextProduit.getText().toString().trim();
        final String quantite = editTextQuantite.getText().toString().trim();
        final boolean isRefused = checkBoxRefus.isChecked();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://gsbcr.alwaysdata.net/Api/AjoutCRApi.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(Add_CR.this, "Compte rendu ajouté avec succès!", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Add_CR.this, "Erreur lors de l'ajout du compte rendu: " + error.toString(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_user", userId);
                params.put("date_de_la_visite", dateVisite);
                params.put("date_de_contre_visite", dateContreVisite);
                params.put("motif_de_la_visite", motifVisite);
                params.put("remarques", remarque);
                params.put("nom_du_praticien", practicien);
                params.put("produit", produit);
                params.put("refus", String.valueOf(isRefused ? 1 : 0));
                params.put("quantite_distribuee", quantite);
                return params;
            }
        };

        queue.add(stringRequest);
    }

}