package com.example.quizme.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.quizme.model.WithdrawRequest;
import com.example.quizme.databinding.FragmentWalletBinding;
import com.example.quizme.model.User;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.perfmark.Tag;


public class WalletFragment extends Fragment {

    FragmentWalletBinding binding;
    FirebaseFirestore database;
    User user;
    RequestQueue requestQueue;

    public WalletFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWalletBinding.inflate(inflater, container, false);
        database = FirebaseFirestore.getInstance();

        database.collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
                binding.currentCoins.setText(String.valueOf(user.getCoins()));

            }
        });

        binding.sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getCoins() > 50000) {
                    String uid = FirebaseAuth.getInstance().getUid();
                    String payPal = binding.emailBox.getText().toString();
                    long withdrawCoins = Long.parseLong(binding.coinsBox.getText().toString());
                    WithdrawRequest request = new WithdrawRequest(uid, payPal, withdrawCoins, user.getName());

                    if(user.getCoins() >= withdrawCoins) {
                        //update coins database
                        database
                                .collection("users")
                                .document(uid)
                                .update("coins", FieldValue.increment(withdrawCoins * (-1)));
                    }

                    //add database
                    database
                            .collection("withdraws")
                            .document(uid)
                            .set(request).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //convert json
                            JSONObject params = new JSONObject();
                            try {
                                params.put("name", user.getName());
                                params.put("email", payPal);
                                params.put("withdrawCoins", "500");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            String url = "https://notification-service-android.herokuapp.com/notification";

                            // Make request for JSONObject
                            JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                                    Request.Method.POST, url, params,
                                    new Response.Listener<JSONObject >() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Toast.makeText(getContext(),"Success",Toast.LENGTH_LONG).show();
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.println(error.getMessage());
                                }
                            }) {

                                /**
                                 * Passing some request headers
                                 */
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    HashMap<String, String> headers = new HashMap<String, String>();
                                    headers.put("Content-Type", "application/json; charset=utf-8");
                                    return headers;
                                }

                            };
                            requestQueue = Volley.newRequestQueue(getContext());
                            requestQueue.add(jsonObjReq);
                            Toast.makeText(getContext(), "Request sent successfully.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "You need more coins to get withdraw.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return binding.getRoot();

    }
}