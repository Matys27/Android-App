package com.matys.tplogin;

import android.annotation.SuppressLint;
import android.app.assist.AssistStructure;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class Add_CR extends AppCompatActivity {

    EditText editTextDateVisite, editTextDateContreVisite, editTextMotifVisite, editTextRemarque, editTextPracticien, checkBoxRefus, editTextProduit, editTextQuantite ;
    Button buttonEnregistrer;

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
        CheckBox checkBoxRefus = findViewById(R.id.checkBoxRefus);
        editTextProduit = findViewById(R.id.editTextProduit);
        editTextQuantite = findViewById(R.id.editTextQuantite);

        // Initialize Enregistrer button
        buttonEnregistrer = findViewById(R.id.buttonEnregistrer);

        // Set click listener for Enregistrer button
        buttonEnregistrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call method to add compte rendu to the database
                addCompteRendu();
            }
        });
    }

    private void addCompteRendu() {
        // Get input values
        String dateVisite = editTextDateVisite.getText().toString().trim();
        String dateContreVisite = editTextDateContreVisite.getText().toString().trim();
        String motifVisite = editTextMotifVisite.getText().toString().trim();
        String remarque = editTextRemarque.getText().toString().trim();
        String practicien = editTextPracticien.getText().toString().trim();
        String produit = editTextProduit.getText().toString().trim();
        String quantite = editTextQuantite.getText().toString().trim();
        boolean isRefused = checkBoxRefus.isPressed();

        // Prepare JSON object to send to the API
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("date_de_la_visite", dateVisite);
            jsonObject.put("date_de_contre_visite", dateContreVisite);
            jsonObject.put("motif_de_la_visite", motifVisite);
            jsonObject.put("remarques", remarque);
            jsonObject.put("nom_du_praticien", practicien);
            jsonObject.put("refus", isRefused);
            jsonObject.put("produit", produit);
            jsonObject.put("quantite_distribuee", quantite);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://gsbcr.alwaysdata.net/Api/AjoutCRApi.php";

        // Request a JSON response from the provided URL
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle response
                        Toast.makeText(Add_CR.this, "Compte rendu ajouté avec succès!", Toast.LENGTH_SHORT).show();
                        // Optionally, you can navigate back to the previous activity or clear input fields
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                Toast.makeText(Add_CR.this, "Une erreur s'est produite lors de l'ajout du compte rendu.", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest);
    }
}
