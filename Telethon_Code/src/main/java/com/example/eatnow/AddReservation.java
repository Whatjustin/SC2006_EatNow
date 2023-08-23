package com.example.eatnow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eatnow.model.Restaurant;
import com.example.eatnow.model.UserAccount;
import com.example.eatnow.utility.ErrorPopUp;
import com.example.eatnow.utility.TimeConverter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

public class AddReservation extends DialogFragment implements TimePickerDialog.OnTimeSetListener, ErrorPopUp {

    // CONSTANTS to limit user's inputs
    static final int MAX_RESERVATION_DAYS = 30;
    static final int MAX_PAX = 10;
    static final int MAX_HOUR_TIME = 20;
    static final int MIN_HOUR_TIME = 10;
    // UIs
    ImageView exit;
    ImageView confirm;
    CalendarView calendarView;
    ImageButton minusPaxBtn;
    ImageButton addPaxBtn;
    TextView paxNum;
    Button periodBtn;
    TextView period;
    // Store restaurant temporarily
    Restaurant restaurant;
    // Store selected date
    Date date = null;
    // Store Hours and Minutes of selected time
    Integer timeInHours = null;
    Integer timeInMinutes = null;
    // Store number of pax (Start from 1)
    int pax = 1;
    // Store new reservation id
    Integer new_reservation_id = null;

    // Constructor for class
    public AddReservation(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    // Called on create view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.reservation_popup, container, false);

        // Retrieve new id for reservation from database
        extractReservationID();

        exit = view.findViewById(R.id.reservation_exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        confirm = view.findViewById(R.id.reservation_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new_reservation_id != null) {
                    if (verifyReservation()) {
                        addReservationToDatabase();
                        getDialog().dismiss();
                    }
                } else {
                    errorPopUp("Error", "Please wait for data to load.");
                }
            }
        });

        calendarView = view.findViewById(R.id.calendarView);
        //
        Calendar min = Calendar.getInstance();
        min.add(Calendar.DAY_OF_MONTH, 1);
        long minDate = min.getTimeInMillis();
        //
        Calendar max = min;
        max.add(Calendar.DAY_OF_MONTH, MAX_RESERVATION_DAYS);
        long maxDate = max.getTimeInMillis();
        //
        calendarView.setMinDate(minDate);
        calendarView.setMaxDate(maxDate);
        //
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                date = new GregorianCalendar(year, month, dayOfMonth, 0, 0, 0).getTime();
            }
        });

        paxNum = view.findViewById(R.id.numberOfPax);
        paxNum.setText(String.valueOf(pax));

        minusPaxBtn = view.findViewById(R.id.minusPaxBtn);
        minusPaxBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pax != 1) {
                    pax -= 1;
                    paxNum.setText(String.valueOf(pax));
                }
            }
        });

        addPaxBtn = view.findViewById(R.id.plusPaxBtn);
        addPaxBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pax != MAX_PAX) {
                    pax += 1;
                    paxNum.setText(String.valueOf(pax));
                }
            }
        });

        period = view.findViewById(R.id.period);

        periodBtn = view.findViewById(R.id.selectPeriodBtn);
        periodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar now = Calendar.getInstance();

                TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                        AddReservation.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false
                );

                timePickerDialog.setMinTime(MIN_HOUR_TIME, 0, 0);
                timePickerDialog.setMaxTime(MAX_HOUR_TIME, 0, 0);
                timePickerDialog.setThemeDark(false);
                timePickerDialog.setTitle("Choose a Timeslot");
                timePickerDialog.setTimeInterval(1, 30, 60);
                timePickerDialog.setAccentColor(Color.parseColor("#55828b"));

                timePickerDialog.show(getActivity().getSupportFragmentManager(), "Picking Timeslots");
            }
        });

        return view;
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        timeInHours = hourOfDay;
        timeInMinutes = minute;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        Date d = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String selectedTime = dateFormat.format(d);

        period.setText(selectedTime);
    }

    private boolean verifyReservation() {

        if (date == null) {
            errorPopUp("No date selected", "Please selected a date for your reservation.");
            return false;
        }

        if (timeInHours == null) {
            errorPopUp("No timeslot selected", "Please selected a timeslot for your reservation.");
            return false;
        }

        return true;
    }

    private void addReservationToDatabase() {

        long timestamp = date.getTime(); // Get Timestamp in milliseconds
        timestamp += (timeInHours * 60 * 60 * 1000); // Convert hours to milliseconds
        timestamp += (timeInMinutes * 60 * 1000);  // Convert minutes to milliseconds

        HashMap<String, Object> reservation = new HashMap<>();

        reservation.put("account_id", UserAccount.getInstance().getAccount_id());
        reservation.put("reservation_date", TimeConverter.convertOffsetUnixTStoSGT(timestamp));
        reservation.put("creation_date", TimeConverter.getSGTUnixTS());
        reservation.put("pax", pax);
        reservation.put("restaurant_id", restaurant.getRestaurant_id());
        reservation.put("reservation_id", new_reservation_id);

        // Adding Reservation to database
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("reservations").child(String.valueOf(new_reservation_id - 1));
        reference.setValue(reservation);

        // Updating last reservation id
        reference = FirebaseDatabase.getInstance().getReference().child("id_counter").child("reservation_id");
        reference.setValue(new_reservation_id);
    }

    private void extractReservationID() {

        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("id_counter").child("reservation_id");
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                new_reservation_id = dataSnapshot.getValue(Integer.class);
                // Set new account id to +1
                new_reservation_id += 1;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    // Adjust Layout of Pop-up to maximize size
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }


    @Override
    public void errorPopUp(String title, String error) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(error)
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }
}
