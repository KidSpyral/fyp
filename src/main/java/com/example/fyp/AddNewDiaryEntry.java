package com.example.fyp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewDiaryEntry extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";
    FirebaseAuth mAuth;
    private TextView setEntryDate;
    private EditText description;
    private EditText title;
    private String sentiment;
    private float confidence;
    private Button save;
    private Button update;
    private Context context;
    private String entryDate = "";
    private String id = "";
    private SentimentApiClient client1, client2;
    private String desc;
    private String prayer;
    private String ttl;
    private String entrydate;
    public static AddNewDiaryEntry newInstance(){
        return new AddNewDiaryEntry();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_task , container , false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseApp.initializeApp(context);

        client1 = new SentimentApiClient("http://10.0.2.2:8000");
        client2 = new SentimentApiClient("http://10.0.2.2:8001");

        mAuth = FirebaseAuth.getInstance();

        setEntryDate = view.findViewById(R.id.entryDate);
        save = view.findViewById(R.id.saveDE);
        description = view.findViewById(R.id.description);
        title = view.findViewById(R.id.title);

        setEntryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();

                int MONTH = calendar.get(Calendar.MONTH);
                int YEAR = calendar.get(Calendar.YEAR);
                int DAY = calendar.get(Calendar.DATE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1;
                        setEntryDate.setText(dayOfMonth + "/" + month + "/" + year);
                        entryDate = dayOfMonth + "/" + month + "/" + year;
                    }
                }, YEAR, MONTH, DAY);

                datePickerDialog.show();
            }
        });



        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desc = description.getText().toString();
                entrydate = entryDate;
                ttl = title.getText().toString();
                String sentence = desc;

                if (TextUtils.isEmpty(sentence)) {
                    Toast.makeText(context, "Empty Diary not Allowed!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(entrydate)) {
                    Toast.makeText(context, "Set a due date!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(ttl)) {
                    Toast.makeText(context, "Enter a title!", Toast.LENGTH_SHORT).show();
                    return;
                }

                predictSentiment(sentence); // Start sentiment prediction

                // Note: Move the rest of the code to the onResponse method of the sentiment prediction callback
            }
        });

    }
    private void predictSentiment(String sentence) {
        client1.predictSentiment(sentence, new Callback<SentimentResponse>() {
            @Override
            public void onResponse(Call<SentimentResponse> call, Response<SentimentResponse> response) {
                if (response.isSuccessful()) {
                    SentimentResponse sentimentResponse = response.body();
                    String sentiment = sentimentResponse.getSentiment();
                    float confidence = sentimentResponse.getConfidence();

                    // Now that sentiment is available, save the diary entry
                    saveDiaryEntry(sentiment);
                } else {
                    // Handle error
                    // Optionally, show a message or log the error
                }
            }

            @Override
            public void onFailure(Call<SentimentResponse> call, Throwable t) {
                // Handle failure
                // Optionally, show a message or log the failure
            }
        });

        client2.predictSentiment(sentence, new Callback<SentimentResponse>() {
            @Override
            public void onResponse(Call<SentimentResponse> call, Response<SentimentResponse> response) {
                if (response.isSuccessful()) {
                    SentimentResponse sentimentResponse = response.body();
                    String prayer = sentimentResponse.getSentiment();
                    float confidence = sentimentResponse.getConfidence();

                    // Now that sentiment is available, save the diary entry
                    savePrayer(prayer);
                } else {
                    // Handle error
                    // Optionally, show a message or log the error
                }
            }

            @Override
            public void onFailure(Call<SentimentResponse> call, Throwable t) {
                // Handle failure
                // Optionally, show a message or log the failure
            }
        });


    }

    private void saveDiaryEntry(String sentiment) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Diary Entries").child(currentUser.getUid());
            String diaryEntryId = referenceProfile.push().getKey(); // Generate a unique key for the task

            diaryEntry newdiaryEntry = new diaryEntry(desc, ttl, sentiment, entrydate);
            newdiaryEntry.setDiaryEntryId(diaryEntryId);

            referenceProfile.child(diaryEntryId).setValue(newdiaryEntry)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Diary Entry Saved", Toast.LENGTH_SHORT).show();
                                // Call the listener when the entry is saved
                                if (savedListener != null) {
                                    savedListener.onDiaryEntrySaved(desc);
                                }
                            } else {
                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        dismiss();
    }

    private void savePrayer(String prayer) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Prayers").child(currentUser.getUid());
            String PrayerId = referenceProfile.push().getKey(); // Generate a unique key for the task

            Prayer newPrayer = new Prayer(prayer);
            newPrayer.setPrayerId(PrayerId);

            referenceProfile.child(PrayerId).setValue(newPrayer)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Prayer Saved", Toast.LENGTH_SHORT).show();
                                // Call the listener when the entry is saved
                                if (savedListener != null) {
                                    savedListener.onDiaryEntrySaved(prayer);
                                }
                            } else {
                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        dismiss();
    }




    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof  OnDialogCloseListener){
            ((OnDialogCloseListener)activity).onDialogClose(dialog);
        }
    }

    public interface OnDiaryEntrySavedListener {
        void onDiaryEntrySaved(String description);
    }
    private OnDiaryEntrySavedListener savedListener;

    // Other existing code

    public void setOnDiaryEntrySavedListener(OnDiaryEntrySavedListener listener) {
        this.savedListener = listener;
    }
}
