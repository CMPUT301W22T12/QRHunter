package com.example.qrhunter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
// the leaderboard activity displayed to users
public class Leaderboard extends AppCompatActivity implements LeaderboardAdapter.OnItemListener {

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ArrayList<LeaderboardHolder> leaderboardHolders = new ArrayList<>();
    LeaderboardAdapter leaderboardAdapter;
    RecyclerView recyclerView;
    private TextView rank;
    private static final String TAG = "LeaderBoard";
    private static final String SHARED_PREFERENCES = "USERNAME-sharedPref";
    HashMap<DocumentReference, Double> myMapOfCodes = new HashMap<>();

    @Override
    public int getResourceId() {
        return R.layout.activity_leaderboard;
    }

    @Override
    public int SelectedItemId() {
        return R.id.goToLeaderboard;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rank = findViewById(R.id.leaderboardRank);
        getFireStoreContent("TOTAL");

    }

    // sort by high score
    public void set_rank_highest_score(View view) {
        rank.setText("User Rank By Score: ");
        leaderboardHolders.clear();
        getFireStoreContent("TOTAL");
    }


    public void set_rank_highest_code(View view) {
        rank.setText("Uer rank by highest score: ");
        leaderboardHolders.clear();
        getFireStoreContent("HIGHEST");
    }


    // sort by number of codes scanned
    public void set_rank_number_codes_scanned(View view) {
        rank.setText("USER RANK BY NUMBER SCANS: ");
        leaderboardHolders.clear();
        getFireStoreContent("AMOUNT");
    }

    // retrieve users from firestore
    public void getFireStoreContent(String method){
        firestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                final Integer[] sizeUsers = {value.size()};
                final Integer[] userCount = {0};
                for(DocumentSnapshot snapshot : value.getDocuments()){
                    Map<String, Object> map = snapshot.getData();
                    if(map.get("QRcode") == null || ((ArrayList<DocumentReference>) map.get("QRcode")).isEmpty() || method.equals("AMOUNT")) {
                        LeaderboardHolder holder;
                        if(map.get("QRcode") == null) {
                            holder = new LeaderboardHolder(map.get("Username").toString(),
                                    String.valueOf(0));
                        } else {
                            holder = new LeaderboardHolder(map.get("Username").toString(),
                                    String.valueOf(((ArrayList<DocumentReference>) map.get("QRcode")).size()));
                        }
                        leaderboardHolders.add(holder);
                        userCount[0] = userCount[0] + 1;
                        if (userCount[0] == (sizeUsers[0])) {
                            sortUsers();
                        }
                        continue;
                    }
                    ArrayList<DocumentReference> myList = (ArrayList<DocumentReference>) map.get("codes");
                    final Integer[] count = { myList.size() };
                    final Integer[] count2 = { 0};
                    final Double[] score = {0.0};
                    final Double[] highestRanking = {0.0};
                    for(DocumentReference el : myList) {
                        if(myMapOfCodes.get(el) != null) {
                            if (highestRanking[0] < myMapOfCodes.get(el)) {
                                highestRanking[0] = myMapOfCodes.get(el);
                            }
                            score[0] = score[0] + myMapOfCodes.get(el);
                            count2[0] = count2[0] + 1;
                            if (count2[0] == count[0]) {
                                LeaderboardHolder holder;
                                if (method.equals("TOTAL")) {
                                    holder = new LeaderboardHolder(map.get("username").toString(),
                                            String.valueOf(score[0]));
                                } else {
                                    holder = new LeaderboardHolder(map.get("username").toString(),
                                            String.valueOf(highestRanking[0]));
                                }
                                leaderboardHolders.add(holder);
                                userCount[0] = userCount[0] + 1;
                                if (userCount[0] == (sizeUsers[0])) {
                                    sortUsers();
                                }
                            }
                        } else {
                            el.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    try {
                                        count2[0] = count2[0] + 1;
                                        if (!documentSnapshot.exists() || documentSnapshot.getData().isEmpty())  {

                                        } else {
                                            Map<String, Object> doc = documentSnapshot.getData();
                                            Double scoreFromDoc = (Double) doc.get("score");
                                            score[0] = score[0] + scoreFromDoc;
                                            myMapOfCodes.put(el, scoreFromDoc);

                                            if (highestRanking[0] < (scoreFromDoc)) {
                                                highestRanking[0] = myMapOfCodes.get(el);
                                            }
                                        }

                                        if (count2[0] == count[0]) {
                                            LeaderboardHolder holder;
                                            if (method.equals("TOTAL")) {
                                                holder = new LeaderboardHolder(map.get("username").toString(),
                                                        String.valueOf(score[0]));
                                            } else {
                                                holder = new LeaderboardHolder(map.get("username").toString(),
                                                        String.valueOf(highestRanking[0]));
                                            }
                                            leaderboardHolders.add(holder);
                                            userCount[0] = userCount[0] + 1;
                                            if (userCount[0] == (sizeUsers[0])) {
                                                sortUsers();
                                            }
                                        }
                                    } catch (Exception e) {
                                        Map<String, Object> doc = documentSnapshot.getData();

                                        String message = e.getMessage();
                                        Log.w("Warning", doc.toString());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    // sort leaderboard holders
    private void sortUsers() {
        Collections.sort(leaderboardHolders, new Comparator<LeaderboardHolder>(){

            public int compare(LeaderboardHolder a1, LeaderboardHolder a2)
            {
                return Double.valueOf(a2.getScore()).compareTo(Double.valueOf(a1.getScore()));
            }
        });
        for(int i = 0; i< leaderboardHolders.size(); i++){
            leaderboardHolders.get(i).setUserRank(i+1);
        }
        for(int i = 0; i < leaderboardHolders.size(); i++){
            if(leaderboardHolders.get(i).getUsername().equals(userNameLoad())){
                rank.setText(rank.getText().toString() + leaderboardHolders.get(i).getUserRank());
            }
        }
        initRecycleView();
    }
    private void initRecycleView(){
        recyclerView = findViewById(R.id.leaderBoardRecycleView);
        leaderboardAdapter = new LeaderboardAdapter(leaderboardHolders, this,this);
        recyclerView.setAdapter(leaderboardAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }



    @Override
    public void OnItemClick(int position) {
        String s= leaderboardHolders.get(position).getUsername();
        Intent intent = new Intent(this, userProfileViewerActivity.class);
        intent.putExtra("leaderBoardUserNameIntent",s);
        startActivity(intent);
    }

    public String userNameLoad() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPref.getString("USERNAME-key", "default-empty-string");
    }

}