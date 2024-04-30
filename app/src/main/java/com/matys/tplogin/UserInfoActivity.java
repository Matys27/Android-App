package com.matys.tplogin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoActivity extends AppCompatActivity {
    private TextView textViewWelcome, textViewID, textViewNom, textViewPrenom, textViewEmail, textViewRole, textViewToken;
    private ListView listViewComptesRendus;
    private List<String> comptesRendusList;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        SharedPreferencesManager prefsManager = new SharedPreferencesManager(getApplicationContext());
        String userId = prefsManager.getUserId();
        String tokenFromPrefs = prefsManager.getToken();

        Button ajouterButton = findViewById(R.id.Ajouter);
        ajouterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInfoActivity.this, Add_CR.class);
                startActivity(intent);
            }
        });

        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewNom = findViewById(R.id.textViewNom);
        textViewPrenom = findViewById(R.id.textViewPrenom);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewRole = findViewById(R.id.textViewRole);
        listViewComptesRendus = findViewById(R.id.listViewComptesRendus);

        comptesRendusList = new ArrayList<>();

        // Récupération des valeurs nom, prénom et email depuis l'intent
        try {
            JSONObject jsonObject = new JSONObject(getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE));

            String nom = jsonObject.getString("nom");
            String prenom = jsonObject.getString("prenom");
            String email = jsonObject.getString("email");
            String role = jsonObject.getString("role");
            String tokenFromIntent = jsonObject.getString("token");

            textViewWelcome.setText(String.format("Bienvenue, %s !", nom));
            textViewNom.setText("Nom : " + nom);
            textViewPrenom.setText("Prénom : " + prenom);
            textViewEmail.setText("Email : " + email);
            textViewRole.setText("Rôle : " + role);

            getComptesRendus(userId);

        } catch (JSONException e) {
            Toast.makeText(this, "Erreur lors du traitement des données JSON", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void getComptesRendus(String userId) {
        String apiUrl = "https://gsbcr.alwaysdata.net/Api/AfficheCRApi.php";
        Map<String, String> params = new HashMap<>();
        params.put("id", userId);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        updateComptesRendus(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UserInfoActivity.this, "Erreur de réseau: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void updateComptesRendus(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            int status = jsonResponse.getInt("status");

            if (status == 200) {
                JSONArray comptesRendusArray = jsonResponse.getJSONArray("comptesRendus");
                List<JSONObject> comptesRendusJsonList = new ArrayList<>();
                if (comptesRendusArray.length() > 0) {
                    for (int i = 0; i < comptesRendusArray.length(); i++) {
                        comptesRendusJsonList.add(comptesRendusArray.getJSONObject(i));
                    }

                    Collections.sort(comptesRendusJsonList, new Comparator<JSONObject>() {
                        @Override
                        public int compare(JSONObject cr1, JSONObject cr2) {
                            try {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                Date date1 = format.parse(cr1.getString("date_de_contre_visite"));
                                Date date2 = format.parse(cr2.getString("date_de_contre_visite"));
                                return date1.compareTo(date2);
                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        }
                    });

                    List<String> formattedComptesRendus = new ArrayList<>();
                    for (JSONObject compteRendu : comptesRendusJsonList) {
                        String displayText = String.format("ID CR CP : %s\nDate de la visite : %s\nDate de contre-visite : %s\nMotif de la visite : %s\nNom du praticien : %s\nProduit : %s\nRefus : %s\nQuantité distribuée : %s\nRemarques : %s\n\n",
                                compteRendu.getString("id_cr_cp"),
                                compteRendu.getString("date_de_la_visite"),
                                compteRendu.getString("date_de_contre_visite"),
                                compteRendu.getString("motif_de_la_visite"),
                                compteRendu.getString("nom_du_praticien"),
                                compteRendu.getString("produit"),
                                compteRendu.getString("refus"),
                                compteRendu.getString("quantite_distribuee"),
                                compteRendu.getString("remarques"));
                        formattedComptesRendus.add(displayText);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, formattedComptesRendus);
                    listViewComptesRendus.setAdapter(adapter);
                } else {
                    comptesRendusList.add("Aucun compte rendu disponible pour cet utilisateur.");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, comptesRendusList);
                    listViewComptesRendus.setAdapter(adapter);
                }
            } else {
                String errorMessage = jsonResponse.getString("message");
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Erreur lors du traitement des données des comptes-rendus.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
