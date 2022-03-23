package com.example.qrhunter;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;

public class QRcodeViewerActivity extends AppCompatActivity {
    String qrCodeHash;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_viewer);

        Intent intent = getIntent();
        if (!intent.hasExtra("QRcode")) {
            Log.i(TAG, "No Intent Passed");
            finish();
        }
        qrCodeHash = intent.getStringExtra("QRcode");
        docRef = db.collection("QRcode").document(qrCodeHash);
        fillOutQRInfo();
    }

    public void fillOutQRInfo(){
        TextView pointsText = findViewById(R.id.qrCodeViewerPointsText);
        TextView uniqueScanText = findViewById(R.id.qrViewerUniqueScansText);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String score = String.valueOf(documentSnapshot.get("Score"));
                List<String> usersList = (List<String>) documentSnapshot.get("Users");
                int uniqueUserCount = usersList.size();
                pointsText.setText("This code is worth: " + score + " points!");
                uniqueScanText.setText("This code has been scanned by: " + uniqueUserCount + " users");
                if(documentSnapshot.contains("Images")){
                    addPicturesToView();
                }
                if(documentSnapshot.contains("Comments")){
                    addCommentsToView();
                }
            }
        });
    }

    public void addPicturesToView(){
        LinearLayout pictureLayout = findViewById(R.id.qrImageLinearLayout);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> imageList = (List<String>) documentSnapshot.get("Images");
                for(String imageString : imageList){
                   StorageReference storRef = storage.getReferenceFromUrl(imageString);
                    Task<byte[]> task = storRef.getBytes(1000000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] imageBytes) {
                            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            ImageView qrImage = new ImageView(QRcodeViewerActivity.this);
                            qrImage.setImageBitmap(imageBitmap);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            qrImage.setLayoutParams(params);
                            qrImage.setAdjustViewBounds(true);
                            qrImage.setPadding(5,0,5, 0);
                            pictureLayout.addView(qrImage);
                        }
                    });
                }
            }
        });
    }

    public void addCommentsToView(){
        LinearLayout commentsLayout = findViewById(R.id.qrViewerCommentsLinearLayout);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> commentList = (List<String>) documentSnapshot.get("Comments");
                for(String comment : commentList){
                    DocumentReference commentRef = db.collection("Comments").document(comment);
                    commentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            LinearLayout commentLayout = createCommentLinearLayout(documentSnapshot);
                            commentsLayout.addView(commentLayout);
                        }
                    });
                }
            }
        });
    }

    public void addCommentButton(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(QRcodeViewerActivity.this);
        LayoutInflater inflater = QRcodeViewerActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_comment_layout, null);
        final EditText commentEditText = dialogView.findViewById(R.id.addCommentLayoutText);
        builder.setView(dialogView)
        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String commentText = commentEditText.getText().toString();
                if(!commentText.equals("")){
                    qrDatabaseAddHandler commentAdder = null;
                    try {
                        commentAdder = new qrDatabaseAddHandler(QRcodeViewerActivity.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String commentId = commentAdder.addCommentToDB(commentText, qrCodeHash, db);
                    //Create a new comment in the database
                    DocumentReference commentRef = db.collection("Comments").document(commentId);
                    commentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //Add the new comment to the screen
                            LinearLayout commentsLayout = findViewById(R.id.qrViewerCommentsLinearLayout);
                            LinearLayout commentLayout = createCommentLinearLayout(documentSnapshot);
                            commentsLayout.addView(commentLayout);
                            //Add the comment ID to the QR code database entry
                            DocumentReference docRef = db.collection("QRcode").document(qrCodeHash);
                            docRef.update("Comments", FieldValue.arrayUnion(commentId));

                        }
                    });
                }else{
                    Toast.makeText(QRcodeViewerActivity.this, "Invalid comment - No text", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public LinearLayout createCommentLinearLayout(DocumentSnapshot documentSnapshot){
        LinearLayout commentLayout = new LinearLayout(QRcodeViewerActivity.this);
        commentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        commentLayout.setOrientation(LinearLayout.VERTICAL);
        commentLayout.setBackground(getResources().getDrawable(R.drawable.comment_border, null));
        commentLayout.setPadding(0, 10, 0, 10);
        TextView usernameText = new TextView(QRcodeViewerActivity.this);
        usernameText.setText(documentSnapshot.getString("User"));
        usernameText.setTextSize(10);
        usernameText.setTypeface(null, Typeface.BOLD);
        TextView commentText = new TextView(QRcodeViewerActivity.this);
        commentText.setText(documentSnapshot.getString("Text"));
        commentLayout.addView(usernameText);
        commentLayout.addView(commentText);
        return commentLayout;
    }

    public void backButtonPressed(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}