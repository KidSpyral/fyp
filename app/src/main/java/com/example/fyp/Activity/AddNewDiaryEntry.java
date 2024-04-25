package com.example.fyp.Activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fyp.Model.Prayer;
import com.example.fyp.Model.diaryEntry;
import com.example.fyp.R;
import com.example.fyp.SentimentApiClient;
import com.example.fyp.SentimentResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

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
    private OnDiaryEntrySavedListener savedListener;

    public static AddNewDiaryEntry newInstance() {
        return new AddNewDiaryEntry();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_task, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseApp.initializeApp(context);

        client1 = new SentimentApiClient("http://10.0.2.2:8000/", "predict_moodsentiment/");
        client2 = new SentimentApiClient("http://10.0.2.2:8000/", "predict_prayersentiment/");

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
            }
        });

    }

    private void predictSentiment(String sentence) {
        client1.predictMoodSentiment(sentence, new Callback<SentimentResponse>() {
            @Override
            public void onResponse(Call<SentimentResponse> call, Response<SentimentResponse> response) {
                if (response.isSuccessful()) {
                    SentimentResponse sentimentResponse = response.body();
                    String sentiment = sentimentResponse.getSentiment();
                    float confidence = sentimentResponse.getConfidence();

                    // diary entry save
                    saveDiaryEntry(sentiment);

                } else {

                }
            }

            @Override
            public void onFailure(Call<SentimentResponse> call, Throwable t) {
                // Handle failure
                // for message or failure log


            }
        });

        client2.predictPrayerSentiment(sentence, new Callback<SentimentResponse>() {
            @Override
            public void onResponse(Call<SentimentResponse> call, Response<SentimentResponse> response) {
                if (response.isSuccessful()) {
                    SentimentResponse sentimentResponse = response.body();
                    String prayer = sentimentResponse.getSentiment();
                    float confidence = sentimentResponse.getConfidence();

                    // diary entry save
                    savePrayer(prayer);
                } else {
                    // Handle error
                    // for message or failure log
                }
            }

            @Override
            public void onFailure(Call<SentimentResponse> call, Throwable t) {
                // Handle failure
                // for message or failure log
            }
        });


    }

    private void saveDiaryEntry(String sentiment) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Diary Entries").child(currentUser.getUid());
            String diaryEntryId = referenceProfile.push().getKey(); // unique key for the task

            diaryEntry newdiaryEntry = new diaryEntry(desc, ttl, sentiment, entrydate, System.currentTimeMillis());
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
            String PrayerId = referenceProfile.push().getKey(); // unique key for the task

            Prayer newPrayer = new Prayer(prayer);
            newPrayer.setPrayerId(PrayerId);

            referenceProfile.child(PrayerId).setValue(newPrayer)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Prayer Saved", Toast.LENGTH_SHORT).show();
                                // listener when the entry is saved
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
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }

    public void setOnDiaryEntrySavedListener(OnDiaryEntrySavedListener listener) {
        this.savedListener = listener;
    }

    public interface OnDiaryEntrySavedListener {
        void onDiaryEntrySaved(String description);
    }
}
