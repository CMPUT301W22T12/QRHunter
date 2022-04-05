package com.example.qrhunter;


import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class databaseAddTest {
    qrDatabaseAddHandler addHandler;
    FirebaseFirestore db;
    deleteHandler deleteHandle;
    String commentID;

    @Before
    public void init() throws IOException {
        db = FirebaseFirestore.getInstance();
        addHandler = new qrDatabaseAddHandler("Test_User");
        deleteHandle = new deleteHandler(db);
    }

    @Test
    public void testA_testQRAdd(){
        addHandler.addQRToDB("", "", 999, 52, 51, "hfv873qhbfd78gfc19p3bfesdv689oq3248wbfuoedg7v8o", db);
        db.collection("QRcode").document("hfv873qhbfd78gfc19p3bfesdv689oq3248wbfuoedg7v8o").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    assert (true);
                } else {
                    assert (false);
                }
            }
        });
    }

    @Test
    public void testB_testCommentAdd(){
        commentID = addHandler.addCommentToDB("Test Comment", "hfv873qhbfd78gfc19p3bfesdv689oq3248wbfuoedg7v8o", db);
        db.collection("Comments").document(commentID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    assert (true);
                } else {
                    assert (false);
                }
            }
        });
    }

}
