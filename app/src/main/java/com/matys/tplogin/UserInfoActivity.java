package com.matys.tplogin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
    private TextView textViewWelcome, textViewNom, textViewPrenom, textViewEmail, textViewRole;
    private ListView listViewComptesRendus;
    private List<JSONObject> comptesRendusJsonList;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        SharedPreferencesManager prefsManager = new SharedPreferencesManager(getApplicationContext());
        String userId = prefsManager.getUserId();

        Button ajouterButton = findViewById(R.id.Ajouter);
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewNom = findViewById(R.id.textViewNom);
        textViewPrenom = findViewById(R.id.textViewPrenom);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewRole = findViewById(R.id.textViewRole);
        listViewComptesRendus = findViewById(R.id.listViewComptesRendus);
        comptesRendusJsonList = new ArrayList<>();

        ajouterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInfoActivity.this, Add_CR.class);
                startActivity(intent);
            }
        });

        Button deconnexionButton = findViewById(R.id.Deconnexion);
        deconnexionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deconnexion();
            }
        });

        // Récupération des valeurs nom, prénom et email depuis l'intent
        try {
            JSONObject jsonObject = new JSONObject(getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE));
            String nom = jsonObject.getString("nom");
            String prenom = jsonObject.getString("prenom");
            String email = jsonObject.getString("email");
            String role = jsonObject.getString("role");

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

        // Gestion du clic sur un élément de la liste
        listViewComptesRendus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    JSONObject compteRendu = comptesRendusJsonList.get(position);
                    String idCompteRendu = compteRendu.getString("id_cr_cp");
                    afficherConfirmationSuppression(idCompteRendu, userId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
                comptesRendusJsonList.clear();
                if (comptesRendusArray.length() > 0) {
                    for (int i = 0; i < comptesRendusArray.length(); i++) {
                        comptesRendusJsonList.add(comptesRendusArray.getJSONObject(i));
                    }
                    Collections.sort(comptesRendusJsonList, new Comparator<JSONObject>() {
                        @Override
                        public int compare(JSONObject cr1, JSONObject cr2) {
                            try {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                Date today = new Date();
                                Date date1 = format.parse(cr1.getString("date_de_contre_visite"));
                                Date date2 = format.parse(cr2.getString("date_de_contre_visite"));
                                long diff1 = Math.abs(date1.getTime() - today.getTime());
                                long diff2 = Math.abs(date2.getTime() - today.getTime());
                                return Long.compare(diff1, diff2);
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
                    comptesRendusJsonList.clear();
                    comptesRendusJsonList.add(new JSONObject().put("id_cr_cp", ""));
                    List<String> formattedComptesRendus = new ArrayList<>();
                    formattedComptesRendus.add("Aucun compte rendu disponible pour cet utilisateur.");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, formattedComptesRendus);
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


    private void afficherConfirmationSuppression(String idCompteRendu, String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Êtes-vous sûr de vouloir supprimer ce compte rendu ?");
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                supprimerCompteRendu(idCompteRendu, userId);
            }
        });
        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void supprimerCompteRendu(String idCompteRendu, String userId) {
        String apiUrl = "https://gsbcr.alwaysdata.net/Api/SuppCRApi.php";
        Map<String, String> params = new HashMap<>();
        params.put("id_cr_cp", idCompteRendu);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(UserInfoActivity.this, "Compte rendu supprimé avec succès", Toast.LENGTH_LONG).show();
                        getComptesRendus(userId);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UserInfoActivity.this, "Erreur lors de la suppression du compte rendu: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void deconnexion() {
        SharedPreferencesManager prefsManager = new SharedPreferencesManager(getApplicationContext());
        prefsManager.clearSession();
        Intent intent = new Intent(UserInfoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}