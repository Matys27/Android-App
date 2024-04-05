package com.matys.tplogin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

        Intent intent = getIntent();

        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewID = findViewById(R.id.textViewID);
        textViewNom = findViewById(R.id.textViewNom);
        textViewPrenom = findViewById(R.id.textViewPrenom);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewRole = findViewById(R.id.textViewRole);
        textViewToken = findViewById(R.id.textViewToken);
        listViewComptesRendus = findViewById(R.id.listViewComptesRendus);

        comptesRendusList = new ArrayList<>();

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
        params.put("id", userId); // Envoyer l'ID utilisateur à l'API

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
                listViewComptesRendus.setTextDirection(Integer.parseInt("Erreur de réseau: " + error.getMessage()));
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
            JSONObject jsonResponse = new JSONObject(response);
            int status = jsonResponse.getInt("status");

            if (status == 200) {
                JSONArray comptesRendusArray = jsonResponse.getJSONArray("comptesRendus");

                if (comptesRendusArray.length() > 0) {
                    // Créer une liste pour stocker les comptes-rendus
                    List<JSONObject> comptesRendusList = new ArrayList<>();
                    for (int i = 0; i < comptesRendusArray.length(); i++) {
                        // Ajouter chaque compte-rendu à la liste
                        comptesRendusList.add(comptesRendusArray.getJSONObject(i));
                    }

                    // Trier les comptes-rendus par ordre de la date de visite
                    Collections.sort(comptesRendusList, new Comparator<JSONObject>() {
                        @Override
                        public int compare(JSONObject cr1, JSONObject cr2) {
                            try {
                                // Récupérer les dates de visite de chaque compte-rendu
                                String date1 = cr1.getString("date_de_contre_visite");
                                String date2 = cr2.getString("date_de_contre_visite");
                                // Convertir les dates en objets de type Date pour les comparer
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                Date parsedDate1 = format.parse(date1);
                                Date parsedDate2 = format.parse(date2);
                                // Comparer les dates et retourner le résultat
                                return parsedDate1.compareTo(parsedDate2);
                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                                // En cas d'erreur, retourner 0 pour indiquer que les deux objets sont égaux
                                return 0;
                            }
                        }
                    });

                    // Afficher les comptes-rendus triés
                    List<String> comptesRendusTextList = new ArrayList<>();
                    for (JSONObject compteRendu : comptesRendusList) {
                        // Construction de la chaîne de texte avec les informations du compte-rendu
                        String idCrCp = compteRendu.getString("id_cr_cp");
                        String dateVisite = compteRendu.getString("date_de_la_visite");
                        String dateContreVisite = compteRendu.getString("date_de_contre_visite");
                        String motifVisite = compteRendu.getString("motif_de_la_visite");
                        String nomPraticien = compteRendu.getString("nom_du_praticien");
                        String produit = compteRendu.getString("produit");
                        String refus = compteRendu.getString("refus");
                        String quantiteDistribuee = compteRendu.getString("quantite_distribuee");
                        String remarques = compteRendu.getString("remarques");

                        String compteRenduText = "ID CR CP : " + idCrCp + "\n" + "Date de la visite : " + dateVisite + "\n" + "Date de contre-visite : " + dateContreVisite + "\n" + "Motif de la visite : " + motifVisite + "\n" + "Nom du praticien : " + nomPraticien + "\n" + "Produit : " + produit + "\n" + "Refus : " + refus + "\n" + "Quantité distribuée : " + quantiteDistribuee + "\n" + "Remarques : " + remarques + "\n\n";

                        // Ajouter le compte-rendu trié à la liste
                        comptesRendusTextList.add(compteRenduText);
                    }

                    // Créer un adaptateur pour le ListView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, comptesRendusTextList);
                    // Définir l'adaptateur pour le ListView
                    listViewComptesRendus.setAdapter(adapter);
                } else {
                    // Aucun compte rendu trouvé pour cet utilisateur
                    comptesRendusList.add("Aucun compte rendu disponible pour cet utilisateur.");
                    // Créer un adaptateur pour le ListView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, comptesRendusList);
                    // Définir l'adaptateur pour le ListView
                    listViewComptesRendus.setAdapter(adapter);
                }
            } else {
                // Le statut n'est pas 200, il y a une erreur dans la réponse
                String message = jsonResponse.getString("message");
                comptesRendusList.add(message);
                // Créer un adaptateur pour le ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, comptesRendusList);
                // Définir l'adaptateur pour le ListView
                listViewComptesRendus.setAdapter(adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            comptesRendusList.add("Erreur lors du traitement des données des comptes-rendus.");
            // Créer un adaptateur pour le ListView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, comptesRendusList);
            // Définir l'adaptateur pour le ListView
            listViewComptesRendus.setAdapter(adapter);
        }
    }
}