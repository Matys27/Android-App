package com.matys.tplogin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserInfoActivity extends AppCompatActivity {
    private TextView textViewWelcome, textViewID, textViewNom, textViewPrenom, textViewEmail, textViewRole, textViewToken, textViewComptesRendus;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Intent intent = getIntent();

        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewID = findViewById(R.id.textViewID);
        textViewNom = findViewById(R.id.textViewNom);
        textViewPrenom = findViewById(R.id.textViewPrenom);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewRole = findViewById(R.id.textViewRole);
        textViewToken = findViewById(R.id.textViewToken);
        textViewComptesRendus = findViewById(R.id.textViewComptesRendus);

        // Récupération de l'objet JSON depuis l'intent
        String message = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
        try {
            JSONObject jsonObject = new JSONObject(message);


            // Récupération des valeurs nom, prénom et email
            String id = jsonObject.getString("id_user");
            String nom = jsonObject.getString("nom");
            String prenom = jsonObject.getString("prenom");
            String email = jsonObject.getString("email");
            String role = jsonObject.getString("role");
            String token = jsonObject.getString("token");

            // Affichage des valeurs
            textViewWelcome.setText(String.format("Bienvenue, %s !", nom));
            textViewID.setText("id : " + id);
            textViewNom.setText("Nom : " + nom);
            textViewPrenom.setText("Prénom : " + prenom);
            textViewEmail.setText("Email : " + email);
            textViewRole.setText("Rôle : " + role);
            textViewToken.setText("Token : " + token);

            // Récupération des comptes-rendus
            getComptesRendus(id);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getComptesRendus(String userId) {
        // Construire l'URL de l'API
        String apiUrl = "https://gsbcr.alwaysdata.net/Api/AfficheCRApi.php";

        // Créer les paramètres POST
        Map<String, String> params = new HashMap<>();
        params.put("id_user", userId);

        // Créer une demande de chaîne (StringRequest) en utilisant Volley
        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // La réponse est retournée avec succès
                        // Mettre à jour l'interface utilisateur avec les données récupérées
                        updateComptesRendus(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Gérer les erreurs de requête
                textViewComptesRendus.setText("Erreur de réseau: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Retourne les paramètres POST
                return params;
            }
        };

        // Ajouter la demande à la file d'attente de Volley
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void updateComptesRendus(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);

            StringBuilder comptesRendusBuilder = new StringBuilder();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject compteRendu = jsonArray.getJSONObject(i);
                // Récupération des informations du compte-rendu
                String idCrCp = compteRendu.getString("id_cr_cp");
                String dateVisite = compteRendu.getString("date_de_la_visite");
                String dateContreVisite = compteRendu.getString("date_de_contre_visite");
                String motifVisite = compteRendu.getString("motif_de_la_visite");
                String nomPraticien = compteRendu.getString("nom_du_praticien");
                String produit = compteRendu.getString("produit");
                String refus = compteRendu.getString("refus");
                String quantiteDistribuee = compteRendu.getString("quantite_distribuee");
                String remarques = compteRendu.getString("remarques");

                // Construction de la chaîne de texte avec les informations du compte-rendu
                comptesRendusBuilder.append("ID CR CP : ").append(idCrCp).append("\n");
                comptesRendusBuilder.append("Date de la visite : ").append(dateVisite).append("\n");
                comptesRendusBuilder.append("Date de contre-visite : ").append(dateContreVisite).append("\n");
                comptesRendusBuilder.append("Motif de la visite : ").append(motifVisite).append("\n");
                comptesRendusBuilder.append("Nom du praticien : ").append(nomPraticien).append("\n");
                comptesRendusBuilder.append("Produit : ").append(produit).append("\n");
                comptesRendusBuilder.append("Refus : ").append(refus).append("\n");
                comptesRendusBuilder.append("Quantité distribuée : ").append(quantiteDistribuee).append("\n");
                comptesRendusBuilder.append("Remarques : ").append(remarques).append("\n\n");
            }

            // Affichage des comptes-rendus dans l'interface utilisateur
            textViewComptesRendus.setText(comptesRendusBuilder.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            textViewComptesRendus.setText("Erreur lors du traitement des données des comptes-rendus.");
        }
    }
}
